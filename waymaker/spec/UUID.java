package waymaker.spec; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import java.util.Comparator;


/** A universally unique, tri-serial identifier.
  */
public abstract class UUID extends ID implements TriSerialUUID
{

    private static final long serialVersionUID = 0L;

///////


    /** Super-constructs a UUID by adopting a byte array that encodes its serial numbers.  The new
      * identifier will thenceforth own the given array; do not alter its contents.
      *
      *     @see #numericBytes()
      */
    UUID( byte[] _numericBytes ) { super( _numericBytes ); }



    /** Super-constructs a UUID by parsing an identifier in basic, {@linkplain
      * #toTriSerialString(StringBuilder) unscoped string form}.
      *
      *     @param _cN The length of the substring to parse, beginning at index 0 in the string.
      *     @see #toTriSerialScopedString(StringBuilder)
      */
    UUID( String _string, int _cN ) throws MalformedID { super( _string, _cN ); }



    /** Constructs a UUID by adopting a byte array that encodes its serial numbers.  The new identifier
      * will thenceforth own the given array; do not alter its contents.
      *
      *     @see #scopeByte()
      *     @see #numericBytes()
      */
    static UUID make( final byte scopeByte, final byte[] numericBytes )
    {
        final UUID id;
        if( scopeByte == PipeID.SCOPE_BYTE ) id = new PipeID( numericBytes );
        else if( scopeByte == PersonID.SCOPE_BYTE ) id = new PersonID( numericBytes );
        else throw new IllegalArgumentException( "Bad scope byte: " + scopeByte );

        return id;
    }



    /** Constructs a UUID by parsing an identifier in {@linkplain
      * #toTriSerialScopedString(StringBuilder) scoped string form}.
      */
    public static UUID make( final String scopedString ) throws MalformedID
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



   // --------------------------------------------------------------------------------------------------


    /** A comparator based on the {@linkplain #compareUniversally(TriSerialUUID) universal comparison}.
      * It accepts null identifiers.
      */
    public static final Comparator<TriSerialUUID> comparatorUniversal = new Comparator<TriSerialUUID>()
    {
        public int compare( final TriSerialUUID i, final TriSerialUUID j )
        {
            if( i == j ) return 0; // short cut, optimizing for a common case

            if( i == null ) return -1;

            if( j == null ) return 1;

            return ((UUID)i).compareUniversally( j );
        }
    };



    /** Compares this identifier to the other based on both its scope and serial numbers.  Does no
      * preliminary "other == this" short cutting, but instead lets the caller do that.
      *
      *     @return A negative number, zero, or a positive number as this identifier is less less than,
      *       equal to, or greater than the other.
      *     @throws NullPointerException if other is null.
      */
    final int compareUniversally( final TriSerialUUID _other )
    {
        final UUID other = (UUID)_other; // gain accesss to package-protected scopeByte for faster comparison
        int result = Byte.compare( scopeByte(), other.scopeByte() ); // or throws NullPointerException
        assert Integer.signum(result) == Integer.signum(scope().compareTo(other.scope()));
          // byte-form comparisons are consistent with name form, as name alone is canonical
        if( result == 0 ) result = compareNumerically( other );
        return result;
    }



    /** The encoded value of the {@linkplain #scope() scope} for this identifier in the current version
      * of the software.  It may change in a future version.
      */
    abstract byte scopeByte(); // for sake of speedy, short term serialization, as in AndroidXID.writeUUID


        /** The encoded form of the scope for a null identifier in this version of the software.
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



   // - C o m p a r a b l e ----------------------------------------------------------------------------


    /** Compares this identifier to the other based on the {@linkplain
      * #compareUniversally(TriSerialUUID) universal comparison}.
      */
    public final int compareTo( final TriSerialUUID other )
    {
        if( other == this ) return 0; // short cut, optimizing for a common case

        return compareUniversally( other );
    }



   // - O b j e c t ------------------------------------------------------------------------------------


    public final @Override boolean equals( final Object o )
    {
        if( o == this ) return true;

        if( !(o instanceof TriSerialUUID) /*or if null*/ ) return false;

        final UUID oUUID = (UUID)o; // all TriSerialUUID implemented as UUID
        if( scopeByte() != oUUID.scopeByte() ) return false;

        return equalsNumerically( oUUID );
    }



    /** @see #toTriSerialScopedString(StringBuilder)
      */
    public final @Override String toString()
    {
        final StringBuilder out = new StringBuilder();
        toTriSerialScopedString( out );
        return out.toString();
    }


}
