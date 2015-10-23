package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import android.content.ContentResolver;
import android.os.Parcel;
import android.widget.TextView;
import java.util.*;
import org.xmlpull.v1.*;
import overware.gen.*;
import overware.spec.VotingID;

import static java.util.logging.Level.WARNING;


/** A model of the forest structure of a pollar count.
  *
  *     @see <a href='../../../../forest' target='_top'>‘forest’</a>
  */
@ThreadRestricted("app main") final class Forest implements PeersReceiver
{

    static final PolyStator<Forest> stators = new PolyStator<>();

///////


    /** Constructs a Forest.
      *
      *     @see #pollName()
      *     @see #startRefreshFromOverrepo(TextView)
      *     @param inP The parceled state to restore, or null to restore none.
      */
    Forest( String _pollName, final TextView refreshFeedbackView, Overguidance _og, final Parcel inP )
    {
        pollName = _pollName;
        og = _og;
        if( inP != null ) stators.restore( this, inP ); // saved by stators in static inits further below
        final boolean isFirstConstruction;
        if( wasConstructorCalled ) isFirstConstruction = false;
        else
        {
            isFirstConstruction = true;
            wasConstructorCalled = true;
        }

      // Cache.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if( isFirstConstruction ) stators.add( new StateSaver<Forest>()
        {
            public void save( final Forest f, final Parcel out )
            {
              // 1. Has precount adjustments?
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                ParcelX.writeBoolean( f.cache.groundUna.precounted() != null, out );

              // 2. Cache
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                Cache.stators.save( f.cache, out );
            }
        });
        if( inP != null ) // restore
        {
          // 1.
          // - - -
            cache = new Cache( ParcelX.readBoolean( inP ));

          // 2.
          // - - -
            Cache.stators.restore( cache, inP );
        }
        else cache = new Cache( /*hasPrecountAdjustments*/false );

