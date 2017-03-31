package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.


/** Utilities for working with {@linkplain Throwable throwables}.
  */
public @ThreadSafe final class ThrowableX
{

    private ThrowableX() {}



    /** Contructs a textual summary of the throwable and its nested causes, appending the
      * whole to the string builder.
      */
    public static void toStringDeeply( final Throwable t, StringBuilder b )
    {
        Throwable cause = t.getCause();
        if( cause != null && cause.toString().equals(t.getMessage()) )
        {
            // cause already summarized as though t constructed by Throwable(Throwable)
            b.append( t.toString() );
            return; // it will probably suffice
        }

        int causeCount = 0;
        for( cause = t;; )
        {
            b.append( cause.toString() );
            cause = cause.getCause();
            if( cause == null ) break;

            ++causeCount;
            b.append( " (" );
        }
        while( causeCount > 0 )
        {
            b.append( ')' );
            --causeCount;
        }
    }


}
