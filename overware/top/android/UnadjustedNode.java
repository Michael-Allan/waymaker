package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import java.util.List;
import overware.gen.ThreadSafe;


/** An original node as read from a guideway count engine, not adjusted by any
  * {@linkplain Precounter precount}.
  */
abstract class UnadjustedNode implements Node
{


    @ThreadSafe UnadjustedNode() {}



   // --------------------------------------------------------------------------------------------------


    /** A version of this node adjusted to reflect changes introduced by a {@linkplain Precounter
      * precount}, or null if no precount introduced changes.
      */
    PrecountNode precounted() { return precounted; }


        private PrecountNode precounted;
          // persisted by Forest.Cache.groundUna stator via precount node constructors


        /** Sets the precount-adjusted version of this node.
          *
          *     @throws IllegalStateException if a precount-adjusted version was already set.
          *     @throws IllegalArgumentException if precountedNew.{@linkplain PrecountNode#unadjusted()
          *       unadjusted} does not equal this node.
          */
        void precounted( final PrecountNode _precounted )
        {
            if( precounted != null ) throw new IllegalStateException( "A value was already set" );

            if( _precounted.unadjusted() != this ) throw new IllegalArgumentException();

            precounted = _precounted;
        }



    /** Returns the shared instance of an unbarred cast to this node, first creating it if necessary.
      */
    abstract RootwardCast<UnadjustedNode> rootwardHither_getOrMake();



   // - N o d e ----------------------------------------------------------------------------------------


    public final RootwardCast<? extends Node> rootwardInPrecount()
    {
        return precounted == null? rootwardInThis(): precounted.rootwardInThis();
    }



    public abstract @Override RootwardCast<UnadjustedNode> rootwardInThis();



    public abstract @Override List<? extends UnadjustedNode> voters();


}
