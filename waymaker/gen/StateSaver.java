package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.

import android.os.Parcel;


/** A stator that saves state, but does not restore it.
  * Restoration is done instead by {@linkplain KittedPolyStatorSR#startCtorRestore CtorRestore}.
  *
  *     @param <T> The type of thing for which state is saved.
  */
public abstract class StateSaver<T> extends Stator<T>
{


   // - S t a t o r ------------------------------------------------------------------------------------


    /** Throws UnsupportedOperationException.
      */
    public final void restore( T _th, Parcel _in )
    {
        throw new UnsupportedOperationException( "Restricted to CtorRestore" );
    }


}
