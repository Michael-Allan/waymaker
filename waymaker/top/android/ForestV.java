package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.widget.*;
import waymaker.gen.*;

import static android.view.View.INVISIBLE;


/** <p>A forest view oriented by the {@linkplain Wayranging#forester() forester}.  Its main components
  * are {@linkplain Node node} views (lettered).  These it divides vertically between a peers viewer for
  * showing the forester’s {@linkplain Forester#moveToPeer(Node) lateral mobility}, and a candidates
  * viewer for showing its {@linkplain Forester#descendToCandidate() downward mobility}.</p>

  * <pre>
  *                     ◢◣    --- Up climber
  *                    ————   --- Up pager for peers
  *                     ti
  *                     sh
  *        Peers ---    rg
  *                     qf ◄  --- Choice indicator
  *                     pe
  *                    ————   --- Down pager for peers
  *                     ca
  *   Candidates ---    db
  *                     ··    --- Ellipsis for omitted candidates
  *                     fd
  *                     ◥◤    --- Down climber
  * </pre>
  * <p>Any {@linkplain Forester#ascendToVoter(Node) upward mobility} it indicates by enabling the up
  * climber (top).  The peers viewer alone is paged; the candidates viewer is ellipsed.</p>
  */
public @ThreadRestricted("app main") final class ForestV extends LinearLayout
{


    /** Constructs a ForestV.
      */
    public ForestV( final Wayranging wr )
    {
        super( /*context*/wr );
        setOrientation( VERTICAL );
        setGravity( android.view.Gravity.BOTTOM );
        final Forester forester = wr.forester();
        groundWidth = forester.nodeCache().ground().voters().size();
        forester.bell().register( new Auditor<Changed>() // TEST
        {
            private NodeCache cache = forester.nodeCache();
            public void hear( Changed _ding )
            {
                final NodeCache _cache = wr().forester().nodeCache();
                if( _cache == cache ) return;

                cache = _cache;
                groundWidth = cache.ground().voters().size();
                System.err.println( " --- forest cache has changed" );
                showRoots();
            }
        });
        wr.forests().voterListingBell().register( new Auditor<Changed>() // TEST
        {
            public void hear( Changed _ding )
            {
                final int _groundWidth = wr().forester().nodeCache().ground().voters().size();
                if( _groundWidth == groundWidth ) return;

                groundWidth = _groundWidth;
                System.err.println( " --- root list has grown" );
                showRoots();
            }
        });
        showRoots(); // TEST
        final TextView upClimber = new TextView( wr );
        addView( upClimber );
        upClimber.setText( "◢◣" );
        final TextView upPager = new TextView( wr );
        addView( upPager );
        upPager.setText( "————" );
        final NodeV placeholderNodeV = new NodeV( wr );
        addView( placeholderNodeV );
        placeholderNodeV.setText( "ww" ); // best guess at widest possible text
        placeholderNodeV.setVisibility( INVISIBLE );
        final TextView downPager = new TextView( wr );
        addView( downPager );
        downPager.setText( "————" );
        final TextView ellipsis = new TextView( wr );
        addView( ellipsis );
        ellipsis.setText( "∼" );
        final TextView downClimber = new TextView( wr );
        addView( downClimber );
        downClimber.setText( "◥◤" );
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private int groundWidth; // TEST



    private final @Warning("init call") void showRoots() // TEST
    {
        final java.util.List<? extends Node> roots = wr().forester().nodeCache().ground().voters();
        System.err.println( " --- root list size=" + roots.size() );
        for( final Node root: roots ) System.err.println( "     root=" + root );
    }



    private Wayranging wr() { return (Wayranging)getContext(); }


}
