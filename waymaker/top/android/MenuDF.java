package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.app.*;
import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import waymaker.gen.*;


/** A dialogue showing the general menu of the waykit user interface.
  */
public @ThreadRestricted("app main") final class MenuDF extends DialogFragment // grep AutoRestore-public
{


   // - F r a g m e n t --------------------------------------------------------------------------------


    public @Override View onCreateView( LayoutInflater _inf, ViewGroup _group, Bundle _in )
    {
        final Context context = getActivity(); // in lieu of API level 23 getContext
        final LinearLayout y = new LinearLayout( context );
        y.setOrientation( LinearLayout.VERTICAL );
        {
          // Wayrepo preview control summoner.
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            final Button button = new Button( context );
            y.addView( button );
            button.setText( "Control wayrepo preview…" );
            button.setOnClickListener( new View.OnClickListener()
            {
                public void onClick( View _src )
                {
                    new WayrepoPreviewControlDF().show( getFragmentManager(), /*fragment tag*/null );
                }
            });
        }
        {
          // Logging test button, to log test messages at all standard logging levels.
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            final Button button = new Button( context );
            y.addView( button );
            button.setText( "Test logging" );
            button.setOnClickListener( new View.OnClickListener()
            {
                public void onClick( View _src ) { LoggerX.test( logger ); }
            });
        }
        {
          // Generic test button.
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            final Button button = new Button( context );
            y.addView( button );
            button.setText( "Extend roots" );
            button.setOnClickListener( new View.OnClickListener()
            {
                public void onClick( final View src )
                {
                    final Wayranging wr = (Wayranging)src.getContext();
                    final String pollName = "end";
                    final Forest forest = wr.forests().get( pollName );
                    new ServerCount(pollName).enqueuePeersRequest( null/*ground*/, forest, /*paddedLimit*/0 );
                }
            });
        }
        return y;
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final java.util.logging.Logger logger = LoggerX.getLogger( MenuDF.class );


}
