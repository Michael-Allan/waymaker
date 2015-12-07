package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.content.*;
import android.net.Uri;
import android.os.Parcel;
import android.view.View;
import android.widget.*;
import waymaker.gen.*;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_OPEN_DOCUMENT;
import static android.content.Intent.ACTION_OPEN_DOCUMENT_TREE;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;


/** A controller and configurer of those models that can introduce changes read from the user’s local
  * wayrepo, which are yet unknown to public sources, thus anticipating a future state.
  */
@ThreadRestricted("app main") final class WayrepoPreviewController extends LinearLayout
{
    /* * *
    - designed for possible use in (among other contexts) a "Refresh" dialogue that floats,
      allowing previews to show behind it
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
            [ note view
                ( text view
                - shows by default the time lapsed since last successful refresh
        - only pieces of this layout are test-coded below
      */


    /** Constructs a WayrepoPreviewController.
      */
    WayrepoPreviewController( final Wayranging wr )
    {
        super( /*context*/wr );
        setOrientation( VERTICAL );

        final WaykitUI wk = WaykitUI.i();
        final SharedPreferences preferences = wk.preferences();
        {
          // Wayrepo location view.
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            final TextView view = new TextView( wr );
            addView( view );
            preferences.registerOnSharedPreferenceChangeListener( new OnSharedPreferenceChangeListener()
            {
                {
                    relay(); // init
                    Object unregistrationAgent = wr.unregisterOnDestruction( this );
                      // no need to unregister the agent itself, its registry does not outlive it
                }
                private void relay()
                {
                    String text = wk.wayrepoTreeLoc();
                    if( text == null ) text = "location unspecified";
                    view.setText( text );
                }
                public void onSharedPreferenceChanged( SharedPreferences _p, String _key ) { relay(); }
            });
        }
        {
            final LinearLayout x = new LinearLayout( wr );
            addView( x );
            {
              // Location clear button, to clear the wayrepo location.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( wr );
                x.addView( button );
                button.setText( "Clear" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v ) { wk.wayrepoTreeLoc( null ); }
                });
            }
            {
              // Location finder button, to open the finder and locate the wayrepo.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( wr );
                x.addView( button );
                button.setText( "Locate wayrepo" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v )
                    {
                        final Intent request;
                     // try
                     // {
                            request = new Intent( /*[SAF]*/ACTION_OPEN_DOCUMENT_TREE ); // or:
                         // request = new Intent( /*[SAF]*/ACTION_OPEN_DOCUMENT ); // simple doc TEST part 1/2
                         // request.addCategory( android.content.Intent.CATEGORY_OPENABLE );
                         // request.setType( "*/*" ); // setType or throws ActivityNotFoundException
                         /// only to a) doc this request, and b) regression test it in external SMBProvider app
                     // }
                     // catch( final ActivityNotFoundException x ) { throw new RuntimeException( x ); }
                     //// but ActivityNotFoundException *is* a RuntimeException
                        wr.startActivityForResult( request, new WayrepoLocator() );
                    }
                });
            }
        }
        {
            final LinearLayout x = new LinearLayout( wr );
            addView( x );
            {
              // Local refresh button, to refresh from local wayrepo.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( wr );
                x.addView( button );
                button.setText( "Refresh from wayrepo" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v )
                    {
                        wr.forests().startRefreshFromWayrepo( wk.wayrepoTreeLoc() );
                    }
                });
                preferences.registerOnSharedPreferenceChangeListener( new OnSharedPreferenceChangeListener()
                {
                    {
                        relay(); // init
                        Object unregistrationAgent = wr.unregisterOnDestruction( this );
                          // no need to unregister the agent itself, its registry does not outlive it
                    }
                    private void relay() { button.setEnabled( wk.wayrepoTreeLoc() != null ); }
                      // hint to user that refreshing from a non-existent wayrepo is pointless
                    public void onSharedPreferenceChanged( SharedPreferences _p, String _key ) { relay(); }
                });
            }
            {
              // Full refresh button, to refresh from all sources.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                final Button button = new Button( wr );
                x.addView( button );
                button.setText( "from all" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _v ) { wr.forests().startRefresh( wk.wayrepoTreeLoc() ); }
                });
            }
        }

      // Note view.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final TextView noteView = new TextView( wr );
        wr.forests().notaryBell().register( new Auditor<Changed>()
        { // no need to unregister, registry does not outlive this registrant
            { relay(); } // init
            private void relay() { noteView.setText( wr.forests().refreshNote() ); }
            public void hear( Changed _ding ) { relay(); }
        });
        addView( noteView );
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final java.util.logging.Logger logger = LoggerX.getLogger( WayrangingV.class );



   // ==================================================================================================


    private static final class WayrepoLocator extends ActivityResultReceiver
    {

        public static final SimpleCreator<WayrepoLocator> CREATOR = new SimpleCreator<WayrepoLocator>()
        {
            public WayrepoLocator createFromParcel( Parcel _in ) { return new WayrepoLocator(); }
        };


        void receive( final int resultCode, final Intent result )
        {
            if( resultCode != RESULT_OK )
            {
                if( resultCode != RESULT_CANCELED )
                {
                    logger.warning( "Unrecognized activity result code: " + resultCode );
                }
                return;
            }

            if( result == null ) // example implies null is possible, https://developer.android.com/guide/topics/providers/document-provider.html#client
            {
                logger.warning( "Activity result is null" );
                return;
            }

            final Uri uri = result.getData();
            WaykitUI.i().wayrepoTreeLoc( uri ); // or:
         // System.out.println( "URI of selected document: " + uri ); // simple doc TEST part 2/2
        }

    }


}


// Notes
// -----
//  [SAF] Storage Access Framework
//      https://developer.android.com/guide/topics/providers/document-provider.html
