package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.widget.TextView;
import waymaker.gen.*;


final class QuestionTexter extends QuestionSyncher
{


    @Warning("Wayranging co-construct") QuestionTexter( final TextView view )
    {
        super( (Wayranging)view.getContext() ); // wr co-construct
        this.view = view;
        sync();
    }



   // - A u d i t o r ----------------------------------------------------------------------------------


    public void hear( Changed _ding )
    {
        final Waynode _leaderWaynode = leaderWaynode( wr );
        if( _leaderWaynode == leaderWaynode ) return;

        leaderWaynode = _leaderWaynode;
        sync();
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private Waynode leaderWaynode = leaderWaynode( wr );



    void sync() { view.setText( leaderWaynode.question() ); }



    private final TextView view;


}
