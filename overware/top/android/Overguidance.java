package overware.top.android;

import android.widget.TextView;


public final class Overguidance extends android.app.Activity
{

    public void onCreate( final android.os.Bundle state )
    {
        super.onCreate( state );
        final TextView v = new TextView( Overguidance.this );
     // v.setText( "This will be a UI for overguideways." ); // TEST
        v.setText( overware.spec.Test.TEST );
        setContentView( v );
    }


}
