package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;


/** A persister of state for something.
  *
  *     @param <T> The type of thing for which state is persisted.
  *
  *     @see <a href='http://developer.android.com/training/basics/activity-lifecycle/recreating.html'
  *       target='_top'>Recreating an Activity</a>
  */
public abstract class Stator<T> implements KittedStatorSR<T,Object,Object>
{


    /** Saves state from the thing, writing out to the parcel.
      */
    public abstract void save( T th, Parcel out );



    /** Restores state to the thing, reading in from the parcel.
      */
    public abstract void restore( T th, Parcel in );



   // - K i t t e d - S t a t o r - S - R --------------------------------------------------------------


    public final void save( final T th, final Parcel out, Object unusedKit ) { save( th, out); }



    public final void restore( final T th, final Parcel in, Object unusedKit ) { restore( th, in ); }


}
