package overware.gen; // Copyright 2008, 2015, Michael Allan.  Licence MIT-Overware.

import java.util.*;


/** An unmodifiable list backed by an array.  It is unmodifiable in the sense that it provides no means
  * to access and modify the backing array; not even the {@linkplain #set(int,Object) set operation}
  * provided by Arrays.{@linkplain java.util.Arrays#asList(Object[]) asList}.  Direct modification of
  * the array by other means, however, could still affect the list.
  *
  *     @see java.util.concurrent.CopyOnWriteArrayList
  *     @see CopyOnResizeArrayList
  */
public final class ListOnArray<E> extends AbstractList<E> implements RandomAccess
{


    /** Contructs a ListOnArray.
      *
      *     @param elements The elements of the backing array, or the backing array itself.
      */
    public @SafeVarargs @SuppressWarnings("varargs") ListOnArray( final E... elements )
    {
        if( elements == null ) throw new NullPointerException(); // fail fast

        this.elements = elements;
    }



   // - C o l l e c t i o n ----------------------------------------------------------------------------


    public int size() { return elements.length; }



   // - L i s t ----------------------------------------------------------------------------------------


    public E get( final int e ) { return elements[e]; }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final E[] elements;


}
