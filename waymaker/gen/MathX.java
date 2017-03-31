package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.


/** More math utilities.
  *
  *     @see java.lang.Math
  */
public @ThreadSafe final class MathX
{

    private MathX() {}



    /** Answers whether two numbers have the same sign; both being negative, or neither being negative.
      */
    public static boolean signsAgree( final int x, final int y ) { return (x ^ y) >= 0;  }
      // http://stackoverflow.com/a/66968/2402790



    /** Answers whether two numbers have opposite signs; exactly one being negative.
      */
    public static boolean signsDiffer( final int x, final int y ) { return (x ^ y) < 0;  }


}
