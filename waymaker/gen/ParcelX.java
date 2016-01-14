package waymaker.gen; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.os.*;


/** Utilities for working with parcels.
  *
  *     @see <a href='http://developer.android.com/reference/android/os/Parcel.html'
  *       target='_top'>android.os.Parcel</a>
  */
public @ThreadSafe final class ParcelX
{

    private ParcelX() {}



    /** Writes a boolean value to a parcel.
      */
    public static void writeBoolean( final boolean value, final Parcel out )
    {
        out.writeByte( value? (byte)1: (byte)0 );
    }


        /** Reads a boolean value from a parcel.
          */
        public static boolean readBoolean( final Parcel in ) { return in.readByte() != 0; }



    /** Writes a parcelable to a parcel without specifying any flags.
      */
    public static void writeParcelable( final Parcelable value, final Parcel out )
    {
        out.writeParcelable( value, /*flags*/0 );
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



    /** Writes a string to a parcel with efficient handling for a frequent default value.
      */
    public static void writeString( final String s, final Parcel out, final String sDefault )
    {
      // 1. Is default?
      // - - - - - - -
        final boolean isDefault = s == sDefault; // == for speed, not equals
        ParcelX.writeBoolean( isDefault, out );

      // 2. Waynode
      // - - - - - -
        if( !isDefault ) out.writeString( s );
    }


        /** Reads a string from a parcel with efficient handling for a frequent default value.
          */
        public static String readString( final Parcel in, final String sDefault )
        {
          // 1.
          // - - -
            if( ParcelX.readBoolean( in )) return sDefault;

          // 2.
          // - - -
            return in.readString();
        }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final ClassLoader classLoader = ParcelX.class.getClassLoader();


}
