package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.content.*;
import android.net.Uri;
import android.os.*;
import android.view.View;
import android.widget.*;
import waymaker.gen.*;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;
import static android.content.Intent.ACTION_OPEN_DOCUMENT_TREE;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static java.util.logging.Level.WARNING;
import static waymaker.gen.ActivityLifeStage.*;


/** The principle (and only) activity of this application.
  */
public @ThreadRestricted("app main") final class Wayranging extends android.app.Activity
{

    private static final PolyStator<Wayranging> stators = new PolyStator<>();

///////


    { lifeStage = INITIALIZING; }
{ System.err.println( " --- init activity on thread " + Thread.currentThread() ); } // TEST, esp. for stators



   // ` c r e a t i o n ````````````````````````````````````````````````````````````````````````````````


    protected @Override void onCreate( final Bundle inB )
    {
        isCreatedAnew = inB == null;
        assert lifeStage.compareTo(CREATING) < 0: "One creation per instance, no colliding creations";
        lifeStage = CREATING;
        lifeStageBell.ring();
        super.onCreate( inB );
        if( isCreatedAnew ) create( null );
        else
        {
            System.err.println( " --- onCreate from saved bundle: " + inB ); // TEST
            final byte[] state = inB.getByteArray( Wayranging.class.getName() ); // get state from bundle
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
                create( inP );
            }
            finally { inP.recycle(); }
        }
        lifeStage = CREATED;
        lifeStageBell.ring();
    }



    /** @param _inP The parceled state to restore, or null to restore none.
      */
    private void create( final Parcel _inP )
    {
      // CONFIGURATION EDITOR FOR WAYREPO PREVIEW.
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        /* * *
        - configuration and control of previews in wayrepo-based UI views
          such as pollar forest (precount) and relational graph (wayscript precompilation)
        - deployment
            - to be shown via ActionBar
                - via Settings item
                    - via standard Preference UI
                        < http://developer.android.com/guide/topics/ui/settings.html
                        - as custom dialogue
                            < http://developer.android.com/guide/topics/ui/settings.html#Custom
                            - formally to select the wayrepo location
                            - but also with side effect (somehow) of affecting enabled state,
                              and possibly other settings
            - designed for possible reuse in other non-setting contexts,
              such as "Refresh" dialogue that floats, allowing preview to show behind it
            - meantime just deployed inline here
        - layout
            - plan
                - wayrepo location (URI)
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
                        - to allow immediate testing of wayrepo changes
                    [ full refresh
                        - e.g. unadjusted cache too
                    - refresh may also be initiated wherever preview itself is shown
                      (i.e. in all wayrepo-based UI views) by impatience gesture
                        - such as deselection with immediate reselection
                        - when refresh gesture immediately repeated, depth of effect escalates:
                            ( notebook 2015.6.4
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
        final SharedPreferences preferences = Application.i().preferences();
        {
          // Wayrepo location view.
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            final TextView view = new TextView( this );
            y.addView( view );
            preferences.registerOnSharedPreferenceChangeListener( new OnSharedPreferenceChangeListener()
            {
                {
                    relay(); // init
                    Object unregistrationAgent = preferencesUnregisterOnDestruction( this );
                      // no need to unregister the agent itself, its registry does not outlive it
                }
                private void relay()
                {
                    String text = wayrepoTreeLoc();
                    if( text == null ) text = "location unspecified";
                    view.setText( text );
                }
                public void onSharedPreferenceChanged( SharedPreferences _p, String _key ) { relay(); }
            });
        }
        {
            final LinearLayout x = new LinearLayout( this );
            y.addView( x );
            {
              // Location clear button, to clear the wayrepo location.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( this );
                x.addView( button );
                button.setText( "Clear" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v ) { wayrepoTreeLoc( null ); }
                });
            }
            {
              // Location finder button, to open the finder and locate the wayrepo.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( this );
                x.addView( button );
                button.setText( "Locate wayrepo" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v )
                    {
                        final int req =
                          REQ_WAYREPO;
                       // REQ_DOCUMENT; // TEST mostly just to doc this type of request, which is more common
                        final Intent intent;
                        try
                        {
                            if( req == REQ_WAYREPO ) intent = new Intent( ACTION_OPEN_DOCUMENT_TREE );
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
        {
            final LinearLayout x = new LinearLayout( this );
            y.addView( x );
            {
              // Local refresh button, to refresh from local wayrepo.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( this );
                x.addView( button );
                button.setText( "Refresh from wayrepo" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v ) { forests.startRefreshFromWayrepo( wayrepoTreeLoc() ); }
                });
                preferences.registerOnSharedPreferenceChangeListener( new OnSharedPreferenceChangeListener()
                {
                    {
                        relay(); // init
                        Object unregistrationAgent = preferencesUnregisterOnDestruction( this );
                          // no need to unregister the agent itself, its registry does not outlive it
                    }
                    private void relay() { button.setEnabled( wayrepoTreeLoc() != null ); }
                      // hint to user that refresh without a wayrepo is pointless
                    public void onSharedPreferenceChanged( SharedPreferences _p, String _key ) { relay(); }
                });
            }
            {
              // Full refresh button, to refresh from all sources.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( this );
                x.addView( button );
                button.setText( "from all" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v ) { forests.startRefresh( wayrepoTreeLoc() ); }
                });
            }
        }

      // Feedback view.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final TextView noteView = new TextView( this );
        y.addView( noteView );


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
                    public void onClick( View _v )
                    {
                        final Forest forest = forests.get( "end" );
                        new ServerCount(forest.pollName()).enqueuePeersRequest( null/*ground*/, forest,
                          /*paddedLimit*/0 );
                    }
                });
            }
        }
        final Parcel inP/*grep CtorRestore*/ = _inP;


      // IN-LINE RESTORATION.  (here following the pattern of restoring (inP) constructors elsewhere)
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        if( inP != null ) stators.restore( this, inP ); // saved by stators in static inits further below
        final boolean isFirstConstruction;
        if( wasConstructorCalled ) isFirstConstruction = false;
        else
        {
            isFirstConstruction = true;
            wasConstructorCalled = true;
        }

      // Forests.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if( isFirstConstruction ) stators.add( new StateSaver<Wayranging>()
        {
            public void save( final Wayranging wr, final Parcel out )
            {
                ForestCache.stators.save( wr.forests, out );
            }
        });
        forests = new ForestCache( inP/*by CtorRestore*/ );
        forests.notaryBell().register( new Auditor<Changed>()
        { // no need to unregister, registry does not outlive this registrant
            { relay(); } // init
            private void relay() { noteView.setText( forests.refreshNote() ); }
            public void hear( Changed _ding ) { relay(); }
        });
        if( isFirstConstruction ) forests.startRefreshFromWayrepo( wayrepoTreeLoc() );

      // Forester.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if( isFirstConstruction ) stators.add( new StateSaver<Wayranging>()
        {
            public void save( final Wayranging wr, final Parcel out )
            {
                Forester.stators.save( wr.forestV.forester(), out );
                ForestV.stators.save( wr.forestV, out );
            }
        });
        forestV = new ForestV( new Forester( forests/*by CtorRestore*/.getOrMakeForest( "end" )));
        if( inP != null ) // restore
        {
            Forester.stators.restore( forestV.forester(), inP );
            ForestV.stators.restore( forestV, inP );
        }

      // - - -
        if( isFirstConstruction ) stators.seal();
    }



    private static boolean wasConstructorCalled; /* or more correctly 'wasCreateCalled',
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



    /** The access location of the user’s wayrepo in the form of a "tree URI", or null if no location is
      * known.  The return value is backed by the {@linkplain Application#preferences() general
      * preference store}.
      *
      *     @see <a href='https://developer.android.com/about/versions/android-5.0.html#DirectorySelection'
      *       target='_top'>Android 5.0 § Directory selection</a>
      */
    String wayrepoTreeLoc()
    {
        return Application.i().preferences().getString( "wayrepoTreeLoc", /*default*/null );
    }


        private void wayrepoTreeLoc( final Uri uri )
        {
            final ContentResolver r = getContentResolver();
            final int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            final String locOld = wayrepoTreeLoc();
            if( locOld != null )
            {
                try { r.releasePersistableUriPermission( Uri.parse(locOld), flags ); }
                catch( final SecurityException x ) { logger.info( x.toString() ); }
            }
            final SharedPreferences.Editor e = Application.i().preferences().edit();
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


        /** Returns a message for the user stating that access via the given wayrepo treeLoc is denied
          * by a SecurityException.  How this might happen in normal operation is unclear; maybe after
          * uninstalling the document provider that formed the treeLoc.
          *
          *     @see #wayrepoTreeLoc()
          */
        static @ThreadSafe String wayrepoTreeLoc_message( final String treeLoc )
        {
            return "Cannot access wayrepo via " + treeLoc
              + "\nTry using the wayrepo preview to reselect its location";
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

        if( req != REQ_WAYREPO ) throw new IllegalStateException( "Unrecognized req: " + req );

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
        wayrepoTreeLoc( treeUri );
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
        outB.putByteArray( Wayranging.class.getName(), state ); // put state into bundle
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private ForestCache forests; // final after create, which adds stator



    private ForestV forestV; // final after create, which adds stator



    private static final java.util.logging.Logger logger = LoggerX.getLogger( Wayranging.class );



 // /** Adds a change listener to the {@linkplain Application#preferences() general preference store}
 //   * and holds its reference in this activity.  This convenience method is a workaround for the
 //   * <a href='http://developer.android.com/reference/android/content/SharedPreferences.html#registerOnSharedPreferenceChangeListener(android.content.SharedPreferences.OnSharedPreferenceChangeListener)'
 //   * target='_top'>weak register</a> in the store.
 //   */
 // private void preferencesRegisterStrongly( final OnSharedPreferenceChangeListener l )
 // {
 //     Application.i().preferences().registerOnSharedPreferenceChangeListener( l );
 //     preferencesStrongRegister.add( l );
 // }
 //
 //
 //     private final ArrayList<OnSharedPreferenceChangeListener> preferencesStrongRegister =
 //       new ArrayList<>();
 //
 //
 //     /** Removes a change listener from the {@linkplain Application#preferences() general preference
 //       * store} and releases its reference from this activity.
 //       */
 //     private void preferencesUnregisterStrongly( final OnSharedPreferenceChangeListener l )
 //     {
 //         Application.i().preferences().unregisterOnSharedPreferenceChangeListener( l );
 //         preferencesStrongRegister.remove( l );
 //     }
 //
 /// not actually needed yet; agents (such as unregisterOnDestruction) that unregister the listeners
 /// all happen to be strongly held, with consequence that listener itself is strongly held



    /** Schedules the listener to be unregistered
      * from the {@linkplain Application#preferences() general preference store}
      * when this activity is destroyed.  This convenience method happens also to defeat the
      * <a href='http://developer.android.com/reference/android/content/SharedPreferences.html#registerOnSharedPreferenceChangeListener(android.content.SharedPreferences.OnSharedPreferenceChangeListener)'
      * target='_top'>weak register</a> in the store by holding a strong reference to the listener.
      *
      *     @return The agent that is responsible soley for unregistering the listener.  The agent is
      *       implemented as an auditor of the {@linkplain #lifeStageBell() life stage bell}.
      */
    private Auditor<Changed> preferencesUnregisterOnDestruction( final OnSharedPreferenceChangeListener l )
    {
        final Auditor<Changed> auditor = new Auditor<Changed>()
        {
            public void hear( Changed _ding )
            {
                if( lifeStage != DESTROYING ) return;

                Application.i().preferences().unregisterOnSharedPreferenceChangeListener( l );
            }
        };
        lifeStageBell.register( auditor );
        return auditor;
    }



    private static final int REQ_WAYREPO = 0; // using SAF [SAF] ACTION_OPEN_DOCUMENT_TREE

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
