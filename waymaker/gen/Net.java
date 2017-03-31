package waymaker.gen; // Copyright © 2010 Michael Allan.  Licence MIT.

import java.io.IOException;
import java.net.*;


/** Networking utilities.
  *
  *     @see java.net
  */
public @ThreadSafe final class Net
{

    private Net() {}



    /** Like c.{@linkplain URLConnection#connect() connect}, but wraps any ConnectException in a more
      * informative IOException.
      */
    public static void connect( final URLConnection c ) throws IOException
    {
        try { c.connect(); }
        catch( final ConnectException x )
        {
            throw new IOException( "Unable to reach " + c.getURL(), x ); // adding info
        }
    }



    /** Like url.{@linkplain URL#openConnection() openConnection} (sic), but conveniently throws an
      * IOException if the resulting connection is non-HTTP.
      */
    public static HttpURLConnection openHttpConnection( final URL url ) throws IOException
    {
        final URLConnection c = url.openConnection();
        if( c instanceof HttpURLConnection ) return (HttpURLConnection)c;

        throw new IOException( "Unexpected connection type " + c.getClass().getName() );
    }


}
