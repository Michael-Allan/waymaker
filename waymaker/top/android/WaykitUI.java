package waymaker.top.android; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.app.Activity;
import android.content.*;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import java.io.*;
import java.util.concurrent.atomic.AtomicReference;
import org.xmlpull.v1.*;
import waymaker.gen.*;

import static java.util.logging.Level.WARNING;


/** A waykit user interface in the form of an Android application.  It takes ownership of the
  * {@linkplain java.net.ResponseCache HTTP response cache}, expecting no contention.
  */
public @ThreadSafe final class WaykitUI extends Application implements Application.ActivityLifecycleCallbacks
{


    /** Constructs the {@linkplain #i() single instance} of WaykitUI.  This constructor is called by
      * the Android runtime during initialization of the actual application, as commanded in
      * <a href='AndroidManifest.xml' target='_top'>AndroidManifest.xml</a>.
      *
      *     @throws IllegalStateException if an instance was already constructed.
      */
    public @Warning("non-API") WaykitUI() {}



   // ` c r e a t i o n ````````````````````````````````````````````````````````````````````````````````


    public @Override @Warning("non-API") void onCreate()
    {
        super.onCreate(); // obeying API

      // Enable response caching by default for each HttpURLConnection.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      // Has no effect on DownloadManager, which apparently cannot do response caching.
      // http://stackoverflow.com/questions/35191718
        installHttpResponseCache();

      // - - -
        registerActivityLifecycleCallbacks( this ); // no need to unregister from self
    }



   // --------------------------------------------------------------------------------------------------


    /** The single instance of WaykitUI as created by the Anroid runtime, or null if there is none.
      */
    public static WaykitUI i() { return instanceA.get(); }


        private static final AtomicReference<WaykitUI> instanceA = new AtomicReference<>();


        { if( !instanceA.compareAndSet( null, this )) throw new IllegalStateException(); }



    /** The access location of the user’s wayrepo in the form of a “tree URI”, or null if no location is
      * known.  The return value is backed by the {@linkplain Application#preferences() general
      * preference store} under the key ‘wayrepoTreeLoc’.
      *
      *     @see <a href='https://developer.android.com/about/versions/android-5.0.html#DirectorySelection'
      *       target='_top'>Android 5.0 § Directory selection</a>
      */
    public @ThreadRestricted("app main") String wayrepoTreeLoc()
    {
        return preferences().getString( "wayrepoTreeLoc", /*default*/null );
    }



    /** Sets the access location of the user’s wayrepo.
      */
    public @ThreadRestricted("app main") void wayrepoTreeLoc( final Uri uri )
    {
        final ContentResolver r = getContentResolver();
        final int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
        final String locOld = wayrepoTreeLoc();
        if( locOld != null )
        {
            try { r.releasePersistableUriPermission( Uri.parse(locOld), flags ); }
            catch( final SecurityException x ) { logger.info( x.toString() ); }
        }
        final SharedPreferences.Editor e = preferences().edit();
        if( uri == null ) e.remove( "wayrepoTreeLoc" );
        else
        {
            e.putString( "wayrepoTreeLoc", uri.toString() );
            try { r.takePersistableUriPermission( uri, flags ); } // persist permissions too
                /* * *
              / ! takePersistableUriPermission throws SecurityException
              /     " java.lang.SecurityException:
              /       No persistable permission grants found for UID 10058 and Uri 0
              /       @ content://de.hahnjo.android.smbprovider/tree/havoc/100-0/
              /     - new FLAG_GRANT_PERSISTABLE_URI_PERMISSION | FLAG_GRANT_PREFIX_URI_PERMISSION
              /         ? might those help
              // recompiled and it took this time
                  */
            catch( final SecurityException x )
            {
                logger.log( WARNING, "Cannot persist permissions to access URI " + uri, x );
            }
        }
        e.apply();
    }



    /** Returns a message for the user stating that access to the wayrepo at the given location is
      * denied by a SecurityException.  The reason for these denials is still unclear; maybe they
      * happen when the document provider that originally formed the URI is uninstalled.
      *
      *     @see #wayrepoTreeLoc()
      */
    public static String wayrepoTreeLoc_message( final String loc )
    {
        return "Cannot access wayrepo via " + loc
          + "\nTry using the wayrepo preview to reselect its location";
    }



    /** Configures a parser factory for Waymaker XHTML documents.
      *
      *     @return The same parser factory.
      */
    public static XmlPullParserFactory xhtmlConfigured( XmlPullParserFactory f ) throws XmlPullParserException
    {
        f.setFeature( XmlPullParser.FEATURE_PROCESS_NAMESPACES, true );
        f.setFeature( XmlPullParser.FEATURE_PROCESS_DOCDECL, true );
        return f;
    }



   // - A c t i v i t y - L i f e c y c l e - C a l l b a c k s ----------------------------------------


    public void onActivityCreated( Activity _ac, Bundle _in ) {}

    public void onActivityDestroyed( Activity _ac ) {}

    public void onActivityPaused( Activity _ac ) {}

    public void onActivityResumed( Activity _ac ) {}

    public void onActivitySaveInstanceState( Activity _ac, Bundle _out ) {}

    public void onActivityStarted( Activity _ac ) {}



    public void onActivityStopped( Activity _ac )
    {
        final HttpResponseCache cache = HttpResponseCache.getInstalled(); // grep HttpResponseCache-TS
        if( cache != null ) cache.flush(); /* Flush buffer to file system in case whole app is exiting.
          Not also closing the cache, because the certainty of exit is too hard to detect.  And closure
          would probably be unnecessary at that point anyway. */
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    @ThreadRestricted("app main"/*for atomic operation*/) void clearHttpResponseCache()
    {
        final HttpResponseCache cache = HttpResponseCache.getInstalled(); // grep HttpResponseCache-TS
        try // for lack of cache.clear, delete and reinstall:
        {
            if( cache != null ) cache.delete(); // before installing new cache, in case the two tangle
            installHttpResponseCache();
        }
        catch( final IOException x ) { logger.log( WARNING, "Failed to clear the HTTP response cache", x ); }
    }



    private static final long HTTP_CACHE_BYTES = 50_000_000L;



    private void installHttpResponseCache()
    {
        try
        {
            final HttpResponseCache cache = HttpResponseCache.install( // grep HttpResponseCache-TS
              new File(getCacheDir(),HttpResponseCache.class.getName()), HTTP_CACHE_BYTES );
            final long sizeBytes = cache.size();
            final String utilization;
            if( sizeBytes >= 0 )
            {
                final float percentF = sizeBytes / (cache.maxSize()/100.0f);
                final int percentI = Math.round( percentF );
                utilization = Integer.toString(percentI) + "%";
            }
            else utilization = "Unable to calculate";
            logger.info( "HTTP response cache utilization: " + utilization );
        }
        catch( final IOException x ) { logger.log( WARNING, "Cannot enable HTTP response cache", x ); }
    }



    private static final java.util.logging.Logger logger = LoggerX.getLogger( WaykitUI.class );


}
