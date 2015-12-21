package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import waymaker.gen.ThreadSafe;
import waymaker.spec.VotingID;


/** An implementation of a barred rootward cast.
  */
public final @ThreadSafe class RootwardCastB<C extends Node> implements RootwardCast<C>
{


    /** Contructs a RootwardCastB.
      *
      *     @see #votedID()
      */
    public RootwardCastB( C _ground, VotingID _votedID )
    {
        ground = _ground;
        votedID = _votedID;
        if( ground.id() != null ) throw new IllegalArgumentException( "Ground has malformed ID" );

        if( votedID == null ) throw new NullPointerException(); // cannot bar a non-vote
    }



   // - R o o t w a r d - C a s t ----------------------------------------------------------------------


    public C candidate() { return ground; }



    public boolean isBarred() { return true; }



    public VotingID votedID() { return votedID; }


        final VotingID votedID;



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final C ground;


}
