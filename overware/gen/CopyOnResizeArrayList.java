package overware.gen; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import java.util.*;


/** A non-blocking variant of {@linkplain java.util.concurrent.CopyOnWriteArrayList
  * CopyOnWriteArrayList} that copies the underlying array only for resizing mutations:
  * only {@linkplain #add(Object) add} and {@linkplain #remove(Object) remove} are
  * “implemented by making a fresh copy” of the array, not {@linkplain #set(int,Object)
  * set}.  This implementation is compact and fast traversing.  It retains the advantage
  * that it “may be more efficient than alternatives when traversal operations vastly
  * outnumber mutations”; but, in this case, only resizing mutations need be infrequent.
  */
public @ThreadRestricted class CopyOnResizeArrayList<E> extends AbstractList<E>
  implements RandomAccess
{


   // - C o l l e c t i o n --------------------------------------------------------------


    public final @Override int size() { return back.length; }



   // - L i s t --------------------------------------------------------------------------


    public @Override void add( final int index, final E element )
    {
        final int sizeOld = back.length;
        final int sizeNew = sizeOld + 1;
        final E[] backNew = ArraysX.newArray( sizeNew );
        if( index > 0 ) System.arraycopy( back, 0, backNew, 0, index );
        backNew[index] = element;
        if( index < sizeOld ) System.arraycopy( back, index, backNew, index + 1, sizeOld - index );
        back = backNew;
    }



    public final @Override E get( final int index ) { return back[index]; }



    public final @Override E remove( final int index )
    {
        final int sizeOld = back.length;
        final int sizeNew = sizeOld - 1;
        final E[] backNew = ArraysX.newArray( sizeNew );
        if( index > 0 ) System.arraycopy( back, 0, backNew, 0, index );
        final E elementRemoved = back[index];
        if( index < sizeNew ) System.arraycopy( back, index + 1, backNew, index, sizeNew - index );
        back = backNew;
        return elementRemoved;
    }



    public @Override E set( final int index, final E element )
    {
        final E elementWas = back[index];
        back[index] = element;
        return elementWas;
    }



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    private E[] back = ArraysX.newArray( 0 );


}
