package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import waymaker.gen.ThreadSafe;


/** A receiver of a response to a
  * {@linkplain ServerCount#enqueuePeersRequest(VotingID,PeersReceiver,int) peers request}.
  */
public interface PeersReceiver
{


   // - P e e r s - R e c e i v e r --------------------------------------------------------------------


    /** Receives the response from the remote count server.
      */
    public @ThreadSafe void receivePeersResponse( Object in );


}
