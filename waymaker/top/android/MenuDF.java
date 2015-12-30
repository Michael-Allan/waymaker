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
          // - - - - - - - - - - - - - - - - - -
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
          // Vote control summoner.
          // - - - - - - - - - - - -
            final Button button = new Button( context );
            y.addView( button );
            button.setText( "Vote…" );
            button.setEnabled( false ); // not yet coded
              /* * *
                - vote control dialogue to float non-modally
                    - allowing actionable cues
                        - e.g. when this summoner (menu summoner) is subject-coloured
                            - e.g. when user is virgin, so chosen node = voted node = null
                            " Choose a candidate from the forest at left
                */
        }
        {
          // Logging test button, to log test messages at all standard logging levels.
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            final Button button = new Button( context );
            y.addView( button );
            button.setText( "Test logging" );
            button.setOnClickListener( new View.OnClickListener()
            {
                public void onClick( View _src ) { LoggerX.test( logger ); }
            });
        }
        {
            LinearLayout x;

          // Generic test buttons.
          // - - - - - - - - - - - -
            y.addView( x = new LinearLayout( context ));
            {
                final Button button = new Button( context );
                x.addView( button );
                button.setText( "Change poll" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( final View src )
                    {
                        final BelledVariable<String> pollNamer = ((Wayranging)src.getContext()).pollNamer();
                        final String otherName = "end".equals(pollNamer.get())? "wk":"end";
                        pollNamer.set( otherName );
                    }
                });
            }
            {
                final Button button = new Button( context );
                x.addView( button );
                button.setText( "Extend roots" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( final View src )
                    {
                        final Forest forest = ((Wayranging)src.getContext()).forester().forest();
                        new ServerCount( forest.pollName() ).
                          enqueuePeersRequest( null/*ground*/, forest, /*paddedLimit*/0 );
                    }
                });
            }
            y.addView( x = new LinearLayout( context ));
            {
                final Button button = new Button( context );
                x.addView( button );
                button.setText( "X" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( final View src )
                    {
                    }
                });
            }
            {
                final Button button = new Button( context );
                x.addView( button );
                button.setText( "Y" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( final View src )
                    {
                    }
                });
            }
            {
                final Button button = new Button( context );
                x.addView( button );
                button.setText( "Z" );
                button.setOnClickListener( new View.OnClickListener()
                {
                    public void onClick( final View src )
                    {
                    }
                });
            }
        }
        {
          // About.
          // - - - -
            final Button button = new Button( context );
            y.addView( button );
            button.setText( "About" );
            button.setEnabled( false ); // not yet coded
        }
        return y;
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final java.util.logging.Logger logger = LoggerX.getLogger( MenuDF.class );


}
