package waymaker.top.android; // Copyright © 2015 Michael Allan.  Licence MIT.

import waymaker.gen.ThreadSafe;


/** An exception thrown when a vote count fails.
  */
public final @SuppressWarnings("serial") @ThreadSafe class CountFailure extends Exception
{

    CountFailure( Throwable cause ) { super( cause ); }


    CountFailure( String message ) { super( message ); }


    CountFailure( String message, Throwable cause ) { super( message, cause ); }


}
