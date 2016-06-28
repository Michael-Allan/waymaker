package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.graphics.drawable.*;
import android.view.View;
import waymaker.gen.*;

import static waymaker.gen.Android.ALPHA_OPAQUE;
import static waymaker.gen.ActivityLifeStage.CREATED;
import static waymaker.top.android.WayscopeZoom.POLL;


/** Synchronizer of many WayrangingV zoom dependents, if not all.
  */
@ThreadRestricted("app main") final class ZoomSyncher implements Auditor<Changed>
{


    @Warning("wrV.wr co-construct") ZoomSyncher( final WayrangingV wrV )
    {
        this.wrV = wrV;
        wr = wrV.wr();
        ensureDecorColor( wrV.questionV ); // for sake of partial alpha backscreen
        wr.wayscopeZoomer().bell().register( this ); // no need to unregister from wr co-construct
        sync();
    }



   // - A u d i t o r ----------------------------------------------------------------------------------

    public void hear( Changed _ding ) { sync(); }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private int decorColor() // assuming decor view is created
    {
        final View decorV = wr.getWindow().peekDecorView();
        if( decorV == null ) // then called too soon
        {
            assert false;
            return DEFAULT_BACK_COLOR;
        }

        final Object back = decorV.getBackground();
        return back instanceof ColorDrawable? ((ColorDrawable)back).getColor(): DEFAULT_BACK_COLOR;
    }



    private static final int DEFAULT_BACK_COLOR = Android.HSVToColor( 0f, 0f, 0.5f ); // failing all else



    private void ensureDecorColor( final View view )
    {
        final ActivityLifeStage lifeStage = wr.lifeStage();
        if( view.getBackground() instanceof ColorDrawable ) return; // leave colour as is

        final int color;
        if( lifeStage == CREATED ) color = decorColor();
        else
        {
            color = DEFAULT_BACK_COLOR;
            if( lifeStage.ordinal() < CREATED.ordinal() ) wr.lifeStageBell().register( new Auditor<Changed>()
            {
                public void hear( Changed _ding )
                {
                    if( wr.lifeStage() != CREATED ) return;

                    wr.lifeStageBell().unregister( this ); // one shot
                    final Object _back = view.getBackground();
                    if( !(_back instanceof ColorDrawable) )
                    {
                        assert false;
                        return;
                    }

                    final ColorDrawable back = (ColorDrawable)_back;
                    back.setColor( decorColor() ); // now that decor view is assuredly created
                }
            }); // no need to unregister from wr co-construct
        }
        view.setBackground( new ColorDrawable( color ));
    }



    private void sync()
    {
        final WayscopeZoomer zoomer = wr.wayscopeZoomer();
        final WayscopeZoom zoom = zoomer.zoom();
        syncBackground( wrV.getBackground(), zoom );

      // Maintain visibility of components.
      // - - - - - - - - - - - - - - - - - -
        final int vis = zoom == POLL? View.INVISIBLE: View.VISIBLE;
        wrV.waypathV.setVisibility( vis );
        wrV.forestV.setVisibility( vis );
        // leaving questionV visible and conspicuous at POLL level

      // Maintain ability of zoom buttons.
      // - - - - - - - - - - - - - - - - - -
        wrV.outZoomButton.setEnabled( zoomer.outZoomEnabled() );
        wrV.inZoomButton.setEnabled( zoomer.inZoomEnabled() );
    }



    void syncBackground( final Drawable back, final WayscopeZoom zoom )
    {
        if( !(back instanceof BitmapDrawable) ) return;
          // Wait till it becomes one.  It becomes one, then stays one; no regression to handle.

        final int backAlpha; // of pollar question image in background
        final int shieldAlpha; // of backshields for foreground components
        if( zoom == POLL ) // then question is featured
        {
            backAlpha = ALPHA_OPAQUE; // leave unsubdued
            shieldAlpha = ALPHA_OPAQUE/2 + 40; // shield components from unsubdued background image
        }
        else // normal case
        {
            backAlpha = ALPHA_OPAQUE / 2;  // subdue back to stop it overwhelming unshielded foreground
              // Subdue it by transparency, effectively blending in the window background.
              // Conveniently this happens to work.  Otherwise might have tried blending in
              // decorColor by setBackgroundTintList.
            shieldAlpha = 0; // no shield
        }
        back.setAlpha( backAlpha );
        wrV.questionV.getBackground().setAlpha( shieldAlpha );
    }



    private final Wayranging wr;



    private final WayrangingV wrV;


}
