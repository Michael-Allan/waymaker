package waymaker.spec; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;
import waymaker.gen.ThreadSafe;


/** Utilities for working with identity tags in Android applications.
  */
public @ThreadSafe final class AndroidXID
{

    private AndroidXID() {}



    /** Reads an identity tag from a parcel.
      */
    public static TriSerialUDID readUDID( final Parcel in )
    {
        return UDID.make( in.readByte(), in.createByteArray() );
    }



    /** Reads an identity tag or null from a parcel.
      *
      *     @return The identity tag or null.
      */
    public static TriSerialUDID readUDIDOrNull( final Parcel in )
    {
        final byte scopeByte = in.readByte();
        if( scopeByte == UDID.SCOPE_BYTE_NULL ) return null;

        return UDID.make( scopeByte, in.createByteArray() );
    }



    /** Writes an identity tag to a parcel.
      */
    public static void writeUDID( final TriSerialUDID _udid, final Parcel out )
    {
        final UDID udid = (UDID)_udid; // gain access to package-protected members
        out.writeByte( udid.scopeByte() );
        out.writeByteArray( udid.numericBytes() );
    }



    /** Writes a null identity tag to a parcel.
      */
    public static void writeUDIDNull( final Parcel out ) { out.writeByte( UDID.SCOPE_BYTE_NULL ); }



    /** Writes an identity tag or null to a parcel.
      *
      *     @param _udid The identity or null.
      */
    public static void writeUDIDOrNull( final TriSerialUDID _udid, final Parcel out )
    {
        if( _udid == null ) out.writeByte( UDID.SCOPE_BYTE_NULL );
        else writeUDID( _udid, out );
    }


}
