package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.widget.*;
import java.util.*;
import waymaker.gen.*;



/** <p>A forest view oriented by the {@linkplain Wayranging#forester() forester}.  Its main components
  * are {@linkplain Node node} views (lettered).  These it divides vertically between a peers viewer for
  * showing the forester’s {@linkplain Forester#ascendTo(Node) leafward paths}, and a candidates viewer
  * for showing its {@linkplain Forester#descend() rootward paths}.</p>

  * <pre>
  *                     ◢◣    --- Up climber
  *                    ————   --- Up pager for peers
  *                     ti
  *                     sh
  *        Peers ---    rg
  *                     qf ◄  --- Choice indicator
  *                     pe
  *                    ————   --- Down pager for peers
  *                     ca     --- Position in forest (forester.position)
  *   Candidates ---    db
  *                     ··    --- Ellipsis for omitted candidates
  *                     fd
  *                     ◥◤    --- Down climber
  * </pre>
  * <p>The peers viewer alone is paged; the candidates viewer is ellipsed.</p>
  */
public @ThreadRestricted("app main") final class ForestV extends LinearLayout
{


    /** Constructs a ForestV.
      */
    public ForestV( final Wayranging wr )
    {
        super( /*context*/wr );

      // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /  LAYOUT
        setOrientation( VERTICAL );
        setGravity( android.view.Gravity.BOTTOM );
        TextView textV;

      // Children at fixed indeces.
      // - - - - - - - - - - - - - -
        addView( upClimber = new TextView(wr) );
        upClimber.setText( "◢◣" );
        addView( textV = new TextView(wr) ); // up pager
        textV.setText( "————" );
        assert getChildCount() == C_TOP_PEER;

      // Floating children, whose indeces depend on viewer population.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        addView( downPager = new TextView(wr) );
        downPager.setText( "————" );
        ellipsis = new TextView( wr );
        ellipsis.setText( "··" );
        addView( downClimber = new TextView(wr) );
        downClimber.setText( "◥◤" );


      // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
        calibrator = new ForestVCalibrator( this );

      // Populate viewers initially.  Replace population when forester replaces node cache.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final Forester forester = wr.forester();
        forester.bell().register( new Auditor<Changed>()
        {
            { populate( forester, forester.nodeCache() ); } // populates initially
            private void populate( final Forester forester, final NodeCache _nodeCache )
            {
                nodeCache = _nodeCache;
                syncViewers( forester );
            }
            public void hear( Changed _ding ) // replaces population if forester has replaced node cache
            {
                final Forester forester = wr().forester();
                final NodeCache _nodeCache = forester.nodeCache();
                if( _nodeCache == nodeCache ) return;

                depopulateViewers();
                populate( forester, _nodeCache );
            }
        });

      // Extend peers viewer when model extends.
      // - - - - - - - - - - - - - - - - - - - - -
        wr.forests().voterListingBell().register( new Auditor<Changed>()
        {
            public void hear( Changed _ding )
            {
                final Node candidate = candidateViewed();
                final List<? extends Node> _peers = candidate.voters();
                if( peerCount == _peers.size() ) return;

                assert peerCount < _peers.size(); // forest.nodeCache guarantees no node is ever removed
                syncPeersViewer( candidate, _peers );
            }
        });
    }



   // - V i e w ----------------------------------------------------------------------------------------


    protected void onLayout( final boolean isChanged, final int left, final int top, final int right,
      final int bottom )
    {
        super.onLayout( isChanged, left, top, right, bottom );
        calibrator.activateMaybe(); /* Poll to detect whether a calibration parameter has changed.
          Simple and robust, if only as a fallback.  Execution may continue at this.reconstrain. */
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final ForestVCalibrator calibrator;



    int candidateCount; // count of nodes in candidates viewer, comparable to forester.height



    private Node candidateViewed() // topmost node of candidates viewer, or ground if viewer is grounded
    {
        return candidateCount > 0? getChildNodeV(cTopCandidate()).node(): nodeCache.ground();
    }



    private int cBottomCandidate() { return getChildCount() + CN_BOTTOM_CANDIDATE; }
      // child index of bottom-most view in candidates viewer, assuming candidateCount > 0



    private int cBottomPeer() { return C_TOP_PEER + peerCount - 1; }
      // child index of bottom-most view in peers viewer, assuming peerCount > 0



    private int cCandidatesEndBound() { return getChildCount() + CN_BOTTOM_CANDIDATE + 1; }
      // child index of first view after candidates viewer



    private static final int CN_BOTTOM_CANDIDATE = -2;
      // negative child index of bottom-most view in candidates viewer, assuming candidateCount > 0



    int cTopCandidate() { return C_TOP_PEER + peerCount + 1; }
      // child index of topmost view in candidates viewer, assuming candidateCount > 0



    static final int C_TOP_PEER = 2; // child index of topmost view in peers viewer, assuming peerCount > 0



    private void depopulateViewers()
    {
System.err.println( " --- depopulating" ); // TEST
      // Peers, cf. syncViewersOnDeficit.
      // - - - - - - - - - - - - - - - - -
        if( peerCount > 0 ) for( int c = cBottomPeer();; --c ) // backward for fast removals
        {
            removePeerToPool( c, getChildNodeV(c) );
            if( peerCount == 0 ) break;
        }

      // Candidates.
      // - - - - - - -
        if( candidateCount > 0 ) for( int c = cBottomCandidate();; --c ) // backward for fast removals
        {
            removeCandidateToPool( c, getChildNodeV(c) );
            if( candidateCount == 0 ) break;
        }
    }



    final TextView downClimber;



    private final TextView downPager;



    final TextView ellipsis;



    private NodeV getChildNodeV( final int c ) { return (NodeV)getChildAt( c ); }



    private NodeCache nodeCache;



    int nodeCount() { return peerCount + candidateCount; }



    int peerCount; // count of nodes in peers viewer, comparable to forester.position.voters.size



    private final ArrayDeque<NodeV> pool = new ArrayDeque<>(); // ∀ node null


        private void enpool( final NodeV nodeV )
        {
            assert nodeV.node() == null;
            pool.add( nodeV );
        }


        private NodeV expool() { return pool.remove(); }



    private void removeCandidateToPool( final int cCandidate, final NodeV candidateV )
    {
        removeViewAt( cCandidate );
        --candidateCount;
        candidateV.node( null ); // release to garbage collector
        enpool( candidateV );
    }



    private void removePeerToPool( final int cPeer, final NodeV peerV )
    {
        removeViewAt( cPeer );
        --peerCount;
        peerV.node( null ); // release to garbage collector
        enpool( peerV );
    }



    private final ServerCount serverCount = new ServerCount();



    /** Ensures the peers viewer is populated to match the forest model.
      *
      *     @param candidate The viewed candidate.
      *     @param _peers The modeled peers, viz. candidate.voters.
      */
    private void syncPeersViewer( final Node candidate, final List<? extends Node> _peers )
    {
        final int pN = _peers.size(); // modeled
System.err.println( " --- syncPeersViewer, peers modeled: " + pN ); // TEST
        if( pN > 0 ) // guarding against frequent, initial case of unextended voters
        {
            final Wayranging wr = wr();
            final int pStart = peerCount; // viewed
            int c = C_TOP_PEER;
            int p = pN - 1; // starting at top peer, to speed each addition
            assert _peers instanceof RandomAccess;
            for(; p >= pStart; --p, ++c ) // close gap between viewed and modeled
            {
                final Node peer = _peers.get( p );
                final NodeV peerV;
                if( pool.size() > 0 )
                {
                    peerV = expool();
                    peerV.node( peer );
                }
                else peerV = new NodeV( wr, peer );
                addView( peerV, c );
                ++peerCount;
            }
        }
        if( candidate.votersMaybeIncomplete() )
        {
            serverCount.enqueuePeersRequest( candidate.id(), wr().forester().forest(), /*paddedLimit*/0 );
        }
    }



    private void syncViewers( final Forester forester ) // ensures each populated to match forest and forester
    {
        final int _candidateCount = forester.height();
        final Node _candidate = forester.position();
        final int heightBalance = candidateCount - _candidateCount;
System.err.println( " --- syncing viewers, heightBalance=" + heightBalance ); // TEST

      // View may be higher than model
      // - - - - - - - - - - - - - - - -
        if( heightBalance > 0 )
        {
            if( _candidateCount == 0 ) syncViewersOnSurplus();
            else
            {
                final NodeV _candidateV = getChildNodeV( cCandidatesEndBound() - _candidateCount );
                  // candidate view at model height
                if( _candidateV.node() == _candidate ) syncViewersOnSurplus();
                else syncViewersOnMismatch( _candidateCount, _candidate );
            }
            return;
        }

        final Node candidate = candidateViewed();

      // Or lower than model
      // - - - - - - - - - - -
        if( heightBalance < 0 )
        {
            Node _node = _candidate;
            int b = heightBalance;
            do // drop _node to view height
            {
                _node = _node.rootwardInPrecount().candidate();
                ++b;
            }
            while( b < 0 );
            if( candidate == _node ) syncViewersOnDeficit( heightBalance, _candidate );
            else syncViewersOnMismatch( _candidateCount, _candidate );
            return;
        }

      // Else at same height.
      // - - - - - - - - - - -
        if( candidate == _candidate ) syncPeersViewer( candidate, candidate.voters() );
        else syncViewersOnMismatch( _candidateCount, _candidate );
    }



    /** @param _candidate The new immediate candidate, viz. forester.position.
      */
    private void syncViewersOnDeficit( final int heightBalance, final Node _candidate )
    {
        assert heightBalance < 0;
        if( heightBalance == -1 ) // then a deficit of one candidate
        {
          // Depopulate peers viewer, cf. depopulateViewers().
          // - - - - - - - - - - - - - - - - - - - - - - - - - -
            if( peerCount > 0 ) for( int c = cBottomPeer();; --c ) // backward for fast removals
            {
                final NodeV peerV = getChildNodeV( c );
                if( peerV.node() == _candidate )
                {
                  // Moving new candidate to candidates viewer.
                  // - - - - - - - - - - - - - - - - - - - - - -
                    final int cDownPager = c + 1; // just below peerV
                    removeViewAt( cDownPager ); // lowering peerV in layout, a change that animates
                    addView( downPager, c ); // just above peerV, leaving peerV atop candidates viewer
                    --peerCount;
                    ++candidateCount;
                }
                else removePeerToPool( c, peerV );
                if( peerCount == 0 ) break;
            }

          // Repopulate peers viewer.
          // - - - - - - - - - - - - -
            syncPeersViewer( _candidate, _candidate.voters() );
        }
        else // a deficit of multiple candidates, an abnormal case
        {
            throw new UnsupportedOperationException( "Not yet coded" );
        }
    }



    /** @param _candidateCount The new candidate count, viz. forester.height.
      * @param _candidate The new immediate candidate, viz. forester.position.
      */
    private void syncViewersOnMismatch( final int _candidateCount, final Node _candidate ) // abnormal case
    {
        depopulateViewers();
        syncViewersOnDeficit( -_candidateCount, _candidate );
    }



    private void syncViewersOnSurplus()
    {
        throw new UnsupportedOperationException( "Not yet coded" );
    }



    final TextView upClimber;



    private Wayranging wr() { return (Wayranging)getContext(); }


}
