package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.view.View;
import waymaker.gen.*;

import static android.view.View.GONE;
import static android.view.View.MeasureSpec;
import static waymaker.top.android.ForestV.C_TOP_PEER;


@ThreadRestricted("app main") final class ForestVCalibrator implements Runnable
{


    ForestVCalibrator( final ForestV _forestV )
    {
        forestV = _forestV;

      // Initialize ellipsis height by measuring height wanted.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final int msConstraint = MeasureSpec.makeMeasureSpec( /*size*/0, MeasureSpec.UNSPECIFIED );
        forestV.ellipsis.measure( msConstraint, msConstraint );
        ellipsisHeight = forestV.ellipsis.getMeasuredHeight();

      // - - -
        calibrate();
    }



   // - R u n n a b l e --------------------------------------------------------------------------------


    public void run()
    {
        final boolean toReconstrain = calibrate();
        System.err.println( " --- to reconstrain forest view: " + toReconstrain ); // TEST
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private void activate()
    {
        if( isActivated ) return;

        isActivated = true;
        Application.i().handler().post( this ); // run in later dispatch loop
    }



    void activateMaybe() // activates if a calibration parameter has changed
    {
        int height;
        boolean toActivate = false; // till proven otherwise

      // Forest.
      // - - - - -
        assert forestV.getVisibility() != GONE; // else should skip forestV on this pass
        height = forestV.getHeight();
        if( forestHeight != height )
        {
            forestHeight = height;
            toActivate = true;
        }

      // Node.
      // - - - -
        node:
        {
            final int cTopmostNode; /* The topmost node is least likely to be squeezed if the layout
              happens to be overconstrained, and therefore most likely to be accurate.  (Android will
              squeeze a view's size rather than clip its drawing.) */
            if( forestV.peerCount > 0 ) cTopmostNode = C_TOP_PEER;
            else if( forestV.candidateCount > 0 ) cTopmostNode = forestV.cTopCandidate();
            else break node; // none in layout

            final View topmostNodeV = forestV.getChildAt( cTopmostNode );
            height = topmostNodeV.getHeight();
            if( nodeHeight == height ) break node;

            if( height == 0 ) break node; // avoid obviously wrong, probably catastrophic value

            final int cN = forestV.getChildCount();
            for( int c = cTopmostNode;; ) // find follower and ensure topmostNodeV is unsqueezed in layout
            {
                ++c;
                if( c == cN ) throw new IllegalStateException( "Node without follower in layout" );

                final View follower = forestV.getChildAt( c );
                if( follower.getHeight() == 0 ) break node;
                  // follower squeezed to nothing, therefore topmostNodeV might also be squeezed

                break; // apparently unsqueezed, thus expect height to be accurate
            }
            nodeHeight = height;
            toActivate = true;
        }

      // Ellipsis.
      // - - - - - -
        if( forestV.ellipsis.getParent() != null )
        {
            height = forestV.ellipsis.getHeight();
            if( ellipsisHeight != height )
            {
                ellipsisHeight = height;
                toActivate = true;
            }
        }

      // - - -
        if( toActivate ) activate();
    }



    private boolean calibrate()
    {
        isActivated = false; // allow a subsequent activation
        if( forestV.downClimber.getHeight() < forestV.upClimber.getHeight() ) // then layout is overconstrained
        {
            throw new IllegalStateException( "Unsupported case" ); // TEST, deferred
        }

        boolean isWorkingCalibrationChanged = false; // till proven otherwise
        final View ellipsis = forestV.ellipsis;
        int spareHeight = forestV.getChildAt(0).getTop();
        if( ellipsis.getParent() == null ) // then layout excludes ellipsis
        {
            int nodeCount = forestV.nodeCount();
            final Calibration _calibration = makeCalibration( /*actual*/nodeCount, spareHeight );
            if( !_calibration.equals( calibration/*maybe null*/ ))
            {
                calibration = _calibration;
                isWorkingCalibrationChanged = true;
            }
            spareHeight -= ellipsisHeight;
            if( spareHeight < 0 )
            {
                final int toFitted = MathX08.floorDiv( spareHeight, nodeHeight ); // node count adjustment
                nodeCount += toFitted; // a subtraction
                spareHeight -= toFitted * nodeHeight; // an addition
                calibrationEllipsed = makeCalibration( /*fitted*/nodeCount );
            }
            else calibrationEllipsed = makeCalibration( /*actual*/nodeCount, spareHeight );
        }
        else // layout includes ellipsis
        {
            final int nodeCount = forestV.nodeCount();
            final Calibration _calibrationEllipsed = makeCalibration( /*actual*/nodeCount, spareHeight );
            if( !_calibrationEllipsed.equals( calibrationEllipsed/*maybe null*/ ))
            {
                calibrationEllipsed = _calibrationEllipsed;
                isWorkingCalibrationChanged = true;
            }
            spareHeight += ellipsisHeight;
            calibration = makeCalibration( /*actual*/nodeCount, spareHeight );
        }

System.err.println( " ---         calibration=" + calibration ); // TEST
System.err.println( " --- calibrationEllipsed=" + calibrationEllipsed ); // TEST
        return isWorkingCalibrationChanged;
    }



    Calibration calibration; // for use *without* candidate ellipsis



    Calibration calibrationEllipsed; // for use *with* candidate ellipsis



    private static final int CANDIDATE_LEND_THRESHOLD = 3; /* While a nascent calibration would give
      more than this number of nodes to the candidates viewer, it can instead lend nodes to the peers
      viewer, resulting therefore in an imbalanced calibration. */



    private int ellipsisHeight; // init in constructor



    private int forestHeight; // init to zero, most constrained case



    private final ForestV forestV;



    private boolean isActivated;



    private Calibration makeCalibration( final int nodeCountFitted )
    {
        return new Calibration( Math.max( NODE_COUNT_MIN, nodeCountFitted ));
    }



    private Calibration makeCalibration( final int nodeCountActual, final int spareHeight )
    {
        assert spareHeight >= 0; // else this calculation may fail
        return makeCalibration( nodeCountActual + spareHeight/nodeHeight );
          // calculating maximum node count by filling spare height without overflowing it
    }



    private final int NODE_COUNT_MIN = 4; /* minimum to alot two candidates, one on each side of
      ellipsis, without which ForestV layout might be complicated */



    private int nodeHeight = Integer.MAX_VALUE << 3; // init to (likely) most constrained case



    private static final int PEER_BORROW_THRESHOLD = 6; /* While a nascent calibration would give fewer
      than this number of nodes to the peers viewer, it will instead try to borrow additional nodes from
      the candidates viewer.  Actual borrowing results therefore in an imbalanced calibration, and will
      occur only if PEER_BORROW_THRESHOLD exceeds CANDIDATE_LEND_THRESHOLD by two or more. */



   // ==================================================================================================


    @SuppressWarnings("overrides") final class Calibration // overrides equals, but not hashCode
    {

        private Calibration( final int nodeCountMaxAllowed )
        {
            nodeCount = nodeCountMaxAllowed;
            int candidateCount = nodeCount / 2;
            int peerCount = nodeCount - candidateCount;
            {
                final int peerDeficit = PEER_BORROW_THRESHOLD - peerCount;
                if( peerDeficit > 0 ) // then try to borrow nodes from candidates view
                {
                    final int candidateSurplus = candidateCount - CANDIDATE_LEND_THRESHOLD;
                    if( candidateSurplus > 0 )
                    {
                        final int loan = Math.min( peerDeficit, candidateSurplus );
                        candidateCount -= loan;
                        peerCount += loan;
                    }
                }
            }
            this.candidateCount = candidateCount;
            this.peerCount = peerCount;
        }


        final int candidateCount; // max allowed


        private final int nodeCount; // max allowed


        final int peerCount; // max allowed


       // - O b j e c t --------------------------------------------------------------------------------

        public @Override boolean equals( final Object o )
        {
            if( !(o instanceof Calibration) /*or if null*/ ) return false;

            final Calibration oC = (Calibration)o;
            return oC.nodeCount == nodeCount;
        }


        public @Override String toString() { return peerCount + "/" + candidateCount; }

    }


}
