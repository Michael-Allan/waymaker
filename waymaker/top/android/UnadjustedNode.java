package waymaker.top.android; // Copyright © 2015 Michael Allan.  Licence MIT.

import java.util.List;
import waymaker.gen.ThreadSafe;


/** An original node as read from a server count engine, not adjusted by any
  * {@linkplain Precounter precount}.
  */
public abstract class UnadjustedNode implements CountNode
{


    public @ThreadSafe UnadjustedNode() {}



   // --------------------------------------------------------------------------------------------------


    /** A version of this node adjusted to reflect changes introduced by a {@linkplain Precounter
      * precount}, or null if no precount introduced changes.
      */
    public PrecountNode precounted() { return precounted; }


        private PrecountNode precounted;
          // persisted by Forest.Cache.groundUna stator via precount node constructors


        /** Sets the precount-adjusted version of this node.
          *
          *     @throws IllegalStateException if a precount-adjusted version was already set.
          *     @throws IllegalArgumentException if precountedNew.{@linkplain PrecountNode#unadjusted()
          *       unadjusted} does not equal this node.
          */
        public void precounted( final PrecountNode _precounted )
        {
            if( precounted != null ) throw new IllegalStateException( "A value was already set" );

            if( _precounted.unadjusted() != this ) throw new IllegalArgumentException();

            precounted = _precounted;
        }



    /** Returns the shared instance of an unbarred cast to this node, first creating it if necessary.
      */
    public abstract RootwardCast<UnadjustedNode> rootwardHither_getOrMake();



   // - C o u n t - N o d e ----------------------------------------------------------------------------


    public final RootwardCast<? extends CountNode> rootwardInPrecount()
    {
        return precounted == null? rootwardInThis(): precounted.rootwardInThis();
    }



    public abstract @Override RootwardCast<UnadjustedNode> rootwardInThis();



    public abstract @Override List<? extends UnadjustedNode> voters();



    public abstract @Override Waynode1 waynode();


}
