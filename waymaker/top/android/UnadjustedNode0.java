package waymaker.top.android; // Copyright © 2015 Michael Allan.  Licence MIT.

import java.util.*;
import waymaker.spec.VotingID;


/** An empty placeholder for an original node that is absent in the server count, but then arrives in
  * the precount.  It {@linkplain #rootwardInThis() casts rootward} to the unadjusted ground but is
  * unlisted among the {@linkplain UnadjustedGround#voters() voters there} because it serves only to
  * attach the model of its arrival: its {@linkplain #precounted() precount-adjustable counterpart},
  * which may {@linkplain #rootwardInPrecount() cast rootward} to a precount node (ground or other) and
  * then <em>will be</em> listed among the {@linkplain PrecountNode#voters() voters there}.
  */
public final class UnadjustedNode0 extends UnadjustedNode
{


    /** Constructs an UnadjustedNode0.
      */
    public UnadjustedNode0( final VotingID _id, final UnadjustedGround ground )
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
    public static UnadjustedNode0 makeMapped( final VotingID id, final Precounter precounter )
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
    public static UnadjustedNode0 makeMappedPrecounted( final VotingID id, final Precounter precounter )
    {
        final UnadjustedNode0 una = makeMapped( id, precounter );
        new PrecountNode1( una );
        return una;
    }



   // - O b j e c t ------------------------------------------------------------------------------------


    public @Override String toString() { return id.toString(); }



   // - C o u n t - N o d e ----------------------------------------------------------------------------


    public VotingID id() { return id; }


        private final VotingID id;



    public boolean isGround() { return false; }



    public final int peerOrdinal() { return Integer.MAX_VALUE; } // but shouldn't matter for this placeholder



    public List<? extends UnadjustedNode> voters() { return Collections.emptyList(); }



    public boolean votersMaybeIncomplete() { return false; }



    public int votersNextOrdinal() { return Integer.MAX_VALUE; }



    public Waynode1 waynode() { return Waynode.EMPTY_WAYNODE; }



   // - U n a d j u s t e d - N o d e ------------------------------------------------------------------


    public RootwardCast<UnadjustedNode> rootwardHither_getOrMake()
    {
        throw new UnsupportedOperationException( "Cannot cast vote to non-existent node" );
    }



    public RootwardCast<UnadjustedNode> rootwardInThis() { return rootwardInThis; }


        private final RootwardCast<UnadjustedNode> rootwardInThis;


}
