package waymaker.spec; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import waymaker.gen.ThreadSafe;


/** Thrown when an identifier string of improper form is rejected.
  */
public @ThreadSafe @SuppressWarnings("serial") final class MalformedID extends Exception
{

    MalformedID( final String message, final String idString ) { super( message + ": " + idString ); }

}
