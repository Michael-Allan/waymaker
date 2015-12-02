package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;
import java.util.*;


/** A {@linkplain PolyStator poly-stator} that depends on additional kit during both saving and
  * restoration.
  *
  *     @param <T> The type of thing for which composite state is persisted.
  *     @param <S> The type of saving kit.
  *     @param <R> The type of restoration kit.
  */
public @ThreadRestricted("app main") class KittedPolyStatorSR<T,S,R> implements KittedStatorSR<T,S,R>
{


    /** Constructs a KittedPolyStatorSR.  Seal it after adding all component stators, and before using it.
      */
    public KittedPolyStatorSR() { assert Application.i().isMainThread(): "Running on app main"; }



    /** Constructs a KittedPolyStatorSR with the given stator as its initial component.  Seal it after
      * adding all component stators, and before using it.
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
        assert Application.i().isMainThread(): "Running on app main";
        try { stators.add( stator ); }
        catch( final UnsupportedOperationException x )
        {
            assert stators.getClass().equals( ListOnArray.class );
            throw new IllegalStateException( "Unable to add, poly-stator is sealed", x );
        }
    }



    /** The synchronization lock for the composition of all poly-stators.  Threads other than
      * “{@linkplain Application#isMainThread() app main}” that save or restore state via a poly-stator
      * must first <a href='ThreadRestricted.html#touch'>touch-synchronize</a> on the intrinsic monitor
      * lock of COMPOSITION_LOCK.
      */
    public static final Object COMPOSITION_LOCK = new Object(); /* so a single touch suffices for access
      to the poly-stator, any poly-stator nested as its component, and any other poly-stator indirectly
      referenced during the save or restore */



    /** Removes the facility to add new component stators, freeing memory.
      */
    public final void seal()
    {
        assert Application.i().isMainThread(): "Running on app main";
        if( stators.getClass().equals( ListOnArray.class ))
        {
            throw new IllegalStateException( "Already sealed" );
        }

     // final Stator<? super T>[] statorArray = new Stator<? super T>[stators.size()];
     /// "error: generic array creation"
        final @SuppressWarnings({ "rawtypes", "unchecked" })
          KittedStatorSR<? super T, ? super S, ? super R>[] statorArray = new KittedStatorSR[stators.size()];
        stators = new ListOnArray<>( stators.toArray( statorArray ));
        synchronized( COMPOSITION_LOCK ) {} // as per stators
    }



   // - K i t t e d - S t a t o r - S - R --------------------------------------------------------------


      @ThreadRestricted("touch COMPOSITION_LOCK before") // as per stators
    public final void save( final T t, final Parcel out, S kit )
    {
        assert stators.getClass().equals( ListOnArray.class ): "Poly-stator is sealed";
        for( KittedStatorSR<? super T, ? super S, ? super R> s: stators ) s.save( t, out, kit );
    }



      @ThreadRestricted("touch COMPOSITION_LOCK before") // as per stators
    public final void restore( final T t, final Parcel in, final R kit )
    {
        assert stators.getClass().equals( ListOnArray.class ): "Poly-stator is sealed";
        for( KittedStatorSR<? super T, ? super S, ? super R> s: stators ) s.restore( t, in, kit );
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


      @ThreadRestricted("touch COMPOSITION_LOCK") // for visibility of both array and elements
    private List<KittedStatorSR<? super T, ? super S, ? super R>> stators = new ArrayList<>();


}

