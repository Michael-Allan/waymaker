package waymaker.gen; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.os.*;
import java.util.concurrent.atomic.AtomicReference;


/** An Android application.
  */
public @ThreadSafe class Application extends android.app.Application
{


    /** Constructs the {@linkplain #i() single instance} of Application.  This constructor is called by
      * the Android runtime during initialization of the actual application as commanded in the manifest
      * file <code>AndroidManifest.xml</code>.
      *
      *     @throws IllegalStateException if an instance was already constructed.
      */
    public @Warning("non-API") Application()
    {
        mainLooper = Looper.getMainLooper();
        handler = new Handler( mainLooper );
    }



    static { System.setProperty( "waymaker.g.LoggerX.classPrefix", "wm" ); }
      // early init: before LoggerX loads, rename the class-based loggers from "PACKAGE.CLASS", which
      // Android logcat tags simply as "CLASS", to "wmCLASS" instead



   // ` c r e a t i o n ````````````````````````````````````````````````````````````````````````````````


    public @Override @Warning("non-API") final void onCreate()
    {
        if( !isMainThread() ) throw new IllegalStateException(); // at least for visibility of this.preferences

        {
            final DisplayMetrics m = getResources().getDisplayMetrics();
            pxDP = m.density;
            pxSP = m.scaledDensity;
        }
        preferences = getSharedPreferences( /*name*/"app", /*mode, typical*/MODE_PRIVATE );
    }



   // --------------------------------------------------------------------------------------------------


    /** A handler for communicating with the application’s main thread.
      *
      *     @see <a href='http://developer.android.com/reference/android/app/Activity.html#runOnUiThread%28java.lang.Runnable%29'
      *       target='_top'>runOnUiThread</a>
      *     @see <a href='http://developer.android.com/reference/android/os/AsyncTask.html'
      *       target='_top'>AsyncTask</a>
      */
    public final Handler handler() { return handler; }


        private final Handler handler;



    /** The single instance of Application as created by the Anroid runtime, or null if there is none.
      */
    public static Application i() { return instanceA.get(); }


        private static final AtomicReference<Application> instanceA = new AtomicReference<>();


        { if( !instanceA.compareAndSet( null, this )) throw new IllegalStateException(); }



    /** Answers whether the current thread is the application main thread.
      */
    public final boolean isMainThread()
    {
     // return mainLooper.isCurrentThread();
     /// assumes API level 23
        return Thread.currentThread() == mainLooper.getThread();

    }



    /** The general preference store for this application.  Be aware that it registers its listeners by
      * <a href='http://developer.android.com/reference/android/content/SharedPreferences.html#registerOnSharedPreferenceChangeListener(android.content.SharedPreferences.OnSharedPreferenceChangeListener)'
      *  target='_top'>weak reference</a>.  Registration alone is therefore insufficient
      * to prevent premature finalization and effective unregistration of the listener.
      * Something else must hold a strong reference to it, such as an unregistration destructible.
      */
    public @ThreadRestricted("app main") final SharedPreferences preferences() { return preferences; }


        private SharedPreferences preferences; // final after onCreate
          // no cost to hold this ref because any getSharedPreferences will "hold" it anyway



    /** The size of a density-indpendent pixel as measured in physical pixels.
      *
      *     @see <a href='http://developer.android.com/guide/topics/resources/more-resources.html#Dimension'
      *       target='_top'>Resources § Dimension</a>
      */
    public final float pxDP() { return pxDP; }


        private volatile float pxDP; // final after onCreate



    /** The size of a scale-indpendent pixel as measured in physical pixels.  This is {@linkplain
      * #pxDP() pxDP} “scaled by the user's font size”, larger or smaller according to preference.
      *
      *     @see <a href='http://developer.android.com/guide/topics/resources/more-resources.html#Dimension'
      *       target='_top'>Resources § Dimension</a>
      */
    public final float pxSP() { return pxSP; }


        private volatile float pxSP; // final after onCreate



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



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final Looper mainLooper; // store to avoid sync in Looper.getMainLooper (API level 23 source)


}
