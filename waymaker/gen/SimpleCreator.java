package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.


/** A creator that supports only simple creation, not array creation.
  */
public abstract class SimpleCreator<T> implements android.os.Parcelable.Creator<T>
{


    /** Throws an UnsupportedOperationException.
      */
    public final T[] newArray( int _size ) { throw new UnsupportedOperationException(); }


}
