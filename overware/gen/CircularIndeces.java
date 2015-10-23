package overware.gen; // Copyright 2015, Michael Allan.  Licence MIT-Overware.


/** A pair of indeces to treat an array as a circular buffer.  Here an array of length 10, for example,
  * uses circular indeces to buffer the characters ‘a’ to ‘e’:<pre>
  *
  *        write
  *          :
  *   [d][e][ ][ ][ ][ ][ ][a][b][c]
  *                         :
  *                        read</pre>
  */
public final class CircularIndeces
{


    /** Contructs a CircularIndeces.
      *
      *     @see #capacity()
      *     @throws IllegalArgumentException if the given capacity does not exceed zero.
      */
    public @ThreadSafe CircularIndeces( final int _capacity )
    {
        if( _capacity <= 0 ) throw new IllegalArgumentException();

        capacity = _capacity;
        max = capacity - 1;
    }



   // --------------------------------------------------------------------------------------------------


    /** Returns the given index incremented in circular fashion, which means returning zero if the
      * result would exceed the maximum value.  Does no other bounds checking.
      *
      *     @throws AssertionError if assertions are enabled and the given index is out of bounds.
      */
    public int advancing( final int i )
    {
        assert i >= 0 && i <= max;
        return i < max? i + 1: 0;
    }



    /** The maximum size of the buffer, which is the length of the indexed array.
      */
    public int capacity() { return capacity; }


        private final int capacity;



    /** Sets the read and write indeces to the same value, and the size to zero.
      */
    public void clear()
    {
        read = write;
        size = 0;
    }



 // /** The index to read from.
 //   */
 // int read() { return read; }


        private int read;



    /** Returns the given index decremented in circular fashion, which means returning the maximum value
      * if the result would be less than zero.  Does no other bounds checking.
      *
      *     @throws AssertionError if assertions are enabled and the given index is out of bounds.
      */
    public int retreating( final int i )
    {
        assert i >= 0 && i <= max;
        return i > 0? i - 1: max;
    }



    /** Returns the circularized difference between the read and write indeces, which is the number of
      * unread elements in the buffer.
      */
    public int size() { return size; }


        private int size;



    /** Retreats the write index and returns its new value.  Use this method to implement an unwrite
      * operation on the buffer.  Or equivalently a tail unqueue, for example.  Or a pop from a stack.
      *
      *     @throws IndexOutOfBoundsException if the size is already at zero.
      */
    public int unwriting()
    {
        if( size == 0 ) throw new IndexOutOfBoundsException();

        write = retreating( write );
        --size;
        return write;
    }



    /** The index to write to.
      */
    int write() { return write; }


        private int write;



    /** Advances the write index and returns its original value, also advancing the read index as
      * necessary to prevent a boundary exception.  Use this method to implement a write operation on
      * the buffer that allows for overwriting of the oldest, unread value.  Or equivalently a tail
      * enqueue, for example, that allows for head overflow.  Or an overflowing push to a stack.
      */
    public int writingOver()
    {
        final int writing = write;
        if( size < capacity )
        {
            write = advancing( write );
            ++size;
        }
        else
        {
            assert size == capacity && read == write;
            write = advancing( write );
            read = write;
        }
        return writing;
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final int max; // maximum value of an index, capacity - 1


}
