package waymaker.gen; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.


/** A thread with a runnable target.
  *
  *     @param <T> The type of target.
  */
public @ThreadSafe final class TargetedThread<T extends Runnable> extends Thread
{


    /** Creates a TargetedThread.
      *
      *     @see #target()
      *     @see #getName()
      */
    public TargetedThread( final T target, final String name )
    {
        super( target, name );
        this.target = target;
    }



   // --------------------------------------------------------------------------------------------------


    /** The target whose run method to invoke when this thread is started, or null to invoke this
      * thread’s {@linkplain #run() own run method}.
      *
      *     @see Thread#Thread(Runnable)
      */
    public T target() { return target; }


        private final T target;


}
