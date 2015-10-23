package overware.gen; // Copyright 2015, Michael Allan.  Licence MIT-Overware.


/** Utilities for working with {@linkplain Object objects}.
  */
public @ThreadSafe final class ObjectX
{

    private ObjectX() {}



    /** Answers whether both objects are equal in the sense of
      * o1.{@linkplain Object#equals(Object) equals}(o2), or both are null.
      */
    public static @ThreadRestricted("as for o1.equals") boolean equals( final Object o1, final Object o2 )
    {
        if( o1 == null ) return o2 == null;

        return o1.equals( o2 );
    }


}
