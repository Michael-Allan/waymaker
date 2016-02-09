package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.view.View;
import waymaker.gen.*;

import static android.view.View.OnLayoutChangeListener;


  @ThreadRestricted("app main")
final class QuestionImager extends QuestionSyncher implements OnLayoutChangeListener, Refreshable
{


    @Warning("wrV.wr co-construct") QuestionImager( final WayrangingV wrV )
    {
        super( wrV.wr() ); // wr co-construct
        this.wrV = wrV;
        wrV.addOnLayoutChangeListener( this ); // no need to unregister from wr co-construct
        wrV.wr().addRefreshable( this );      // "
        sync();
    }



   // - A u d i t o r ----------------------------------------------------------------------------------


    public void hear( Changed _ding )
    {
        final String _imageLoc = leaderWaynode(wr).questionBackImageLoc();
        if( ObjectX.equals( _imageLoc, imageLoc )) return;

        imageLoc = _imageLoc;
        sync();
    }



   // - O n - L a y o u t - C h a n g e - L i s t e n e r ----------------------------------------------


    public void onLayoutChange( View _view, final int left, final int top, final int right, final int bottom,
      final int leftWas, final int topWas, final int rightWas, final int bottomWas )
    {
        if( Android.width(left,right) != Android.width(leftWas,rightWas)
         || Android.height(top,bottom) != Android.height(topWas,bottomWas) ) sync();
    }



   // - R e f r e s h a b l e --------------------------------------------------------------------------


    public void refreshFromAllSources()
    {
        if( imageLoc != null ) sync(); // force refetch of image, now cleared from HTTP cache
        // else nothing to refresh
    }



    public void refreshFromLocalWayrepo() {} // nothing to do, no direct dependence on local wayrepo



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    String imageLoc = leaderWaynode(wr).questionBackImageLoc(); // maybe null



    @ThreadSafe volatile int sImaging; /* Serial number as conflict flag for parallel worker threads.
      Write from app main, read from threads themselves.  Not using tImaging as flag, or tImaging(null)
      might overwrite and prevent join by subsequent QuestionImaging.run. */



    void sync()
    {
        ++sImaging; // flag to all prior worker threads, "you're superceded"
        if( tImaging != null && tImaging.target().isInterruptible() ) tImaging.interrupt(); /* Tap on
          shoulder, "no longer wanted".  To save resources, otherwise harmless running to completion. */
        if( imageLoc == null )
        {
            wrV.setBackground( null );
            return;
        }

        final QuestionImaging qI = new QuestionImaging( sImaging, /*tImagingPrior*/tImaging, this );
        final Thread t = tImaging = new TargetedThread<QuestionImaging>( qI,
          QuestionImaging.class.getSimpleName() + " " + sImaging );
        t.setPriority( Thread.NORM_PRIORITY ); // or to limit of group
        t.setDaemon( true );
        t.start(); // grep StartSync, continues at QuestionImaging.run
    }



    TargetedThread<QuestionImaging> tImaging; // parallel worker thread, null to garbage on termination



    final WayrangingV wrV;


}
