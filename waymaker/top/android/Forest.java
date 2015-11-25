package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;
import java.util.*;
import waymaker.gen.*;
import waymaker.spec.VotingID;


/** A model of the forest structure of a pollar count.
  *
  *     @see <a href='../../../../forest' target='_top'>‘forest’</a>
  */
final class Forest implements PeersReceiver
{

    static final PolyStator<Forest> stators = new PolyStator<>();

///////


    /** Constructs a Forest.
      *
      *     @see #pollName()
      *     @see #forestCache()
      */
    Forest( final String pollName, final ForestCache forestCache )
    {
        this( pollName, forestCache, /*inP*/null, /*nodeCache*/null );
    }



    /** Constructs a Forest from stored state.
      *
      *     @see #pollName()
      *     @see #forestCache()
      *     @param inP Parceled state to restore.
      */
    Forest( final String pollName, final ForestCache forestCache, final Parcel inP )
    {
        this( pollName, forestCache, inP, /*nodeCache*/null );
    }



    /** Constructs a Forest from a node cache.
      *
      *     @see #pollName()
      *     @see #forestCache()
      *     @see #nodeCache()
      */
    Forest( final String pollName, final ForestCache forestCache, final NodeCacheF nodeCache )
    {
        this( pollName, forestCache, /*inP*/null, nodeCache );
    }



    private Forest( final String pollName, final ForestCache forestCache,
      final Parcel inP/*grep CtorRestore*/, NodeCacheF nodeCache )
    {
        this.pollName = pollName;
        this.forestCache = forestCache;
        if( inP != null ) stators.restore( this, inP ); // saved by stators in static inits further below
        final boolean isFirstConstruction;
        if( wasConstructorCalled ) isFirstConstruction = false;
        else
        {
            isFirstConstruction = true;
            wasConstructorCalled = true;
        }

      // Node cache.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if( isFirstConstruction ) stators.add( new StateSaver<Forest>()
        {
            public void save( final Forest f, final Parcel out )
            {
              // 1. Size.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                out.writeInt( f.nodeCache.nodeMap.size() );

              // 2. Has precount adjustments?
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                ParcelX.writeBoolean( f.nodeCache.groundUna.precounted() != null, out );

              // 3. Cache.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                NodeCacheF.stators.save( f.nodeCache, out );
            }
        });
        if( inP != null ) // restore
        {
            assert nodeCache == null; // exclusive parameters
            nodeCache = new NodeCacheF( /* 1 */inP.readInt(), /* 2 */ParcelX.readBoolean(inP) );
              // CtorRestore for this config of NodeCacheF construction (q.v.) based on saved state

          // 3.
          // - - -
            NodeCacheF.stators.restore( nodeCache, inP );
        }
        else if( nodeCache == null ) nodeCache = new NodeCacheF( 0, /*hasPrecountAdjustments*/false );
        this.nodeCache = nodeCache;

