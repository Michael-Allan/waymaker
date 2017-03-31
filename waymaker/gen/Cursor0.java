package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.

import android.database.*;
import android.net.Uri;
import android.os.Bundle;


/** An empty, immutable cursor that is forever stuck at position zero.
  */
public final @Warning("unused code") class Cursor0 implements Cursor
{


    /** Constructs a Cursor0.
      *
      *     @see #getColumnNames()
      */
    public @ThreadSafe Cursor0( String[] _columnNames ) { columnNames = _columnNames; }



   // --------------------------------------------------------------------------------------------------


    /** A common instance of an empty cursor without any columns.
      */
    public static final Cursor VOID_CURSOR = new Cursor0( (String[])ObjectX.EMPTY_OBJECT_ARRAY );



   // - C u r s o r ------------------------------------------------------------------------------------


    /** Does nothing.
      */
    public @Override void close() {}



    public @Override void copyStringToBuffer( int _columnIndex, CharArrayBuffer _buffer ) {}



    /** Does nothing.
      */
    public @Override @SuppressWarnings("deprecation") void deactivate() {}



    public @Override byte[] getBlob( int _col ) { throw new IllegalStateException(); }



    public @Override int getColumnCount() { return columnNames.length; }



    public @Override int getColumnIndex( String _columnName )
    {
        throw new UnsupportedOperationException( "Not yet coded" );
    }



    public @Override int getColumnIndexOrThrow( String _columnName )
    {
        throw new UnsupportedOperationException( "Not yet coded" );
    }



    public @Override String getColumnName( final int col ) { return columnNames[col]; }



    /** @return The same instance provided to the constructor.  Do not modify it.
      */
    public @Override String[] getColumnNames() { return columnNames; }


        private final String[] columnNames;



    /** Returns zero.
      */
    public @Override int getCount() { return 0; }



    public @Override Bundle getExtras() { return Bundle.EMPTY; }



    public @Override double getDouble( int _col ) { throw new IllegalStateException(); }



    public @Override float getFloat( int _col ) { throw new IllegalStateException(); }



    public @Override int getInt( int _col ) { throw new IllegalStateException(); }



    public @Override long getLong( int _col ) { throw new IllegalStateException(); }



    public @Override Uri getNotificationUri() { return null; }



    public @Override int getPosition() { return 0; }



    public @Override short getShort( int _col ) { throw new IllegalStateException(); }



    public @Override String getString( int _col ) { throw new IllegalStateException(); }



    public @Override int getType( int _col )
    {
        throw new UnsupportedOperationException( "Not yet coded" );
    }



    public @Override boolean getWantsAllOnMoveCalls() { return false; }



    public @Override boolean isAfterLast() { return true; }



    public @Override boolean isBeforeFirst() { return false; }



    /** Returns false.
      */
    public @Override boolean isClosed() { return false; }



    public @Override boolean isFirst() { return false; }



    public @Override boolean isLast() { return false; }



    public @Override boolean isNull( int _col ) { throw new IllegalStateException(); }



    public @Override boolean move( int _offset ) { throw new UnsupportedOperationException(); }



    public @Override boolean moveToFirst() { return false; }



    public @Override boolean moveToLast() { return false; }



    public @Override boolean moveToNext() { return false; }



    public @Override boolean moveToPosition( int _p ) { throw new UnsupportedOperationException(); }



    public @Override boolean moveToPrevious() { throw new UnsupportedOperationException(); }



    public @Override void registerContentObserver( ContentObserver _o ) {}



    public @Override void registerDataSetObserver( DataSetObserver _o ) {}



    public @Override @SuppressWarnings("deprecation") boolean requery()
    {
        throw new UnsupportedOperationException();
    }



    public @Override Bundle respond( Bundle _extras ) { return Bundle.EMPTY; }



    public @Override void setNotificationUri( android.content.ContentResolver _c, Uri _u )
    {
        throw new UnsupportedOperationException();
    }



    public @Override void unregisterContentObserver( ContentObserver _o ) {}



    public @Override void unregisterDataSetObserver( DataSetObserver _o ) {}


}
