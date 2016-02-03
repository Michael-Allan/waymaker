package waymaker.gen; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;
import java.util.*;

import static java.lang.Boolean.TRUE;


/** A {@linkplain PolyStator poly-stator} that depends on additional kit during both saving and
  * restoration.
  *
  *     @param <T> The type of thing for which composite state is persisted.
  *     @param <S> The type of saving kit.
  *     @param <R> The type of restoration kit.
  */
public class KittedPolyStatorSR<T,S,R> implements KittedStatorSR<T,S,R>
{


    /** Constructs a KittedPolyStatorSR.  Seal it after adding all component stators, and before using
      * it to save state.
      */
    public KittedPolyStatorSR() {}



    /** Constructs a KittedPolyStatorSR with the given stator as its initial component.  Seal it after
      * adding all component stators, and before using it to save state.
      */
    public KittedPolyStatorSR( final KittedStatorSR<? super T, ? super S, ? super R> stator )
    {
        this();
        add( stator );
    }



   // --------------------------------------------------------------------------------------------------


    /** Appends a component stator to the leading part of this poly-stator, ahead of any trailing
      * CtorRestore state savers.
      *
      *     @throws IllegalStateException if this poly-stator is already sealed.
      */
    public final void add( final KittedStatorSR<? super T, ? super S, ? super R> stator )
    {
        add( leaderSize, stator );
        ++leaderSize;
    }


        private void add( final int s, final KittedStatorSR<? super T, ? super S, ? super R> stator )
        {
            try{ stators.add( s, stator ); }
            catch( final UnsupportedOperationException x )
            {
                assert stators.getClass().equals( ListOnArray.class );
                throw new IllegalStateException( "Unable to add, poly-stator is sealed", x );
            }
        }



    /** Appends a component stator to the trailing part of this poly-stator, which is reserved for
      * CtorRestore state savers.
      *
      *     @return The same saver, returned as a convenience for use in declaring an assignment for
      *       subsequent {@linkplain #get(int) get} testing.
      *     @throws IllegalStateException if this poly-stator is already sealed.
      */
    public final Object add( final StateSaver<? super T> saver )
    {
        add( size(), saver );
        return saver;
    }



    /** Returns the component stator at the given index.  This method is used for assertions of order
      * during {@linkplain #startCtorRestore(Object,Parcel,Object) CtorRestore} that depend on identity
      * tests alone, hence the general return type.
      */
    public final Object get( final int s ) { return stators.get( s ); }



    /** The size of the leading part of this poly-stator, which is the number of component stators of
      * the ordinary, restoring type.  Any trailing part is reserved for CtorRestore state savers.
      *
      *     @see #size()
      *     @see #startCtorRestore(Object,Parcel,Object)
      */
    public final int leaderSize() { return leaderSize; }


        private int leaderSize;



    /** Gives the calling thread access to the deep composition of all poly-stators that are currently
      * sealed regardless of which threads composed and sealed them.  Each use of a poly-stator to save
      * or restore state must, in addition to the basic thread restriction demanded by the poly-stator
      * (locking or other synchronization), be preceded by a call to this method.  A single call
      * suffices to access the poly-stator, any poly-stator nested as its component, and any other
      * poly-stator that will be indirectly referenced by that particular save or restore operation.
      * Any later operation may involve additional, newly sealed poly-stators, and must therefore be
      * preceded by its own call.
      */
    public static @ThreadSafe void openToThread()
    {
        synchronized( COMPOSITION_LOCK ) {}
        openToThread.set( TRUE ); // compiliance test only, imperfect
    }

        // The instigating case is that of precount threads in top/android.  A precount thread might
        // happen to be the first constructor of a class, and thus the sealer of its poly-stator.
        // Subsequently it communicates its results back to the main thread by TermSync, which also
        // happens to communicate the composition of the poly-stator.  But this is an accident of one
        // particular case, and it might happen otherwise in other cases.


        private static final Object COMPOSITION_LOCK = new Object();
          // synchronization lock for the composition of all poly-stators