      // - - -
        if( isFirstConstruction ) stators.seal();
    }



    private static volatile boolean wasConstructorCalled;



   // --------------------------------------------------------------------------------------------------


    /** The store of this forest.
      */
    ForestCache forestCache() { return forestCache; }


        private final ForestCache forestCache;



    /** The current cache of nodes that defines the forest structure.  The cache is never cleared but
      * may be wholly replaced at any time.  Each replacement is signalled by the {@linkplain
      * ForestCache#nodeCacheBell() node cache bell}.
      */
    NodeCache nodeCache() { return nodeCache; }


        private NodeCacheF nodeCache; // constructor adds stator


        /** Sets the cache of nodes.  Does not ring the node cache bell, leaving that to the caller.
          */
        @Warning("non-API") void nodeCache( final NodeCacheF _nodeCache ) { nodeCache = _nodeCache; }


        @Warning("non-API") NodeCacheF nodeCacheF() { return nodeCache; }



    /** The identifier of the poll that was counted to form this forest.  It also identifies the forest.
      */
    String pollName() { return pollName; }


        private final String pollName;



   // - P e e r s - R e c e i v e r --------------------------------------------------------------------


    /** {@inheritDoc} Ultimately this may extend a voter list and possibly
      * {@linkplain Node#votersMaybeIncomplete() complete it}.
      */
    public @ThreadSafe void receivePeersResponse( final Object _in )
    {
      // Decode the default response, pending real server counts.
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
                  pending real server counts
                  */

              // Get candidate from node cache.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final UnadjustedNodeV candidateUna;
                {
                    final UnadjustedNode node = nodeCache.nodeMap.get( req.rootwardID );
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
                        - do something to invalidate node cache and escape cleanly from here

                    - if peer.peerOrdinal < candidateUna.votersNextOrdinal
                        ( response overlaps a prior extension
                        - skip

                    - if peer.peerOrdinal >= peersEndBound of request
                        - log as anomaly
                        - skip

                    - get peer from nodeMap
                    - if none
                        - make peer as UnadjustedNode
                    - else remove from nodeCache.outlyingUnadjustedVoters

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
                        - do something to invalidate node cache and escape cleanly from here
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

                    final List<PrecountNode1> outlyingVoters = nodeCache.outlyingVotersPre;
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
                if( isChanged ) forestCache.voterListingBell().ring();
            }
        });
    }



   // ==================================================================================================


    static @Warning("non-API") final class NodeCacheF implements NodeCache,
      PrecountNode.SKit, PrecountNode.RKit, UnadjustedNodeV.RKit, UnadjustedNodeV.SKit
    {
        static final PolyStator<NodeCacheF> stators = new PolyStator<>();
    ///////

        /** Contructs a NodeCacheF based on the results of a precount.
          */
        @ThreadSafe/*grep FreezeSync*/ NodeCacheF( final Precounter precounter )
        {
            nodeMap = precounter.nodeMap();
            groundUna = precounter.ground();
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
                    // else cannot become an inlier, ∴ is not a proper outlier
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


        /** Contructs a NodeCacheF.
          *
          *     @param originalUnaCount Zero if the new cache is to be an original construction; else
          *        the number of unadjusted nodes in the original.  The value serves only to enlarge the
          *        initial capacity of the cache and speed the subsequent restoration of its state.
          *     @param hasPrecountAdjustments Whether to construct a precount ground for the node cache,
          *       and generally to allow for the subsequent restoration of precount adjustments.
          */
        @ThreadSafe NodeCacheF( final int originalUnaCount, final boolean hasPrecountAdjustments )
        {
            nodeMap = new HashMap<>( MapX.hashCapacity(originalUnaCount + INITIAL_HEADROOM),
              MapX.HASH_LOAD_FACTOR );
            encache( groundUna = new UnadjustedGround() );
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


        /** A map of all cached nodes including the ground pseudo-node, each keyed by its identifier.
          */
        private final HashMap<VotingID,UnadjustedNode> nodeMap;
          // content persisted by groundUna stator via node stators


       // ----------------------------------------------------------------------------------------------

        /** The number of unadjusted nodes in this cache.
          */
        int size() { return nodeMap.size(); }


       // - N o d e - C a c h e ------------------------------------------------------------------------

        /** @return The precount adjusted ground if any, else the unadjusted ground.
          */
        public Node ground() { return ground; }

            private final Node ground;


       // - P r e c o u n t - N o d e . R - K i t ------------------------------------------------------

        public void encache( final UnadjustedNode node ) { nodeMap.put( node.id(), node ); }


        public UnadjustedNode certainlyCached( final VotingID id )
        {
            final UnadjustedNode node = nodeMap.get( id );
            if( node == null ) throw new IllegalStateException();

            return node;
        }


        public void enlistOutlyingVoter( final PrecountNode1 node ) { outlyingVotersPre.add( node ); }


        public UnadjustedGround groundUna() { return groundUna; }

            private final UnadjustedGround groundUna;

            static { stators.add( new Stator<NodeCacheF>()
            {
                public void save( final NodeCacheF nodeCache, final Parcel out )
                {
                  // 1. Unadjusted nodes.
                  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                    final UnadjustedGround groundUna = nodeCache.groundUna;
                    UnadjustedGround.stators.save( groundUna, out, nodeCache );

                  // 2. Precount nodes.
                  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                    final PrecountGround groundPre = groundUna.precounted();
                    if( groundPre != null ) PrecountGround.stators.save( groundPre, out, nodeCache );
                      // else cache constructed without precount adjustments
                }
                public void restore( final NodeCacheF nodeCache/*precountless construction*/, final Parcel in )
                {
                  // 1.
                  // - - -
                    final UnadjustedGround groundUna = nodeCache.groundUna;
                    UnadjustedGround.stators.restore( groundUna, in, nodeCache );

                  // 2.
                  // - - -
                    final PrecountGround groundPre = groundUna.precounted();
                    if( groundPre != null ) PrecountGround.stators.restore( groundPre, in, nodeCache );
                      // else nodeCache constructed without precount adjustments
                }
            });}


       // - P r e c o u n t - N o d e . S - K i t ------------------------------------------------------

        public List<PrecountNode1> outlyingVotersPre() { return outlyingVotersPre; }

            private final List<PrecountNode1> outlyingVotersPre;
              // persisted by groundUna stator via precount node stators


       // - U n a d j u s t e d - N o d e - V . R - K i t ----------------------------------------------

        public void enlistOutlyingVoter( final UnadjustedNode1 node ) { outlyingVotersUna.add( node ); }


       // - U n a d j u s t e d - N o d e - V . S - K i t ----------------------------------------------

        public List<UnadjustedNode1> outlyingVotersUna() { return outlyingVotersUna; }

            private final List<UnadjustedNode1> outlyingVotersUna; /* Unadjusted counterparts of
              precount nodes, they are cached for that purpose though they happen to be outliers.
              Persisted by groundUna stator via unadjusted node stators. */

    ///////
        static { stators.seal(); }
    };


}
