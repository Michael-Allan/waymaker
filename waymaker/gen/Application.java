package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.content.SharedPreferences;
import android.os.*;
import java.util.concurrent.atomic.AtomicReference;


/** An Android application.
  */
public @ThreadRestricted("app main") class Application extends android.app.Application
{


    /** Constructs the {@linkplain #i() single instance} of Application.  This constructor is called by
      * the Android runtime during initialization of the actual application as commanded in the manifest
      * file <code>AndroidManifest.xml</code>.
      *
      *     @throws IllegalStateException if an instance was already constructed, or the calling thread
      *       is not the application {@linkplain #isMainThread() main thread}.
      */
    public @Warning("non-API") Application()
    {
        mainLooper = Looper.getMainLooper();
        if( !isMainThread() ) throw new IllegalStateException();

        handler = new Handler( mainLooper );
    }



   // ` e a r l y ``````````````````````````````````````````````````````````````````````````````````````


    static { System.setProperty( "waymaker.g.LoggerX.classPrefix", "wm" ); }
      // before LoggerX loads, rename the class-based loggers from "PACKAGE.CLASS", which Android logcat
      // tags simply as "CLASS", to "wmCLASS" instead



   // ` c r e a t i o n ````````````````````````````````````````````````````````````````````````````````


    public @Override final void onCreate()
    {
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
    public @ThreadSafe final Handler handler() { return handler; }


        private final Handler handler;



    /** The single instance of Application as created by the Anroid runtime, or null if there is none.
      */
    public static @ThreadSafe Application i() { return instanceA.get(); }


        private static final AtomicReference<Application> instanceA = new AtomicReference<>();


        { if( !instanceA.compareAndSet( null, this )) throw new IllegalStateException(); }



    /** Answers whether the current thread is the application main thread.
      */
    public final boolean isMainThread()
    {
     // return mainLooper.isCurrentThread();
     /// requires SDK 23
        return Thread.currentThread() == mainLooper.getThread();

    }



    /** The general preference store for this application.  Be aware that its change listeners are only
      * <a href='http://developer.android.com/reference/android/content/SharedPreferences.html#registerOnSharedPreferenceChangeListener(android.content.SharedPreferences.OnSharedPreferenceChangeListener)'
      * target='_top'>weakly registered</a>.
      */
    public final SharedPreferences preferences() { return preferences; }


        private SharedPreferences preferences; // final after onCreate
          // no cost to hold this ref because any getSharedPreferences will "hold" it anyway



    /** Clears and returns a common string builder for atomic use on the application main thread.
      * Consider calling {@linkplain StringBuilder#trimToSize trimToSize} after building a large string.
      */
    public final StringBuilder stringBuilderClear() { return StringBuilderX.clear( stringBuilder ); }


        private final StringBuilder stringBuilder = new StringBuilder();



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final Looper mainLooper; // store to save sync in Looper.getMainLooper (API 23)


}
