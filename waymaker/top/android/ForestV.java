package waymaker.top.android; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.content.res.*;
import android.graphics.drawable.VectorDrawable;
import android.util.*;
import android.widget.*;
import java.io.IOException;
import java.util.*;
import org.xmlpull.v1.XmlPullParserException;
import waymaker.gen.*;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;
import static waymaker.top.android.ForestVCalibrator.Calibration;


/** <p>A forest view oriented by the {@linkplain Wayranging#forester() forester}.  Its main components
  * are {@linkplain CountNode node} views (lettered).  These it divides vertically between a peers viewer for
  * showing the forester’s {@linkplain Forester#ascendTo(CountNode) leafward paths}, and a candidates viewer
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
  *
  *                           --- Spaces
  * </pre>
  * <p>The peers viewer alone is paged; the candidates viewer is ellipsed.  The spaces pseudo-viewer is
  * populated at the bottom with unused candidate spaces to achieve a rough, vertical centering that
  * animates easily with LinearLayout.</p>
  */
public @ThreadRestricted("app main") final class ForestV extends LinearLayout
{
    /* * *
    = constrain candidate addition
    = constrain peer addition
        - also using the constraint to guard and constrain call to enqueuePeersRequest
    = reconstrain on calibration change
        - direct call from calibrator
    = allow for case of overconstrained parent in ForestVCalibrator
        ( deferred from above, for realistic test
        - detected by squeezed height of down climber (bottom component) compared to up climber
        - calibrated by (somehow) measuring height parent wants, compared with actual height available
    - peer paging
        ( notebook 2015.12.14, 15
        - long press to enable paging control
            - to prevent accidental paging
          / - enabling is linked inversely to node specification
          /     ( notebook 2015.12.16
          /     - to prevent depaging of specific node
          // then must do same for descent control
                - rather: automatically despecify in both cases
                    - and respecify in back cases, provided nothing else specified meantime
            - any other press will reveal a fading cue that explains the mechanism
      */


    /** Constructs a ForestV.
      */
    public @Warning("wr co-construct") ForestV( final Wayranging wr )
    {
        super( /*context*/wr );
        pxActorMarkerWidth = Math.max( Math.round(wr.pxSP()), /*at least*/1 );

      // Climber icons.
      // - - - - - - - -
        final VectorDrawable upClimberIcon;
        final LinearLayout.LayoutParams climberLayoutParams;
        {
          // Make.
          // - - - -
            final Resources res = wr.getResources();
         // try
         // {
         //     final Class<?> c = Class.forName( "android.graphics.drawable.VectorDrawable" );
         //     final Method m = c.getMethod( "create", Resources.class, int.class );
         //     upClimberIcon = (VectorDrawable)m.invoke( null, res, R.drawable.top_android_forestv_climber );
         // }
         // catch( final Exception ex ) { throw new RuntimeException( ex ); }
         //// call to API-hidden create method, but better to inline it for production code:
            final XmlResourceParser p = res.getXml( R.drawable.top_android_forestv_climber );
            try
            {
                // Advance to first element, else VectorDrawable throws XmlPullParserException
                // "Binary XML file line #-1<vector> tag requires viewportWidth > 0".
                for( int t = p.getEventType(); t != START_TAG; t = p.next() )
                {
                    if( t == END_DOCUMENT ) throw new XmlPullParserException( "Missing start tag" );
                }

                upClimberIcon = new VectorDrawable();
                upClimberIcon.inflate( res, p, Xml.asAttributeSet(p) );
            }
            catch( final IOException|XmlPullParserException ex ) { throw new RuntimeException( ex ); }

            final WaykitUI wk = WaykitUI.i();
            final TypedValue tV = wk.typedValue();
            if( wr.getTheme().resolveAttribute( android.R.attr.colorForeground, tV, /*resolveRefs*/true ))
            {
                upClimberIcon.setTint( tV.data );
            }

          // Scale.
          // - - - -
            final float aspectRatio =
              (float)upClimberIcon.getIntrinsicWidth() / upClimberIcon.getIntrinsicHeight();
            final int pxWidth = wk.px9mmExtendedWidth( Math.round( 35/*sp*/ * wr.pxSP() ));
              // text sibling ∴ sp
            final int pxHeight = Math.round( pxWidth / aspectRatio );
         // upClimberIcon.setBounds( /*left*/0, /*top*/0, Android.right(0,pxWidth), Android.bottom(0,pxHeight) );
         /// needn't scale icon itself, just the button:
            climberLayoutParams = new LinearLayout.LayoutParams( pxWidth, pxHeight );
        }


      // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /  LAYOUT
        setOrientation( VERTICAL );
        setGravity( android.view.Gravity.BOTTOM );
        TextView textV;

      // Children at fixed indeces.
      // - - - - - - - - - - - - - -
        addView( upClimber = new Button(wr), climberLayoutParams );
        upClimber.setBackground( upClimberIcon );
        addView( textV = new TextView(wr) ); // up pager
        textV.setText( "————" );
        assert getChildCount() == C_TOP_PEER;

      // Floating children, whose indeces depend on viewer population.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        addView( downPager = new TextView(wr) );
        downPager.setText( "————" );
        ellipsis = new TextView( wr );
        ellipsis.setText( "··" );
        addView( downClimber = new Button(wr), climberLayoutParams );
        downClimber.setBackground( upClimberIcon );
        downClimber.setRotation( 180f );
        // changing floaters?  maybe change c* indexing methods


      // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
        calibrator = new ForestVCalibrator( this );

      // Initialize population of spaces pseudo-viewer.
      // - - - - - - - - - - - - - - - - - - - - - - - -
        spaceLayoutParams_sync();
        {
            final int target = spaceCountTarget( calibration() );
            if( target > 0 ) syncSpacesUp( target );
        }

      // Initialize population of node viewers.  Replace population when forester replaces node cache.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final Forester forester = wr.forester();
        forester.bell().register( new Auditor<Changed>()
        {
            { populate( forester, forester.nodeCache() ); } // populate initially
            private NodeCache nodeCache;
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

                depopulateNodeViewers();
                populate( forester, _nodeCache );
            }
        }); // no need to unregister from wr co-construct

      // Extend peers viewer when model extends.
      // - - - - - - - - - - - - - - - - - - - - -
        wr.forests().voterListingBell().register( new Auditor<Changed>()
        {
            public void hear( Changed _ding )
            {
                final CountNode candidate = candidateViewed( wr().forester() );
                final List<? extends CountNode> _peers = candidate.voters();
                if( peerCount == _peers.size() ) return;

                assert peerCount < _peers.size(); // forest.nodeCache guarantees no node is ever removed
                syncPeersViewer( candidate, _peers );
            }
        }); // no need to unregister from wr co-construct
    }



   // - V i e w ----------------------------------------------------------------------------------------


    protected @Override void onLayout( final boolean isChanged, final int left, final int top, final int right,
      final int bottom )
    {
        super.onLayout( isChanged, left, top, right, bottom );
        calibrator.activateMaybe(); /* Poll to detect whether a calibration parameter has changed.
          Simple and robust, if only as a fallback.  Execution may continue at this.reconstrain. */
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private Calibration calibration() { return calibrator.calibration; }
      // only default calibration is used, till ellipse is supported



    private final ForestVCalibrator calibrator;



    int candidateCount; // count of nodes in candidates viewer, comparable to forester.height



    private CountNode candidateViewed( final Forester forester )
    {
        // topmost node of candidates viewer, or ground if viewer is grounded
        return candidateCount > 0? getNodeChild(cTopCandidate()).node(): forester.nodeCache().ground();
    }



    private int cBottomCandidate() { return cCandidatesEndBound() - 1; }
      // child index of bottom-most view in candidates viewer, assuming candidateCount > 0



    private int cBottomPeer() { return C_TOP_PEER + peerCount - 1; }
      // child index of bottom-most view in peers viewer, assuming peerCount > 0



    private int cBottomSpace() { return cSpacesEndBound() - 1; }
      // child index of bottom-most view in spaces pseudo-viewer, assuming spaceCount > 0



    private int cCandidatesEndBound() { return cSpacesEndBound() - spaceCount; }
      // child index of first view after candidates viewer



    private int cSpacesEndBound() { return getChildCount(); }
      // child index of first view after spaces pseudo-viewer; being none, returns end bound of all children



    int cTopCandidate() { return C_TOP_PEER + peerCount + 1; }
      // child index of topmost view in candidates viewer, assuming candidateCount > 0



    static final int C_TOP_PEER = 2; // child index of topmost view in peers viewer, assuming peerCount > 0



    private void depopulateNodeViewers() // backward for faster removals
    {
      // Depopulate candidates.
      // - - - - - - - - - - - -
        if( candidateCount > 0 ) for( int c = cBottomCandidate();; --c )
        {
            removeCandidateToPool( c, getNodeChild(c) );
            if( candidateCount == 0 ) break;
        }

      // Depopulate peers, cf. syncViewersOnDeficit.
      // - - - - - - - - - - - - - - - - - - - - - - -
        if( peerCount > 0 ) for( int c = cBottomPeer();; --c )
        {
            removePeerToPool( c, getNodeChild(c) );
            if( peerCount == 0 ) break;
        }

      // Sync spaces.
      // - - - - - - -
        final int target = spaceCountTarget( calibration() );
        if( spaceCount == target ) return;

        if( spaceCount < target ) syncSpacesUp( target );
        else syncSpacesDown( target );
    }



    private void depopulateViewers() // backward for faster removals
    {
      // Depopulate spaces pseudo-viewer.
      // - - - - - - - - - - - - - - - - -
        if( spaceCount > 0 ) syncSpacesDown( 0 );

      // Depopulate other viewers.
      // - - - - - - - - - - - - - -
        depopulateNodeViewers();
    }



    final TextView downClimber;



    private final TextView downPager;



    final TextView ellipsis;



    private NodeV getNodeChild( final int c ) { return (NodeV)getChildAt( c ); }



    private Space getSpaceChild( final int c ) { return (Space)getChildAt( c ); } // hoping for Barbarella



    int nodeCountForCalibration() { return peerCount + candidateCount + spaceCount; }



    private final ArrayDeque<NodeV> nodePool = new ArrayDeque<>(); /* Pool because they're wr co-constructs.
      ∀ pooled node views, node model must be null to allow garbage collection. */

        private void enpool( final NodeV nodeV )
        {
            assert nodeV.node() == null;
            nodePool.add( nodeV );
        }


        private NodeV expoolNode() { return nodePool.remove(); }



    int peerCount; // count of nodes in peers viewer, comparable to forester.position.voters.size



    final int pxActorMarkerWidth;



    void reconstrain() // called on calibration change
    {
        spaceLayoutParams_sync();
        depopulateViewers(); // including spaces, whose dimensions may now be obsolete
        syncViewers( wr().forester() );
    }



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



    private void removeSpaceToPool( final int cSpace, final Space space )
    {
        removeViewAt( cSpace );
        --spaceCount;
        enpool( space );
    }



    private final ServerCount serverCount = new ServerCount();



    private int spaceCount; // count of nodes in spaces pseudo-viewer



    private int spaceCountTarget( final Calibration cal )
    {
        int target = cal.candidateCount - candidateCount;
        if( target < 0 ) target = 0; // likely impossible
        return target;
    }



    private LayoutParams spaceLayoutParams;


        private void spaceLayoutParams_sync()
        {
            int height = calibrator.nodeHeight;
            if( height == ForestVCalibrator.NODE_HEIGHT_DEFAULT ) height = 0; // pending calibration
            spaceLayoutParams = new LayoutParams( /*width*/0, height );
        }



    private final ArrayDeque<Space> spacePool = new ArrayDeque<>(); // pool for efficiency


        private void enpool( final Space space ) { spacePool.add( space ); }


        private Space expoolSpace() { return spacePool.remove(); }



    /** Ensures the peers viewer is populated to match the forest model.
      *
      *     @param candidate The viewed candidate.
      *     @param _peers The modeled peers, viz. candidate.voters.
      */
    private void syncPeersViewer( final CountNode candidate, final List<? extends CountNode> _peers )
    {
        final int pN = _peers.size(); // modeled
        if( pN > 0 ) // guarding against frequent, initial case of unextended voters
        {
            final Wayranging wr = wr();
            final int pStart = peerCount; // viewed
            int c = C_TOP_PEER;
            int p = pN - 1; // starting at top peer, to speed each addition
            assert _peers instanceof RandomAccess;
            for(; p >= pStart; --p, ++c ) // close gap between viewed and modeled
            {
                final CountNode peer = _peers.get( p );
                final NodeV peerV;
                if( nodePool.size() > 0 )
                {
                    peerV = expoolNode();
                    peerV.node( peer );
                }
                else peerV = new NodeV( this, peer );
                addView( peerV, c );
                ++peerCount;
            }
        }
        if( candidate.votersMaybeIncomplete() )
        {
            serverCount.enqueuePeersRequest( candidate.id(), wr().forester().forest(), /*paddedLimit*/0 );
        }
    }



    private void syncSpacesDown( final int target )
    {
        assert spaceCount > target;
        for( int c = cBottomSpace();; --c ) // backward for faster removals
        {
            removeSpaceToPool( c, getSpaceChild(c) );
            if( spaceCount == target ) break;
        }
    }



    private void syncSpacesUp( final int target )
    {
        assert spaceCount < target;
        for( int c = cSpacesEndBound();; ++c )
        {
            final Space space = spacePool.size() > 0? expoolSpace(): new Space(getContext());
            addView( space, c, spaceLayoutParams );
            ++spaceCount;
            if( spaceCount == target ) break;
        }
    }



    private void syncViewers( final Forester forester ) // ensures each populated to match forest and forester
    {
        final int _candidateCount = forester.height();
        final CountNode _candidate = forester.position();
        final int heightBalance = candidateCount - _candidateCount;

      // View may be higher than model
      // - - - - - - - - - - - - - - - -
        if( heightBalance > 0 )
        {
            if( _candidateCount == 0 ) syncViewersOnSurplus();
            else
            {
                final NodeV _candidateV = getNodeChild( cCandidatesEndBound() - _candidateCount );
                  // candidate view at model height
                if( _candidateV.node() == _candidate ) syncViewersOnSurplus();
                else syncViewersOnMismatch( _candidateCount, _candidate );
            }
            return;
        }

        final CountNode candidate = candidateViewed( forester );

      // Or lower than model
      // - - - - - - - - - - -
        if( heightBalance < 0 )
        {
            CountNode _node = _candidate;
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
    private void syncViewersOnDeficit( final int heightBalance, final CountNode _candidate )
    {
        assert heightBalance < 0;
        if( heightBalance == -1 ) // then a deficit of one candidate
        {
          // Depopulate peers viewer, cf. depopulateNodeViewers().
          // - - - - - - - - - - - - - - - - - - - - - - - - - -
            if( peerCount > 0 ) for( int c = cBottomPeer();; --c ) // backward for fast removals
            {
                final NodeV peerV = getNodeChild( c );
                if( peerV.node() == _candidate )
                {
                  // Moving new candidate to candidates viewer.
                  // - - - - - - - - - - - - - - - - - - - - - -
                    final int cDownPager = c + 1; // just below peerV
                    removeViewAt( cDownPager ); // lowering peerV in layout, a change that animates
                    addView( downPager, c ); // just above peerV, leaving peerV atop candidates viewer
                    --peerCount;
                    ++candidateCount;

                  // Removing one space from spaces viewer.
                  // - - - - - - - - - - - - - - - - - - - -
                    final int cSpace = cBottomSpace();
                    removeSpaceToPool( cSpace, getSpaceChild(cSpace) );
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
    private void syncViewersOnMismatch( final int _candidateCount, final CountNode _candidate )
    {
        // abnormal case, no need of finesse
        depopulateNodeViewers();
        syncViewersOnDeficit( -_candidateCount, _candidate );
    }



    private void syncViewersOnSurplus()
    {
        throw new UnsupportedOperationException( "Not yet coded" );
    }



    final Button upClimber;



    Wayranging wr() { return (Wayranging)getContext(); }


}
