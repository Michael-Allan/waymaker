package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import java.util.*;
import overware.gen.Warning;


/** A position that defers part of its implementation to a backing position.
  */
class Position2 implements Position
{


    /** Constructs a Position2.
      *
      *     @see #back
      */
    Position2( Position _back ) { back = _back; }



   // - P o s i t i o n ------------------------------------------------------------------


    public Position candidate() { return back.candidate(); }



    public final void fetchVoters( int _threshold ) { back.fetchVoters( _threshold ); }



    public final Poll poll() { return back.poll(); }



    public final List<Position> voters() { return back.voters(); }



    public final boolean votersMaybeIncomplete() { return back.votersMaybeIncomplete(); }



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    /** The backing position.  This field is exposed for the use of subclasses only; it
      * should not be used by other API clients.
      */
    protected final @Warning("non-API") Position back;


}
