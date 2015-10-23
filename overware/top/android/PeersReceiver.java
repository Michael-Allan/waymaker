package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import overware.gen.ThreadSafe;


/** A receiver of a response to a
  * {@linkplain GuidewayCount#enqueuePeersRequest(VotingID,PeersReceiver,int) peers request}.
  */
interface PeersReceiver
{


   // - P e e r s - R e c e i v e r --------------------------------------------------------------------


    /** Receives the response from the remote guideway.
      */
    public @ThreadSafe void receivePeersResponse( Object in );


}
