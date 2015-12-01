package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.


/** Utilities for working with {@linkplain Thread threads}.
  */
public @ThreadSafe final @Warning("unused code") class ThreadX
{

    private ThreadX() {}



 // /** Stubbornly attempts to {@linkplain Thread#join() join} the specified thread, retrying repeatedly
 //   * even in the face of interruptions.
 //   *
 //   *     @param logger The logger in which to report interruptions.
 //   */
 // public static void ensureJoin( final Thread thread, final java.util.logging.Logger logger )
 // {
 //     for( ;; )
 //     {
 //         try
 //         {
 //             thread.join();
 //             return;
 //         }
 //         catch( final InterruptedException x )
 //         {
 //             logger.info( "While trying to join thread \"" + thread.getName() + "\": " + x.toString() );
 //         }
 //     }
 // }
 /// Bad form to suppress an interruption when unsure how to handle it.  Should immediately forward
 /// instead, either by rethrowing, or by reinterrupting own thread.


}
