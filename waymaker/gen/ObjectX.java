package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.


/** Utilities for working with {@linkplain Object objects}.
  */
public @ThreadSafe final class ObjectX
{

    private ObjectX() {}



    /** Answers whether the given objects are equal in the sense either of both being null, or
      * <code>o1.{@linkplain Object#equals(Object) equals}(o2)</code>.
      */
    public static boolean equals( final Object o1, final Object o2 )
    {
        if( o1 == null ) return o2 == null;

        return o1.equals( o2 );
    }


}
