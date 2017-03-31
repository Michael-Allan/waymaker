package waymaker.top.android; // Copyright © 2015 Michael Allan.  Licence MIT.

import waymaker.gen.ThreadSafe;


/** An exception thrown on a failed attempt to communicate with a wayrepo.
  */
public final @SuppressWarnings("serial") @ThreadSafe class WayrepoAccessFailure extends java.io.IOException
{

    WayrepoAccessFailure( Throwable cause ) { super( cause ); }


    WayrepoAccessFailure( String message ) { super( message ); }


    WayrepoAccessFailure( String message, Throwable cause ) { super( message, cause ); }


}
