package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import waymaker.gen.ThreadSafe;
import waymaker.spec.VotingID;


/** An implementation of a proper unadjusted node.
  */
public final class UnadjustedNode1 extends UnadjustedNodeV
{


    /** Contructs an UnadjustedNode1.
      *
      *     @see #id()
      *     @see #peerOrdinal()
      *     @see #rootwardInThis()
      */
      @ThreadSafe
    public UnadjustedNode1( final VotingID id, final int peerOrdinal,
      final RootwardCast<UnadjustedNode> rootwardInThis, final Waynode1 _waynode )
    {
        super( id, peerOrdinal, rootwardInThis );
        assert id != null; // not ground
        waynode = _waynode;
        WaykitUI.setRemotelyUsable(); // since one is actually constructed
    }



   // - N o d e ----------------------------------------------------------------------------------------


    public boolean isGround() { return false; }



    public Waynode1 waynode() { return waynode; }


        private final Waynode1 waynode;



   // - O b j e c t ------------------------------------------------------------------------------------


    public @Override String toString() { return id().toString(); }


}
