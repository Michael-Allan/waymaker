package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.view.View;
import android.widget.*;
import waymaker.gen.ThreadRestricted;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static waymaker.gen.RelativeLayoutJig.jigRelative;


/** A wayranging view.
  */
@ThreadRestricted("app main") final class WayrangingV extends RelativeLayout
{
    /* * *
                                           WaypathV
                                            /
                               ____        /
                  end ← norm ← norm ···       *act* (=)

                  4
                 ---     Lorem ipsum et dolore
        ForestV   *      ---------------------
           \      *      Lorem ipsum dolor sit amet       ---- WayscopeV
            \     *      Consectetur adipiscing elit  ←
                 (*)     Ded do eiusmod tempor
                  *      Incididunt ut labore         ←
                 ---   ← Et dolore magna aliqua
                  *
                  *
                  *                                  (≡)
                  *                               (-)(+)

    - decorational cues (primary)
        - in overlap order from top down
            - end/means colours
                - end/act waynode in WayV
                - end/means link (←) that is on chosen waypath
                    - or all (unlooped) means links in case of leading (rightmost) edge
                      of exploratory waypath
                - out zoomer (-) when in-zoomed beneath/above all on-path end/means links (←)
                  so they have disappeared
                - forest node to choose in order to heal endward/meansward break
                  that cannot be healed at current node
            - subject colour
                ( or "vote" colour
                - forest node to choose in order to approach subject node
                - subject node
                    ( only node that retains subject colour on choosing
                - menu summoner (≡) and its vote control summoner when chosen node = voted node
                    - including negative case of no node (grounded) and no vote
                    - overloads general summoner with specific cue
                        - cue would otherwise be lost, vote control being removed from main screen
    [ WaypathV
        - narrow view of subject's way, showing only one waypath at a time
        - choice of waypath
            | explicitly by chooser
                - waypaths that may be chosen
                    ( functional categories, orthogonal to formal typology (positional|temporal)
                    | established (0..*)
                        - anchored by act
                        | voted
                            - waypaths are positionally documented in wayrepo
                            - formal type: positional
                            - action vote determines personal waypath
                                - being that waypath documented leafmost on vote path, including self
                        | stored
                            - waypath (exploratory or voted) that was replaced implicitly by action vote
                            - removed from storage either automatically when it matches a voted waypath,
                              or manually by user
                    | exploratory (0..1)
                        - incomplete, missing an act
                        - formal type: temporal
            | implicitly by link travel
                ( forest travel cannot change the waypath choice, only introduce a break
                | exploring into an established waypath
                    - bringing an exploratory waypath to match the endward (leftmost) subpath
                      of just one established waypath chooses that waypath
                | breaking out of an established waypath
                    - travel to an off-path waynode chooses an exploratory path through to that waynode
            | implicitly by action vote
                - vote (or vote shift) in action poll chooses the voted waypath
                - previously chosen waypath becomes stored, if not already stored, and user is told of this
        - waynodes
            - end [ norm* [ act ]]
                - one end, any number of norms, and one act (established way only)
            - abstract definition as series of polls made concrete (as positions) by way of (in priority order)
                | recent node of forester travel
                    - temporarily stored in order to stabilize link backtracking and re-traversal
                    - temporary, so not to confuse user by masking the following:
                | subject's vote
                | default position, which is part of the waypath definition
                    - in case of exploratory waypath, default position is last traversed during exploration
        - parallel subpaths
            ( notebook 2015.11.4-5 (Q1, A1)
            - e.g. for electoral action, diverging endward into multiple laws before converging again
        - breaks
            - caused by forest travel (node choice) that breaks end/means relation
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
            - right side is truncated to the waynode choice
                - moving leftward therefore retracts the waypath
                - except when waypath is broken
                    - then either of the two right waynodes may be chosen
                    - link traversal rightward from either replaces the rightmost waynode
    [ (=)
        - waypath chooser summoner
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
    [ (≡)
        - menu summoner
    [ (-)
        - out zoomer
        - zooms view out of parent element
        - function also accessible by pinch gesture in WayscopeV
    [ (+)
        - in zoomer
        - main control, big
        - viewer function
            | in zoomer
                - zooms view into chosen child element, so it appears as parent
        - function also accessible by pinch gesture in WayscopeV
      */


    /** Constructs a WayrangingV.
      */
    WayrangingV( final Wayranging wr )
    {
        super( /*context*/wr );

      // Forest view.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final ForestV forestV = new ForestV( wr );

      // Menu summoner.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        {
            final Button button = new Button( wr );
            addView( button, jigRelative().rule(ALIGN_PARENT_BOTTOM).rule(ALIGN_PARENT_RIGHT).unjig() );
            button.setText( "≡" );
            button.setOnClickListener( new View.OnClickListener()
            {
                public void onClick( View _src )
                {
                    new MenuDF().show( wr.getFragmentManager(), /*fragment tag*/null );
                }
            });
        }
    }


}
