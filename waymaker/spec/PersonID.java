package waymaker.spec; // Copyright © 2015 Michael Allan.  Licence MIT.


/** A universally decisive identity tag for a person.
  */
public final class PersonID extends UDID implements VotingID
{

    private static final long serialVersionUID = 0L;

///////

    /* * *
    - normally 1:1 person:ID
        - 1:N is allowed
            - cannot prevent in case of persons
            - though multiple tri-serial IDs of a person are not necessarily tracked
                - need not be
                - usually not a problem
        - N:1 is disallowed
            - only expected in cases of identity theft
      */


    /** Constructs a PersonID by parsing an identity tag in basic, {@linkplain
      * #toTriSerialString(StringBuilder) unscoped string form}.
      *
      *     @see #toTriSerialScopedString(StringBuilder)
      */
    public PersonID( final String basicString ) throws MalformedID
    {
        super( basicString, basicString.length() );
    }



    /** Constructs a PersonID by adopting a byte array that encodes its serial numbers.  The new
      * identity tag will thenceforth own the given array; do not alter its contents.
      *
      *     @see #numericBytes()
      */
    PersonID( byte[] _numericBytes ) { super( _numericBytes ); }



    /** Constructs a PersonID by parsing an identity tag in basic, {@linkplain
      * #toTriSerialString(StringBuilder) unscoped string form}.
      *
      *     @param _cN The length of the substring to parse, beginning at index 0 in the string.
      *     @see #toTriSerialScopedString(StringBuilder)
      */
    PersonID( String _string, int _cN ) throws MalformedID { super( _string, _cN ); }



   // - T r i - S e r i a l - U D I D ------------------------------------------------------------------


    public String scope() { return SCOPE; }


        /** The nominal scope of decision for a personal identity tag, which is {@value}.  When
          * comparing SCOPE and {@linkplain #scope() scope}, the == operator is effectively the same as
          * the {@linkplain #equals(Object) equals} method.
          */
        public static final String SCOPE = "person";



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


   // - U D I D ----------------------------------------------------------------------------------------


    byte scopeByte() { return SCOPE_BYTE; }


        /** The encoded form of the personal scope in this version of the software.
          */
        static final byte SCOPE_BYTE = 0; // from UDID.SCOPE_BYTE_NULL, continues lexically with PipeID


}
