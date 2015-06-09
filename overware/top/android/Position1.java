package overware.top.android; // Copyright 2015, Michael Allan.

import java.util.*;


/** An implementation of a position.  Currently it is a null implementation as explained
  * in {@linkplain Poll1 Poll1}.
  */
final class Position1 implements Position
{


    /** Constructs a Position1.
      *
      *     @see #poll()
      */
    Position1( Poll _poll ) { poll = _poll; }



   // - P o s i t i o n ------------------------------------------------------------------


    public Position candidate() { return poll.ground(); }



    public void fetchVoters( int _threshold )
    {
     // if( !votersMaybeIncomplete() || voters.size() >= threshold ) return;
     //// always the case in this null implementation
    }



    public Poll poll() { return poll; }


        private final Poll poll;



    public List<Position> voters() { return voters; }


        private final List<Position> voters = Collections.emptyList();



    public boolean votersMaybeIncomplete() { return false; }


}
