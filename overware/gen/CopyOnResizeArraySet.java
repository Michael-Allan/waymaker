package overware.gen; // Copyright 2015, Michael Allan.

import java.util.*;


/** A non-blocking variant of {@linkplain java.util.concurrent.CopyOnWriteArraySet
  * CopyOnWriteArraySet} that is based on CopyOnResizeArrayList instead of
  * CopyOnWriteArrayList.
  */
public @ThreadRestricted final class CopyOnResizeArraySet<E> extends CopyOnResizeArrayList<E>
  implements Set<E>
{


   // - C o l l e c t i o n --------------------------------------------------------------


    public final @Override boolean add( final E element )
    {
        boolean wasAdded;
        try{ wasAdded = super.add( element ); }
        catch( Duplication _x ) { wasAdded = false; }
        return wasAdded;
    }



   // - L i s t --------------------------------------------------------------------------


    public final @Override void add( final int index, final E element )
    {
        if( contains( element )) throw new Duplication();
          // obey the contract for this method, treating the rejection as exceptional

        super.add( index, element );
    }



    public final @Override E set( final int index, final E element )
    {
        if( contains( element )) throw new Duplication();
          // obey the contract for this method, treating the rejection as exceptional

        return super.set( index, element );
    }



   // ====================================================================================


    /** Thrown when a duplicate element is offered through a method that provides no
      * other, suitable means of rejecting it.
      */
      @SuppressWarnings("serial")
    public static final class Duplication extends IllegalArgumentException {}


}
