package waymaker.spec; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import java.util.Arrays;
import waymaker.gen.ThreadSafe;


/** The implementation of a tri-serial identity tag.
  */
public @ThreadSafe class ID implements TriSerialID
{

    private static final long serialVersionUID = 0L;

///////


    /** Constructs an ID by adopting a byte array that encodes its serial numbers.  The new identity tag
      * will thenceforth own the given array; do not alter its contents.
      *
      *     @see #numericBytes()
      */
    ID( byte[] _numericBytes ) { numericBytes = _numericBytes; }



    /** Constructs an ID by parsing an identity tag in {@linkplain #toTriSerialString(StringBuilder)
      * string form}.
      *
      *     @param cN The length of the substring to parse, beginning at index 0 in the string.
      */
    ID( final String string, final int cN ) throws MalformedID
    {
        numericBytes = new byte[cN];
        int c = 0; // index of next character in string
        int d = 0; // count of digits stored thus far
        final int sa1N; // length of sa1, the first sub-array
        final byte sa1FirstDigit;
        final byte sa2FirstDigit;
        final boolean sa2IsMultiDigit;
        try
        {
          // (sa1) Store the first sub-array.
          // - - - - - - - - - - - - - - - - -
            // Three leading digits, at least.
            try
            {
                sa1FirstDigit = digit( string.charAt( c++ ));
                numericBytes[d++] = sa1FirstDigit;
                do numericBytes[d++] = digit(string.charAt(c++)); while( d < SA1N_MIN );
            }
            catch( IndexOutOfBoundsException _x ) { throw new MalformedID( "Too short", string ); }

            // Any remaining digits, followed by a dash.
            for( ;; )
            {
                if( c >= cN ) throw new MalformedID( "Missing dash", string );

                final char ch = string.charAt( c++ );
                if( ch == '-' )
                {
                    sa1N = d; // length is count of digits stored
                    if( sa1N > SA1N_MAX ) throw new MalformedID( "Numeric overflow before dash", string );

                    break; // without d++, not storing the dash
                }

                numericBytes[d++] = digit( ch );
            }

          // (sa2) Store the second sub-array.
          // - - - - - - - - - - - - - - - - - -
            // One leading digit, at least.
            try
            {
                sa2FirstDigit = digit( string.charAt( c++ ));
                numericBytes[d++] = sa2FirstDigit;
            }
            catch( IndexOutOfBoundsException _x ) { throw new MalformedID( "No digit after dash", string ); }

            // Any remaining digits.
            if( c < cN )
            {
                sa2IsMultiDigit = true;
                do numericBytes[d++] = digit(string.charAt(c++)); while( c < cN );
            }
            else sa2IsMultiDigit = false;
        }
        catch( BadCharacter _x ) { throw new MalformedID( "Bad character at index " + c, string ); }

      // (sa1NEnc) Encode the length of sa1.
      // - - - - - - - - - - - - - - - - - - -
        assert d == numericBytes.length - 1; // storing in final byte
        numericBytes[d] = (byte)(sa1N + SA1N_ENCODER);

      // - - -
        if( sa1N > SA1N_MIN && sa1FirstDigit == 0 ) throw new MalformedID( "Zero leader", string );

        if( sa2IsMultiDigit && sa2FirstDigit == 0 ) throw new MalformedID( "Zero leader after dash", string );
    }



   // --------------------------------------------------------------------------------------------------


    /** Answers whether the given string is a well formed serial number.  It must have a non-zero
      * length, a non-zero leading character, and each character must be one of the following radix 62
      * digits: {@value #RADIX_62_STRING}.
      */
    public static boolean isSerialForm( final String string ) // cf. digit(ch)
    {
        final int cN = string.length();
        if( cN == 0 ) return false; // empty string

        int c = 0;
        char ch = string.charAt( 0 );
        if( ch == '0' ) return false; // zero leader

        for( ;; )
        {
            if( ch < '0' ) return false;

            if( ch > '9' )
            {
                if( ch < 'A' ) return false;

                if( ch > 'Z' )
                {
                    if( ch < 'a' || ch > 'z' ) return false;
                }
            }

            ++c;
            if( c == cN ) return true;

            ch = string.charAt( c );
        }
    }



   // - O b j e c t ------------------------------------------------------------------------------------


    /** Answers whether o has a class of ID, and the same serial numbers as this identity tag.
      */
    public @Override boolean equals( final Object o )
    {
        if( o == this ) return true;

        if( o == null ) return false;

        if( !o.getClass().equals( ID.class )) return false;
          // Merely testing instanceof would allow this ID to equal a UDID.  That would be asymmetric if
          // this ID not subclassed, but just an unscoped ID, because no UDID can equal an unscoped ID.

        return equalsNumerically( (ID)o );
    }



    /** Derives a hash code from the instance number of this identity tag.
      */
    public final @Override int hashCode()
    {
        final int dEnd = numericBytes.length - 1; // end bound (index of last+1)
        int d = numericBytes[dEnd] - SA1N_ENCODER; // start index
        int hashCode = 31 + numericBytes[d++];
        while( d < dEnd ) hashCode = 31 * hashCode + numericBytes[d++];
        return hashCode;
    }



    /** Outputs the tri-serial string form of this identity tag.
      */
    public @Override String toString()
    {
        final StringBuilder out = new StringBuilder();
        toTriSerialString( out );
        return out.toString();
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


 // /** A comparator that sorts identity tags by their serial numbers and class names.  It accepts null
 //   * identity tags.
 //   */
 // static final Comparator<ID> comparatorNumerical = new Comparator<ID>()
 // {
 //     public int compare( final ID i, final ID j )
 //     {
 //         if( i == j ) return 0; // short cut, optimizing for a common case
 //
 //         if( i == null ) return -1;
 //
 //         if( j == null ) return 1;
 //
 //         final Class<?> iClass = i.getClass();
 //         final Class<?> jClass = j.getClass();
 //         if( iClass != jClass ) // != for speed; may still be 'equal' in edge cases, so test further:
 //         {
 //             final int result = iClass.getName().compareTo( jClass.getName() );
 //             if( result != 0 ) return result; // so compatible with equals(), which also tests class
 //         }
 //
 //         return i.compareNumerically( j );
 //     }
 // };



    /** Compares this identity tag to jID based on its serial numbers.  Does no preliminary "jID == this"
      * short cutting, but instead lets the caller do that.
      *
      *     @return A negative number, zero, or a positive number as this identity tag is less less than,
      *       equal to, or greater than jID.
      *     @throws NullPointerException if jID is null.
      *
      *     @see #equalsNumerically(ID)
      */
    final int compareNumerically( final ID jID )
    {
        final byte[] jBytes = jID.numericBytes;
        if( numericBytes == jBytes ) return 0;

        // (i) this identity tag, and (j) other
        final int iSA2End = numericBytes.length - 1; // end bound (index of last+1) in sub-array 2
        final int jSA2End =       jBytes.length - 1;
        final int iSA1N = numericBytes[iSA2End] - SA1N_ENCODER; // end bound in sub-array 1, start index in 2
        final int jSA1N =       jBytes[jSA2End] - SA1N_ENCODER;
        int result =               compareNumerically(     0, iSA1N,   jBytes,     0, jSA1N ); // sub-array 1
        if( result == 0 ) result = compareNumerically( iSA1N, iSA2End, jBytes, jSA1N, jSA2End ); //    "    2
        return result;
    }


        private int compareNumerically( int i, final int iEnd, final byte[] jBytes, int j, final int jEnd )
        {
            int result = Integer.compare( iEnd - i, jEnd - j ); // compare lengths
            if( result == 0 ) for( ;; )
            {
                result = Byte.compare( numericBytes[i], jBytes[j] );
                if( result != 0 ) break;

                ++i;
                if( i >= iEnd ) break;

                ++j;
            }

            return result;
        }



    private static byte digit( final char ch ) throws BadCharacter // cf. isSerialNumber(String)
    {
        final int digit;
        if( ch >= 'A' )
        {
            if( ch <= 'Z' ) digit = 11 + (ch - 'A') * 2;
            else if( ch >= 'a' && ch <= 'z' ) digit = 10 + (ch - 'a') * 2;
            else throw new BadCharacter();
        }
        else if( ch >= '0' && ch <= '9' ) digit = ch - '0';
        else throw new BadCharacter();

        return (byte)digit;
    }



    /** Answers whether this identity tag has the same serial numbers as oID.  This method will usually
      * answer faster than compareNumerically because it tests in reverse beginning with the instance
      * number, which is more likely to differ.  Does no preliminary "oID == this" short cutting, but
      * instead lets the caller do that.
      *
      *     @throws NullPointerException if oID is null.
      */
    final boolean equalsNumerically( final ID oID )
    {
        final byte[] oBytes = oID.numericBytes;
        if( numericBytes == oBytes ) return true;

        int b = numericBytes.length;
        if( b != oBytes.length ) return false;

        for( --b;; --b )
        {
            if( numericBytes[b] != oBytes[b] ) return false;

            if( b == 0 ) return true;
        }
    }



    /** The serial numbers of this identity tag together encoded as a byte array.  Do not modify it.
      */
    byte[] numericBytes() { return numericBytes; }

        // Three fields: sa1, sa2, sa1NEnc.  The first (sa1) is a sub-array storing the digits of the
        // domain number, followed by the digits of the generator number.  The generator number is
        // always two digits in length.  The first sub-array is constrained in length to 3..SA1N_MAX.
        //
        // The second field (sa2) is a sub-array storing the digits of the instance number.  It is
        // constrained to a minimum length of one, but has no maximum length.
        //
        // The final byte (sa1NEnc) stores the byte length of the first sub-array, plus SA1N_ENCODER.
        //
        // Serial numbers (domain, generator and instance) are all ordered big endian.  Each digit is
        // encoded as a single byte in the range 0..61 inclusive (radix 62), except the first digit of a
        // multi-digit domain or instance number is never a zero.

        private final byte[] numericBytes;



    private static final String RADIX_62_STRING =
      "0123456789aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ";
    // ^0        ^10       ^20       ^30       ^40       ^50       ^60


    private static final int SA1N_ENCODER; // value added to encode sa1N, subtracted to decode it



    private static final int SA1N_MIN = /*domain*/1 + /*generator*/2;

    private static final int SA1N_MAX; // minimum & maximum length allowed for first sub-array


        static
        {
            SA1N_ENCODER = Byte.MIN_VALUE - SA1N_MIN; // thus encoding 3..258 to byte -128..127
            SA1N_MAX = Byte.MAX_VALUE - SA1N_ENCODER;
        }



    final void toTriSerialString( final StringBuilder out )
    {
        final int sa2End = numericBytes.length - 1; // end bound (index of last+1) in sub-array 2
        final int sa1N = numericBytes[sa2End] - SA1N_ENCODER; // end bound in sub-array 1, start index in 2

        toTriSerialString( 0, sa1N, out );
        out.append( '-' );
        toTriSerialString( sa1N, sa2End, out );
    }


        private void toTriSerialString( int d, final int dEnd, final StringBuilder out )
        {
            do out.append(RADIX_62_STRING.charAt(numericBytes[d++])); while( d < dEnd );
        }


        /* * *
        - for example:

                   Typical                Minimal             Very large (unlikely so)
                -------------------    -------------------    -------------------
             /  domain   instance      domain   instance      domain   instance
             /      ||   |||                |   |               ||||   ||||||
           (a)      A400-qT7                100-0               tN8k03-5PsjEW
             /        ||                     ||                     ||
             /        generator              generator              generator

           (b)  aA/4/0/0/_/q/tT/7/__    1/0/0/_/0/__           t/nN/8/k/0/3/_/5/pP/s/j/eE/wW/__
                -------------------    -------------------    -------------------

        (a) proper string form
            - hyphenated string of radix 62 digits
              0123456789aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ
            - later we'll figure out how to weed out the offensive ones
            - joins 3 serial numbers: [domain] [generator] - [instance]
                [ domain
                    - 1..* digits, minimum 0 (1 for normal domains)
                    - as registered in repocaster registry
                    - might instead use a prefix registered in the Handle System
                        ( http://handle.net/
                        - except
                            - increases deployment friction, as each prefix costs $50 plus $50 per year
                                ( http://www.handle.net/service_agreement.html
                            - risky to depend on
                                - Handle System not well known, though it's been operational for years
                                - the larger "Digital Object Architecture" surrounding it looks shaky
                                    - it should be applicable to waymaking
                                        - both based on the storage of object data as "discrete data structures
                                          with unique, resolvable identity tags"
                                            ( http://www.dorepository.org/
                                    - but the DOA form is clumsy in comparison with my repocasters
                                        ( http://www.dorepository.org/documentation/RepositoryJavaAPIDocumentation.html
                [ generator
                    - 2 digits, minimal form 00
                    - domain + generator identifies a single tri-serial ID generator
                    - typically two generators operate per waykit provider
                        - even and odd
                        - each generates instance serial numbers
                        - if a genarator's numbers become too large for comfort,
                          then the administrator may replace it with a new generator
                [ instance
                    - 1..* digits, minimum 0
        (b) indexed storage in directories, e.g. for repocasters
            - each upper case digit is prefixed by its lower cased form (e.g. aA)
                - works for caseless file systems
                - sorts correctly with all file listers
            - underscore (_) instead of hypen to avoid confusing with option dashes on command line
            - double underscore (__) at end to distinguish storage directory
              from sister digit/hyphen directories
        - validation of new IDs
            - only a master rep bindery (typically remote) can validate a new tri-serial ID by binding it
            - vetting criteria will include testing the domain serial vs. requestor domain
              to ensure that the requestor is registered
        - reserved serial numbers
            - domain serial 0 is reserved for network use
          / - standard 0 generators are:
          /     [ 00p-INSTANCE
          /         ( referring to poll (p), not issue, per waymaker/gen/poll
          /         - reserved for issues
          /             - all issues have an identity tag in this form
          /                 ( examples: 00p-0 | 00p-torM
          /         - the instance serial number is largely unregulated in this case
          /             - i.e. collision avoidance is not enforced
          /             - only the following instance serial is predefined:
          /                 [ 1
          /                     - universally collective end
          /                     - predefinition required by top/android issue train
          // not clear how 00p-* form will be useful
          */



   // ==================================================================================================


    private static final @SuppressWarnings("serial") class BadCharacter extends Exception {}


}
