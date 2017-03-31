package waymaker.top.android; // Copyright © 2015 Michael Allan.  Licence MIT.

import android.os.Parcel;
import java.util.*;
import waymaker.gen.*;
import waymaker.spec.VotingID;


final class NodeCache1 implements NodeCache, PrecountNode.SKit, PrecountNode.RKit,
  UnadjustedNodeV.RKit, UnadjustedNodeV.SKit
{

    static final PolyStator<NodeCache1> stators = new PolyStator<>();

///////


    /** Contructs a NodeCache1 based on the results of a precount.
      */
    @ThreadSafe/*grep FreezeSync*/ NodeCache1( final Precounter precounter )
    {
        nodeMap = precounter.nodeMap();
        groundUna = precounter.ground();
        final CountNode groundPre = groundUna.precounted(); // if any
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



    /** Contructs a NodeCache1.
      *
      *     @param originalUnaCount Zero if the new cache is to be an original construction; else
      *        the number of unadjusted nodes in the original.  The value serves only to enlarge the
      *        initial capacity of the cache and speed the subsequent restoration of its state.
      *     @param hasPrecountAdjustments Whether to construct a precount ground for the node cache,
      *       and generally to allow for the subsequent restoration of precount adjustments.
      */
    @ThreadSafe NodeCache1( final int originalUnaCount, final boolean hasPrecountAdjustments )
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



   // - N o d e - C a c h e --------------------------------------------------------------------------------


    /** @return The precount adjusted ground if any, else the unadjusted ground.
      */
    public CountNode ground() { return ground; }


        private final CountNode ground;



    public CountNode leader()
    {
        final List<? extends CountNode> roots = ground.voters();
        return roots.size() == 0? ground: roots.get(0);
    }



   // - P r e c o u n t - N o d e . R - K i t --------------------------------------------------------------


    public UnadjustedNode certainlyCached( final VotingID id )
    {
        final UnadjustedNode node = nodeMap.get( id );
        if( node == null ) throw new IllegalStateException();

        return node;
    }



    public void encache( final UnadjustedNode node ) { nodeMap.put( node.id(), node ); }



    public void enlistOutlyingVoter( final PrecountNode1 node ) { outlyingVotersPre.add( node ); }



    public UnadjustedGround groundUna() { return groundUna; }


        private final UnadjustedGround groundUna;


        static { stators.add( new Stator<NodeCache1>()
        {
            public void save( final NodeCache1 nodeCache, final Parcel out )
            {
              // 1. Unadjusted nodes.
              // - - - - - - - - - - -
                final UnadjustedGround groundUna = nodeCache.groundUna;
                UnadjustedGround.stators.save( groundUna, out, nodeCache );

              // 2. Precount nodes.
              // - - - - - - - - - -
                final PrecountGround groundPre = groundUna.precounted();
                if( groundPre != null ) PrecountGround.stators.save( groundPre, out, nodeCache );
                  // else cache constructed without precount adjustments
            }
            public void restore( final NodeCache1 nodeCache/*precountless construction*/, final Parcel in )
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



   // - P r e c o u n t - N o d e . S - K i t ----------------------------------------------------------


    public List<PrecountNode1> outlyingVotersPre() { return outlyingVotersPre; }


        private final List<PrecountNode1> outlyingVotersPre;
          // persisted by groundUna stator via precount node stators



   // - U n a d j u s t e d - N o d e - V . R - K i t --------------------------------------------------


    public void enlistOutlyingVoter( final UnadjustedNode1 node ) { outlyingVotersUna.add( node ); }



   // - U n a d j u s t e d - N o d e - V . S - K i t --------------------------------------------------


    public List<UnadjustedNode1> outlyingVotersUna() { return outlyingVotersUna; }


        private final List<UnadjustedNode1> outlyingVotersUna; /* Unadjusted counterparts of
          precount nodes, they are cached for that purpose though they happen to be outliers.
          Persisted by groundUna stator via unadjusted node stators. */



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    /** A map of all cached nodes including the ground pseudo-node, each keyed by its identity tag.
      */
    final HashMap<VotingID,UnadjustedNode> nodeMap; // content persisted by groundUna stator via node stators


///////

    static { stators.seal(); }

};
