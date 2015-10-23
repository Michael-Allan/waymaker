package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import android.content.*;
import android.net.Uri;
import android.os.*;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import overware.gen.*;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;
import static android.content.Intent.ACTION_OPEN_DOCUMENT_TREE;
import static java.util.logging.Level.WARNING;
import static overware.gen.ActivityLifeStage.*;


/** The principle (and only) activity of this application.
  */
public @ThreadRestricted("app main") final class Overguidance extends android.app.Activity
{

    private static final PolyStator<Overguidance> stators = new PolyStator<>();

///////


    { lifeStage = INITIALIZING; }



    protected @Override void onCreate( final Bundle inB )
    {
        isCreatedAnew = inB == null;
        assert lifeStage.compareTo(CREATING) < 0: "One creation per instance, no colliding creations";
        lifeStage = CREATING;
        lifeStageBell.ring();
        super.onCreate( inB );
        if( isCreatedAnew ) make( null );
        else
        {
            System.err.println( " --- onCreate from saved bundle: " + inB ); // TEST
            final byte[] state = inB.getByteArray( Overguidance.class.getName() ); // get state from bundle
            // Not following the Android convention of using the bundle to save and restore each complex
            // object as a whole Parcelable, complete with its references to external dependencies.  Rather
            // restoring the complex whole as originally created using constructors and/or initializers to
            // inject its external dependencies.  All that remains therefore is to restore the state of the
            // internal variables of each reconstructed object.  All state is restored from a single parcel:
            final Parcel inP = Parcel.obtain();
            try
            {
                inP.unmarshall( state, 0, state.length ); // (sic) form state into parcel
                inP.setDataPosition( 0 ); // (undocumented requirement)
                make( inP );
            }
            finally { inP.recycle(); }
        }
        lifeStage = CREATED;
        lifeStageBell.ring();
    }



    private void make()
    {
      // CONFIGURATION EDITOR FOR OVERREPO PREVIEW.
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        /* * *
        - configuration and control of previews in overrepo-based UI views
          such as pollar forest (precount) and relational graph (overscript precompilation)
        - deployment
            - to be shown via ActivityBar
                - via Settings item
                    - via standard Preference UI
                        < http://developer.android.com/guide/topics/ui/settings.html
                        - as custom dialogue
                            < http://developer.android.com/guide/topics/ui/settings.html#Custom
                            - formally to select the overrepo location
                            - but also with side effect (somehow) of affecting enabled state,
                              and possibly other settings
            - designed for possible reuse in other non-setting contexts,
              such as "Refresh" dialogue that floats, allowing preview to show behind it
            - meantime just deployed inline here
        - layout
            - plan
                - overrepo location (URI)
                    [ view
                        ( text view
                    [ clear button
                        ( push-button
                    [ finder button
                        ( push-button
                        - pops the Android "document" finder
                [ enabling switch
                    ( toggle button
                    - enabling implies also an immediate refresh
                - refresh buttons
                    ( push buttons
                    [ local refresh
                        - e.g. precount only, leaving unadjusted cache
                        - to allow immediate testing of overrepo changes
                    [ full refresh
                        - e.g. unadjusted cache too
                    - refresh may also be initiated wherever preview itself is shown
                      (i.e. in all overrepo-based UI views) by impatience gesture
                        - such as deselection with immediate reselection
                        - when refresh gesture immediately repeated, depth of effect escalates:
                            ( as per notebook 2015.6.4
                            - 1st locally refreshes
                            - 2nd fully refreshes
                [ feedback view
                    ( text view
                    - shows by default the time lapsed since last successful refresh
            - only pieces of this layout are test-coded below
          */
        final LinearLayout y = new LinearLayout( this );
        setContentView( y );
        y.setOrientation( LinearLayout.VERTICAL );
        {
          // Overrepo location view.
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            final TextView view = new TextView( this );
            y.addView( view );
            registerStrongly( new SharedPreferences.OnSharedPreferenceChangeListener()
            {
                { set(); } // init
                private void set() { view.setText( overrepoTreeLoc() ); }
                public void onSharedPreferenceChanged( SharedPreferences _p, String _key ) { set(); }
            });
        }
        {
            final LinearLayout x = new LinearLayout( this );
            y.addView( x );
            {
              // Location clear button, to clear the overrepo location.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( this );
                x.addView( button );
                button.setText( "Clear" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v ) { overrepoTreeLoc( null ); }
                });
            }
            {
              // Location finder button, to open the finder and locate the overrepo.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( this );
                x.addView( button );
                button.setText( "Locate overrepo" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v )
                    {
                        final int req =
                          REQ_OVERREPO;
                       // REQ_DOCUMENT; // TEST mostly just to doc this type of request, which is more common
                        final Intent intent;
                        try
                        {
                            if( req == REQ_OVERREPO ) intent = new Intent( ACTION_OPEN_DOCUMENT_TREE );
                            else
                            {
                                intent = new Intent( ACTION_OPEN_DOCUMENT );
                                intent.addCategory( android.content.Intent.CATEGORY_OPENABLE );
                                intent.setType( "*/*" ); // setType or throws ActivityNotFoundException
                            }
                        }
                        catch( final ActivityNotFoundException x ) { throw new RuntimeException( x ); }

                        startActivityForResult( intent, req ); // hence to onActivityResult
                    }
                });
            }
        }
        feedbackView = new TextView( this );
        {
            final LinearLayout x = new LinearLayout( this );
            y.addView( x );
            {
              // Local refresh button, to refresh from local overrepo.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( this );
                x.addView( button );
                button.setText( "Refresh from overrepo" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v ) { forest.startRefreshFromOverrepo( feedbackView ); }
                });
            }
            {
              // Full refresh button, to refresh from all sources.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( this );
                x.addView( button );
                button.setText( "From all sources" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v ) { forest.startRefresh( feedbackView ); }
                });
            }
        }

      // Feedback view.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        y.addView( feedbackView );
        if( isCreatedAnew ) feedbackView.setText( "Not yet refreshed" );


      // MISCELLANEOUS TOOLS.
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        {
            final LinearLayout x = new LinearLayout( this );
            y.addView( x );
            {
              // Logging test button, to log test messages at all standard logging levels.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( this );
                x.addView( button );
                button.setText( "Test logging" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v ) { LoggerX.test( logger ); }
                });
            }
            {
              // Generic test button.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( this );
                x.addView( button );
                button.setText( "Extend roots" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    private final GuidewayCount guidewayCount = new GuidewayCount( /*poll*/"1" );
                    public void onClick( View _v )
                    {
                        guidewayCount.enqueuePeersRequest( null/*ground*/, forest, /*paddedLimit*/0 );
                    }
                });
            }
        }
    }



    /** @param inP The parceled state to restore, or null to restore none.
      */
    private void make( final Parcel inP )
    {
        // here following the pattern of restoring (inP) constructors elsewhere

        make(); // whatever can be made without inline restoration
        if( inP != null ) stators.restore( this, inP ); // saved by stators in static inits further below
        final boolean isFirstConstruction;
        if( wasConstructorCalled ) isFirstConstruction = false;
        else
        {
            isFirstConstruction = true;
            wasConstructorCalled = true;
        }

      // Forest.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if( isFirstConstruction ) stators.add( new StateSaver<Overguidance>()
        {
            public void save( final Overguidance og, final Parcel out )
            {
                Forest.stators.save( og.forest, out );
            }
        });
        forest = new Forest( /*poll*/"1", feedbackView, this, inP );

      // Forester.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if( isFirstConstruction ) stators.add( new StateSaver<Overguidance>()
        {
            public void save( final Overguidance og, final Parcel out )
            {
                Forester.stators.save( og.foresterV.forester(), out );
                ForesterV.stators.save( og.foresterV, out );
            }
        });
        foresterV = new ForesterV( new Forester( forest ));
        if( inP != null ) // restore
        {
            Forester.stators.restore( foresterV.forester(), inP );
            ForesterV.stators.restore( foresterV, inP );
        }

      // - - -
        if( isFirstConstruction ) stators.seal();
    }



    private static boolean wasConstructorCalled; /* or more correctly 'wasMakeCalled',
      but here following the pattern of restoring (inP) constructors elsewhere */



   // --------------------------------------------------------------------------------------------------


    /** Answers whether this activity’s creation is a creation from scratch, as opposed to {@linkplain
      * #onRestoreInstanceState(Bundle) saved state}.
      *
      *     @throws IllegalStateException if the life stage is less than CREATING.
      */
    boolean isCreatedAnew()
    {
        if( lifeStage.compareTo(CREATING) < 0 ) throw new IllegalStateException();

        return isCreatedAnew;
    }


        private boolean isCreatedAnew;



    /** The life stage of this activity.  Initially set to INITIALIZING, any subsequent change to the
      * return value will be signalled by the life stage bell.
      */
    ActivityLifeStage lifeStage() { return lifeStage; }


        private ActivityLifeStage lifeStage;



    /** A bell that rings when the life stage changes.
      */
    Bell<Changed> lifeStageBell() { return lifeStageBell; }


        private final ReRinger<Changed> lifeStageBell = Changed.newReRinger();



    /** The location of the user’s overrepo in the form of a "tree URI", or null if no location is
      * known.  The return value is backed by the {@linkplain Application#preferences() general
      * preference store}.
      *
      *     @see <a href='https://developer.android.com/about/versions/android-5.0.html#DirectorySelection'
      *       target='_top'>Android 5.0 § Directory selection</a>
      */
    String overrepoTreeLoc() { return preferences.getString( "overrepoTreeLoc", /*default*/null ); }


        private void overrepoTreeLoc( final Uri uri )
        {
            final ContentResolver r = getContentResolver();
            final int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            final String locOld = overrepoTreeLoc();
            if( locOld != null )
            {
                try { r.releasePersistableUriPermission( Uri.parse(locOld), flags ); }
                catch( final SecurityException x ) { logger.info( x.toString() ); }
            }
            final SharedPreferences.Editor e = preferences.edit();
            if( uri == null ) e.remove( "overrepoTreeLoc" );
            else
            {
                e.putString( "overrepoTreeLoc", uri.toString() );
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


        /** Returns a message to show the user in the event access via an overrepo treeLoc is denied by
          * a SecurityException.  How this might occur in normal operation is unclear; maybe after
          * uninstalling the document provider that formed the treeLoc.
          *
          *     @see #overrepoTreeLoc()
          */
        static @ThreadSafe String overrepoTreeLoc_message( final String treeLoc )
        {
            return "Cannot access overrepo via " + treeLoc
              + "\nTry using the overrepo preview to reselect its location";
        }



    /** Adds a change listener to the {@linkplain Application#preferences() general preference store}
      * and holds its reference in this activity.  This convenience method is a workaround for the
      * <a href='http://developer.android.com/reference/android/content/SharedPreferences.html#registerOnSharedPreferenceChangeListener(android.content.SharedPreferences.OnSharedPreferenceChangeListener)'
      * target='_top'>weak register</a> in the store.
      *
      *     @see #unregisterStrongly(SharedPreferences.OnSharedPreferenceChangeListener)
      */
    void registerStrongly( final SharedPreferences.OnSharedPreferenceChangeListener l )
    {
        preferences.registerOnSharedPreferenceChangeListener( l );
        strongRegister.add( l );
    }


        private final ArrayList<SharedPreferences.OnSharedPreferenceChangeListener> strongRegister =
          new ArrayList<>();


        /** Removes a change listener from the {@linkplain Application#preferences() general preference
          * store} and releases its reference from this activity.
          *
          *     @see #registerStrongly(SharedPreferences.OnSharedPreferenceChangeListener)
          */
        void unregisterStrongly( final SharedPreferences.OnSharedPreferenceChangeListener l )
        {
            preferences.unregisterOnSharedPreferenceChangeListener( l );
            strongRegister.remove( l );
        }



   // - A c t i v i t y --------------------------------------------------------------------------------


    protected @Override void onActivityResult( final int req, final int res, final Intent intent )
    {
        if( req == REQ_DOCUMENT )
        {
            if( res == RESULT_OK )
            {
                final Uri uri = intent.getData();
                System.out.println( "URI of selected document: " + uri );
            }
            return;
        }

        if( req != REQ_OVERREPO ) throw new IllegalStateException( "Unrecognized req: " + req );

        if( res != RESULT_OK )
        {
            if( res != RESULT_CANCELED )
            {
                logger.warning( "Document request returned an unrecognized result code: " + res );
            }
            return;
        }

        if( intent == null ) // docs imply null is indeed possible
        {                   // https://developer.android.com/guide/topics/providers/document-provider.html#client
            logger.warning( "Null response to document request" );
            return;
        }

        final Uri treeUri = intent.getData();
        overrepoTreeLoc( treeUri );
    }



    protected @Override void onDestroy()
    {
        assert lifeStage.compareTo(DESTROYING) < 0: "One destruction per instance, no colliding destructions";
        lifeStage = DESTROYING;
        lifeStageBell.ring();
        super.onDestroy();
        lifeStage = DESTROYED;
        lifeStageBell.ring();
    }



    protected @Override void onSaveInstanceState( final Bundle outB ) // [RA]
    {
        System.err.println( " --- onSaveInstanceState to bundle" ); // TEST
        super.onSaveInstanceState( outB );
        final byte[] state;
        final Parcel outP = Parcel.obtain();
        try
        {
            stators.save( this, outP ); // save all state variables to parcel
            state = outP.marshall(); // (sic) form parcel into state
        }
        finally { outP.recycle(); }
        outB.putByteArray( Overguidance.class.getName(), state ); // put state into bundle
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private TextView feedbackView; // final after onCreate


        static { stators.add( new Stator<Overguidance>()
        {
            public void save( final Overguidance og, final Parcel out )
            {
                out.writeString( og.feedbackView.getText().toString() );
            }
            public void restore( final Overguidance og, final Parcel in )
            {
                og.feedbackView.setText( in.readString() );
            }
        });}



    private Forest forest; // final after make, which adds stator



    private ForesterV foresterV; // final after make, which adds stator



    private static final java.util.logging.Logger logger = LoggerX.getLogger( Overguidance.class );



    private static final SharedPreferences preferences = Application.i().preferences();



    private static final int REQ_OVERREPO = 0; // using SAF [SAF] ACTION_OPEN_DOCUMENT_TREE

    private static final int REQ_DOCUMENT = 1; /* using SAF [SAF] ACTION_OPEN_DOCUMENT (single,
      isolated doc) for regression testing of this action in external Android SMBProvider app */


}


// Notes
// -----
//  [RA] Recreating an Activity
//      http://developer.android.com/training/basics/activity-lifecycle/recreating.html
//
//  [SAF] Storage Access Framework
//      https://developer.android.com/guide/topics/providers/document-provider.html
