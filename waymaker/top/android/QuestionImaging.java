package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import java.io.*;
import java.net.*;
import waymaker.gen.*;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.logging.Level.WARNING;


@ThreadRestricted("StartSync from app main") final class QuestionImaging implements Runnable
{


      @ThreadRestricted("app main")
    QuestionImaging( final int sImaging, final Thread tImagingPrior, final QuestionImager imager )
    {
        this.sImaging = sImaging;
        this.tImagingPrior = tImagingPrior;
        this.imager = imager;
        imageLoc = imager.imageLoc;
        widthV = imager.wrV.getWidth();
        heightV = imager.wrV.getHeight();
    }



   // - R u n n a b l e --------------------------------------------------------------------------------


    public void run()
    {
        final Thread tCurrent = Thread.currentThread();
        if( tImagingPrior != null )
        {
            try{ tImagingPrior.join(); }
              // let any prior HTTP response finish and cache before possibly re-requesting same URL
            catch( final InterruptedException x )
            {
                assert !isInterruptible;
                logger.log( WARNING, "Unexpected, untimely interrupt", x );
                tCurrent.interrupt(); // pass it on
                return;
            }
        }
        // else prior thread already terminated, nulling en passant imager.tImaging
        connection: try // fetch bitmapOriginal
        {
            final HttpURLConnection con = Net.openHttpConnection( new URL( imageLoc )); // needs INTERNET
            assert con.getUseCaches();
            Net.connect( con );
            final int statusCode = con.getResponseCode();
            if( statusCode != /*200*/HTTP_OK ) break connection;

          // Determine whether to allow interruption of fetch.
          // - - - - - - - - - - - - - - - - - - - - - - - - - -
            {
             // System.err.println( " --- headers=" + con.getHeaderFields() ); // TEST
                final String responseSource = con.getHeaderField( "X-Android-Response-Source" );
                  // SOURCE " " STATUS-CODE is the form, where SOURCE is enum name of:
                  // http://square.github.io/okhttp/1.x/okhttp/com/squareup/okhttp/ResponseSource.html
                final boolean isRecognized;
                if( responseSource == null ) isRecognized = false;
                else if( responseSource.startsWith( "CACHE " )
                      || responseSource.startsWith( "NONE " ))
                {
                    isRecognized = true;
                    isInterruptible = true; // surely not a network connection better left to finish and cache
                }
                else isRecognized = responseSource.startsWith( "NETWORK " )
                                 || responseSource.startsWith( "CONDITIONAL_CACHE " );
                assert isRecognized: "HTTP response source '" + responseSource + "' is recognized";
            }

          // Fetch image.
          // - - - - - - -
            try( final InputStream in = new BufferedInputStream( con.getInputStream() ); )
            {
                bitmapOriginal = BitmapFactory.decodeStream( in );
                  // decodeStream swallows interrupts (Android 23).  Throws InterruptedIOException with
                  // interrupt status clear, then catches it internally and merely logs it before
                  // aborting and returning to here, leaving interrupt status unknown at this point.
                if( tCurrent.isInterrupted() ) return; // imaging no longer wanted
                  // unlikely to detect, as decodeStream above 'swallows' it
            }
            finally{ con.disconnect(); }
        }
        catch( final InterruptedIOException x ) // unlikely to catch, as decodeStream above 'swallows' it
        { // unlike ClosedByInterruptException, leaves tCurrent status in doubt, so:
            tCurrent.interrupt(); // to be sure, pass it on
            return; // interrupted, which is okay
        }
        catch( final IOException x ) { logger.log( WARNING, "Unable to fetch question back image", x ); }
        if( imager.sImaging != sImaging ) return; /* Superceded, imaging no longer wanted.
          QuestionImager.sImaging was exposed volatile for just this purpose, to enable deferred
          detection of being unwanted in lieu of receiving an interrupt, while !isInterruptible. */

      // Scale fetched image.
      // - - - - - - - - - - -
        isInterruptible = true; // now that HTTP response is finished and cached
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
            if( tCurrent.isInterrupted() ) return; // imaging no longer wanted

            bitmapOriginal = null; // signal that imaging is complete
        }

      // Set scaled image as background.
      // - - - - - - - - - - - - - - - - -
        ApplicationX.i().handler().post( new GuardedJointRunnable( /*threadToJoin*/tCurrent )
        {
            // joining back into "app main" thread
            public boolean toProceed()
            {
                if( imager.sImaging != sImaging ) return false; // superceded, abort to avoid collision

                assert threadToJoin().equals( imager.tImaging );
                imager.tImaging = null; // release to garbage collector
                return true;
            }
            public void runAfterJoin() // reading QuestionImaging.this variables by TermSync
            {
                final WayrangingV wrV = imager.wrV;
                final BitmapDrawable background;
                if( bitmapScaled != null )
                {
                    background = new BitmapDrawable( wrV.wr().getResources(), bitmapScaled );
                    background.setGravity( gravity );
                }
                else background = null;
                wrV.setBackground( background );
            }
        });
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private Bitmap bitmapOriginal;



    private Bitmap bitmapScaled;



    private int gravity;



    private final int heightV; // height of wrV



    private final String imageLoc; // URL or null



    private @Warning("thread restricted object, app main") final QuestionImager imager;



    @ThreadSafe boolean isInterruptible() { return isInterruptible; } // once true it never changes


        private volatile boolean isInterruptible;



    private static final java.util.logging.Logger logger = LoggerX.getLogger( QuestionImaging.class );



    private final int sImaging; // serial



    private final Thread tImagingPrior;



    private final int widthV; // width of wrV


}
