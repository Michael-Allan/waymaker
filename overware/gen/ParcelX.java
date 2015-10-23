package overware.gen; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import android.os.Parcel;


/** Utilities for working with parcels.
  *
  *     @see <a href='http://developer.android.com/reference/android/os/Parcel.html'
  *       target='_top'>android.os.Parcel</a>
  */
public @ThreadSafe final class ParcelX
{

    private ParcelX() {}



    /** Reads a boolean value from a parcel.
      */
    public static boolean readBoolean( final Parcel in ) { return in.readByte() != 0; }



    /** Writes a boolean value to a parcel.
      */
    public static void writeBoolean( final boolean value, final Parcel out )
    {
        out.writeByte( value? (byte)1: (byte)0 );
    }


}
