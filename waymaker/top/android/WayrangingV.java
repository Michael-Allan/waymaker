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
  *    (-)   Out-zoom button
  *    (+)   In-zoom button
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
                - out-zoom button (-) when in-zoomed beneath/above all on-path end/means links (←)
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
      */


    /** Constructs a WayrangingV.
      */
    public @Warning("wr co-construct") WayrangingV( final Wayranging wr )
    {
        super( /*context*/wr );

      // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /  LAYOUT

      // Waypath view.
      // - - - - - - - -
        final WaypathV waypathV = new WaypathV( wr ); // wr co-construct
        addView( waypathV, jigRelative().rule(ALIGN_PARENT_TOP)
          .rule(ALIGN_PARENT_LEFT).rule(ALIGN_PARENT_RIGHT).unjig() );
        waypathV.setId( WAYPATH_VID );

      // Question view.
      // - - - - - - - -
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
                    final Wayranging wr = wr();
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
        final ForestV forestV = new ForestV( wr ); // wr co-construct
        addView( forestV, jigRelative().rule(BELOW,QUESTION_VID)
          .rule(ALIGN_PARENT_LEFT).rule(ALIGN_PARENT_BOTTOM).rule(ALIGN_PARENT_RIGHT).unjig() );

      // Out-zoom button (-).
      // - - - - - - - - - - -
        final Button outZoomButton = new Button( wr );
        addView( outZoomButton, jigRelative().rule(ABOVE,IN_ZOOM_VID).rule(ALIGN_PARENT_RIGHT).unjig() );
        outZoomButton.setText( "-" );
        outZoomButton.setOnClickListener( new View.OnClickListener()
        {
            public void onClick( View _src ) { wr().wayscopeZoomer().outZoom(); }
        });

      // Menu summoner (≡).
      // - - - - - - - - - -
        {
            final Button button = new Button( wr );
            addView( button, jigRelative().rule(LEFT_OF,IN_ZOOM_VID).rule(ALIGN_PARENT_BOTTOM).unjig() );
            button.setText( "≡" );
            button.setOnClickListener( new View.OnClickListener()
            {
                public void onClick( View _src )
                {
                    new MenuDF().show( wr().getFragmentManager(), /*fragment tag*/null );
                }
            });
        }

      // In-zoom button (+).
      // - - - - - - - - - - -
      //    - main control, big
      //    - zooms into chosen wayscript child element, making it parent and revealing its own children
      //    - remembers deepest parent to enable backtrack (re-zoom) after out-zoom
      //        - else backtracking is difficult and exploring is discouraged
        final Button inZoomButton = new Button( wr );
        addView( inZoomButton, jigRelative().rule(ALIGN_PARENT_BOTTOM).rule(ALIGN_PARENT_RIGHT).unjig() );
        inZoomButton.setId( IN_ZOOM_VID );
        inZoomButton.setText( "+" );
        inZoomButton.setOnClickListener( new View.OnClickListener()
        {
            public void onClick( View _src ) { wr().wayscopeZoomer().inZoom(); }
        });


      // / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
        wr.wayscopeZoomer().bell().register( new Auditor<Changed>()
        {
            { sync(); } // init
            private void sync()
            {
                final WayscopeZoomer zoomer = wr().wayscopeZoomer();

              // Maintain ability of zoom buttons.
              // - - - - - - - - - - - - - - - - - -
                outZoomButton.setEnabled( zoomer.outZoomEnabled() );
                inZoomButton.setEnabled( zoomer.inZoomEnabled() );

              // Scope in regard to poll level.
              // - - - - - - - - - - - - - - - -
                final int vis = zoomer.zoom() == WayscopeZoom.POLL? INVISIBLE: VISIBLE;
                waypathV.setVisibility( vis );
                forestV.setVisibility( vis );
                // leaving the question visible and conspicuous at POLL level
            }
            public void hear( Changed _ding ) { sync(); }
        }); // no need to unregister from wr co-construct
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final int IN_ZOOM_VID = generateViewId();



    private static final int QUESTION_VID = generateViewId();



    private static final int WAYPATH_VID = generateViewId();



    private Wayranging wr() { return (Wayranging)getContext(); }


}
