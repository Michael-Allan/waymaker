package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import waymaker.gen.ThreadSafe;
import waymaker.spec.VotingID;


/** An implementation of an unbarred rootward cast.
  */
final @ThreadSafe class RootwardCastU<C extends Node> implements RootwardCast<C>
{


    /** Contructs a RootwardCastU.
      *
      *     @see #candidate()
      */
    RootwardCastU( final C candidate )
    {
        if( candidate == null ) throw new NullPointerException();

        this.candidate = candidate;
    }



   // - R o o t w a r d - C a s t ----------------------------------------------------------------------


    public C candidate() { return candidate; }


        private final C candidate;



    public boolean isBarred() { return false; }



    public VotingID votedID() { return candidate.id(); }


}
