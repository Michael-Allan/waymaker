package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.content.*;
import android.database.*;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.DocumentsContract; // grep DocumentsContract-TS
import waymaker.gen.ThreadSafe;

import static android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME;
import static android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID;
import static android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE;
import static android.provider.DocumentsContract.Document.MIME_TYPE_DIR;


/** A tool for reading from the user’s local wayrepo.
  */
public final class WayrepoReader implements java.io.Closeable
{


    /** Constructs a WayrepoReader.  Call {@linkplain #close close}() when done with it.
      *
      *     @see #wayrepoTreeUri()
      *     @throws WayrepoAccessFailure if access to the wayrepo is denied by a security exception.
      *       See {@linkplain WaykitUI#wayrepoTreeLoc_message(String) wayrepoTreeLoc_message}.
      */
    public @ThreadSafe WayrepoReader( final Uri wayrepoTreeUri, final ContentResolver contentResolver )
      throws WayrepoAccessFailure
    {
        this.wayrepoTreeUri = wayrepoTreeUri;
        this.contentResolver = contentResolver;
        try { provider = contentResolver.acquireContentProviderClient( wayrepoTreeUri ); }
        catch( final SecurityException x )
        {
            throw new WayrepoAccessFailure( WaykitUI.wayrepoTreeLoc_message(wayrepoTreeUri.toString()), x );
        }
    }



   // --------------------------------------------------------------------------------------------------


    /** Returns the document identifier of the named directory, or null if the directory is not found.
      *
      *     @param parentID The document identifier of the parent.
      */
    public String findDirectory( final String name, final String parentID ) throws WayrepoAccessFailure, InterruptedException
    {
        try( final Cursor c/*proID_NAME_TYPE*/ = queryChildren( parentID ); )
        {
            while( c.moveToNext() )
            {
                if( !name.equals( c.getString(1) )) continue;

                if( !MIME_TYPE_DIR.equals( c.getString(2) )) continue;

                return c.getString( 0 );
            }
        }
        return null; // directory not found
    }



    /** A query projection of three formal parameters:
      * <a href='http://developer.android.com/reference/android/provider/DocumentsContract.Document.html#COLUMN_DOCUMENT_ID'
      *  target='_top'>document identifier</a> (ID),
      * <a href='http://developer.android.com/reference/android/provider/DocumentsContract.Document.html#COLUMN_DISPLAY_NAME'
      *  target='_top'>display name</a> (NAME) and
      * <a href='http://developer.android.com/reference/android/provider/DocumentsContract.Document.html#COLUMN_MIME_TYPE'
      *  target='_top'>MIME type</a> (TYPE).
      * Do not modify it.
      */
    public static final String[] proID_NAME_TYPE =
      new String[] { COLUMN_DOCUMENT_ID, COLUMN_DISPLAY_NAME, COLUMN_MIME_TYPE };



    /** The device that gives this reader access to the wayrepo.
      */
    public ContentProviderClient provider() { return provider; }


        private final ContentProviderClient provider; // grep ContentProviderClient-TS



    /** Returns an {@linkplain #proID_NAME_TYPE ID_NAME_TYPE} cursor over the children of the given
      * parent document.  Close the cursor when done with it.
      *
      *     @param parentID The document identifier of the parent.
      */
    public Cursor queryChildren( final String parentID ) throws WayrepoAccessFailure, InterruptedException
    {
        return queryChildren( parentID, false );
    }



    /** The access location of the user’s wayrepo in the form of a "tree URI"
      *
      *     @see <a href='https://developer.android.com/about/versions/android-5.0.html#DirectorySelection'
      *       target='_top'>Android 5.0 § Directory selection</a>
      */
    public Uri wayrepoTreeUri() { return wayrepoTreeUri; }


        private final Uri wayrepoTreeUri;



   // - A u t o - C l o s e a b l e --------------------------------------------------------------------


    public void close() { provider.release(); } // if not already released



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final ContentResolver contentResolver; // grep ContentResolver-TS



    private static final long MS_TIMEOUT_MIN = 4500;



    private static final long MS_TIMEOUT_INTERVAL = 500;



    private Cursor queryChildren( final String parentID, final boolean isRetry )
      throws WayrepoAccessFailure, InterruptedException
    {
        final Cursor c;
        try
        {
            c = provider.query( DocumentsContract.buildChildDocumentsUriUsingTree(wayrepoTreeUri,parentID),
              proID_NAME_TYPE, /*selector, unsupported*/null, /*selectorArgs*/null, /*order*/null );
              // selector unsupported in base impl (DocumentsProvider.queryChildDocuments)
        }
        catch( final RemoteException x ) { throw new WayrepoAccessFailure( x ); }

        if( c == null ) throw new WayrepoAccessFailure( "Cannot read wayrepo directory: " + parentID );

      // Return response if fully loaded.
      // - - - - - - - - - - - - - - - - -
        if( !c.getExtras().getBoolean( DocumentsContract.EXTRA_LOADING )) return c;

        if( isRetry ) { throw new WayrepoAccessFailure( "Incomplete response from documents provider after retry" ); }

      // Else wait for response to fully load.
      // - - - - - - - - - - - - - - - - - - - -
        Uri nUri = c.getNotificationUri();
        final boolean nUriDescendentsToo;
        if( nUri == null ) // probable bug, https://code.google.com/p/android/issues/detail?id=182258
        {
            nUri = topmostUri(); // default
            nUriDescendentsToo = true;
        }
        else nUriDescendentsToo = false;
        final Observer o = new Observer();
        contentResolver.registerContentObserver( nUri, nUriDescendentsToo, o );
          // should eventually set o.isFullyLoaded, then call WayrepoReader.this.notify()
        try
        {
            final long msStart = System.currentTimeMillis();
            synchronized( WayrepoReader.this )
            {
                WayrepoReader.this.wait( MS_TIMEOUT_MIN + MS_TIMEOUT_INTERVAL );
                while( !o.isFullyLoaded )
                {
                    final long msElapsed = System.currentTimeMillis() - msStart;
                    if( msElapsed > MS_TIMEOUT_MIN )
                    {
                        throw new WayrepoAccessFailure( "Wayrepo timeout after " + msElapsed + " ms" );
                    }

                    WayrepoReader.this.wait( MS_TIMEOUT_INTERVAL );
                }
            }
        }
        finally{ contentResolver.unregisterContentObserver( o ); }

      // Retry query now that response is fully loaded.
      // - - - - - - - - - - - - - - - - - - - - - - - -
        return queryChildren( parentID, true );
    }



    private Uri topmostUri() // topmost ancestor of wayrepoTreeUri
    {
        if( topmostUri == null )
        {
            topmostUri = uriBuilderSA().scheme(wayrepoTreeUri.getScheme())
              .authority(wayrepoTreeUri.getAuthority()).build();
        }
        return topmostUri;
    }


        private Uri topmostUri;



    private Uri.Builder uriBuilderSA() // scheme + authority only; cannot clear Uri.Builder
    {
        if( uriBuilderSA == null ) uriBuilderSA = new Uri.Builder();

        return uriBuilderSA;
    }


        private Uri.Builder uriBuilderSA;



   // ==================================================================================================


    private final class Observer extends ContentObserver
    {

        Observer() { super( WaykitUI.i().handler() ); }


        volatile boolean isFullyLoaded;


        public @Override void onChange( boolean _selfChange, Uri _n )
        {
            isFullyLoaded = true;
            synchronized( WayrepoReader.this ) { WayrepoReader.this.notify(); }
        }

    }


}
