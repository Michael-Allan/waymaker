package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.content.*;
import android.net.Uri;
import java.util.concurrent.atomic.AtomicReference;
import org.xmlpull.v1.*;
import waymaker.gen.*;

import static java.util.logging.Level.WARNING;


/** A waykit user interface in the form of an Android application.
  */
public @ThreadRestricted("app main") final class WaykitUI extends Application
{


    /** Constructs the {@linkplain #i() single instance} of WaykitUI.  This constructor is called by
      * the Android runtime during initialization of the actual application, as commanded in
      * <a href='AndroidManifest.xml' target='_top'>AndroidManifest.xml</a>.
      *
      *     @throws IllegalStateException if an instance was already constructed.
      */
    public @Warning("non-API") WaykitUI() {}



   // --------------------------------------------------------------------------------------------------


    /** The single instance of WaykitUI as created by the Anroid runtime, or null if there is none.
      */
    public static @ThreadSafe WaykitUI i() { return instanceA.get(); }


        private static final AtomicReference<WaykitUI> instanceA = new AtomicReference<>();


        { if( !instanceA.compareAndSet( null, this )) throw new IllegalStateException(); }



    /** The access location of the user’s wayrepo in the form of a “tree URI”, or null if no location is
      * known.  The return value is backed by the {@linkplain Application#preferences() general
      * preference store} under the key ‘wayrepoTreeLoc’.
      *
      *     @see <a href='https://developer.android.com/about/versions/android-5.0.html#DirectorySelection'
      *       target='_top'>Android 5.0 § Directory selection</a>
      */
    String wayrepoTreeLoc()
    {
        return preferences().getString( "wayrepoTreeLoc", /*default*/null );
    }



    /** Sets the access location of the user’s wayrepo.
      */
    void wayrepoTreeLoc( final Uri uri )
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
    static @ThreadSafe String wayrepoTreeLoc_message( final String loc )
    {
        return "Cannot access wayrepo via " + loc
          + "\nTry using the wayrepo preview to reselect its location";
    }



    /** Configures a parser factory for Waymaker XHTML documents.
      *
      *     @return The same parser factory.
      */
    static @ThreadSafe XmlPullParserFactory xhtmlConfigured( XmlPullParserFactory f )
      throws XmlPullParserException
    {
        f.setFeature( XmlPullParser.FEATURE_PROCESS_NAMESPACES, true );
        f.setFeature( XmlPullParser.FEATURE_PROCESS_DOCDECL, true );
        return f;
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final java.util.logging.Logger logger = LoggerX.getLogger( WaykitUI.class );


}
