package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.view.View;
import waymaker.gen.*;


  @ThreadRestricted("app main")
final class QuestionImageSyncher extends QuestionSyncher implements View.OnLayoutChangeListener
{


    @Warning("wrV.wr co-construct") QuestionImageSyncher( final WayrangingV wrV )
    {
        super( wrV.wr() ); // wr co-construct
        this.wrV = wrV;
        wrV.addOnLayoutChangeListener( this ); // no need to unregister from wr co-construct
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



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    String imageLoc = leaderWaynode(wr).questionBackImageLoc(); // maybe null



    QuestionImaging incompleteImaging; /* Fetched but unscaled because wrV dimesions unknown.  Will be
      reused by next sync, then nulled when complete.  Might instead defer imaging entirely till
      dimesions are known, but that would needlessly postpone the fetch. */



    void sync()
    {
        if( imageLoc == null )
        {
            tImager( null );
            wrV.setBackground( null );
            return;
        }

        final Thread t = new Thread( new QuestionImaging(this), "question imager" );
        tImager( t );
        t.setPriority( Thread.NORM_PRIORITY ); // or to limit of group
        t.setDaemon( true );
        t.start(); // grep StartSync, continues at QuestionImaging.run
    }



    Thread tImager; /* Reference of current "question imager" thread.  Use as conflict guard and
      interrupt handle.  Interrupt to synchronize (TermSync) and to conserve resources.  Otherwise,
      running to completion is harmless.  Null on thread termination to enable garbage collection. */



    private void tImager( final Thread _tImager )
    {
        final Thread tWas = tImager;
        tImager = _tImager; // raise conflict signal to any prior tImager, "you're superceded"
        if( tWas != null ) tWas.interrupt(); // tap on shoulder, "no longer wanted"
    }



    final WayrangingV wrV;


}
