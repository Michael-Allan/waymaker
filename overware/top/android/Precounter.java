package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import android.content.*;
import android.content.res.AssetFileDescriptor;
import android.database.*;
import android.net.Uri;
import android.os.*;
import android.provider.DocumentsContract; // grep DocumentsContract-TS
import java.io.*;
import java.util.HashMap;
import org.xmlpull.v1.*;
import overware.gen.*;
import overware.spec.*;

import static android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME;
import static android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID;
import static android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE;
import static android.provider.DocumentsContract.Document.MIME_TYPE_DIR;
import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;


/** A count engine that generates an adjusted count by introducing changes read from the user’s local
  * overrepo, which are yet unknown to the guideway count engine, thus anticipating a future guideway
  * count.  Precounters are single use facilities; construct one, use it, and discard it.
  */
final @Warning("no hold") class Precounter implements UnadjustedNodeV.RKit
{


    /** Constructs a precounter.
      *
      *     @see #pollName()
      *     @param groundState The state of ground on which to base the precount, obtained from
      *       UnadjustedGround.{@linkplain UnadjustedGround#stators stators}, or null to use an empty,
      *       newly constructed ground.
      *     @see Overguidance#overrepoTreeLoc()
      */
   @ThreadSafe Precounter( String _pollName, final byte[] groundState, ContentResolver _contentResolver,
     String _overrepoTreeLoc )
    {
        pollName = _pollName;
        contentResolver = _contentResolver;
        overrepoTreeLoc = _overrepoTreeLoc;
        guidewayCount = new GuidewayCount( pollName );
        try { xhtmlParserFactory = Application.i().xhtmlConfigured( XmlPullParserFactory.newInstance() ); }
        catch( final XmlPullParserException x ) { throw new RuntimeException( x ); }

        ground = new UnadjustedGround();
        nodeMap.put( ground.id(), ground );
        if( groundState != null )
        {
            final Parcel in = Parcel.obtain(); // grep Parcel-TS
            try
            {
                in.unmarshall( groundState, 0, groundState.length ); // sic
                in.setDataPosition( 0 ); // undocumented requirement
                UnadjustedGround.stators.restore( ground, in, this );
            }
            finally { in.recycle(); }
        }
    }



   // --------------------------------------------------------------------------------------------------


    /** A cycle foreseer for general reuse during the precount.
      */
    CycleForeseer cycleForeseer() { return cycleForeseer; }


        private final CycleForeseer cycleForeseer = new CycleForeseer();



    /** Returns the identified node from the local node map, or a newly constructed one whose
      * properties are initialized from the latest count on the remote guideway, or null if the
      * identified node is both unmapped and uncounted.
      */
    UnadjustedNode getOrFetchUnadjusted( final VotingID id )
    {
        UnadjustedNode una = nodeMap.get( id );
        if( una == null ) una = guidewayCount.fetchNode( id, nodeMap );
        return una;
    }



    /** The original, unadjusted {@linkplain NodeCache#ground() ground} to which the {@linkplain
      * #precount() precount} attaches the adjusted ground, if any.
      */
    UnadjustedGround ground() { return ground; }


        private UnadjustedGround ground;



    /** A grounder for general reuse during the precount.
      */
    EffectiveGrounder grounder() { return grounder; }


        private final EffectiveGrounder grounder = new EffectiveGrounder(); // q.v. for "no hold"



    /** A map of all known nodes including the ground pseudo-node, each keyed by identifier.
      */
    HashMap<VotingID,UnadjustedNode> nodeMap() { return nodeMap; }


        private final HashMap<VotingID,UnadjustedNode> nodeMap = new HashMap<>();



    /** The name of the guideway poll that is here precounted.
      */
    String pollName() { return pollName; }


        private final String pollName;



    /** Forms the adjusted count if there is one, and attaches it among the nodes of the cache
      * ({@linkplain #ground() ground} and {@linkplain #nodeMap() map}) as
      * <var>node</var>.{@linkplain UnadjustedNode#precounted() precounted}.  Call once only.
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
                        ( as per notebook 2015.7.2
                        - active is he currently predicted to win a seat in the next election
                        - meaning that he is expected to have an official vote in the assembly
                            ( making the results a prediction of future legal code
                    | else action poll is executive
                        ( partly as per notebook 2015.7.3
                        - active is he voting for an end or intermediate guide on the endward
                          relational path of an executive officer (office pipe) who recognizes him
                            ( implies that he votes for the officer in the executive poll
                        - meaning that he is considered active in executing the issue
          / [ active recognition (variable weighting)
          // no utility to such weights, no meaning, instead recognition is all or nothing
          */
        if( overrepoTreeLoc == null ) return; // no local overrepo

        final PersonID ownerID; // owner of overrepo, typically the user
        VotingID ownerCandidateNewID = null; // thus far
        overrepoTreeUri = Uri.parse( overrepoTreeLoc );
        final ContentProviderClient provider; // grep ContentProviderClient-TS
        try { provider = contentResolver.acquireContentProviderClient( overrepoTreeUri ); }
        catch( final SecurityException x )
        {
            throw new CountFailure( Overguidance.overrepoTreeLoc_message(overrepoTreeLoc), x );
        }

        read: try
        {
            String docID;
            docID = DocumentsContract.getTreeDocumentId( overrepoTreeUri );
            final Uri overrepoUri;
            try { overrepoUri = DocumentsContract.buildDocumentUriUsingTree( overrepoTreeUri, docID ); }
            catch( final SecurityException x )
            {
                // usually but not always thrown first on acquireContentProviderClient above
                throw new CountFailure( Overguidance.overrepoTreeLoc_message(overrepoTreeLoc), x );
            }

            docID = findDirectory( "poll", docID, provider );
            if( docID == null ) throw new CountFailure( "Overrepo has no 'poll' directory" );

          // Identify owner of overrepo.
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            try // must be just after findDirectory (queryChildDocuments) or SMBProvider returns null cursor
            (
                final Cursor c = provider.query( overrepoUri, proNAME, /*selector*/null, /*selectorArgs*/null,
                  /*order*/null );
            ){
                if( c == null || !c.moveToFirst() ) throw new CountFailure( "Cannot read overrepo directory" );

                ownerID = new PersonID( c.getString(0) );
            }
            catch( final RemoteException x ) { throw new CountFailure( x ); }

          // Read the overrepo documents.
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            docID = findDirectory( pollName, docID, provider );
            if( docID == null ) break read; // overrepo has no nodes for this poll

            try( final Cursor cPos/*proID_NAME_TYPE*/ = queryChildIDs( docID, provider ); )
            {
                personalPositionFiles: while( cPos.moveToNext() )
                {
                    final String name = cPos.getString( 1 );
                    final boolean isDirectory = MIME_TYPE_DIR.equals( cPos.getString( 2 ));
                    if( !isDirectory && "position.xht".equals(name) )
                    {
                        ownerCandidateNewID = parsePosition( ownerID, /*docID*/cPos.getString(0), provider );
                    }
                    else if( isDirectory && "pipe".equals(name) )
                    {
                        docID = cPos.getString( 0 );
                        try( final Cursor cPP/*proID_NAME_TYPE*/ = queryChildIDs( docID, provider ); )
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
                                    try( final Cursor cPipe/*pro same*/ = queryChildIDs( docID, provider ); )
                                    {
                                        pipePositionFiles: while( cPipe.moveToNext() )
                                        {
                                            if( !MIME_TYPE_DIR.equals(cPipe.getString(2))
                                              && "position.xht".equals(cPipe.getString(1)) )
                                            {
                                                candidateNewID = parsePosition( pipeID,
                                                  /*docID*/cPipe.getString(0), provider );
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
        catch( MalformedID _x ) { throw new CountFailure( _x ); }
        finally { provider.release(); }

      // Ensure owner is precounted.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final PrecountNode ownerPre = PrecountNode.getOrMakeIfVoteChanged( ownerID, this,
          /*toForceNode*/false, ownerCandidateNewID );
        if( ownerPre != null ) ownerPre.rootwardInThis( ownerCandidateNewID, this );
          // else ownerCandidateNewID is unchanged, not really new
    }



   // - U n a d j u s t e d - N o d e - V . R - K i t --------------------------------------------------


    public void cache( final UnadjustedNode node ) { nodeMap.put( node.id(), node ); }



    public void enlistOutlyingVoter( UnadjustedNode1 node ) {} // no need of list here in precounter



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final ContentResolver contentResolver; // grep ContentResolver-TS



    /** @return The document ID of the directory, or null if none was found.
      */
    private String findDirectory( final String name, final String parentID,
      final ContentProviderClient provider ) throws CountFailure, InterruptedException
    {
        try( final Cursor c/*proID_NAME_TYPE*/ = queryChildIDs( parentID, provider ); )
        {
            while( c.moveToNext() )
            {
                if( !name.equals( c.getString(1) )) continue;

                if( !MIME_TYPE_DIR.equals( c.getString(2) )) continue;

                return c.getString( 0 );
            }
        }
        return null; // directory not found
    }



    private final GuidewayCount guidewayCount;



    private static final long MS_TIMEOUT_MIN = 4500;



    private static final long MS_TIMEOUT_INTERVAL = 500;



    private Uri overrepoTopmostUri() // topmost ancestor of overrepoTreeUri
    {
        if( overrepoTopmostUri == null )
        {
            overrepoTopmostUri = uriBuilderSA().scheme(overrepoTreeUri.getScheme())
              .authority(overrepoTreeUri.getAuthority()).build();
        }
        return overrepoTopmostUri;
    }


        private Uri overrepoTopmostUri;



    private final String overrepoTreeLoc;



    private Uri overrepoTreeUri; // init in precount()



    /** @return Well formed identifier of voted candidate, or null if no candidate is voted.
      * @throws CountFailure if identifier is malformed, or identifies self.
      */
    private VotingID parsePosition( final VotingID voterID, final String docID,
      final ContentProviderClient provider ) throws CountFailure, MalformedID
    {
        VotingID candidateID = null; // thus far
        final Uri fileUri = DocumentsContract.buildDocumentUriUsingTree( overrepoTreeUri, docID );
        try
        (
            final AssetFileDescriptor aFD = provider.openTypedAssetFileDescriptor( fileUri, /*type, any*/"*/*",
              /*options*/null );
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



    private static final String[] proID_NAME_TYPE = new String[] { COLUMN_DOCUMENT_ID, COLUMN_DISPLAY_NAME,
      COLUMN_MIME_TYPE }; // formal projection of results for directory search



    private static final String[] proNAME = new String[] { COLUMN_DISPLAY_NAME };
      // formal projection of results for name of document



    /** @return A proID_NAME_TYPE cursor over the children.  Be sure to close it when finished with it.
      */
    private Cursor queryChildIDs( String _parentID, final ContentProviderClient provider )
      throws CountFailure, InterruptedException
    {
        return queryChildIDs( _parentID, provider, false );
    }



    private Cursor queryChildIDs( final String parentID, final ContentProviderClient provider,
      final boolean isRetry ) throws CountFailure, InterruptedException
    {
        final Cursor c;
        try
        {
            c = provider.query( DocumentsContract.buildChildDocumentsUriUsingTree(overrepoTreeUri,parentID),
              proID_NAME_TYPE, /*selector, unsupported*/null, /*selectorArgs*/null, /*order*/null );
              // selector unsupported in base impl (DocumentsProvider.queryChildDocuments)
        }
        catch( final RemoteException x ) { throw new CountFailure( x ); }

        if( c == null ) throw new CountFailure( "Cannot read overrepo directory: " + parentID );

      // Return response if fully loaded.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if( !c.getExtras().getBoolean( DocumentsContract.EXTRA_LOADING )) return c;

        if( isRetry ) { throw new CountFailure( "Incomplete response from documents provider after retry" ); }

      // Else wait for response to fully load.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        Uri nUri = c.getNotificationUri();
        final boolean nUriDescendentsToo;
        if( nUri == null ) // probable bug, https://code.google.com/p/android/issues/detail?id=182258
        {
            nUri = overrepoTopmostUri(); // default
            nUriDescendentsToo = true;
        }
        else nUriDescendentsToo = false;
        final Observer o = new Observer();
        contentResolver.registerContentObserver( nUri, nUriDescendentsToo, o );
          // should eventually set o.isFullyLoaded, then call Precounter.this.notify()
        try
        {
            final long msStart = System.currentTimeMillis();
            synchronized( Precounter.this )
            {
                Precounter.this.wait( MS_TIMEOUT_MIN + MS_TIMEOUT_INTERVAL );
                while( !o.isFullyLoaded )
                {
                    final long msElapsed = System.currentTimeMillis() - msStart;
                    if( msElapsed > MS_TIMEOUT_MIN )
                    {
                        throw new CountFailure( "Overrepo timeout after " + msElapsed + " ms" );
                    }

                    Precounter.this.wait( MS_TIMEOUT_INTERVAL );
                }
            }
        }
        finally{ contentResolver.unregisterContentObserver( o ); }

      // Retry query now that response is fully loaded.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        return queryChildIDs( parentID, provider, true );
    }



    private Uri.Builder uriBuilderSA() // scheme + authority only; cannot clear Uri.Builder
    {
        if( uriBuilderSA == null ) uriBuilderSA = new Uri.Builder();

        return uriBuilderSA;
    }


        private Uri.Builder uriBuilderSA;



    private final XmlPullParserFactory xhtmlParserFactory; /* for Overware XHTML documents because
      parser cannot be reused, https://code.google.com/p/android/issues/detail?id=182605 */



   // ==================================================================================================


    private final class Observer extends ContentObserver
    {

        Observer() { super( Application.i().handler() ); }


        volatile boolean isFullyLoaded;


        public @Override void onChange( boolean _selfChange, Uri _n )
        {
            isFullyLoaded = true;
            synchronized( Precounter.this ) { Precounter.this.notify(); }
        }

    }


}
