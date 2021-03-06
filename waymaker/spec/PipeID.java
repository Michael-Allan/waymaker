package waymaker.spec; // Copyright © 2015 Michael Allan.  Licence MIT.


/** A universally decisive identity tag for a pipe.
  */
public final class PipeID extends UDID implements VotingID
{

    private static final long serialVersionUID = 0L;

///////


    /** Constructs a PipeID by parsing an identity tag in basic, {@linkplain
      * #toTriSerialString(StringBuilder) unscoped string form}.
      *
      *     @see #toTriSerialScopedString(StringBuilder)
      */
    public PipeID( final String basicString ) throws MalformedID
    {
        super( basicString, basicString.length() );
    }



    /** Constructs a PipeID by adopting a byte array that encodes its serial numbers.  The new
      * identity tag will thenceforth own the given array; do not alter its contents.
      *
      *     @see #numericBytes()
      */
    PipeID( byte[] _numericBytes ) { super( _numericBytes ); }



    /** Constructs a PipeID by parsing an identity tag in basic, {@linkplain
      * #toTriSerialString(StringBuilder) unscoped string form}.
      *
      *     @param _cN The length of the substring to parse, beginning at index 0 in the string.
      *     @see #toTriSerialScopedString(StringBuilder)
      */
    PipeID( String _string, int _cN ) throws MalformedID { super( _string, _cN ); }



   // - T r i - S e r i a l - U D I D ------------------------------------------------------------------


    public String scope() { return SCOPE; }


        /** The nominal scope of decision for a pipe ID, which is {@value}.  When comparing
          * SCOPE and {@linkplain #scope() scope}, the == operator is effectively the same as the
          * {@linkplain #equals(Object) equals} method.
          */
        public static final String SCOPE = "pipe";



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


   // - U D I D ----------------------------------------------------------------------------------------


    byte scopeByte() { return SCOPE_BYTE; }


        /** The encoded form of the pipe scope in this version of the software.
          */
        static final byte SCOPE_BYTE = 1; // from PersonID, lexical sequence ends here


}
