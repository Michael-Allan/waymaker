package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;


/** A {@linkplain Stator stator} that depends on additional kit during both saving and restoration.
  *
  *     @param <T> The type of thing for which state is persisted.
  *     @param <S> The type of saving kit.
  *     @param <R> The type of restoration kit.
  *
  *     @see <a href='http://developer.android.com/training/basics/activity-lifecycle/recreating.html'
  *       target='_top'>Recreating an Activity</a>
  */
public interface KittedStatorSR<T,S,R>
{


   // - K i t t e d - S t a t o r - S - R --------------------------------------------------------------


    /** Saves state from the thing, writing out to the parcel.
      */
    public void save( T t, Parcel out, S kit );



    /** Restores state to the thing, reading in from the parcel.
      */
    public void restore( T t, Parcel in, R kit );


}
