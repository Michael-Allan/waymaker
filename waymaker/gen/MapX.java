package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.

import java.util.HashMap;


/** Utilities for working with maps.
  */
public @ThreadSafe final class MapX
{

    private MapX() {}



    /** Calulates an <code>initialCapacity</code> for the construction
      * “<code>new HashMap( initialCapacity, HASH_LOAD_FACTOR )</code>”
      * such that the map will grow to <code>expectedMaximumSize</code> without a rehash.
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
    public static final float HASH_LOAD_FACTOR = 0.75f; // the traditional default for HashMap


}
