package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.view.View;
import android.widget.*;
import waymaker.gen.*;


/** A wayranging view.
  */
@ThreadRestricted("app main") final class WayrangingV extends LinearLayout
{
    /* * *
                                              WaypathV
                                               /
                               ____           /
                  end ← norm ← norm ···              *act*

        ForestV       * * * *
           \
            \      - *      Lorem ipsum et dolore
                  |- *      ---------------------
                  |- *      Lorem ipsum dolor sit amet
                  |-(*)     Consectetur adipiscing elit  ←
                  |- *      Ded do eiusmod tempor
                  |         Incididunt ut labore         ←
                  *       ← Et dolore magna aliqua
                  *
                  *                                    (m)
                  *                       /         (-)(+)
                                         /                 \
                                     WayscopeV              \
                                                       .cornerButtons

    - decorational cues (primary)
        - in overlap order from top down
            - end/means colours
                - end/act waynode in WayV
                - end/means link (←) that is on selected waypath
                    - or all (unlooped) means links in case of leading (rightmost) edge
                      of exploratory waypath
                - out zoomer (-) when in-zoomed beneath/above all on-path end/means links (←)
                  so they have disappeared
                - forest node to select in order to heal endward/meansward break
                  that cannot be healed at current node
            - vote colour
                - forest node to select in order to approach subject node
    [ WaypathV
        - narrow view of subject's way, showing only one waypath at a time
        - selection of waypath
            | explicitly by selector
                ( selector and pop button not shown
                - waypaths that may be selected
                    | established (0..*)
                        - anchored by act
                        | voted
                            - waypaths are positionally documented in wayrepo
                            - action vote determines personal waypath
                                - being that waypath documented leafmost on vote path, including self
                        | mnemonic
                            - memorization of waypath (exploratory or voted) that was replaced
                              implicitly by action vote
                            - forgotten either automatically when it matches a voted waypath,
                              or manually by user
                    | exploratory (0..1)
                        - incomplete, missing an act
            | implicitly by link travel
                ( forest travel cannot change the waypath selection, only introduce a break
                | exploring into an established waypath
                    - bringing an exploratory waypath to match the endward (leftmost) subpath
                      of just one established waypath selects that waypath
                | breaking out of an established waypath
                    - travel to an off-path waynode selects an exploratory path through to that waynode
            | implicitly by action vote
                - vote (or vote shift) in action poll selects the voted waypath
                - previously selected waypath is saved (if not already saved) as a mnemonic waypath
                    - user being told of this
        - waynodes
            - end [ norm* [ act ]]
                - one end, any number of norms, and one act (established way only)
            - abstract definition as series of polls made concrete (as positions)
              by way of (in priority order)
                | selected node in ForestV
                | subject's vote
                | default position, which is part of the waypath definition
                    - to stabilize link backtracking and re-traversal
                    - in case of exploratory or mnemonic waypath,
                      default position is last traversed during exploration
        - parallel subpaths
            ( notebook 2015.11.4-5 (Q1, A1)
            - e.g. for electoral action, diverging endward into multiple laws before converging again
        - breaks
            - caused by forest travel (node selection) that breaks end/means relation
            | in established path
                - between any two waynodes
            | in exploratory path
                - at most one intermediate break between the two rightmost waynodes:

                        end ← norm ← norm ··· norm ···     *act*
                                           /
                                          /
                            intermediate break
                               /
                              /
                        end ··· norm ···                    *act*

                - final break (always present) to waynode placeholder *act*
                    - so here is default exploratory waypath for virgin user

                        end ···                             *act*

        - truncation of exploratory waypath
            ( leaving aside placeholder *act*, which is not a proper waynode
            - right side is truncated to the waynode selection
                - moving leftward therefore retracts the waypath
                - except when waypath is broken
                    - then either of the two right waynodes may be selected
                    - link traversal rightward from either replaces the rightmost waynode
    [ WayscopeV
        - way element graph
            - elements
                - with graphic indication (not shown) of element type
                [ parent
                [ children
            [ end link ←
                ( left ←
                - pans view to next poll endward in waypath
            [ means link ←
                ( right ←
                - pans view to next poll meansward in waypath
        [ cornerButtons
            - placed bottom-right where disruption to view is minimized
            [ (m) menu popper
            [ (-) out zoomer
                - zooms view out of parent element
                - function also accessible by pinch gesture in WayscopeV
            [ (+) in zoomer (big)
                - viewer function
                    | in zoomer
                        - zooms view into selected child element, so it appears as parent
                - function also accessible by pinch gesture in WayscopeV
      */


    /** Constructs a WayrangingV.
      */
    WayrangingV( final Wayranging wr )
    {
        super( /*context*/wr );
        setOrientation( VERTICAL );

      // Wayrepo preview controller.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        addView( new WayrepoPreviewController( wr ));
            /* * *
            - to be deployed via menu popper (m)
                - via Settings item
                    - via standard Preference UI
                        < http://developer.android.com/guide/topics/ui/settings.html
                        - as custom dialogue
                            < http://developer.android.com/guide/topics/ui/settings.html#Custom
              */
        {
            final LinearLayout x = new LinearLayout( wr );
            addView( x );
            {
              // Logging test button, to log test messages at all standard logging levels.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( wr );
                x.addView( button );
                button.setText( "Test logging" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v ) { LoggerX.test( logger ); }
                });
            }
            {
              // Generic test button.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( wr );
                x.addView( button );
                button.setText( "Extend roots" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v )
                    {
                        final String pollName = "end";
                        final Forest forest = wr.forests().get( pollName );
                        new ServerCount(pollName).enqueuePeersRequest( null/*ground*/, forest,
                          /*paddedLimit*/0 );
                    }
                });
            }
        }
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final java.util.logging.Logger logger = LoggerX.getLogger( WayrangingV.class );


}
