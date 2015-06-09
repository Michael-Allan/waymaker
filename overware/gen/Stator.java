package overware.gen; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import android.os.Parcel;


/** A maintainer of state for an instance of type T.
  */
public interface Stator<T>
{


   // - S t a t o r ----------------------------------------------------------------------


    /** Restores state to the instance, reading in from a parcel.
      */
    public void get( T i, Parcel in );



    /** Saves state from the instance, writing out to a parcel.
      */
    public void put( T i, Parcel out );


}