        private static final ThreadLocal<Boolean> openToThread = new ThreadLocal<>(); // TRUE or null
          // Compliance test.  Not foolproof.  The synchronization of openToThread() should be done
          // *immediately* before each use, while this tests that it was done *some time* before.


        private static @ThreadSafe boolean isOpenToThread() { return openToThread.get() == TRUE; }



    /** {@linkplain #save(Object,Parcel,Object) Saves state} from the thing with efficient handling for a
      * frequent default instance.  The thing is considered at default if <code>th == thDefault</code>.
      * Restoration requires a call to a factory method such as <code>T.makeD( in, kit, thDefault )</code>.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread")
    public final void saveD( final T th, final Parcel out, final S kit, final T thDefault )
    {
      // 1. Is default?
      // - - - - - - - -
        final boolean isDefault = th == thDefault; // == for speed, not equals
        ParcelX.writeBoolean( isDefault, out );

      // 2. Thing
      // - - - - -
        if( !isDefault ) save( th, out, kit );
    }



    /** Removes the facility to add new component stators to this poly-stator, freeing its memory.
      * Generally you should seal a poly-stator before using it.  Accordingly the {@linkplain
      * #save(Object,Parcel,Object) save} method checks the seal with an assert statement.  (The
      * {@linkplain KittedStatorSR#restore(Object,Parcel,Object) restore} method makes no such check,
      * thus allowing for CtorRestore; q.v. by grep in source.)
      */
    public final void seal()
    {
        if( stators.getClass().equals( ListOnArray.class ))
        {
            throw new IllegalStateException( "Already sealed" );
        }

     // final Stator<? super T>[] statorArray = new Stator<? super T>[stators.size()];
     /// "error: generic array creation"
        final @SuppressWarnings({ "rawtypes", "unchecked" })
          KittedStatorSR<? super T, ? super S, ? super R>[] statorArray = new KittedStatorSR[stators.size()];
        stators = new ListOnArray<>( stators.toArray( statorArray ));
        synchronized( COMPOSITION_LOCK ) {} // flush composition to main memory
    }



    /** The number of component stators added to this poly-stator.
      *
      *     @see #leaderSize()
      */
    public final int size() { return stators.size(); }



    /** Partly restores state to the thing by calling
      * st.{@linkplain KittedStatorSR#restore(Object,Parcel,Object) restore}
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
    public final int startCtorRestore( final T th, final Parcel in, final R kit )
    {
        restore( th, in, kit, leaderSize );
        return leaderSize;
    }



   // - K i t t e d - S t a t o r - S - R --------------------------------------------------------------


    /** Saves state from the thing by calling
      * st.{@linkplain KittedStatorSR#save(Object,Parcel,Object) save}
      * for each component st of this poly-stator.
      *
      *     @throws AssertionError if assertions are enabled and this poly-stator is still unsealed, or
      *       is {@linkplain #openToThread() unopen} to the calling thread.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread")
    public final void save( final T th, final Parcel out, final S kit )
    {
        assert stators.getClass().equals(ListOnArray.class) && isOpenToThread(): "Sealed and openToThread";
        for( KittedStatorSR<? super T, ? super S, ? super R> st: stators ) st.save( th, out, kit );
    }



    /** Restores state to the thing by calling
      * st.{@linkplain KittedStatorSR#restore(Object,Parcel,Object) restore}
      * for each component st of this poly-stator.
      *
      *     @throws AssertionError if assertions are enabled and this poly-stator is still unsealed, or
      *       is {@linkplain #openToThread() unopen} to the calling thread.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread")
    public final void restore( final T th, final Parcel in, final R kit ) { restore( th, in, kit, size() ); }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


      @ThreadRestricted("further KittedPolyStatorSR.openToThread")
    private void restore( final T th, final Parcel in, final R kit, final int sN )
    {
        assert stators.getClass().equals(ListOnArray.class) && isOpenToThread(): "Sealed and openToThread";
        for( int s = 0; s < sN; ++s )
        {
            final KittedStatorSR<? super T, ? super S, ? super R> st = stators.get( s );
            st.restore( th, in, kit );
        }
    }



    private List<KittedStatorSR<? super T, ? super S, ? super R>> stators = new ArrayList<>();


}

