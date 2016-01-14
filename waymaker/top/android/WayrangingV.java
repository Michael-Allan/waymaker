package waymaker.top.android; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.view.View;
import android.widget.*;
import waymaker.gen.*;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static waymaker.gen.RelativeLayoutJig.jigRelative;


/** <p>A wayranging view.  Its greater components are subviews to show the waypath, pollar question,
  * forest, and wayscope:</p>
  * <pre>
  *                                      Waypath
  *                                       /
  *                                      /
  *             end ← norm ← norm ···        *act* (=)
  *                          ————
  *                   Lorem ipsum et dolore?            --- Question
  *
  *             ∧           Type  Waynode summary
  *            ---        Type  Next outer summary
  *   Forest    *       Type  Next outer summary          --- Wayscope
  *      \      *     Type  Parent summary
  *       \     *       Type  First child summary     ←
  *            (*)      Type  Second child summary
  *             *       Type  Third child summary     ←
  *            ---      Type  Fourth child summary
  *             *     ← Type  Fifth child summary
  *             *
  *             *                                   (-)
  *             ∨                                (≡)(+)
  * </pre>
  * <p>Its lesser components are controls:</p>
  * <pre>
  *    (=)   Waypath chooser summoner
  *     ←    End/means link
  *    (-)   Out zoomer for wayscope
  *    (+)   In zoomer
  *    (≡)   Menu summoner
  * </pre>
  */
public @ThreadRestricted("app main") final class WayrangingV extends RelativeLayout
{
    /* * *
    - decorational cues (primary)
        - in overlap order from top down
            - end/means colours
                - end/act waynode in WayV
                - end/means link (←) that is on chosen waypath
                    - or all (unlooped) means links in case of leading (rightmost) edge of nascent waypath
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
        - basis of waypath view
            - models
                - waypath
                    - types of origin
                        | temporal
                            - originating as nascent waypath
                        | positional
                            - originating in position document
                    - identification
                        ( as per 2015.12.12
                        - using only string form, "name"
                - Wayranging.pollNamer
                - Wayranging.waypathNamer
            - controls
                - waypath chooser, summoner (=)
        - choice of waypath
            | explicitly by chooser
                - waypaths that may be chosen
                    ( functional categories, orthogonal to original typology (positional|temporal)
                    | nascent (0..1)
                        - incomplete, missing act
                        - type of origin: temporal
                    | adopted (0..*)
                        - determined by action vote
                            - leafmost positional waypath on action vote path, including self
                        - type of origin: positional
                    | stowed (0..*)
                        - waypath (nascent or adopted) that was replaced implicitly by action vote
                        - removed from storage either automatically when it matches an adopted waypath,
                          or manually by user
            | implicitly by link travel
                ( forest travel cannot change the waypath choice, only introduce a break
                | exploring into an established waypath
                    - bringing a nascent waypath to match the endward (leftmost) subpath
                      of just one established waypath chooses that waypath
                | breaking out of an established waypath
                    - travel to an off-path waynode chooses a nascent path through to that waynode
            | implicitly by action vote
                - vote (or vote shift) in action poll chooses the adopted waypath
                - previously chosen waypath becomes stowed, if not already stowed, and user is told of this
        - waynodes
            - end [ norm* [ act ]]
                - one end, any number of norms, and one act (established way only)
            - abstract definition as series of polls made concrete (as positions) by way of (in priority order)
                | recent node of forester travel
                    - temporarily stored in order to stabilize link backtracking and re-traversal
                    - temporary, so not to confuse user by masking the following:
                | subject's vote
                | default position, which is part of the waypath definition
                    - in case of nascent waypath, default position is last traversed during exploration
        - parallel subpaths
            ( notebook 2015.11.4-5 (Q1, A1)
            - e.g. for electoral action, diverging endward into multiple laws before converging again
        - breaks
            - caused by forest travel (node choice) that breaks end/means relation
            | in established path
                - between any two waynodes
            | in nascent path
                - at most one intermediate break between the two rightmost waynodes:

                        end ← norm ← norm ··· norm ···     *act*
                                           /
                                          /
                            intermediate break
                               /
                              /
                        end ··· norm ···                    *act*

                - final break (always present) to waynode placeholder *act*
                    - so here is default nascent waypath for virgin user

                        end ···                             *act*

        - truncation of nascent waypath
            ( leaving aside placeholder *act*, which is not a proper waynode
            - right side is truncated to the waynode choice
                - moving leftward therefore retracts the waypath
                - except when waypath is broken
                    - then either of the two right waynodes may be chosen
                    - link traversal rightward from either replaces the rightmost waynode
    [ (=)
        - waypath chooser summoner
        - chooser is controller of Wayranging.waypathNamer
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
        - wayscope out zoomer
        - zooms view out of parent element
        - function also accessible by pinch gesture in WayscopeV
    [ (+)
        - wayscope in zoomer
        - main control, big
        - viewer function
            | in zoomer
                - zooms view into chosen child element, so it appears as parent
        - function also accessible by pinch gesture in WayscopeV
      */


    /** Constructs a WayrangingV.
      */
    public @Warning("wr co-construct") WayrangingV( final Wayranging wr )
    {
        super( /*context*/wr );

      // Question.
      // - - - - - -
        {
            final TextView textV = new TextView( wr );
            addView( textV,
              jigRelative().rule(ALIGN_PARENT_LEFT).rule(ALIGN_PARENT_TOP).rule(ALIGN_PARENT_RIGHT).unjig() );
            textV.setId( QUESTION_VID );
            final class TextSetter implements Auditor<Changed>
            {
                public void hear( Changed _ding ) { sync(); }
                private void sync()
                {
                    textV.setText( wr.forests().getOrMakeForest(wr.pollNamer().get())
                      .nodeCache().leader().waynode().question() );
                }
            };
            final TextSetter setter = new TextSetter();
            setter.sync();
            wr.pollNamer().bell().register( setter ); // no need to unregister from wr co-construct
            wr.forests().nodeCacheBell().register( setter ); // "
        }

      // Forest view.
      // - - - - - - -
        addView( new ForestV(wr)/*wr co-construct*/, jigRelative().rule(ALIGN_PARENT_LEFT)
          .rule(BELOW,QUESTION_VID).rule(ALIGN_PARENT_BOTTOM).rule(ALIGN_PARENT_RIGHT).unjig() );

      // Menu summoner.
      // - - - - - - - -
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



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final int QUESTION_VID = generateViewId();


}
