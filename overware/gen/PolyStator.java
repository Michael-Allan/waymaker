package overware.gen;

import android.os.Parcel;
import java.util.*;


/** A composite stator implemented purely of component stators.
  */
public final class PolyStator<T> implements Stator<T>
{


    /** Adds a component to this poly-stator.
      *
      *     @throws IllegalStateException if this poly-stator is sealed.
      */
    public void add( final Stator<T> stator )
    {
        try{ stators.add( stator ); }
        catch( UnsupportedOperationException _x )
        {
            assert stators instanceof ImmutableArrayList;
            throw new IllegalStateException( "Unable to add, poly-stator is sealed" );
        }
    }



    /** Removes the facility to add new components, freeing memory.
      */
    public void seal()
    {
        @SuppressWarnings({ "rawtypes", "unchecked" })
          final Stator<T>[] statorArray = new Stator[stators.size()];
        stators = new ImmutableArrayList<>( stators.toArray( statorArray ));
    }



   // - S t a t o r ----------------------------------------------------------------------


    public void get( final T i, final Parcel in ) { for( Stator<T> s: stators ) s.get( i, in ); }



    public void put( final T i, final Parcel out ) { for( Stator<T> s: stators ) s.put( i, out ); }



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    private List<Stator<T>> stators = new ArrayList<>();



}

