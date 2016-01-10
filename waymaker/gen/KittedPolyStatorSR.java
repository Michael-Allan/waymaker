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


    /** Adds a component stator to this poly-stator.
      *
      *     @throws IllegalStateException if this poly-stator is already sealed.
      */
    public final void add( final KittedStatorSR<? super T, ? super S, ? super R> stator )
    {
        try { stators.add( stator ); }
        catch( final UnsupportedOperationException x )
        {
            assert stators.getClass().equals( ListOnArray.class );
            throw new IllegalStateException( "Unable to add, poly-stator is sealed", x );
        }
    }



    /** Gives the calling thread access to the current composition of every poly-stator regardless of
      * which thread composed it.  Each composite (top level) use of a poly-stator to save or restore
      * state must, in addition to the basic thread restriction demanded by the poly-stator (obeyed by
      * locking or other synchronization), be immediately preceded by a call to this method.  A single
      * call (per use) suffices to access the poly-stator, any poly-stator nested as its component, and
      * any other poly-stator indirectly referenced during the save or restore.
      */
    public static @ThreadSafe void openToThread()
    {
        synchronized( COMPOSITION_LOCK ) {}
        openToThread.set( TRUE ); // compiliance test only, imperfect
    }

        // The instigating case is that of precount threads in top/android.  A precount thread might
        // happen to be the first constructor of a class, and thus the composer of its poly-stator.  It
        // subsequently communicates its results back to the main thread by TermSync, which also happens
        // to communicate the composition of the poly-stator.  But this is an accident, a side effect of
        // the design in one particular case; it might be otherwise in other cases.


        private static final Object COMPOSITION_LOCK = new Object();
          // synchronization lock for the composition of all poly-stators


        private static final ThreadLocal<Boolean> openToThread = new ThreadLocal<>(); // TRUE or null
          // Compliance test.  Not foolproof.  The synchronization of openToThread() should be done
          // *immediately* before each use, while this tests that it was done *some time* before.


        private static @ThreadSafe boolean isOpenToThread() { return openToThread.get() == TRUE; }



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
      */
    public final int size() { return stators.size(); }



   // - K i t t e d - S t a t o r - S - R --------------------------------------------------------------


    /** Saves state from the thing by calling
      * s.{@linkplain KittedStatorSR#save(Object,Parcel,Object) save}
      * for each component s of this poly-stator.
      *
      *     @throws AssertionError if assertions are enabled and this poly-stator is still unsealed, or
      *       is {@linkplain #openToThread() unopen} to the calling thread.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread")
    public final void save( final T t, final Parcel out, S kit )
    {
        assert stators.getClass().equals(ListOnArray.class) && isOpenToThread(): "Sealed and openToThread";
        for( KittedStatorSR<? super T, ? super S, ? super R> s: stators ) s.save( t, out, kit );
    }



    /** Restores state to the thing by calling
      * s.{@linkplain KittedStatorSR#restore(Object,Parcel,Object) restore}
      * for each component s of this poly-stator.
      *
      *     @throws AssertionError if assertions are enabled and this poly-stator is
      *       {@linkplain #openToThread() unopen} to the calling thread.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread")
    public final void restore( final T t, final Parcel in, final R kit )
    {
        assert isOpenToThread(): "openToThread";
        for( KittedStatorSR<? super T, ? super S, ? super R> s: stators ) s.restore( t, in, kit );
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


      @ThreadRestricted("touch COMPOSITION_LOCK") // for visibility of both array and elements
    private List<KittedStatorSR<? super T, ? super S, ? super R>> stators = new ArrayList<>();


}

