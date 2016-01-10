package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;
import java.util.*;


/** A composite of stators that together persist the composite state of a thing, each component of the
  * poly-stator (component stator) persisting a distinct component of the state.
  *
  *     @param <T> The type of thing for which composite state is persisted.
  */
public final class PolyStator<T> extends KittedPolyStatorSR<T,Object,Object>
{


    /** Constructs a PolyStator.  Seal it after adding all component stators, and before using it.
      */
    public PolyStator() {}



    /** Constructs a PolyStator with the given stator as its initial component.  Seal it after adding
      * all component stators, and before using it.
      */
    public PolyStator( final Stator<? super T> stator ) { super( stator ); }



   // --------------------------------------------------------------------------------------------------


    /** Saves state from the thing by calling s.{@linkplain Stator#save(Object,Parcel) save}
      * for each component s of this poly-stator.
      *
      *     @throws AssertionError if assertions are enabled and this poly-stator is still unsealed, or
      *       is {@linkplain #openToThread() unopen} to the calling thread.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread")
    public void save( final T t, final Parcel out ) { save( t, out, /*kit*/null ); }



    /** Restores state to the thing by calling s.{@linkplain Stator#restore(Object,Parcel) restore}
      * for each component s of this poly-stator.
      *
      *     @throws AssertionError if assertions are enabled and this poly-stator is
      *       {@linkplain #openToThread() unopen} to the calling thread.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread")
    public void restore( final T t, final Parcel in ) { restore( t, in, /*kit*/null ); }


}

