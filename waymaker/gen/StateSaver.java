package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;


/** A stator that saves state, but does not restore it.
  *
  *     @param <T> The type of thing for which state is saved.
  */
public abstract class StateSaver<T> extends Stator<T>
{


   // - S t a t o r ------------------------------------------------------------------------------------


    /** Does nothing
      */
    public final void restore( T t, Parcel in ) {}


}
