package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import java.util.regex.Pattern;


/** A formal element in the structure of an <a href='../../../../way' target='_top'>ultimate way</a>:
  * either an end, transnorm or act.
  */
public interface Waynode
{
    /* * *
    - unlike count nodes, waynodes need not be compiled to any kind of whole on a server
        - they have no equivalent of vote sums
      */


    /** An immutable instance of an empty waynode.
      */
    public static final Waynode1 EMPTY_WAYNODE = new Waynode1( "" );



    /** The allowable pattern of a whole ({@linkplain java.util.regex.Matcher#matches() matches}) handle.
      */
    public static final Pattern HANDLE_PATTERN = Pattern.compile( "[\\p{javaLowerCase}]{0,2}" );
      // Lowercase letters only, for rapid readability in lists.
      // No more than 2 characters in length to allow use in narrow summary views.
      //
      // Using javaLowerCase as the only Unicode discriminator in common between OpenJDK 1.8 and Android
      // 23 (before move from Harmony to OpenJDK in Android 24).
      // https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
      // http://developer.android.com/reference/java/util/regex/Pattern.html



   // - W a y n o d e ----------------------------------------------------------------------------------


    /** The symbolic name of the waynode, from zero to two characters in length (never null).
      */
    public String handle();


}
