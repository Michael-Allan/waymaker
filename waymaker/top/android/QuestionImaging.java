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


    @ThreadRestricted("app main") QuestionImaging( final QuestionImager imager )
    {
        this.imager = imager;
        imageLoc = imager.imageLoc;
        final QuestionImaging incomplete = imager.incompleteImaging;
        if( incomplete != null && imageLoc != null && imageLoc.equals(incomplete.imageLoc) )
        {
            bitmapOriginal = incomplete.bitmapOriginal; // if any, avoid refetching it
              // bitmapOriginal to be readable by StartSync
        }
        widthV = imager.wrV.getWidth();
        heightV = imager.wrV.getHeight();
    }



   // - R u n n a b l e --------------------------------------------------------------------------------


    public void run()
    {
        final Thread thread = Thread.currentThread();
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
                      // decodeStream swallows interrupts (Android 23).  Throws InterruptedIOException
                      // with interrupt status clear, then catches it internally and merely logs it,
                      // leaving interrupt status unknown at this point.
                    if( thread.isInterrupted() ) return; // imaging no longer wanted
                      // unlikely to detect, as decodeStream above 'swallows' it
                }
                finally{ con.disconnect(); }
            }
            catch( final InterruptedIOException x ) // unlikely to catch, as decodeStream above 'swallows' it
            { // unlike ClosedByInterruptException, leaves thread status in doubt, so:
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
            if( thread.isInterrupted() ) return; // imaging no longer wanted

            bitmapOriginal = null; // signal that imaging is complete
        }
        Application.i().handler().post( new GuardedJointRunnable( /*threadToJoin*/Thread.currentThread() )
        {
            // joining back into "app main" thread
            public boolean toProceed()
            {
                if( !threadToJoin().equals( imager.tImager )) return false; // superceded, avoid collision

                imager.tImager = null; // release to garbage collector
                return true;
            }
            public void runAfterJoin() // reading QuestionImaging variables by TermSync
            {
                final WayrangingV wrV = imager.wrV;
                final BitmapDrawable background;
                if( bitmapOriginal == null ) // (normal case)
                {
                    if( bitmapScaled != null )
                    {
                        background = new BitmapDrawable( wrV.wr().getResources(), bitmapScaled );
                        background.setGravity( gravity );
                    }
                    else background = null;
                    imager.incompleteImaging = null; // if any, release to garbage collector
                }
                else // this imaging is incomplete
                {
                    background = null;
                    imager.incompleteImaging = QuestionImaging.this;
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



    private @Warning("thread restricted object, app main") final QuestionImager imager;



    private static final java.util.logging.Logger logger = LoggerX.getLogger( QuestionImaging.class );



    private final int widthV; // width of wrV


}
