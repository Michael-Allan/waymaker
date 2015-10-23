package overware.gen; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import android.os.Parcel;
import java.util.*;


/** A composite of stators that together persist the composite state of a thing, each component of the
  * poly-stator (component stator) persisting a distinct component of the state.
  *
  *     @param <T> The type of thing for which composite state is persisted.
  */
public final class PolyStator<T> extends Stator<T>
{

    // Changing?  See also KittedPolyStatorSR.


    /** Constructs a PolyStator.  Be sure to seal it after adding all component stators, and before
      * using it.
      */
    public @ThreadSafe PolyStator() {}



    /** Constructs a PolyStator with the given stator as its initial component.  Be sure to seal it
      * after adding all component stators, and before using it.
      */
    public PolyStator( final Stator<? super T> stator ) { add( stator ); }



   // --------------------------------------------------------------------------------------------------


    /** Adds a component stator to this poly-stator.
      *
      *     @throws IllegalStateException if this poly-stator is already sealed.
      */
    public void add( final Stator<? super T> stator )
    {
        try { stators.add( stator ); }
        catch( final UnsupportedOperationException x )
        {
            assert stators.getClass().equals( ListOnArray.class );
            throw new IllegalStateException( "Unable to add, poly-stator is sealed", x );
        }
    }



    /** Removes the facility to add new component stators, freeing memory.
      */
    public void seal()
    {
        if( stators.getClass().equals( ListOnArray.class ))
        {
            throw new IllegalStateException( "Already sealed" );
        }

     // final Stator<? super T>[] statorArray = new Stator<? super T>[stators.size()];
     /// "error: generic array creation"
        final @SuppressWarnings({ "rawtypes", "unchecked" })
          Stator<? super T>[] statorArray = new Stator[stators.size()];
        stators = new ListOnArray<>( stators.toArray( statorArray ));
    }



   // - S t a t o r ------------------------------------------------------------------------------------


    /** @throws AssertionError if assertions are enabled and this poly-stator is still unsealed.
      */
    public void save( final T t, final Parcel out )
    {
        assert stators.getClass().equals( ListOnArray.class ): "Poly-stator is sealed";
        for( Stator<? super T> s: stators ) s.save( t, out );
    }



    /** @throws AssertionError if assertions are enabled and this poly-stator is still unsealed.
      */
    public void restore( final T t, final Parcel in )
    {
        assert stators.getClass().equals( ListOnArray.class ): "Poly-stator is sealed";
        for( Stator<? super T> s: stators ) s.restore( t, in );
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private List<Stator<? super T>> stators = new ArrayList<>();


}

