package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.

import android.os.Parcel;


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


    /** Saves state from the thing by calling st.{@linkplain Stator#save(Object,Parcel) save}
      * for each component st of this poly-stator.
      *
      *     @throws AssertionError if assertions are enabled and this poly-stator is still unsealed, or
      *       is {@linkplain #openToThread() unopen} to the calling thread.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread")
    public void save( final T th, final Parcel out ) { save( th, out, /*kit*/null ); }



    /** {@linkplain #save(Object,Parcel) Saves state} from the thing with efficient handling for a
      * frequent default instance.  The thing is considered at default if <code>th == thDefault</code>.
      * Restoration requires a call to a factory method such as <code>T.makeD( in, thDefault )</code>.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread")
    public void saveD( final T th, final Parcel out, final T thDefault )
    {
        saveD( th, out, /*kit*/null, thDefault );
    }



    /** Restores state to the thing by calling st.{@linkplain Stator#restore(Object,Parcel) restore}
      * for each component st of this poly-stator.
      *
      *     @throws AssertionError if assertions are enabled and this poly-stator is still unsealed, or
      *       is {@linkplain #openToThread() unopen} to the calling thread.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread")
    public void restore( final T th, final Parcel in ) { restore( th, in, /*kit*/null ); }



    /** Partly restores state to the thing by calling
      * st.{@linkplain Stator#restore(Object,Parcel) restore}
      * for each component st in the leading part of this poly-stator.
      * The stators of the trailing part are mere {@linkplain StateSaver state savers}, unable to
      * restore state.  Their state should instead be restored by the caller after the call.  Usually
      * the caller is a constructor or factory method, so this type of restore is called a
      * “CtorRestore”.
      *
      *     @return The {@linkplain #leaderSize() size of the leading part}.
      *     @throws AssertionError if assertions are enabled and this poly-stator is still unsealed, or
      *       is {@linkplain #openToThread() unopen} to the calling thread.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread")
    public final int startCtorRestore( final T th, final Parcel in )
    {
        return startCtorRestore( th, in, /*kit*/null );
    }


}

