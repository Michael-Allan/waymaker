package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.*;
import android.os.*;
import java.util.concurrent.atomic.AtomicReference;


/** An Android application with member extensions.
  */
public @ThreadSafe class ApplicationX extends Application
{


    /** Constructs the {@linkplain #i() single instance} of ApplicationX.  This constructor is called by
      * the Android runtime during initialization of the actual application as commanded in the manifest
      * file <code>AndroidManifest.xml</code>.
      *
      *     @throws IllegalStateException if an instance was already constructed.
      */
    public @Warning("non-API") ApplicationX()
    {
        mainLooper = Looper.getMainLooper();
        handler = new Handler( mainLooper );
    }



    static { System.setProperty( "waymaker.g.LoggerX.classPrefix", "wm" ); }
      // early init: before LoggerX loads, rename the class-based loggers from "PACKAGE.CLASS", which
      // Android logcat tags simply as "CLASS", to "wmCLASS" instead



   // ` c r e a t i o n ````````````````````````````````````````````````````````````````````````````````


    public @Override @Warning("non-API") void onCreate()
    {
        super.onCreate(); // obeying API
        if( !isMainThread() ) throw new IllegalStateException(); // at least for visibility of this.preferences

        preferences = getSharedPreferences( /*name*/"app", /*mode, typical*/MODE_PRIVATE );
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        pxDP = metrics.density;
        final float px_per_mm = metrics.xdpi/*px/inch*/ / (General.CM_PER_INCH * 10)/*mm/inch*/;
        px7mmX = (int)Math.ceil( 7/*mm*/ * px_per_mm );
        px9mmX = (int)Math.ceil( 9/*mm*/ * px_per_mm );
    }



   // --------------------------------------------------------------------------------------------------


    /** A handler for communicating with the application’s main thread.
      *
      *     @see <a href='http://developer.android.com/reference/android/app/Activity.html#runOnUiThread(java.lang.Runnable)'
      *       target='_top'>runOnUiThread</a>
      *     @see <a href='http://developer.android.com/reference/android/os/AsyncTask.html'
      *       target='_top'>AsyncTask</a>
      */
    public final Handler handler() { return handler; }


        private final Handler handler;



    /** The single instance of ApplicationX as created by the Android runtime, or null if there is none.
      */
    public static ApplicationX i() { return instanceA.get(); }


        private static final AtomicReference<ApplicationX> instanceA = new AtomicReference<>();


        { if( !instanceA.compareAndSet( null, this )) throw new IllegalStateException(); }



    /** Answers whether the current thread is the application main thread.
      */
    public final boolean isMainThread()
    {
     // return mainLooper.isCurrentThread();
     /// assumes API level 23
        return Thread.currentThread() == mainLooper.getThread();

    }



    /** Answers whether an application might run remotely from a server, as during normal use, as
      * opposed to local testing.  Currently this is false.  Assert it as false from any code that might
      * break on remote usage <em>provided the fix can reasonably be deferred</em>.
      *
      *     @see #setRemotelyUsable()
      */
    public static boolean isRemotelyUsable() { return isRemotelyUsable; }


        private static volatile boolean isRemotelyUsable;


        /** Establishes that an application might run remotely from a server.
          *
          *     @see #isRemotelyUsable()
          */
        public static void setRemotelyUsable() { isRemotelyUsable = true; }



    /** The general preference store for this application.  Be aware that it registers its listeners by
      * <a href='http://developer.android.com/reference/android/content/SharedPreferences.html#registerOnSharedPreferenceChangeListener(android.content.SharedPreferences.OnSharedPreferenceChangeListener)'
      *  target='_top'>weak reference</a>.  Registration alone is therefore insufficient
      * to prevent premature finalization and effective unregistration of the listener.
      * Something else must hold a strong reference to it, such as an unregistration destructible.
      */
    public @ThreadRestricted("app main") final SharedPreferences preferences() { return preferences; }


        private SharedPreferences preferences; // final after onCreate
          // no cost to hold this ref because any getSharedPreferences will "hold" it anyway



    /** Returns either the given width or <code>px7mmX</code>, whichever is greater.  The value
      * <code>px7mmX</code> is the minimum width (as measured in physical pixels) that is not less than
      * 7 mm.  This is recommended as the absolute minimum for touch targets, demanding some care on the
      * part of the user, e.g. touching with the tip of the index finger.
      *
      *     @param pxWidth The given width as measured in physical pixels.
      *     @see <a href='http://www.lukew.com/ff/entry.asp?1085' target='_top'>Touch Target Sizes</a>
      */
    public final int px7mmExtendedWidth( final int pxWidth ) { return Math.max( pxWidth, px7mmX ); }


        private volatile int px7mmX; // final after onCreate



    /** Returns either the given width or <code>px9mmX</code>, whichever is greater.  The value
      * <code>px9mmX</code> is the minimum width (as measured in physical pixels) that is not less than
      * 9 mm.  This is recommended as the general minimum for touch targets.
      *
      *     @param pxWidth The given width as measured in physical pixels.
      *     @see <a href='http://www.lukew.com/ff/entry.asp?1085' target='_top'>Touch Target Sizes</a>
      */
    public final int px9mmExtendedWidth( final int pxWidth ) { return Math.max( pxWidth, px9mmX ); }


        private volatile int px9mmX; // final after onCreate



    /** The size of a density-independent pixel as measured in physical pixels.  Returns the value of
      * getResources.getDisplayMetrics.<a href='http://developer.android.com/reference/android/util/DisplayMetrics.html#density' target='_top'>density</a>
      * cached (for speed) when this application was created.
      *
      *     @see ActivityX#pxSP()
      *     @see <a href='http://developer.android.com/guide/topics/resources/more-resources.html#Dimension'
      *       target='_top'>Resources § Dimension</a>
      */
    public final float pxDP() { return pxDP; }


        private volatile float pxDP; // final after onCreate



    /** A common rectangle for isolated use on the application main thread.
      */
    public @Warning("thread restricted object, app main") final Rect rect() { return rect; }


        private final Rect rect = new Rect();



    /** Clears and returns a common string builder for isolated use on the application main thread.
      * Consider calling {@linkplain StringBuilder#trimToSize trimToSize} after building a large string.
      */
      @ThreadRestricted("app main")
    public final StringBuilder stringBuilderClear() { return StringBuilderX.clear( stringBuilder ); }


        private final StringBuilder stringBuilder = new StringBuilder();



    /** A common value container for isolated use on the application main thread.
      */
    public @Warning("thread restricted object, app main") final TypedValue typedValue() { return typedValue; }


        private final TypedValue typedValue = new TypedValue();



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final Looper mainLooper; // store to avoid sync in Looper.getMainLooper (API level 23 source)


}
