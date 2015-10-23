package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import java.util.*;
import overware.spec.VotingID;


/** An empty placeholder for an original node that is absent in the guideway count, but then arrives in
  * the precount.  It {@linkplain #rootwardInThis() casts rootward} to the unadjusted ground but is
  * unlisted among the {@linkplain UnadjustedGround#voters() voters there} because it serves only to
  * attach the model of its arrival: its {@linkplain #precounted() precount-adjustable counterpart},
  * which may {@linkplain #rootwardInPrecount() cast rootward} to a precount node (ground or other) and
  * then <em>will be</em> listed among the {@linkplain PrecountNode#voters() voters there}.
  */
final class UnadjustedNode0 extends UnadjustedNode
{


    /** Constructs an UnadjustedNode0.
      */
    UnadjustedNode0( final VotingID _id, final UnadjustedGround ground )
    {
        id = _id;
        rootwardInThis = ground.rootwardHither_getOrMake();
        if( id == null ) throw new NullPointerException(); // fail fast
    }



    /** Contructs an UnadjustedNode0 and adds it to precounter.{@linkplain Precounter#nodeMap()
      * nodeMap}.
      *
      *     @see #id()
      */
    static UnadjustedNode0 makeMapped( final VotingID id, final Precounter precounter )
    {
        final UnadjustedNode0 una = new UnadjustedNode0( id, precounter.ground() );
        precounter.nodeMap().put( id, una );
        return una;
    }



    /** Contructs an UnadjustedNode0 and adds it to precounter.{@linkplain Precounter#nodeMap()
      * nodeMap}, also adding its {@linkplain #precounted() precount-adjustable counterpart}.
      *
      *     @see #id()
      */
    static UnadjustedNode0 makeMappedPrecounted( final VotingID id, final Precounter precounter )
    {
        final UnadjustedNode0 una = makeMapped( id, precounter );
        new PrecountNode1( una );
        return una;
    }



   // - O b j e c t ------------------------------------------------------------------------------------


    public @Override String toString() { return id.toString(); }



   // - N o d e ----------------------------------------------------------------------------------------


    public VotingID id() { return id; }


        private final VotingID id;



    public boolean isGround() { return false; }



    public final int peerOrdinal() { return Integer.MAX_VALUE; } // but shouldn't matter for this placeholder



    public List<? extends UnadjustedNode1> voters() { return Collections.emptyList(); }



    public boolean votersMaybeIncomplete() { return false; }



    public int votersNextOrdinal() { return Integer.MAX_VALUE; }



   // - U n a d j u s t e d - N o d e ------------------------------------------------------------------


    public RootwardCast<UnadjustedNode> rootwardHither_getOrMake()
    {
        throw new UnsupportedOperationException( "Cannot cast vote to non-existent node" );
    }



    public RootwardCast<UnadjustedNode> rootwardInThis() { return rootwardInThis; }


        private final RootwardCast<UnadjustedNode> rootwardInThis;


}
