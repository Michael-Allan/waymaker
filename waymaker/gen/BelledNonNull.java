package waymaker.gen; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.


/** A belled variable that is constrained to a non-null value.  Attempting to set a null value will
  * instead throw a null pointer exception.
  *
  *     @param <V> The type of variable belled.
  */
public final class BelledNonNull<V> extends BelledVariable<V>
{


    /** Constructs a BelledNonNull.
      *
      *     @throws NullPointerException if the initial value is null.
      */
    public BelledNonNull( final V initialValue )
    {
        super( initialValue );

        if( initialValue == null ) throw new NullPointerException();
    }



   // --------------------------------------------------------------------------------------------------


    /** Assigns the given value to this variable if it does not ‘equal’ the present value.  Returns true
      * if the value is changed as a result, false otherwise.
      *
      *     @see Object#equals(Object)
      *     @throws NullPointerException if the given value is null.
      */
    public @Override boolean setSilently( final V _v )
    {
        if( _v == null ) throw new NullPointerException();

        if( _v.equals( v )) return false;

        v = _v;
        return true;
    }


}
