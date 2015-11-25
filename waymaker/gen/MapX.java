package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import java.util.HashMap;


/** Utilities for working with maps.
  */
public @ThreadSafe final class MapX
{

    private MapX() {}



    /** Calulates an <code>initialCapacity</code> for the construct “<code>{@linkplain
      * HashMap#HashMap(int,float) HashMap}( initialCapacity, {@linkplain #HASH_LOAD_FACTOR
      * HASH_LOAD_FACTOR} )</code>” that will suffice to allow the map to grow to
      * <code>expectedMaximumSize</code> without a rehash.
      */
    public static int hashCapacity( final int expectedMaximumSize )
    {
        final int rehashSize = expectedMaximumSize + 1; // not expected to be reached
        final float sufficientF = rehashSize / HASH_LOAD_FACTOR;
        int sufficient = (int)Math.ceil( sufficientF );
        ++sufficient; // to be sure
        return sufficient;
    }



    /** A generally good {@linkplain HashMap load factor} for a hash map: {@value}.
      */
    public static final float HASH_LOAD_FACTOR = 0.75f; // the default for HashMap, last I looked


}
