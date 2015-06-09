package overware.gen; // Copyright 2015, Michael Allan.  Licence MIT-Overware.


/** More utilities for working with arrays.
  *
  *     @see java.util.Arrays
  */
public @ThreadSafe final class ArraysX
{

    private ArraysX() {}



    /** Creates a generically typed array.  This is a convenience method.
      */
    public static @SuppressWarnings("unchecked") <E> E[] newArray( final int size )
    {
        final Object[] newArray;
        if( size == 0 ) newArray = ObjectX.EMPTY_OBJECT_ARRAY; // spare the unecessary garbage
        else newArray = new Object[size];
        return (E[])newArray;
    }


}
