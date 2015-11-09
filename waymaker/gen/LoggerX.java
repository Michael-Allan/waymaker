package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import java.util.logging.*;
import waymaker.gen.ThreadSafe;


/** Utilities and policies for working with {@linkplain Logger loggers}.  As a matter of policy, never
  * log anything at CONFIG or lower in product code, but always at INFO or higher.  The purpose of this
  * restriction is to ensure that messages are visible on Android, which imposes a hard filter on level
  * CONFIG and lower.
  */
public @ThreadSafe final class LoggerX
{
   /* * *
   - Android logging via "java.util.logging" is hard-filtered to level INFO and higher
       - cannot find an easy way to pass the lower levels
       - the following (with TAG=wayWayranging) did not help
           > adb shell setprop log.tag.TAG VERBOSE
           > adb logcat TAG:V
     */

    private LoggerX() {}



    /** The common prefix for {@linkplain #getLogger(Class) class-based logger names}, or null if the
      * prefix varies according the package name of the class.  This is just the value of system
      * property "waymaker.g.LoggerX.classPrefix" at time of loading class LoggerX.
      */
    public static String classPrefix() { return classPrefix; }


        private static final String classPrefix = System.getProperty( "waymaker.g.LoggerX.classPrefix" );



    /** Finds or creates a logger for the given class.  The logger is named either by the concatenation
      * of the class prefix and simple class name (e.g. "ovFoo"), or, if the class prefix is null, by
      * the full class name ("waymaker.bar.Foo").
      *
      *     @see Logger#getLogger(String)
      *     @see Logger#getLogger(String,String)
      */
    public static Logger getLogger( final Class<?> cl )
    {
        return Logger.getLogger( classPrefix == null? cl.getName(): classPrefix + cl.getSimpleName() );
    }



    /** Logs test messages at all standard logging levels.
      */
    public static void test( final Logger logger )
    {
        logger.log( Level.OFF, "testing OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST and ALL..." );
        test( logger, Level.OFF ); // not normally used for messages, as likely irrepressable
        test( logger, Level.SEVERE );
        test( logger, Level.WARNING );
        test( logger, Level.INFO );
        test( logger, Level.CONFIG );
        test( logger, Level.FINE );
        test( logger, Level.FINER );
        test( logger, Level.FINEST );
        test( logger, Level.ALL ); // not normally used for messages, as unlikely ever to be logged
    }



    /** Logs a test message at the specified level.
      */
    public static void test( final Logger logger, final Level level )
    {
        logger.log( level, "this is " + level.toString() );
    }


}
