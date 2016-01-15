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
  *    (-)   Out-zoomer for wayscope
  *    (+)   In-zoomer
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
                - out-zoomer (-) when in-zoomed beneath/above all on-path end/means links (←)
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
    [ (=)
        - waypath chooser summoner
        - chooser itself is controller of Wayranging.waypathNamer
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
        - wayscope out-zoomer
        - zooms view out of parent element
        - function also accessible by pinch gesture in WayscopeV
    [ (+)
        - wayscope in-zoomer
        - main control, big
        - viewer function
            | in-zoomer
                - zooms view into chosen child element, so it appears as parent
        - function also accessible by pinch gesture in WayscopeV
      */


    /** Constructs a WayrangingV.
      */
    public @Warning("wr co-construct") WayrangingV( final Wayranging wr )
    {
        super( /*context*/wr );

      // Waypath view.
      // - - - - - - - -
        {
            final WaypathV view = new WaypathV( wr ); // wr co-construct
            addView( view, jigRelative().rule(ALIGN_PARENT_TOP)
              .rule(ALIGN_PARENT_LEFT).rule(ALIGN_PARENT_RIGHT).unjig() );
            view.setId( WAYPATH_VID );
        }

      // Question.
      // - - - - - -
        {
            final TextView view = new TextView( wr );
            addView( view, jigRelative().rule(BELOW,WAYPATH_VID)
              .rule(ALIGN_PARENT_LEFT).rule(ALIGN_PARENT_RIGHT).unjig() );
            view.setId( QUESTION_VID );
            final class TextSetter implements Auditor<Changed>
            {
                public void hear( Changed _ding ) { sync(); }
                private void sync()
                {
                    view.setText( wr.forests().getOrMakeForest(wr.pollNamer().get())
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
        addView( new ForestV(wr)/*wr co-construct*/, jigRelative().rule(BELOW,QUESTION_VID)
          .rule(ALIGN_PARENT_LEFT).rule(ALIGN_PARENT_BOTTOM).rule(ALIGN_PARENT_RIGHT).unjig() );

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


    private static final int WAYPATH_VID = generateViewId();


}
