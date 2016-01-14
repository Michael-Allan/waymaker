package waymaker.top.android; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;
import java.util.List;
import waymaker.gen.*;
import waymaker.spec.VotingID;


/** A model of the forest structure of a pollar count.
  *
  *     @see <a href='../../../../forest' target='_top'>‘forest’</a>
  */
public final class Forest implements PeersReceiver
{

    static final PolyStator<Forest> stators = new PolyStator<>();

///////


    /** Constructs a Forest.
      *
      *     @see #pollName()
      *     @see #forestCache()
      */
    public Forest( final String pollName, final ForestCache forestCache )
    {
        this( pollName, forestCache, /*inP*/null, /*nodeCache*/null );
    }



    /** Constructs a Forest from stored state.
      *
      *     @see #pollName()
      *     @see #forestCache()
      *     @param inP The parceled state to restore, or null to restore none, in which case the
      *       openToThread restriction is lifted.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread") // for stators.restore
    public Forest( final String pollName, final ForestCache forestCache, final Parcel inP )
    {
        this( pollName, forestCache, inP, /*nodeCache*/null );
    }



    /** Constructs a Forest from a node cache.
      *
      *     @see #pollName()
      *     @see #forestCache()
      *     @see #nodeCache()
      */
    public Forest( final String pollName, final ForestCache forestCache, final NodeCache1 nodeCache )
    {
        this( pollName, forestCache, /*inP*/null, nodeCache );
    }



      @ThreadRestricted("further KittedPolyStatorSR.openToThread") // for stators.restore
    private Forest( final String pollName, final ForestCache forestCache,
      final Parcel inP/*grep CtorRestore*/, NodeCache1 nodeCache )
    {
        this.pollName = pollName;
        this.forestCache = forestCache;
        final boolean toInitClass;
        if( wasConstructorCalled ) toInitClass = false;
        else
        {
            toInitClass = true;
            wasConstructorCalled = true;
        }
        if( inP != null ) stators.restore( this, inP ); // saved by stators in static inits further below

      // Node cache.
      // - - - - - - -
        if( toInitClass ) stators.add( new StateSaver<Forest>()
        {
            public void save( final Forest f, final Parcel out )
            {
              // 1. Size.
              // - - - - -
                out.writeInt( f.nodeCache.nodeMap.size() );

              // 2. Has precount adjustments?
              // - - - - - - - - - - - - - - -
                ParcelX.writeBoolean( f.nodeCache.groundUna().precounted() != null, out );

              // 3. Cache.
              // - - - - - -
                NodeCache1.stators.save( f.nodeCache, out );
            }
        });
        if( inP != null ) // restore
        {
            assert nodeCache == null; // exclusive parameters
            nodeCache = new NodeCache1( /* 1 */inP.readInt(), /* 2 */ParcelX.readBoolean(inP) );
              // CtorRestore for this config of NodeCache1 construction (q.v.) based on saved state

          // 3.
          // - - -
            NodeCache1.stators.restore( nodeCache, inP );
        }
        else if( nodeCache == null ) nodeCache = new NodeCache1( 0, /*hasPrecountAdjustments*/false );
        this.nodeCache = nodeCache;

      // - - -
        if( toInitClass ) stators.seal();
    }



   // --------------------------------------------------------------------------------------------------


    /** The store that holds this forest.
      */
    public ForestCache forestCache() { return forestCache; }


        private final ForestCache forestCache;



    /** The store of nodes that defines the forest structure.  No content is ever removed or replaced,
      * but the whole cache may be replaced at any time by an instance with different content.  Each
      * replacement will be signalled by the {@linkplain ForestCache#nodeCacheBell() node cache bell}.
      */
    public NodeCache nodeCache() { return nodeCache; }


        private NodeCache1 nodeCache; // constructor adds stator


        /** Sets the cache of nodes.  Does not ring the node cache bell, leaving that to the caller.
          */
        @Warning("non-API") void nodeCache( final NodeCache1 _nodeCache ) { nodeCache = _nodeCache; }


        @Warning("non-API") NodeCache1 nodeCache1() { return nodeCache; }



    /** The identity of the poll that was counted to form this forest.  It also identifies the forest.
      */
    public String pollName() { return pollName; }


        private final String pollName;



   // - P e e r s - R e c e i v e r --------------------------------------------------------------------


    /** {@inheritDoc} Ultimately this may extend a voter list and possibly
      * {@linkplain CountNode#votersMaybeIncomplete() complete it}.
      */
    public @ThreadSafe void receivePeersResponse( final Object _in )
    {
      // Decode the default response, pending real server counts.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        class Request
        {
            final VotingID rootwardID = (VotingID)_in;
            final int peersStart = 0; // request is for initial peers data
         // final int peersEndBound ... not yet used below
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
              // - - - - - - - - - - - - - - - -
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
              // - - - - - - - - - - - - - - - - - - - - - - -
                boolean areVotersChanged = false;
                /* * *
                - for each peer in response
                    - if peer reveals serial inconstency (RepocastSer)
                        - do something to invalidate node cache and escape cleanly from here

                    - if peer.peerOrdinal < candidateUna.votersNextOrdinal
                        ( response overlaps a prior extension
                        - skip

                    - if peer.peerOrdinal >= req.peersEndBound
                        - log as anomaly
                        - skip

                    - get peer from nodeMap
                    - if none
                        - make peer as UnadjustedNode
                    - else remove from nodeCache.outlyingUnadjustedVoters

                    - ensure peer orderly inserted into tail of candidateUna.voters
                        ( cannot already be present
                        ( no need to search in head (pre-existing members), list contracted to grow by pure extension
                    - set areVotersChanged true
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
                    areVotersChanged = true;
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
                    - set candidateUna.votersNextOrdinal to req.peersEndBound
                      */
                    throw new UnsupportedOperationException(); // for pseudo code above
                }

                final boolean isLeaderChanged;
                if( votersNextOrdinal == 0 ) // this is initial extension covering major voters
                {
                    assert candidateUna.votersNextOrdinal() != 0; // this happens just once

                  // Extend with any adjusted voters from precount.
                  // - - - - - - - - - - - - - - - - - - - - - - - -
                    final PrecountNode candidatePre = candidateUna.precounted();
                    if( candidatePre != null )
                    {
                        final List<PrecountNode1> outlyingVoters = nodeCache.outlyingVotersPre();
                        for( int v = outlyingVoters.size() - 1; v >= 0; --v )
                        {
                            final PrecountNode outlyingVoter = outlyingVoters.get( v );
                            if( outlyingVoter.rootwardInThis().candidate() != candidatePre ) continue;

                            assert outlyingVoter.peerOrdinal() == 0; // major voter, okay for initial extension
                            candidatePre.enlistVoter( outlyingVoter );
                            ListX.removeUroboros( v, outlyingVoters ); // no longer outlying
                            areVotersChanged = true;
                        }
                    }
                    // else no adjusted candidate, ∴ no adjusted voters

                  // - - -
                    isLeaderChanged = candidateUna.isGround() && !nodeCache.leader().isGround();
                      // the initial extension of ground voters (forest roots) exposes actual root as leader
                }
                else isLeaderChanged = false;

              // Ring out any changes.
              // - - - - - - - - - - - - - - - - - - - - - - - -
                if( areVotersChanged ) forestCache.voterListingBell().ring();
                if( isLeaderChanged ) forestCache.nodeCacheBell().ring();
            }
        });
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static volatile boolean wasConstructorCalled;


}
