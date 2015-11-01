package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import java.util.*;


/** An unmodifiable iterator backed by an array.  It is unmodifiable in the sense that it provides no
  * means to access and modify the backing array during iteration.  Modification of the array by other
  * means could still affect the iteration.
  */
public final class IteratorOnArray<E> implements ListIterator<E>
{


    /** Contructs an IteratorOnArray.
      *
      *     @param elements The elements of the backing array, or the backing array itself.
      */
    public @SafeVarargs @SuppressWarnings("varargs") IteratorOnArray( final E... elements )
    {
        if( elements == null ) throw new NullPointerException(); // fail fast

        this.elements = elements;
    }



    /** Contructs an IteratorOnArray.
      *
      *     @see #nextIndex()
      *     @param elements The elements of the backing array, or the backing array itself.
      */
      @SafeVarargs @SuppressWarnings("varargs")
    public IteratorOnArray( final int nextIndex, final E... elements )
    {
        this( elements );
        this.nextIndex = nextIndex;
    }



   // - I t e r a t o r --------------------------------------------------------------------------------


    public boolean hasNext() { return nextIndex < elements.length; }



    public E next()
    {
        final E element;
        try { element = elements[nextIndex]; }
        catch( final ArrayIndexOutOfBoundsException x ) { throw new NoSuchElementException( x.getMessage() ); }

        ++nextIndex;
        return element;
    }



    public void remove() { throw new UnsupportedOperationException(); }



   // - L i s t - I t e r a t o r ----------------------------------------------------------------------


    public void add( E _element ) { throw new UnsupportedOperationException(); }



    public boolean hasPrevious() { return nextIndex > 1; } // ePrevious >= 0



    public int nextIndex() { return nextIndex; }


        private int nextIndex;



    public E previous()
    {
        final E element;
        try { element = elements[previousIndex()]; }
        catch( final ArrayIndexOutOfBoundsException x ) { throw new NoSuchElementException( x.getMessage() ); }

        --nextIndex;
        return element;
    }



    public int previousIndex() { return nextIndex - 1; }



    public void set( E element ) { throw new UnsupportedOperationException(); }
      // for a version that supports this, see ._/SettableIteratorOnArray



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final E[] elements;


}
