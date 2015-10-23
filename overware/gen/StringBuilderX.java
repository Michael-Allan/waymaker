package overware.gen; // Copyright 2015, Michael Allan.  Licence MIT-Overware.


/** Utilities for working with {@linkplain StringBuilder string builders}.
  */
public @ThreadSafe final class StringBuilderX
{

    private StringBuilderX() {}



    /** Clears the string builder.
      *
      *     @return The same string builder.
      */
    public static StringBuilder clear( final StringBuilder b )
    {
        b.delete( 0, b.length() );
        return b;
    }


}
