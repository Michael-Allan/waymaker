package overware.spec; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import android.os.Parcel;
import overware.gen.ThreadSafe;


/** Utilities for working with identifiers in Android applications.
  */
public @ThreadSafe final class AndroidXID
{

    private AndroidXID() {}



    /** Reads an identifier from a parcel.
      */
    public static TriSerialUUID readUUID( final Parcel in )
    {
        return UUID.make( in.readByte(), in.createByteArray() );
    }



    /** Reads an identifier or null from a parcel.
      *
      *     @return The identifier or null.
      */
    public static TriSerialUUID readUUIDOrNull( final Parcel in )
    {
        final byte scopeByte = in.readByte();
        if( scopeByte == UUID.SCOPE_BYTE_NULL ) return null;

        return UUID.make( scopeByte, in.createByteArray() );
    }



    /** Writes an identifier to a parcel.
      */
    public static void writeUUID( final TriSerialUUID _uuid, final Parcel out )
    {
        final UUID uuid = (UUID)_uuid; // gain access to package-protected members
        out.writeByte( uuid.scopeByte() );
        out.writeByteArray( uuid.numericBytes() );
    }



    /** Writes a null identifier to a parcel.
      */
    public static void writeUUIDNull( final Parcel out ) { out.writeByte( UUID.SCOPE_BYTE_NULL ); }



    /** Writes an identifier or null to a parcel.
      *
      *     @param _uuid The identifier or null.
      */
    public static void writeUUIDOrNull( final TriSerialUUID _uuid, final Parcel out )
    {
        if( _uuid == null ) out.writeByte( UUID.SCOPE_BYTE_NULL );
        else writeUUID( _uuid, out );
    }


}
