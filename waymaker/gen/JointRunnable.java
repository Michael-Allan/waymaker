package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.


/** A runnable that runs after joining a thread in order to synchronize with it.
  *
  *     @see <a href='package-summary.html#TermSync'>TermSync</a>
  */
public abstract class JointRunnable implements Runnable
{


    /** Constructs a JointRunnable.
      *
      *     @see #threadToJoin()
      */
    public @ThreadSafe JointRunnable( final Thread threadToJoin ) { this.threadToJoin = threadToJoin; }



   // --------------------------------------------------------------------------------------------------


    /** Implements the run after joining the thread.
      */
    public abstract void runAfterJoin();



    /** The thread to join before running.
      */
    public @ThreadSafe final Thread threadToJoin() { return threadToJoin; }


        private final Thread threadToJoin;



   // - R u n n a b l e --------------------------------------------------------------------------------


    /** {@linkplain Thread#join Joins} the thread and calls {@linkplain #runAfterJoin() runAfterJoin}.
      * Skips the call if the join is interrupted, and instead returns with interrupt status true.
      */
    public void run()
    {
        try{ threadToJoin.join(); }
        catch( InterruptedException _x )
        {
            Thread.currentThread().interrupt(); // pass it on
            return;
        }

        runAfterJoin();
    }


}
