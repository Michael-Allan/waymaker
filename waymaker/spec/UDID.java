package waymaker.spec; // Copyright © 2015 Michael Allan.  Licence MIT.

import java.util.Comparator;


/** The implementation of a universally decisive, tri-serial identity tag.
  */
public abstract class UDID extends ID implements TriSerialUDID
{

    private static final long serialVersionUID = 0L;

///////


    /** Super-constructs a UDID by adopting a byte array that encodes its serial numbers.  The new
      * identity tag will thenceforth own the given array; do not alter its contents.
      *
      *     @see #numericBytes()
      */
    UDID( byte[] _numericBytes ) { super( _numericBytes ); }



    /** Super-constructs a UDID by parsing an identity tag in basic, {@linkplain
      * #toTriSerialString(StringBuilder) unscoped string form}.
      *
      *     @param _cN The length of the substring to parse, beginning at index 0 in the string.
      *     @see #toTriSerialScopedString(StringBuilder)
      */
    UDID( String _string, int _cN ) throws MalformedID { super( _string, _cN ); }



   // --------------------------------------------------------------------------------------------------


    /** A comparator based on the {@linkplain #compareUniversally(TriSerialUDID) universal comparison}.
      * It accepts null identity tags.
      */
    public static final Comparator<TriSerialUDID> comparatorUniversal = new Comparator<TriSerialUDID>()
    {
        public int compare( final TriSerialUDID i, final TriSerialUDID j )
        {
            if( i == j ) return 0; // short cut, optimizing for a common case

            if( i == null ) return -1;

            if( j == null ) return 1;

            return ((UDID)i).compareUniversally( j );
        }
    };



    /** Constructs a UDID by parsing an identity tag in {@linkplain
      * #toTriSerialScopedString(StringBuilder) scoped string form}.
      */
    public static UDID make( final String scopedString ) throws MalformedID
    {
        final int cLast = scopedString.length() - 1;
        final char charLast = scopedString.charAt( cLast );
        if( charLast == 'e' )
        {
            final String scope = PipeID.SCOPE; // expected
            final int remainingLength = scope.length() - 1; // remaining to compare
            final int cScope = cLast - remainingLength; // start of scope in scopedString
            if( scopedString.regionMatches( cScope, scope, 0, remainingLength ))
            {
                final int cDash = cScope - 1;
                if( scopedString.charAt(cDash) == '-' ) return new PipeID( scopedString, cDash );
            }
        }
        else if( charLast == 'n' )
        {
            final String scope = PersonID.SCOPE; // expected
            final int remainingLength = scope.length() - 1; // remaining to compare
            final int cScope = cLast - remainingLength; // start of scope in scopedString
            if( scopedString.regionMatches( cScope, scope, 0, remainingLength ))
            {
                final int cDash = cScope - 1;
                if( scopedString.charAt(cDash) == '-' ) return new PersonID( scopedString, cDash );
            }
        }
        throw new MalformedID( "Bad scope suffix", scopedString );
    }



   // - C o m p a r a b l e ----------------------------------------------------------------------------


    /** Compares this identity tag to the other based on the {@linkplain
      * #compareUniversally(TriSerialUDID) universal comparison}.
      */
    public final int compareTo( final TriSerialUDID other )
    {
        if( other == this ) return 0; // short cut, optimizing for a common case

        return compareUniversally( other );
    }



   // - O b j e c t ------------------------------------------------------------------------------------


    public final @Override boolean equals( final Object o )
    {
        if( o == this ) return true;

        if( !(o instanceof TriSerialUDID) /*or if null*/ ) return false;

        final UDID oUDID = (UDID)o; // all TriSerialUDID implemented as UDID
        if( scopeByte() != oUDID.scopeByte() ) return false;

        return equalsNumerically( oUDID );
    }



    /** @see #toTriSerialScopedString(StringBuilder)
      */
    public final @Override String toString()
    {
        final StringBuilder out = new StringBuilder();
        toTriSerialScopedString( out );
        return out.toString();
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    /** Compares this identity tag to the other based on both its scope and serial numbers.  Does no
      * preliminary "other == this" short cutting, but instead lets the caller do that.
      *
      *     @return A negative number, zero, or a positive number as this identity tag is less less than,
      *       equal to, or greater than the other.
      *     @throws NullPointerException if other is null.
      */
    final int compareUniversally( final TriSerialUDID _other )
    {
        final UDID other = (UDID)_other; // gain accesss to package-protected scopeByte for faster comparison
        int result = Byte.compare( scopeByte(), other.scopeByte() ); // or throws NullPointerException
        assert Integer.signum(result) == Integer.signum(scope().compareTo(other.scope()));
          // byte-form comparisons are consistent with name form, as name alone is canonical
        if( result == 0 ) result = compareNumerically( other );
        return result;
    }



    /** Constructs a UDID by adopting a byte array that encodes its serial numbers.  The new identity tag
      * will thenceforth own the given array; do not alter its contents.
      *
      *     @see #scopeByte()
      *     @see #numericBytes()
      */
    static UDID make( final byte scopeByte, final byte[] numericBytes )
    {
        final UDID id;
        if( scopeByte == PipeID.SCOPE_BYTE ) id = new PipeID( numericBytes );
        else if( scopeByte == PersonID.SCOPE_BYTE ) id = new PersonID( numericBytes );
        else throw new IllegalArgumentException( "Bad scope byte: " + scopeByte );

        return id;
    }



    /** The encoded value of the {@linkplain #scope() scope} for this identity tag in the current version
      * of the software.  It may change in a future version.
      */
    abstract byte scopeByte(); // for sake of speedy, short term serialization, as in AndroidXID.writeUDID


        /** The encoded form of the scope for a null identity tag in this version of the software.
          */
        static final byte SCOPE_BYTE_NULL = -1; // start of sequence, continues lexically with PersonID



    /** Outputs a {@linkplain #toTriSerialString(StringBuilder) tri-serial string} suffixed by a
      * {@linkplain #scope() scope string}.
      */
    final void toTriSerialScopedString( final StringBuilder out )
    {
        toTriSerialString( out );
        out.append( '-' );
        out.append( scope() );
    }


}
