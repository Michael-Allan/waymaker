package waymaker.spec; // Copyright © 2015 Michael Allan.  Licence MIT.

import waymaker.gen.ThreadSafe;


/** An exception thrown to reject an improperly formed identity tag.
  */
public @ThreadSafe @SuppressWarnings("serial") final class MalformedID extends Exception
{

    /** Contructs a MalformedID.
      *
      *     @param idString The malformed string form of the identity tag.
      */
    MalformedID( final String message, final String idString ) { super( message + ": " + idString ); }

}
