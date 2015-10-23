package overware.spec; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import overware.gen.ThreadSafe;


/** Thrown when an identifier string of improper form is rejected.
  */
public @ThreadSafe @SuppressWarnings("serial") final class MalformedID extends Exception
{

    MalformedID( final String message, final String idString ) { super( message + ": " + idString ); }

}
