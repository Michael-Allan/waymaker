package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import overware.gen.ThreadSafe;


/** Thrown when a vote count fails.
  */
final @SuppressWarnings("serial") @ThreadSafe class CountFailure extends Exception
{

    CountFailure( Throwable cause ) { super( cause ); }


    CountFailure( String message ) { super( message ); }


    CountFailure( String message, Throwable cause ) { super( message, cause ); }


}
