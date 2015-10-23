package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import overware.gen.ThreadSafe;
import overware.spec.VotingID;


/** An implementation of a proper unadjusted node.
  */
class UnadjustedNode1 extends UnadjustedNodeV
{


    /** Contructs an UnadjustedNode1.
      *
      *     @see #id()
      *     @see #peerOrdinal()
      *     @see #rootwardInThis()
      */
    @ThreadSafe UnadjustedNode1( VotingID id, int peerOrdinal, RootwardCast<UnadjustedNode> rootwardInThis )
    {
        super( id, peerOrdinal, rootwardInThis );
        assert id != null; // not ground
    }



    /** Contructs an UnadjustedNode1 with a {@linkplain #rootwardInThis() rootward cast} formed as
      * candidate.{@linkplain UnadjustedNode#rootwardHither_getOrMake() rootwardHither_getOrMake}.
      *
      *     @see #id()
      *     @see #peerOrdinal()
      */
    UnadjustedNode1( final VotingID id, final int peerOrdinal, final UnadjustedNode candidate )
    {
        super( id, peerOrdinal, candidate.rootwardHither_getOrMake() );
        assert id != null; // not ground
    }



   // - N o d e ----------------------------------------------------------------------------------------


    public boolean isGround() { return false; }



   // - O b j e c t ------------------------------------------------------------------------------------


    public @Override String toString() { return id().toString(); }


}
