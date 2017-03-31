package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.

import java.util.*;


/** Utilities for working with lists.
  */
public @ThreadSafe final class ListX
{

    private ListX() {}



    /** Removes the e’th element of the list by removing and substituting the last element in its place.
      * Depending on the type of list, this may be faster than an ordinary removal.
      *
      *     @return The removed element.
      *     @throws AssertionError if assertions are enabled and the list does not implement
      *       RandomAccess, in which case this method is unlikely to be an efficient choice.
      */
    public static <E> E removeUroboros( final int e, final List<E> list )
    {
        assert list instanceof RandomAccess: "List supports random access";
        final int eLast = list.size() - 1;
        E element = list.remove( eLast );
        if( eLast != e ) element = list.set( e, element );
        return element;
    }


}
