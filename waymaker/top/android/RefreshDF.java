package waymaker.top.android; // Copyright © 2015 Michael Allan.  Licence MIT.

import android.app.Dialog;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import waymaker.gen.*;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_OPEN_DOCUMENT_TREE;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;


/** A one-shot, disposable dialogue that shows a refresh facility.
  */
  @ThreadRestricted("app main")
public final class RefreshDF extends android.app.DialogFragment // grep AutoRestore-public
{


    /** The title of this dialogue.
      */
    public static final String TITLE = "Refresh";



   // - F r a g m e n t --------------------------------------------------------------------------------


    public @Override Dialog onCreateDialog( final Bundle in )
    {
        final Dialog dialog = super.onCreateDialog( in );
        dialog.setTitle( RefreshDF.TITLE );
        return dialog;
    }



    public @Override View onCreateView( LayoutInflater _inf, ViewGroup _group, Bundle _in )
    {
        final Wayranging wr = wr();
        final LinearLayout y = new LinearLayout( wr );
        y.setOrientation( LinearLayout.VERTICAL );

      // Refresh notes.
      // - - - - - - - -
        {
            final TextView noteView = new TextView( wr );
            y.addView( noteView );
            wr.forests().notaryBell().registerDestructibly( new Auditor<Changed>()
            {
                // only forest cache writes refresh notes at present, so let them stand for all
                { sync(); } // init
                private void sync() { noteView.setText( wr().forests().refreshNote() ); }
                public void hear( Changed _ding ) { sync(); }
            }, destructor );
        }

      // General refresh button, to refresh from all sources.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - -
        {
            final Button button = new Button( wr );
            y.addView( button );
            button.setText( "From all sources" );
            button.setOnClickListener( new View.OnClickListener()
            {
                public void onClick( View _src ) { wr().refreshFromAllSources(); }
            });
        }

      // Local refresh button, to refresh from user's local wayrepo.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final SharedPreferences preferences = WaykitUI.i().preferences();
        {
            final Button button = new Button( wr );
            y.addView( button );
            button.setText( "From local wayrepo" );
            button.setOnClickListener( new View.OnClickListener()
            {
                public void onClick( View _src ) { wr().refreshFromLocalWayrepo(); }
            });
            Android.registerDestructibly( preferences, new OnSharedPreferenceChangeListener()
            {
                { sync(); } // init
                private void sync() { button.setEnabled( WaykitUI.i().wayrepoTreeLoc() != null ); }
                  // hint to user that refreshing from a non-existent wayrepo is useless
                public void onSharedPreferenceChanged( SharedPreferences _p, String _key ) { sync(); }
            }, destructor );
        }
        /* * *
        = also allow trigger of refresh by impatience gesture on refreshable view itself
            - such as deselection with immediate reselection
            - scope of refresh escalates on immediate repeat of impatience gesture
                ( notebook 2015.6.4
                - 1st gesture refreshes locally
                - 2nd gesture refreshes generally
          */

      // Local wayrepo location.
      // - - - - - - - - - - - - -
        {
            final TextView view = new TextView( wr );
            y.addView( view );
            Android.registerDestructibly( preferences, new OnSharedPreferenceChangeListener()
            {
                { sync(); } // init
                private void sync()
                {
                    final WaykitUI wk = WaykitUI.i();
                    final StringBuilder b = wk.stringBuilderClear();
                    b.append( "Local wayrepo: " );
                    final String loc = wk.wayrepoTreeLoc();
                    b.append( loc == null? "Location unspecified": loc );
                    view.setText( b.toString() );
                }
                public void onSharedPreferenceChanged( SharedPreferences _p, String _key ) { sync(); }
            }, destructor );
        }
        {
            final LinearLayout x = new LinearLayout( wr );
            y.addView( x );
            x.setGravity( Gravity.RIGHT );

          // Clear button.
          // - - - - - - - -
            {
                final Button button = new Button( wr );
                x.addView( button );
                button.setText( "Clear" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _src ) { WaykitUI.i().wayrepoTreeLoc( null ); }
                });
            }

          // Finder button.
          // - - - - - - - -
            {
                final Button button = new Button( wr );
                x.addView( button );
                button.setText( "Find" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( View _src )
                    {
                        final Intent request;
                     // try
                     // {
                            request = new Intent( /*[SAF], minSdkVersion 21*/ACTION_OPEN_DOCUMENT_TREE );

                         // OR:
                         // request = new Intent( /*[SAF]*/ACTION_OPEN_DOCUMENT ); // simple doc TEST part 1/2
                         // request.addCategory( android.content.Intent.CATEGORY_OPENABLE );
                         // request.setType( "*/*" ); // setType or throws ActivityNotFoundException
                         /// only to a) doc this request type, and b) regression test it in external SMBProvider app
                     // }
                     // catch( final ActivityNotFoundException x ) { throw new RuntimeException( x ); }
                     //// but ActivityNotFoundException *is* a RuntimeException
                        wr().startActivityForResult( request, new WayrepoLocator() );
                    }
                });
            }
        }
        return y;
    }



    public @Override void onDestroyView()
    {
        destructor.close();
        super.onDestroyView();
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final Destructor destructor = new Destructor1();



    private static final java.util.logging.Logger logger = LoggerX.getLogger( WayrangingV.class );



    private Wayranging wr() { return (Wayranging)getActivity(); }



   // ==================================================================================================


    private static final class WayrepoLocator extends ActivityResultReceiver
    {

        public static final SimpleCreator<WayrepoLocator> CREATOR = new SimpleCreator<WayrepoLocator>()
        {
            public WayrepoLocator createFromParcel( Parcel _in ) { return new WayrepoLocator(); }
        };


        public void receive( final int resultCode, final Intent result )
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
            WaykitUI.i().wayrepoTreeLoc( uri ); // OR:
         // System.out.println( "URI of selected document: " + uri ); // simple doc TEST part 2/2
        }

    }


}


// Notes
// -----
//  [SAF] Storage Access Framework
//      https://developer.android.com/guide/topics/providers/document-provider.html