      // - - -
        if( isFirstConstruction ) stators.seal();
        if( inP == null ) startRefreshFromOverrepo( refreshFeedbackView ); // if new, not restoration
    }



    private static boolean wasConstructorCalled;



   // --------------------------------------------------------------------------------------------------


    /** The current cache of nodes that defines the forest structure.  The cache is never cleared but
      * may be wholly replaced at any time.  Each replacement is signalled by the {@linkplain
      * Forest#cacheBell() cache bell}.
      */
    NodeCache cache() { return cache; }


        private Cache cache; // constructor adds stator



    /** A bell that rings when the {@linkplain #cache() cache} is replaced.
      */
    Bell<Changed> cacheBell() { return cacheBell; }


        private final ReRinger<Changed> cacheBell = Changed.newReRinger();



    /** The name of the guideway poll that was counted to form this forest.
      */
    String pollName() { return pollName; }


        private final String pollName;



    /** Initiates a refresh of this forest by attempting to re-read all contributing data, including
      * position data from the user's local overrepo, and count data from the remote guideway.  A
      * successful refresh will eventually replace the underlying {@linkplain #cache() cache}, sounding
      * its bell.
      *
      *     @param feedbackView A channel to inform the user of the ultimate result.
      */
    void startRefresh( final TextView feedbackView ) { ensurePrecount( true, feedbackView ); }



    /** Initiates a refresh of this forest by attempting to re-read position data from the user’s local
      * overrepo.  A successful refresh will eventually replace the underlying {@linkplain #cache()
      * cache}, sounding its bell.
      *
      *     @param feedbackView A channel to inform the user of the ultimate result.
      */
    void startRefreshFromOverrepo( final TextView feedbackView ) { ensurePrecount( false, feedbackView ); }



    /** A bell that rings when a nodal {@linkplain Node#voters() voter list} is extended.
      */
    Bell<Changed> voterListingBell() { return voterListingBell; }


        /* * *
        - after hearing a ring, client may retest extension of precount node's voter list
            - it may happen that the extension covered one or more voters who shifted away in the precount
                - so precount list might not extend as much as expected, or not at all
            - client may then issue another extension request
          */


        private final ReRinger<Changed> voterListingBell = Changed.newReRinger();


   // - P e e r s - R e c e i v e r --------------------------------------------------------------------


    /** {@inheritDoc} Ultimately this may extend a voter list and possibly
      * {@linkplain Node#votersMaybeIncomplete() complete it}.
      */
    public @ThreadSafe void receivePeersResponse( final Object _in )
    {
      // Decode the default response, pending real guideway counts.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        class Request
        {
            final VotingID rootwardID = (VotingID)_in;
            final int peersStart = 0; // request is for initial peers data
            final boolean exhaustsPeers = true;
        }
        class Response
        {
            final boolean isReal = false;
        }
        final Request req = new Request();
        final Response res = new Response();
        Application.i().handler().post( new Runnable()
        {
            public void run() // on application main thread
            {
                /* * *
                - much of what follows is still pseudo code in extended comments (such as this)
                  pending real guideway counts
                  */

              // Get candidate from cache.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final UnadjustedNodeV candidateUna;
                {
                    final UnadjustedNode node = cache.nodeMap.get( req.rootwardID );
                    if( node == null ) return; // obsolete response, node no longer cached

                    if( node instanceof UnadjustedNode0 ) return; /* obsolete response, node no longer
                      has unadjusted voters, which are therefore already complete */

                    candidateUna = (UnadjustedNodeV)node;
                }
                final int votersNextOrdinal = candidateUna.votersNextOrdinal();
                if( votersNextOrdinal == Integer.MAX_VALUE ) return; // obsolete response, voters now complete

                if( votersNextOrdinal < req.peersStart ) return;
                  // obsolete response, voters now insufficiently extended (gap versus req.peersStart)

              // Extend with unadjusted voters from response.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                boolean isChanged = false; // so far
                /* * *
                - for each peer in response
                    - if peer reveals serial inconstency (RepocastSer)
                        - do something to invalidate cache and escape cleanly from here

                    - if peer.peerOrdinal < candidateUna.votersNextOrdinal
                        ( response overlaps a prior extension
                        - skip

                    - if peer.peerOrdinal >= peersEndBound of request
                        - log as anomaly
                        - skip

                    - get peer from nodeMap
                    - if none
                        - make peer as UnadjustedNode
                    - else remove from cache.outlyingUnadjustedVoters

                    - ensure peer orderly inserted into tail of candidateUna.voters
                        ( cannot already be present
                        ( no need to search in head (pre-existing members), list contracted to grow by pure extension
                    - set isChanged true
                    - if candidateUna.precounted
                        - if no peer.precounted
                            - ensure peer orderly inserted into tail of candidateUna.precounted.voters
                                ( cannot already be present
                                ( no need to search in head... "
                        ( else already added (or to be added) by "precount" extender below
                  */
                if( res.isReal ) throw new UnsupportedOperationException(); // for pseudo code above

                if( req.exhaustsPeers )
                {
                    candidateUna.votersNextOrdinal( Integer.MAX_VALUE );
                    isChanged = true;
                    /* * *
                    - sum outflow of all candidateUna.voters (or inflow if voters are actually roots)
                    - if sum does not equal candidateUna inflow (or poll turnout)
                        ( inconsistency from voters shifting away or unvoting, RepocastSer cannot reliably detect
                            ( will be less than expected, never more
                        - do something to invalidate cache and escape cleanly from here
                      */
                    if( res.isReal ) throw new UnsupportedOperationException(); // for pseudo code above
                }
                else
                {
                    /* * *
                    - set candidateUna.votersNextOrdinal to request peersEndBound
                      */
                    throw new UnsupportedOperationException(); // for pseudo code above
                }

              // Extend with any adjusted voters from precount.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                precount: if( votersNextOrdinal == 0 ) // this is initial extension covering major voters
                {
                    assert candidateUna.votersNextOrdinal() != 0; // will do just once
                    final PrecountNode candidatePre = candidateUna.precounted();
                    if( candidatePre == null ) break precount; // no adjusted candidate, no adjusted voters

                    final List<PrecountNode1> outlyingVoters = cache.outlyingVotersPre;
                    for( int v = outlyingVoters.size() - 1; v >= 0; --v )
                    {
                        final PrecountNode outlyingVoter = outlyingVoters.get( v );
                        if( outlyingVoter.rootwardInThis().candidate() != candidatePre ) continue;

                        assert outlyingVoter.peerOrdinal() == 0; // major voter, okay for initial extension
                        candidatePre.enlistVoter( outlyingVoter );
                        ListX.removeUroboros( v, outlyingVoters ); // no longer outlying
                        isChanged = true;
                    }
                }

              // - - -
                if( isChanged ) voterListingBell.ring();
            }
        });
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private void ensurePrecount( final boolean isDeep, final TextView feedbackView )
    {
        if( tPrecount != null ) tPrecount.interrupt(); // old one no longer wanted, try to tell it
        final Application app = Application.i();

        // thread-safe reference and construction of resources:
        final byte[] groundState;
        if( isDeep ) groundState = null;
        else
        {
            final Parcel out = Parcel.obtain();
            try
            {
                UnadjustedGround.stators.save( cache.groundUna, out, cache );
                groundState = out.marshall(); // sic
            }
            finally { out.recycle(); }
        }
        final ContentResolver cResolver = app.getContentResolver(); // grep ContentResolver-TS
        final String overrepoTreeLoc = og.overrepoTreeLoc();
        final int serial = ++tPrecountSerialLast;

        tPrecount = new Thread( new Runnable() // grep StartSync
        {
            public void run() // in parallel
            {
                final Precounter precounter = new Precounter( pollName, groundState, cResolver,
                  overrepoTreeLoc );
                final Thread t = Thread.currentThread();
                try { precounter.precount(); }
                catch( final CountFailure x )
                {
                    logger.log( WARNING, "Unable to precount from local overrepo", x );
                    app.handler().post( new Runnable()
                    {
                        public void run() // on application main thread
                        {
                            if( t != tPrecount ) return; // this precount superceded

                            tPrecount = null; // release to garbage collector
                            final StringBuilder b = app.stringBuilderClear();
                            b.append( "Refresh " ).append( serial ).append( ": " );
                            ThrowableX.toStringDeeply( x, b );
                            feedbackView.setText( b.toString() );
                        }
                    });
                    return;
                }
                catch( InterruptedException _x ) { return; } // this precount no longer wanted

                final Cache _cache = new Cache( precounter ); // collate results of precount
                app.handler().post( new Runnable() // try to apply results
                {
                    public void run() // on application main thread
                    {
                        if( t != tPrecount ) return; // this precount superceded

                        tPrecount = null; // release to garbage collector
                        try{ t.join(); } // grep TermSync
                        catch( InterruptedException _x )
                        {
                            Thread.currentThread().interrupt(); // pass it on
                            return;
                        }

                        cache = _cache;
                        cacheBell.ring();
                        feedbackView.setText( "Refresh " + serial + " done" );
                    }
                });
            }
        }, /*name*/"precount " + serial );
        tPrecount.setPriority( Thread.NORM_PRIORITY ); // or to limit of group
        tPrecount.setDaemon( true );
        tPrecount.start(); /* No harm running to completion regardless of app state.  If unwanted later,
          can interrupt or not as convenient. */
    }


        private Thread tPrecount; /* Normally nulled when it ends unless already overwritten by start of
          successor.  Nulled only to enable garbage collection, not as signal to be relied on. */


        private int tPrecountSerialLast; // to prevent applying results of superceded precount thread

            static { stators.add( new Stator<Forest>()
            {
                // mainly to preserve "refresh" count, as no thread is likely to survive save/restore cycle
                public void save( final Forest f, final Parcel out ) { out.writeInt( f.tPrecountSerialLast ); }
                public void restore( final Forest f, final Parcel in )
                {
                    f.tPrecountSerialLast = in.readInt();
                }
            });}



    private static final java.util.logging.Logger logger = LoggerX.getLogger( Forest.class );



    private final Overguidance og;



   // ==================================================================================================


    private static final class Cache implements NodeCache, PrecountNode.SKit, PrecountNode.RKit,
      UnadjustedNodeV.RKit, UnadjustedNodeV.SKit
    {
        static final PolyStator<Cache> stators = new PolyStator<>();
    ///////

        /** Contructs a Cache based on the results of a precount.
          */
        Cache( final Precounter p )
        {
            nodeMap = p.nodeMap();
            groundUna = p.ground();
            final Node groundPre = groundUna.precounted(); // if any
            if( groundPre != null )
            {
                outlyingVotersPre = new ArrayList<>();
                outlyingVotersUna = new ArrayList<>();
                for( final UnadjustedNode una: nodeMap.values() )
                {
                    if( una.getClass() != UnadjustedNode0.class )
                    {
                        final RootwardCast<UnadjustedNode> castUna = una.rootwardInThis();
                        if( castUna == null ) continue; // ground

                        if( una.peerOrdinal() >= castUna.candidate().votersNextOrdinal() )
                        {
                            outlyingVotersUna.add( (UnadjustedNode1)una );
                        }
                    }
                    // else cannot become an inlier, ∴ is xsnot a proper outlier
                    final PrecountNode pre = una.precounted();
                    if( pre == null ) continue;

                    assert pre.peerOrdinal() == 0;
                    if( 0 >= pre.rootwardInThis().candidate().votersNextOrdinal() )
                    {
                        outlyingVotersPre.add( (PrecountNode1)pre );
                    }
                }
                ground = groundPre;
            }
            else // no precount adjustments
            {
                outlyingVotersPre = Collections.emptyList();
                outlyingVotersUna = Collections.emptyList();
                ground = groundUna;
            }
        }


        /** Contructs a Cache to be restored by stators.
          *
          *     @param hasPrecountAdjustments Whether to construct a precount ground for the cache, and
          *       generally to allow for the restoration of precount adjustments.
          */
        Cache( final boolean hasPrecountAdjustments )
        {
            nodeMap = new HashMap<>();
            groundUna = new UnadjustedGround();
            nodeMap.put( groundUna.id(), groundUna );
            if( hasPrecountAdjustments )
            {
                outlyingVotersPre = new ArrayList<>();
                outlyingVotersUna = new ArrayList<>();
                ground = new PrecountGround( groundUna );
            }
            else
            {
                outlyingVotersPre = Collections.emptyList(); // outliers impossible without a precount
                outlyingVotersUna = Collections.emptyList();
                ground = groundUna;
            }
        }


       // ----------------------------------------------------------------------------------------------

        /** A map of all cached nodes including the ground pseudo-node, each keyed by its identifier.
          */
        final HashMap<VotingID,UnadjustedNode> nodeMap; // persisted by groundUna stator via node stators


       // - N o d e - C a c h e ------------------------------------------------------------------------

        public Node ground() { return ground; }

            final Node ground; // points to groundUna.precounted if any, else to groundUna


       // - P r e c o u n t - N o d e . R - K i t ------------------------------------------------------

        public void cache( final UnadjustedNode node ) { nodeMap.put( node.id(), node ); }


        public UnadjustedNode certainlyCached( final VotingID id )
        {
            final UnadjustedNode node = nodeMap.get( id );
            if( node == null ) throw new IllegalStateException();

            return node;
        }


        public void enlistOutlyingVoter( final PrecountNode1 node ) { outlyingVotersPre.add( node ); }


        public UnadjustedGround groundUna() { return groundUna; }

            final UnadjustedGround groundUna;

            static { stators.add( new Stator<Cache>()
            {
                public void save( final Cache cache, final Parcel out )
                {
                  // 1. Unadjusted nodes.
                  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                    final UnadjustedGround groundUna = cache.groundUna;
                    UnadjustedGround.stators.save( groundUna, out, cache );

                  // 2. Precount nodes.
                  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                    final PrecountGround groundPre = groundUna.precounted();
                    if( groundPre != null ) PrecountGround.stators.save( groundPre, out, cache );
                      // else cache constructed without precount adjustments
                }
                public void restore( final Cache cache/*precountless construction*/, final Parcel in )
                {
                  // 1.
                  // - - -
                    final UnadjustedGround groundUna = cache.groundUna;
                    UnadjustedGround.stators.restore( groundUna, in, cache );

                  // 2.
                  // - - -
                    final PrecountGround groundPre = groundUna.precounted();
                    if( groundPre != null ) PrecountGround.stators.restore( groundPre, in, cache );
                      // else cache constructed without precount adjustments
                }
            });}


       // - P r e c o u n t - N o d e . S - K i t ------------------------------------------------------

        public List<PrecountNode1> outlyingVotersPre() { return outlyingVotersPre; }

            final List<PrecountNode1> outlyingVotersPre;
              // persisted by groundUna stator via precount node stators


       // - U n a d j u s t e d - N o d e - V . R - K i t ----------------------------------------------

        public void enlistOutlyingVoter( final UnadjustedNode1 node ) { outlyingVotersUna.add( node ); }


       // - U n a d j u s t e d - N o d e - V . S - K i t ----------------------------------------------

        public List<UnadjustedNode1> outlyingVotersUna() { return outlyingVotersUna; }

            final List<UnadjustedNode1> outlyingVotersUna; /* Unadjusted counterparts of precount nodes,
              they are cached for that purpose though they happen to be outliers.  Persisted by
              groundUna stator via unadjusted node stators. */

    ///////
        static { stators.seal(); }
    };


}
