package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import java.lang.reflect.Array;
import java.util.*;


/** A thread restricted variant of {@linkplain java.util.concurrent.CopyOnWriteArrayList
  * CopyOnWriteArrayList} that is non-blocking.  It retains the advantages of compactness and speed,
  * often being “more efficient than alternatives when traversal operations vastly outnumber mutations”.
  * Moreover {@linkplain #set(int,Object) set mutations} will not affect efficiency because this variant
  * copies the underlying array only for resize mutations: only {@linkplain #add(Object) add} and
  * {@linkplain #remove(Object) remove} are “implemented by making a fresh copy” of the array, not
  * {@linkplain #set(int,Object) set}.
  */
public abstract class CopyOnResizeArrayList<E> implements List<E>, RandomAccess
{


    public @ThreadSafe CopyOnResizeArrayList() {}



   // --------------------------------------------------------------------------------------------------


    /** Sets the elements by setting the underlying array, which is thenceforth owned by this list.
      */
    public void array( final E[] _array ) { array = _array; }


        private E[] array = emptyArray();



    private E[] array( final int length ) { return length == 0? emptyArray(): newArray(length); }


        /** Returns a zero-length array of the correct element type.
          */
        public abstract E[] emptyArray();


        /** Constructs a array of the correct element type, and a length of 1 or longer.
          *
          *     @param length The non-zero length of array to construct.
          */
        public abstract E[] newArray( int length );



   // - C o l l e c t i o n ----------------------------------------------------------------------------


    /** {@inheritDoc} <p>This method just calls <code>{@linkplain #add(int,Object) add}(size, addendum)</code>
      * and returns true.</p>
      */
    public boolean add( final E addendum )
    {
        add( array.length, addendum );
        return true;
    }



    /** {@inheritDoc} <p>This method just calls and returns
      * <code>{@linkplain #addAll(int,Collection) addAll}(size, addenda)</code>.</p>
      */
    public final boolean addAll( final Collection<? extends E> addenda )
    {
        return addAll( array.length, addenda );
    }



    public final void clear() { throw new UnsupportedOperationException( "Not yet coded" ); }



    public final boolean contains( final Object ob ) { return indexOf(ob) > 0; }



    public final boolean containsAll( Collection<?> col )
    {
        throw new UnsupportedOperationException( "Not yet coded" );
    }



    public final boolean isEmpty() { return array.length == 0; }



    /** {@inheritDoc}
      * This method just calls <code>{@linkplain #listIterator(int) listIterator}(0)</code>.
      */
    public final ListIterator<E> iterator() { return listIterator( 0 ); }



    public final boolean remove( final Object o )
    {
        final int e = indexOf( o );
        if( e < 0 ) return false;

        remove( e );
        return true;
    }



    public final boolean removeAll( Collection<?> col )
    {
        throw new UnsupportedOperationException( "Not yet coded" );
    }



    public final boolean retainAll( Collection<?> col )
    {
        throw new UnsupportedOperationException( "Not yet coded" );
    }



    public final int size() { return array.length; }



    public final Object[] toArray() { return array.clone(); }



    public final @SuppressWarnings("unchecked") <T> T[] toArray( T[] _array )
    {
        final int eN = array.length;
        final int _eN = _array.length;
        if( _eN > eN ) _array[eN] = null; // null terminate
        else if( _eN < eN ) _array = (T[])Array.newInstance( _array.getClass().getComponentType(), eN );
        System.arraycopy( array, 0, _array, 0, eN );
        return _array;
    }



   // - L i s t ----------------------------------------------------------------------------------------


    public void add( final int e, final E addendum )
    {
        grow( e, 1 );
        array[e] = addendum;
    }



    public boolean addAll( int e, final Collection<? extends E> addenda )
    {
        final int addendumCount = addenda.size();
        if( addendumCount == 0 ) return false;

        grow( e, addendumCount ); // all at once for speed
        for( final E addendum: addenda ) array[e++] = addendum;
        return true;
    }



    public final E get( final int e ) { return array[e]; }



    public final int indexOf( final Object ob )
    {
        if( ob == null ) { for( int e = 0; e < array.length; ++e ) if( array[e] == null ) return e; }
        else               for( int e = 0; e < array.length; ++e ) if( ob.equals( array[e] )) return e;
        return -1;
    }



    public final int lastIndexOf( final Object ob )
    {
        if( ob == null ) { for( int e = array.length - 1; e >=0; ++e ) if( array[e] == null ) return e; }
        else               for( int e = array.length - 1; e >=0; ++e ) if( ob.equals( array[e] )) return e;
        return -1;
    }



    /** {@inheritDoc}
      * This method just calls <code>{@linkplain #listIterator(int) listIterator}(0)</code>.
      */
    public final ListIterator<E> listIterator() { return listIterator( 0 ); }



    /** {@inheritDoc} <p>The returned iterator is based on a snapshot reference to the backing array.
      * It is similar in behaviour to the snapshot iterator of CopyOnWriteArrayList, except that it may
      * see underlying {@linkplain #set(int,Object) set mutations}.  The iterator itself has no mutation
      * methods in the current implementation, pending future need.</p>
      *
      *     @see java.util.concurrent.CopyOnWriteArrayList#listIterator(int)
      */
    public final ListIterator<E> listIterator( final int e ) { return new IteratorOnArray<>( e, array ); }



    public final E remove( final int e )
    {
        final int eN = array.length; // old
        final int _eN = eN - 1; // new
        final E[] _array = array( _eN );
        if( e > 0 ) System.arraycopy( array, 0, _array, 0, e );
        final E removal = array[e];
        if( e < _eN ) System.arraycopy( array, e + 1, _array, e, _eN - e );
        array = _array;
        return removal;
    }



    public E set( final int e, final E element )
    {
        final E elementWas = array[e];
        array[e] = element;
        return elementWas;
    }



    public final List<E> subList( int eFirst, int eEndBound )
    {
        throw new UnsupportedOperationException( "Not yet coded" );
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    /** Extends the list by addedSize then shifts the elements beginning at index e to the new end, thus
      * introducing a gap of length addedSize at e.
      */
    private void grow( final int e, final int addedSize )
    {
        final int eN = array.length; // old
        final int _eN = eN + addedSize; // new
        final E[] _array = array( _eN );
        if( e > 0 ) System.arraycopy( array, 0, _array, 0, e );
        if( e < eN ) System.arraycopy( array, e, _array, e + addedSize, eN - e );
        array = _array;
    }


}
