package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.


/** A selection of {@linkplain Math Math} utilities that were added in Java 1.8, and are reproduced here
  * for use in programs that target earlier runtimes.
  */
public @ThreadSafe final class MathX08
{

    private MathX08() {}



 // /** @see Math#floorDiv(int,int)
 //   */
 /// reference not found
    public static int floorDiv( final int dividend, final int divisor )
    {
        int quotient = dividend / divisor;
        if( MathX.signsDiffer( dividend, divisor ))
        {
            if( quotient * divisor != dividend ) // if remainder is non-zero
            {
                --quotient;
            }
        }
        return quotient;
    }



    public static int incrementExact( final int value )
    {
        if( value == Integer.MAX_VALUE ) throw new ArithmeticException( "Numeric overflow" );

        return value + 1;
    }


}
