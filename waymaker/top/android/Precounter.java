package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.content.*;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.DocumentsContract; // grep DocumentsContract-TS
import java.io.*;
import java.util.HashMap;
import org.xmlpull.v1.*;
import waymaker.gen.*;
import waymaker.spec.*;

import static android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME;
import static android.provider.DocumentsContract.Document.MIME_TYPE_DIR;
import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;


/** A count engine that uses a {@linkplain WayrepoPreviewController wayrepo preview} to form an
  * adjusted, local count, thus anticipating a future server count.  Precounters are single use
  * facilities; construct one, use it, and discard it.
  */
final @Warning("no hold") class Precounter implements UnadjustedNodeV.RKit
{


    /** Constructs a precounter.
      *
      *     @see #pollName()
      *     @param groundUnaState The {@linkplain UnadjustedGround#restore(byte[],UnadjustedNodeV.RKit)
      *       marshalled state} of the unadjusted ground on which to base the precount, or null to base
      *       it on nothing, in which case the COMPOSITION_LOCK synchronization may be skipped.
      *     @param originalUnaCount The number of unadjusted nodes in the original groundUnaState cache,
      *        or zero if groundUnaState is null.  The value serves only to enlarge the initial capacity
      *        of the node map in order to avoid forseeable rehashes.
      *     @see WaykitUI#wayrepoTreeLoc()
      */
      @ThreadRestricted("touch stators.COMPOSITION_LOCK before") // as per UnadjustedGround.restore
   Precounter( final String pollName, final byte[] groundUnaState, final int originalUnaCount,
     final ContentResolver contentResolver, final String wayrepoTreeLoc )
    {
        this.pollName = pollName;
        this.contentResolver = contentResolver;
        this.wayrepoTreeLoc = wayrepoTreeLoc;
        nodeMap = new HashMap<>( MapX.hashCapacity(originalUnaCount + NodeCache.INITIAL_HEADROOM),
          MapX.HASH_LOAD_FACTOR );
        serverCount = new ServerCount( pollName );
        try { xhtmlParserFactory = WaykitUI.xhtmlConfigured( XmlPullParserFactory.newInstance() ); }
        catch( final XmlPullParserException x ) { throw new RuntimeException( x ); }

        ground = new UnadjustedGround();
        encache( ground );
        if( groundUnaState != null ) ground.restore( groundUnaState, /*kit*/this );
    }



   // --------------------------------------------------------------------------------------------------


    /** A cycle foreseer for general reuse during the precount.
      */
    CycleForeseer cycleForeseer() { return cycleForeseer; }


        private final CycleForeseer cycleForeseer = new CycleForeseer();



    /** Returns the identified node from the local node map, or a newly constructed one whose
      * properties are initialized from the latest count on the remote count server, or null if the
      * identified node is both unmapped and uncounted.
      */
    UnadjustedNode getOrFetchUnadjusted( final VotingID id )
    {
        UnadjustedNode una = nodeMap.get( id );
        if( una == null ) una = serverCount.fetchNode( id, nodeMap );
        return una;
    }



    /** The original, unadjusted {@linkplain NodeCache#ground() ground}.  Here the {@linkplain
      * #precount() precount} attaches the adjusted ground, if any.
      */
    UnadjustedGround ground() { return ground; }


        private final UnadjustedGround ground;



    /** A grounder for general reuse during the precount.
      */
    EffectiveGrounder grounder() { return grounder; }


        private final EffectiveGrounder grounder = new EffectiveGrounder(); // q.v. for "no hold"



    /** A map of all known nodes including the ground pseudo-node, each keyed by identifier.
      */
    HashMap<VotingID,UnadjustedNode> nodeMap() { return nodeMap; }


        private final HashMap<VotingID,UnadjustedNode> nodeMap;



    /** The name of the poll that is here precounted.
      */
    String pollName() { return pollName; }


        private final String pollName;



    /** Forms the adjusted count if possible, and attaches it among the {@linkplain #nodeMap() mapped}
      * nodes as <var>node</var>.{@linkplain UnadjustedNode#precounted() precounted}.  Call once only.
      */
    void precount() throws CountFailure, InterruptedException
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
        VotingID ownerCandidateNewID = null; // thus far
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
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
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
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            docID = inWr.findDirectory( pollName, docID );
            if( docID == null ) break read; // wayrepo has no nodes for this poll

            try( final Cursor cPos/*proID_NAME_TYPE*/ = inWr.queryChildren( docID ); )
            {
                personalPositionFiles: while( cPos.moveToNext() )
                {
                    final String name = cPos.getString( 1 );
                    final boolean isDirectory = MIME_TYPE_DIR.equals( cPos.getString( 2 ));
                    if( !isDirectory && "position.xht".equals(name) )
                    {
                        ownerCandidateNewID = parsePosition( ownerID, /*docID*/cPos.getString(0), inWr );
                    }
                    else if( isDirectory && "pipe".equals(name) )
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
                                    VotingID candidateNewID = null; // thus far
                                    try( final Cursor cPipe/*pro same*/ = inWr.queryChildren( docID ); )
                                    {
                                        pipePositionFiles: while( cPipe.moveToNext() )
                                        {
                                            if( !MIME_TYPE_DIR.equals(cPipe.getString(2))
                                              && "position.xht".equals(cPipe.getString(1)) )
                                            {
                                                candidateNewID = parsePosition( pipeID,
                                                  /*docID*/cPipe.getString(0), inWr );
                                                break;
                                            }
                                        }
                                    }
                                    final PrecountNode pipePre = PrecountNode.getOrMakeIfVoteChanged( pipeID,
                                      this, /*toForceNode*/true, candidateNewID ); /* Forcibly include pipe
                                      in forest regardless of whether it yet participates in vote flow.
                                      Unlike a person, a pipe is useful only as candidate to vote for,
                                      and voting is easier when the candidate is already in the forest. */
                                    if( pipePre != null ) pipePre.rootwardInThis( candidateNewID, this );
                                      // else candidateNewID is unchanged, not really new
                                }
                            }
                        }
                    }
                }
            }
        }
        catch( final MalformedID|WayrepoX x ) { throw new CountFailure( x ); }

      // Ensure owner is precounted.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final PrecountNode ownerPre = PrecountNode.getOrMakeIfVoteChanged( ownerID, this,
          /*toForceNode*/false, ownerCandidateNewID );
        if( ownerPre != null ) ownerPre.rootwardInThis( ownerCandidateNewID, this );
          // else ownerCandidateNewID is unchanged, not really new
    }



   // - U n a d j u s t e d - N o d e - V . R - K i t --------------------------------------------------


    public void encache( final UnadjustedNode node ) { nodeMap.put( node.id(), node ); }



    public void enlistOutlyingVoter( UnadjustedNode1 node ) {} // no need of list here in precounter



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final ContentResolver contentResolver; // grep ContentResolver-TS



    private final ServerCount serverCount;



    /** @return Well formed identifier of voted candidate, or null if no candidate is voted.
      * @throws CountFailure if identifier is malformed, or identifies self.
      */
    private VotingID parsePosition( final VotingID voterID, final String docID, final WayrepoReader inWr )
      throws CountFailure, MalformedID
    {
        VotingID candidateID = null; // thus far
        final Uri fileUri = DocumentsContract.buildDocumentUriUsingTree( inWr.wayrepoTreeUri(), docID );
        try
        (
            final AssetFileDescriptor aFD = inWr.provider().
              openTypedAssetFileDescriptor( fileUri, /*type, any*/"*/*", /*options*/null );
            final InputStream in = new BufferedInputStream( aFD.createInputStream() );
        ){
            final XmlPullParser p = xhtmlParserFactory.newPullParser();
            p.setInput( in, /*encoding, self detect*/null );
            for( int t = p.getEventType(); t != END_DOCUMENT; t = p.next() )
            {
                if( t != START_TAG || !"vote".equals(p.getName()) ) continue;

                final String uuidString = p.getAttributeValue( null, "candidate" );
                if( uuidString != null )
                {
                    candidateID = (VotingID)UUID.make( uuidString );
                    if( candidateID.equals( voterID ))
                    {
                        throw new CountFailure( "Self voting " + voterID + " in file " + docID );
                          // demand correction of this senseless vote
                    }
                }
                // keep going, only final vote applies
            }
        }
        catch( IOException|RemoteException|XmlPullParserException x ) { throw new CountFailure( x ); }

        return candidateID;
    }



    private static final String[] proNAME = new String[] { COLUMN_DISPLAY_NAME };
      // query projection of one formal parameter: display name



    private final String wayrepoTreeLoc;



    private final XmlPullParserFactory xhtmlParserFactory; /* for Waymaker XHTML documents because
      parser cannot be reused, https://code.google.com/p/android/issues/detail?id=182605 */


}
