package waymaker.gen; // Copyright © 2016 Michael Allan.  Licence MIT.


/** A joint runnable that asks permission for each run.
  */
public abstract class GuardedJointRunnable extends JointRunnable
{


    /** Constructs a GuardedJointRunnable.
      *
      *     @see #threadToJoin()
      */
    public @ThreadSafe GuardedJointRunnable( final Thread threadToJoin ) { super( threadToJoin ); }



   // --------------------------------------------------------------------------------------------------


    /** Called before the join of each run, this method answers whether the run should proceed.
      */
    public abstract boolean toProceed();



   // - R u n n a b l e --------------------------------------------------------------------------------


    /** {@linkplain #toProceed() Asks permission} and proceeds only if allowed.  {@inheritDoc}
      */
    public final void run() { if( toProceed() ) super.run(); }


}
