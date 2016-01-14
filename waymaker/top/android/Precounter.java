package waymaker.top.android; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.content.*;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.DocumentsContract; // grep DocumentsContract-TS
import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import org.xmlpull.v1.*;
import waymaker.gen.*;
import waymaker.spec.*;

import static android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME;
import static android.provider.DocumentsContract.Document.MIME_TYPE_DIR;
import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_TAG;
import static org.xmlpull.v1.XmlPullParser.TEXT;
import static waymaker.top.android.Waynode.EMPTY_WAYNODE;


/** A count engine that uses a {@linkplain WayrepoPreviewController wayrepo preview} to form an
  * adjusted, local count, thus anticipating a future server count.  Precounters are single use
  * facilities; construct one, use it, and discard it.
  */
public final @Warning("no hold") class Precounter implements UnadjustedNodeV.RKit
{


    /** Constructs a precounter.
      *
      *     @see #pollName()
      *     @param groundUnaState The {@linkplain UnadjustedGround#restore(byte[],UnadjustedNodeV.RKit)
      *       marshalled state} of the unadjusted ground on which to base the precount, or null to base
      *       it on nothing, in which case the openToThread restriction is lifted.
      *     @param originalUnaCount The number of unadjusted nodes in the original groundUnaState cache,
      *        or zero if groundUnaState is null.  The value serves only to enlarge the initial capacity
      *        of the node map in order to avoid forseeable rehashes.
      *     @see WaykitUI#wayrepoTreeLoc()
      */
     @ThreadRestricted("KittedPolyStatorSR.openToThread") // for ground.restore
   public Precounter( final String pollName, final byte[] groundUnaState, final int originalUnaCount,
     final ContentResolver contentResolver, final String wayrepoTreeLoc )
    {
        this.pollName = pollName;
        this.contentResolver = contentResolver;
        this.wayrepoTreeLoc = wayrepoTreeLoc;
        nodeMap = new HashMap<>( MapX.hashCapacity(originalUnaCount + NodeCache.INITIAL_HEADROOM),
          MapX.HASH_LOAD_FACTOR );
        serverCount = new ServerCount();
        try { xhtmlParserFactory = WaykitUI.xhtmlConfigured( XmlPullParserFactory.newInstance() ); }
        catch( final XmlPullParserException x ) { throw new RuntimeException( x ); }

        ground = new UnadjustedGround();
        encache( ground );
        if( groundUnaState != null ) ground.restore( groundUnaState, /*kit*/this );
    }



   // --------------------------------------------------------------------------------------------------


    /** A cycle foreseer for general reuse during the precount.
      */
    public CycleForeseer cycleForeseer() { return cycleForeseer; }


        private final CycleForeseer cycleForeseer = new CycleForeseer();



    /** Returns the identified node from the local node map, or a newly constructed one whose properties
      * are initialized from the latest count on the remote count server, or null if the identified node
      * is both unmapped and uncounted.
      */
    public UnadjustedNode getOrFetchUnadjusted( final VotingID id )
    {
        UnadjustedNode una = nodeMap.get( id );
        if( una == null ) una = serverCount.fetchNode( id, nodeMap );
        return una;
    }



    /** The original, unadjusted {@linkplain NodeCache#ground() ground}.  Here the {@linkplain
      * #precount() precount} attaches the adjusted ground, if any.
      */
    public UnadjustedGround ground() { return ground; }


        private final UnadjustedGround ground;



    /** A grounder for general reuse during the precount.
      */
    public EffectiveGrounder grounder() { return grounder; }


        private final EffectiveGrounder grounder = new EffectiveGrounder(); // q.v. for "no hold"



    /** A map of all known nodes including the ground pseudo-node, each keyed by identity tag.
      */
    public HashMap<VotingID,UnadjustedNode> nodeMap() { return nodeMap; }


        private final HashMap<VotingID,UnadjustedNode> nodeMap;



    /** The name of the poll that is here precounted.
      */
    public String pollName() { return pollName; }


        private final String pollName;



    /** Forms the adjusted count if possible, and attaches it among the {@linkplain #nodeMap() mapped}
      * nodes as <var>node</var>.{@linkplain UnadjustedNode#precounted() precounted}.  Call once only.
      */
    public void precount() throws CountFailure, InterruptedException
    {
        /* * *
        - count registers
            ( "poll type" below is determined branch-wise, poll not being monotypical
            [ turnout
                - all poll types
                ( meaning one is a person
            [ active turnout
                - non-action poll types only
                - recognized as active in corresponding "action poll":
                    | action poll is assembly election poll if non-action poll is law
                        ( notebook 2015.7.2
                        - active is he currently predicted to win a seat in the next election
                        - meaning that he is expected to have an official vote in the assembly
                            ( making the results a prediction of future legal code
                    | else action poll is executive
                        ( partly notebook 2015.7.3
                        - active is he voting for an end or intermediate norm on the endward
                          relational path of an executive officer (office pipe) who recognizes him
                            ( implies that he votes for the officer in the executive poll
                        - meaning that he is considered active in executing the issue
          / [ active recognition (variable weighting)
          // no utility to such weights, no meaning, instead recognition is all or nothing
          */
        if( wayrepoTreeLoc == null ) throw new CountFailure( "User has set no wayrepo location" );

        final PersonID ownerID; // owner of wayrepo, typically the user
        VotingID _votedID_owner = null; // thus far
        final Uri wayrepoTreeUri = Uri.parse( wayrepoTreeLoc );
        read: try( final WayrepoReader inWr = new WayrepoReader( wayrepoTreeUri, contentResolver ))
        {
            String docID;
            docID = DocumentsContract.getTreeDocumentId( wayrepoTreeUri );
            final Uri wayrepoUri;
            try { wayrepoUri = DocumentsContract.buildDocumentUriUsingTree( wayrepoTreeUri, docID ); }
            catch( final SecurityException x )
            {
                // usually but not always thrown first by acquireContentProviderClient in WayrepoReader
                throw new CountFailure( WaykitUI.wayrepoTreeLoc_message(wayrepoTreeLoc), x );
            }

            docID = inWr.findDirectory( "poll", docID );
            if( docID == null ) throw new CountFailure( "Wayrepo has no 'poll' directory" );

          // Identify owner of wayrepo.
          // - - - - - - - - - - - - - -
            final ContentProviderClient provider = inWr.provider(); // grep ContentProviderClient-TS
            try // must be just after findDirectory (queryChildren) or SMBProvider returns null cursor
            (
                final Cursor c = inWr.provider().
                  query( wayrepoUri, proNAME, /*selector*/null, /*selectorArgs*/null, /*order*/null );
            ){
                if( c == null || !c.moveToFirst() ) throw new CountFailure( "Cannot read wayrepo directory" );

                ownerID = new PersonID( c.getString(0) );
            }
            catch( final RemoteException x ) { throw new CountFailure( x ); }

          // Read the wayrepo documents.
          // - - - - - - - - - - - - - - -
            docID = inWr.findDirectory( pollName, docID );
            if( docID == null ) break read; // wayrepo has no nodes for this poll

            try( final Cursor cPos/*proID_NAME_TYPE*/ = inWr.queryChildren( docID ); )
            {
                personalPositionFiles: while( cPos.moveToNext() )
                {
                    final String filename = cPos.getString( 1 );
                    final boolean isDirectory = MIME_TYPE_DIR.equals( cPos.getString( 2 ));
                    if( !isDirectory && "position.xht".equals(filename) )
                    {
                        _votedID_owner = parseVote( /*docID*/cPos.getString(0), inWr, ownerID );
                    }
                    else if( isDirectory && "pipe".equals(filename) )
                    {
                        docID = cPos.getString( 0 );
                        try( final Cursor cPP/*proID_NAME_TYPE*/ = inWr.queryChildren( docID ); )
                        {
                            pipes: while( cPP.moveToNext() )
                            {
                                if( MIME_TYPE_DIR.equals( cPP.getString( 2 )))
                                {
                                    docID = cPP.getString( 0 );
                                    final PipeID pipeID = new PipeID( cPP.getString(1) );

                                  // Ensure pipe is precounted.
                                  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                                    VotingID _votedID = null;        // thus far
                                    Waynode _waynode = EMPTY_WAYNODE; // "
                                    try( final Cursor cPipe/*pro same*/ = inWr.queryChildren( docID ); )
                                    {
                                        pipePositionFiles: while( cPipe.moveToNext() )
                                        {
                                            if( MIME_TYPE_DIR.equals(cPipe.getString(2)) ) continue;

                                            final String fn = cPipe.getString( 1 );
                                            if( "position.xht".equals(fn) )
                                            {
                                                _votedID = parseVote( /*docID*/cPipe.getString(0), inWr,
                                                  pipeID );
                                            }
                                            else if( "end.xht".equals(fn) || "transnorm.xht".equals(fn)
                                                  || "act.xht".equals(fn) )
                                            {
                                                _waynode = parseWaynode( /*docID*/cPipe.getString(0), inWr );
                                            }
                                        }
                                    }
                                    precountIfChanged( pipeID, _votedID, _waynode, /*allowStubRoot*/true );
                                      // Let pipe be stub root.  Unlike a person, a pipe is useful only
                                      // as a candidate to vote for, and voting is easier when the
                                      // candidate is already in the forest.
                                }
                            }
                        }
                    }
                }
            }
        }
        catch( final MalformedID|WayrepoAccessFailure x ) { throw new CountFailure( x ); }

      // Ensure owner is precounted.
      // - - - - - - - - - - - - - - -
        precountIfChanged( ownerID, _votedID_owner, EMPTY_WAYNODE, /*allowStubRoot*/false );
    }



   // - U n a d j u s t e d - N o d e - V . R - K i t --------------------------------------------------


    public void encache( final UnadjustedNode node ) { nodeMap.put( node.id(), node ); }



    public void enlistOutlyingVoter( UnadjustedNode1 node ) {} // no need of list here in precounter



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final ContentResolver contentResolver; // grep ContentResolver-TS



    private static final java.util.logging.Logger logger = LoggerX.getLogger( Precounter.class );



    /** @return Identity tag of vote, or null if there is none.  Instead throws CountFailure if identity
      *   tag is malformed or identifies self.
      */
    private VotingID parseVote( final String docID, final WayrepoReader inWr, final VotingID actorID )
      throws CountFailure, MalformedID
    {
        // position.xht form: http://reluk.ca/100-0/  (view source)
        VotingID votedID = null; // thus far
        final Uri fileUri = DocumentsContract.buildDocumentUriUsingTree( inWr.wayrepoTreeUri(), docID );
        try
        (
            final AssetFileDescriptor aFD = inWr.provider().openTypedAssetFileDescriptor( fileUri,
              /*type, any*/"*/*", /*options*/null );
            final InputStream in = new BufferedInputStream( aFD.createInputStream() );
        ){
            final XmlPullParser p = xhtmlParserFactory.newPullParser();
            p.setInput( in, /*encoding, self detect*/null );
            for( int t = p.getEventType(); t != END_DOCUMENT; t = p.next() )
            {
                if( t != START_TAG || !"vote".equals(p.getName()) ) continue;

                final String udidString = p.getAttributeValue( null, "candidate" );
                if( udidString != null )
                {
                    votedID = (VotingID)UDID.make( udidString );
                    if( votedID.equals( actorID ))
                    {
                        throw new CountFailure( "Self voting " + actorID + " in file " + docID );
                          // demand correction of this senseless vote
                    }
                }
                // keep going, only final vote applies
            }
        }
        catch( IOException|RemoteException|XmlPullParserException x ) { throw new CountFailure( x ); }

        return votedID;
    }



    private WaynodeJig parseWaynode( final String docID, final WayrepoReader inWr )
      throws CountFailure
    {
        // end|transnorm|act.xht form: http://reluk.ca/100-0/tool/xhwsPretty/pretty.js
        final WaynodeJig jig = parseWaynode_jig;
        jig.clear();
        final Uri fileUri = DocumentsContract.buildDocumentUriUsingTree( inWr.wayrepoTreeUri(), docID );
        try
        (
            final AssetFileDescriptor aFD = inWr.provider().openTypedAssetFileDescriptor( fileUri,
              /*type, any*/"*/*", /*options*/null );
            final InputStream in = new BufferedInputStream( aFD.createInputStream() );
        ){
            final XmlPullParser p = xhtmlParserFactory.newPullParser();
            p.setInput( in, /*encoding, self detect*/null );
            doc: for( int t = p.getEventType(); t != END_DOCUMENT; t = p.next() )
            {
                if( t != START_TAG ) continue doc;

                if( "title".equals(p.getName()) )
                {
                  // Pollar prompt.
                  // - - - - - - - -
                    title: for( t = p.next(); t != END_TAG || !"title".equals(p.getName()); t = p.next() )
                    {
                        if( t == TEXT )
                        {
                            jig.question = p.getText().trim();
                            break title;
                        }
                    }
                    continue doc;
                }

                if( "wayscript".equals(p.getName()) )
                {
                  // Summary.
                  // - - - - -
                    t = p.next();
                    if( t == TEXT )
                    {
                        jig.answer = p.getText().trim();
                        t = p.next();
                    }

                  // Handle.
                  // - - - - -
                    wayscript: for(; t != END_TAG || !"wayscript".equals(p.getName()); t = p.next() )
                    {
                        if( t != START_TAG || !"handle".equals(p.getName()) ) continue wayscript;

                        handle: for( t = p.next(); t != END_TAG || !"handle".equals(p.getName());
                          t = p.next() )
                        {
                            if( t != TEXT ) continue handle;

                            final String handle = p.getText().trim();
                            if( !parseWaynode_matcher.reset(handle).matches() )
                            {
                                throw new CountFailure( "Malformed handle '" + handle + "' in file " + docID );
                            }

                            jig.handle = handle;
                            break handle;
                        }
                        break wayscript;
                    }
                    break doc;
                }
            }
        }
        catch( IOException|RemoteException|XmlPullParserException x ) { throw new CountFailure( x ); }
        return jig;
    }


        private final WaynodeJig parseWaynode_jig = new WaynodeJig(); // cache for reuse

        private final Matcher parseWaynode_matcher = Waynode.HANDLE_PATTERN.matcher( "" );



    /** Ensures that any change of position in the local wayrepo is cached in a precount-adjustable
      * node.  This may involve communication with the remote count server.
      *
      *     @param _votedID The {@linkplain RootwardCast#votedID() vote} from the local wayrepo, which
      *       may be null.
      *     @param waynodeTmp A copy of the {@linkplain CountNode#waynode() way contribution} from the
      *       local wayrepo, for temporary access only (it might be a mutable WaynodeJig).
      *     @param allowStubRoot Ensures the node is cached even if the position is missing from both
      *       the original count and the local wayrepo (_votedID is null and _waynode is empty).  This
      *       creates a “stub root” that will appear in the forest, even though it does not participate
      *       in vote flow.
      */
    private void precountIfChanged( final VotingID id, final VotingID _votedID, final Waynode waynodeTmp,
      final boolean allowStubRoot )
    {
        final PrecountNode pre;
        final boolean isVoteChanged;
        final boolean isWaynodeChanged;
        UnadjustedNode una = getOrFetchUnadjusted( id );
        if( una == null )
        {
            isVoteChanged = _votedID != null;
            isWaynodeChanged = !waynodeTmp.equals( EMPTY_WAYNODE );
            if( !isVoteChanged && !isWaynodeChanged )
            {
                if( allowStubRoot )
                {
                    logger.info( "(poll " + pollName() + ") Precounting " + id + " as stub root" );
                    UnadjustedNode0.makeMappedPrecounted( id, this );
                      // adds a precount-adjustable node that ensures visibility as a root (stub root)
                      // in the precount ground
                }
                return;
            }

            una = UnadjustedNode0.makeMapped( id, this );
            pre = new PrecountNode1( una );
        }
        else
        {
            final PrecountNode p = una.precounted();
            if( p == null )
            {
                isVoteChanged = !ObjectX.equals( _votedID, una.rootwardInThis().votedID() );
                isWaynodeChanged = !waynodeTmp.equals( una.waynode() );
                if( !isVoteChanged && !isWaynodeChanged ) return;

                pre = new PrecountNode1( una );
            }
            else
            {
                pre = p;
                isVoteChanged = !ObjectX.equals( _votedID, pre.rootwardInThis().votedID() );
                isWaynodeChanged = !waynodeTmp.equals( pre.waynode() );
            }
        }
        if( isVoteChanged ) pre.rootwardInThis( _votedID, this );
        if( isWaynodeChanged ) pre.waynode( new Waynode1( waynodeTmp ));
    }



    private static final String[] proNAME = new String[] { COLUMN_DISPLAY_NAME };
      // query projection of one formal parameter: display name



    private final ServerCount serverCount;



    private final String wayrepoTreeLoc;



    private final XmlPullParserFactory xhtmlParserFactory; /* for Waymaker XHTML documents because
      parser cannot be reused, https://code.google.com/p/android/issues/detail?id=182605 */


}
