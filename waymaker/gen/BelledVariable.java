package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.


/** A variable with a change bell.
  *
  *     @param <V> The type of variable belled.
  */
public class BelledVariable<V>
{


    /** Constructs a BelledVariable with an initial value of null.
      */
    public BelledVariable() {}



    /** Constructs a BelledVariable.
      */
    public BelledVariable( final V initialValue ) { v = initialValue; }



   // --------------------------------------------------------------------------------------------------


     /** A bell that rings when the value of this variable changes.
      */
    public final ReRinger<Changed> bell() { return bell; }


        private final ReRinger<Changed> bell = Changed.newReRinger();



   /** The value of this variable.
      */
    public final V get() { return v; }


        V v;



    /** Assigns the given value to this variable if it does not ‘equal’ the present value.  Rings
      * the bell if the value is changed as a result.
      *
      *     @see ObjectX#equals(Object,Object)
      */
    public final void set( final V _v ) { if( setSilently( _v )) bell.ring(); }



    /** Assigns the given value to this variable if it does not ‘equal’ the present value.  Returns true
      * if the value is changed as a result, false otherwise.
      *
      *     @see ObjectX#equals(Object,Object)
      */
    public boolean setSilently( final V _v )
    {
        if( ObjectX.equals( _v, v )) return false;

        v = _v;
        return true;
    }


}
