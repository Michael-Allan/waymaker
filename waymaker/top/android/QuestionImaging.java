package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import java.io.*;
import java.net.*;
import waymaker.gen.*;

import static java.util.logging.Level.WARNING;


@ThreadRestricted("StartSync from app main") final class QuestionImaging implements Runnable
{


    @ThreadRestricted("app main") QuestionImaging( final QuestionImageSyncher syncher )
    {
        this.syncher = syncher;
        imageLoc = syncher.imageLoc;
        final QuestionImaging incomplete = syncher.incompleteImaging;
        if( incomplete != null && imageLoc != null && imageLoc.equals(incomplete.imageLoc) )
        {
            bitmapOriginal = incomplete.bitmapOriginal; // if any, avoid refetching it
              // bitmapOriginal to be readable by StartSync
        }
        widthV = syncher.wrV.getWidth();
        heightV = syncher.wrV.getHeight();
    }



   // - R u n n a b l e --------------------------------------------------------------------------------


    public void run()
    {
        if( imageLoc != null && bitmapOriginal == null ) // then fetch it
        {
            try
            {
                final URL url = new URL( imageLoc );
                {
                    final String hostName = url.getHost();
                    if( hostName != null && !hostName.endsWith("reluk.ca") ) WaykitUI.setRemotelyUsable();
                      // (a) before (b)
                }
                final HttpURLConnection con = Net.openHttpConnection( url );
                Net.connect( con );
                try( final InputStream in = new BufferedInputStream( con.getInputStream() ); )
                {
                    bitmapOriginal = BitmapFactory.decodeStream( in );
                }
                finally{ con.disconnect(); }
            }
            catch( final InterruptedIOException x )
            {
                Thread.currentThread().interrupt(); // to be sure, pass it on
                return; // interrupted, which is okay
            }
            catch( final IOException x ) { logger.log( WARNING, "Unable to display question image", x ); }
            if( WaykitUI.isRemotelyUsable() ) throw new UnsupportedOperationException( "Needs caching" );
              // (b) after (a).  Image files are likely large; should be cached before allowing remote use.
        }

        if( bitmapOriginal != null && widthV != 0 && heightV != 0 )
        {
            final int widthB; // when scaled
            final int heightB;
            {
                final float ratioV = (float)widthV / heightV; // aspect ration
                final float ratioB = (float)bitmapOriginal.getWidth() / bitmapOriginal.getHeight();
                if( ratioB > ratioV ) // then bitmap is wider, proportionally wider than view
                {
                    heightB = heightV; // match height
                    widthB = Math.round( ratioB * heightB ); // let width stick out
                    gravity = Gravity.TOP;
                }
                else // then bitmap is higher
                {
                    widthB = widthV; // match width
                    heightB = Math.round( widthB / ratioB ); // let height stick out
                    gravity = Gravity.LEFT;
                }
            }
            bitmapScaled = Bitmap.createScaledBitmap( bitmapOriginal, widthB, heightB,
              /*Paint.FILTER_BITMAP_FLAG*/true );
            bitmapOriginal = null; // signal that imaging is complete
        }
        Application.i().handler().post( new GuardedJointRunnable( /*threadToJoin*/Thread.currentThread() )
        {
            // joining back into "app main" thread
            public boolean toProceed()
            {
                if( !threadToJoin().equals( syncher.tImager )) return false; // superceded, avoid collision

                syncher.tImager = null; // release to garbage collector
                return true;
            }
            public void runAfterJoin() // reading QuestionImaging variables by TermSync
            {
                final WayrangingV wrV = syncher.wrV;
                final BitmapDrawable background;
                if( bitmapOriginal == null ) // (normal case)
                {
                    if( bitmapScaled != null )
                    {
                        background = new BitmapDrawable( wrV.wr().getResources(), bitmapScaled );
                        background.setGravity( gravity );
                    }
                    else background = null;
                    syncher.incompleteImaging = null; // if any, release to garbage collector
                }
                else // this imaging is incomplete
                {
                    background = null;
                    syncher.incompleteImaging = QuestionImaging.this;
                }
                wrV.setBackground( background );
            }
        });
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private Bitmap bitmapOriginal; // if left non-null, then imaging is incomplete pending height or width



    private Bitmap bitmapScaled; // null if imaging is incomplete



    private int gravity;



    private final int heightV; // height of wrV



    private final String imageLoc; // URL or null



    private static final java.util.logging.Logger logger = LoggerX.getLogger( QuestionImaging.class );



    private @Warning("thread restricted object, app main") final QuestionImageSyncher syncher;



    private final int widthV; // width of wrV


}
