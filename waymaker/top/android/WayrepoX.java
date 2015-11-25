package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import waymaker.gen.ThreadSafe;


/** Thrown when a wayrepo is inaccessible.
  */
final @SuppressWarnings("serial") @ThreadSafe class WayrepoX extends java.io.IOException
{

    WayrepoX( Throwable cause ) { super( cause ); }


    WayrepoX( String message ) { super( message ); }


    WayrepoX( String message, Throwable cause ) { super( message, cause ); }


}
