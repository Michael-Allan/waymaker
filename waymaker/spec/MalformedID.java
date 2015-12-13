package waymaker.spec; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import waymaker.gen.ThreadSafe;


/** An exception thrown to reject an improperly formed identifier string.
  */
public @ThreadSafe @SuppressWarnings("serial") final class MalformedID extends Exception
{

    MalformedID( final String message, final String idString ) { super( message + ": " + idString ); }

}
