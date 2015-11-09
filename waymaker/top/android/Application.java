package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.content.SharedPreferences;
import android.os.*;
import java.util.concurrent.atomic.AtomicReference;
import org.xmlpull.v1.*;
import waymaker.gen.*;


/** Utilities for this Android application.
  */
public @ThreadRestricted("app main") final class Application extends android.app.Application
{


    /** Constructs the {@linkplain #i() single instance} of Application.  This constructor is called by
      * the Android runtime during initialization of the actual application, as commanded in
      * <a href='../../../../../top/android/AndroidManifest.xml' target='_top'>AndroidManifest.xml</a>.
      *
      *     @throws IllegalStateException if an instance was already constructed.
      */
    public Application() {}



    static { System.setProperty( "waymaker.g.LoggerX.classPrefix", "way" ); }
      // before LoggerX loads, rename class-based loggers from "PACKAGE.CLASS", which Android log
      // renders as simple tag "CLASS", to "ovCLASS" instead



    public @Override void onCreate()
    {
        preferences = getSharedPreferences( /*name*/"app", /*mode, typical*/MODE_PRIVATE );
    }



    /** The single instance of Application, or null if none is constructed.
      */
    static @ThreadSafe Application i() { return instanceA.get(); }


        private static final AtomicReference<Application> instanceA = new AtomicReference<>();


        { if( !instanceA.compareAndSet( null, this )) throw new IllegalStateException(); }



   // --------------------------------------------------------------------------------------------------


    /** A handler for communicating with the application’s main thread.
      *
      *     @see <a href='http://developer.android.com/reference/android/app/Activity.html#runOnUiThread%28java.lang.Runnable%29'
      *       target='_top'>runOnUiThread</a>
      *     @see <a href='http://developer.android.com/reference/android/os/AsyncTask.html'
      *       target='_top'>AsyncTask</a>
      */
    @ThreadSafe Handler handler() { return handler; }


        private final Handler handler = new Handler( Looper.getMainLooper() );



    /** The general preference store for this application.
      *
      *     @see Wayranging#registerStrongly(SharedPreferences.OnSharedPreferenceChangeListener)
      */
    SharedPreferences preferences() { return preferences; }


        private SharedPreferences preferences; // final after onCreate
          // no cost to hold this ref because any getSharedPreferences will "hold" it anyway



    /** Clears and returns a common string builder for atomic use.  Consider calling
      * {@linkplain StringBuilder#trimToSize trimToSize} after building a large string.
      */
    public StringBuilder stringBuilderClear() { return StringBuilderX.clear( stringBuilder ); }


        private final StringBuilder stringBuilder = new StringBuilder();



    /** Configures a parser factory for Waymaker XHTML documents.
      *
      *     @return The same parser factory.
      */
    @ThreadSafe XmlPullParserFactory xhtmlConfigured( XmlPullParserFactory f )
      throws XmlPullParserException
    {
        f.setFeature( XmlPullParser.FEATURE_PROCESS_NAMESPACES, true );
        f.setFeature( XmlPullParser.FEATURE_PROCESS_DOCDECL, true );
        return f;
    }


}
