package overware.gen; // Copyright 2008, 2015, Michael Allan.  Licence MIT-Overware.

import java.util.*;


/** An unmodifiable list backed by an array.  It is unmodifiable in the sense that clients
  * are provided no means to access and modify the backing array.  Modification of the
  * array by other means will still affect the list.
  *
  *     @see java.util.concurrent.CopyOnWriteArrayList
  */
public final @ThreadSafe class ImmutableArrayList<E> extends AbstractList<E> implements RandomAccess
{


    /** Contructs an ImmutableArrayList.
      *
      *     @param _back The backing array.
      */
    public ImmutableArrayList( E[] _back )
    {
        if( _back == null ) throw new NullPointerException(); // fail fast

        back = _back;
    }



   // - C o l l e c t i o n --------------------------------------------------------------


    public @Override int size() { return back.length; }



   // - L i s t --------------------------------------------------------------------------


    public @Override E get( final int index ) { return back[index]; }



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    private final E[] back;


}
