package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import java.util.*;


/** A thread restricted, non-blocking variant of {@linkplain java.util.concurrent.CopyOnWriteArraySet
  * CopyOnWriteArraySet} that is based on CopyOnResizeArrayList instead of CopyOnWriteArrayList.
  */
public abstract class CopyOnResizeArraySet<E> extends CopyOnResizeArrayList<E> implements Set<E>
{


    public final @Override void array( E[] _array )
    {
        throw new UnsupportedOperationException( "Not yet coded" );
    }



   // - C o l l e c t i o n ----------------------------------------------------------------------------


    public final @Override/*to properly handle Duplication*/ boolean add( final E element )
    {
        if( contains( element )) return false;

        return super.add( element );
    }



   // - L i s t ----------------------------------------------------------------------------------------


    public final @Override void add( final int e, final E element )
    {
        if( contains( element )) throw new Duplication();
          // obey the contract for this method, treating the rejection as exceptional

        super.add( e, element );
    }



    public final @Override boolean addAll( int e, Collection<? extends E> addenda )
    {
        throw new UnsupportedOperationException( "Not yet coded" );
    }



    public final @Override E set( final int e, final E element )
    {
        if( indexOf(element) != e ) throw new Duplication();
          // obey the contract for this method, treating the rejection as exceptional

        return super.set( e, element );
    }



   // ==================================================================================================


    /** Thrown when a duplicate element is offered through a method that provides no other, suitable
      * means of rejecting it.
      */
    public static @SuppressWarnings("serial") final class Duplication extends IllegalArgumentException {}


}
