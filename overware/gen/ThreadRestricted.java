package overware.gen; // Copyright 2006-2009, 2013, Michael Allan.  Licence MIT-Overware.

import java.lang.annotation.*;


/** Warns that access to a field, constructor or method is restricted to a particular
  * thread, or to a thread that holds a particular lock.  The opposite of ThreadRestricted
  * is {@linkplain ThreadSafe ThreadSafe}.
  *
  * <p>The restriction applies to a field, constructor or method.  It does not necessarily
  * apply to any associated object that is read from the field, or created by the
  * constructor, or returned by the method.  The associated object's thread safety is
  * normally documented by its own type API.  However, if the type API documents no
  * specific restriction, then the object assumes the restriction of the field,
  * constructor or method whence it came.  [See also &#064;{@linkplain Warning
  * Warning}("thread restricted object") and &#064;{@linkplain Warning Warning}( "thread
  * restricted elements")].</p>
  *
  * <h3>Default restriction for methods</h3>
  *
  * <p>When applied to a type (class or interface), the ThreadRestricted annotation
  * specifies the default restriction for all public methods.  Any public method that
  * lacks its own restriction is bound by the default.  See the <a
  * href='ThreadSafe.html#method-test'>step-by-step rules</a> for resolving the thread
  * safety of public methods.  Methods alone have such defaults, not fields or
  * constructors.</p>
  *
  * <h3>Restriction to a particular thread</h3>
  *
  * <p>The restriction may be to a particular thread.  For instance, most Swing code is
  * restricted to the AWT event dispatch thread.  An appropriate annotation would be:</p>
  *
  * <pre>
  *     &#064;ThreadRestricted("AWT event dispatch")</pre>
  *
  * <p>As well, a constructor or method might implement an internal, runtime test of
  * compliance:</p>
  *
  * <pre>
  *     assert java.awt.EventQueue.isDispatchThread();</pre>
  *
  * <p>The thread may be specified as the constructor.  In this case, access is restricted
  * to the construction thread <em>as though for purposes of construction</em>.  This
  * means that access is forbidden even to the construction thread if another thread has
  * already accessed an instance member.</p>
  *
  * <pre>
  *     &#064;ThreadRestricted("constructor")</pre>
  *
  * <h3>Restriction to threads holding a particular lock</h3>
  *
  * <p>The restriction may specify a particular synchronization lock.  For example:</p>
  *
  * <pre>
  *     &#064;ThreadRestricted("holds <em>Class</em>.this")
  *     &#064;ThreadRestricted("holds <em>object</em>")
  *     &#064;ThreadRestricted("holds <em>lock</em>")</pre>
  *
  * <p>These specify that the thread must hold the monitor lock of the containing instance
  * (this), or of some other object; or that it must hold a particular {@linkplain
  * java.util.concurrent.locks.ReentrantLock ReentrantLock}.  As well, the restricted code
  * may internally test compliance at runtime:</p>
  *
  * <pre>
  *     assert Thread.holdsLock( <em>Class</em>.this );
  *     assert Thread.holdsLock( <em>object</em> );
  *     assert <em>lock</em>.isHeldByCurrentThread();</pre>
  *
  * <h3>Restriction to touch-synchronizing threads</h3>
  *
  * <p>The restriction may specify that threads must touch-synchronize.  For example:</p>
  *
  * <pre>
  *     &#064;ThreadRestricted("touch")
  *     &#064;ThreadRestricted("touch <em>Class</em>.this")
  *     &#064;ThreadRestricted("touch <em>object</em>")
  *     &#064;ThreadRestricted("touch <em>lock</em>")</pre>
  *
  * <p>These mean that access is superficially thread-safe, but modifications to state
  * variables are not guaranteed visible to other threads unless those threads
  * touch-synchronize on a common lock.  The method of touch-synchronizing depends on
  * whether the thread is reading from state, or writing to it.  If reading, the thread
  * must grab the lock at some point <em>before</em> reading (thus ensuring that its local
  * memory cache is invalidated).  The thread need not continue to hold the lock while
  * reading, but may release it beforehand.</p>
  *
  * <p>If writing to state, the thread must release the lock at some point <em>after</em>
  * writing (thus ensuring that its local memory cache is flushed).  The thread need not
  * actually hold the lock at time of writing, but may grab it and subsequently release
  * it.  Only after the lock is released are the state changes guaranteed to reach main
  * memory and become readable by other threads that touch-synchronize.</p>
  *
  * <p>If the thread is both reading and writing, then it must do both: grab the lock
  * before reading and release it after writing.  It need not hold onto the lock in the
  * meantime, but may grab it and release it once beforehand, then once afterward.</p>
  *
  * <p>If no particular lock is specified, then any lock (or locks) will suffice.
  * Visibility is guaranteed only among those threads that touch-synchronize on the same
  * lock.</p>
  *
  * <h3>Restriction unspecified</h3>
  *
  * <p>The restriction may be left unspecified.  This is the default value, so these are
  * equivalent:</p>
  *
  * <pre>
  *     &#064;ThreadRestricted
  *     &#064;ThreadRestricted("unspecified")</pre>
  *
  * <p>This form of the annotation is generally applied at the type level.  It defers the
  * choice of restriction to the runtime code that constructs instances of the type.  The
  * code may choose either a single threaded or a locking restriction, and its choices may
  * vary from instance to instance.  In other words, the unpecified thread restriction
  * simply means “not thread safe” and is handled accordingly.</p>
  *
  *     @see ThreadSafe
  */
  @Documented @Retention(RetentionPolicy.SOURCE) // till further retention needed
  @Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
public @interface ThreadRestricted
{


    /** Details of the thread restriction.
      */
    public String value() default "unspecified";


}
