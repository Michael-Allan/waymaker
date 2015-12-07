package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.os.*;


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



    /** Reads a parcelable from a parcel using ParcelX’s own class loader.
      *
      *     @param <T> The type of parcelable.
      */
    public static <T extends Parcelable> T readParcelable( final Parcel in )
    {
        return in.readParcelable( classLoader ); /* cannot simply pass null here, and use the default
          class loader, else "Class not found using the boot class loader" */
    }



    /** Writes a parcelable to a parcel without specifying any flags.
      */
    public static void writeParcelable( final Parcelable value, final Parcel out )
    {
        out.writeParcelable( value, /*flags*/0 );
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final ClassLoader classLoader = ParcelX.class.getClassLoader();


}
