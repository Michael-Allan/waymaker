package waymaker.gen; // Copyright 2006-2009, 2013, 2015, Michael Allan.  Licence MIT-Waymaker.

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
  * <h3>Restriction for constructors and methods defaults to type</h3>
  *
  * <p>Any non-private constructor or method that lacks a ThreadRestricted or ThreadSafe annotation is
  * instead bound by the restriction of its declaring type (class or interface). See the <a
  * href='ThreadSafe.html#ctor-method-test'>step-by-step rules</a> in this case.
  *
  * <p>If the type lacks annotation, then the restriction is <a href='#unspecified'>unspecified</a>.</p>
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
  * <h3 id='touch'>Restriction to touch-synchronizing threads</h3>
  *
  * <p>The restriction may specify that threads must touch-synchronize.  For example:</p>
  *
  * <pre>
  *     &#064;ThreadRestricted("touch")
  *     &#064;ThreadRestricted("touch <em>object</em>")
  *     &#064;ThreadRestricted("touch <em>lock</em>")
  *     &#064;ThreadRestricted("touch <em>Class</em>.this")</pre>
  *
  * <p>Access is superficially thread-safe, but modifications to state variables are not
  * guaranteed visible across threads unless the threads touch-synchronize on a common
  * lock.  The form of touch-synchronization depends on whether the thread is reading
  * from state, or writing to it.  If reading, the thread must grab the lock at some point
  * <em>before</em> reading in order to invalidate its local memory cache.  The thread
  * need not continue to hold the lock while reading, but may release it beforehand.</p>
  *
  * <p>If writing to state, the thread must release the lock at some point <em>after</em>
  * writing in order to flush its local memory cache.  The thread need not hold the lock
  * at time of writing, but may subsequently grab it and release it.  Only after the lock
  * is released are the state changes guaranteed to reach main memory and become readable
  * by other threads that touch-synchronize.</p>
  *
  * <p>If the thread is both reading and writing, then it must touch-synchronize twice, grabbing the
  * lock before reading, and releasing it after writing.  It need not hold the lock in the meantime
  * unless it wants exclusive access, but may instead grab and immediately release the lock once
  * beforehand, then once again afterward.  In the case of a monitor lock, each such immediate
  * grab-and-release (each touch per se) is formed as an empty <code>synchronized</code> block.</p>
  *
  * <p>If no particular lock is specified, then any lock will suffice.  Visibility is guaranteed
  * only among threads that touch-synchronize on the same lock.  This guarantee rests on the
  * <a href='http://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.4.4' target='_top'
  * >Java memory model</a>, in particular on the rule that
  * an “unlock action on monitor m <em>synchronizes-with</em> all subsequent lock actions on m”
  * and thereby <em>happens-before</em> those actions.
  * (See also this <a target='_top' href='http://stackoverflow.com/questions/686415#31933260'
  * >answer on Stack Exchange</a>.)</p>
  *
  * <h3>Restriction unspecified</h3>
  *
  * <p>The restriction may be left unspecified.  This is the default value.  The following
  * forms of annotation are therefore equivalent:</p>
  *
  * <pre>
  *     &#064;ThreadRestricted
  *     &#064;ThreadRestricted("unspecified")
  *     <em>no annotation, neither &#064;ThreadRestricted nor &#064;ThreadSafe</em></pre>
  *
  * <p>An unspecified restriction simply means “not thread safe” without specifying a particular means
  * of synchronization.  Any general means will suffice.  The choice may vary from instance to instance,
  * with the decision falling to the code that accesses each instance.</p>
  *
  * <p>A common pattern is a constructor or factory method annotated as thread safe, while other members
  * default to unsafe.  This means that any thread may safely construct an instance of the type, and any
  * thread may first access it.  No synchronization is required between initial construction and access
  * (A1).  Only between A1 and each subsequent access (A2, A3, ...) is synchronization required, the
  * means of which is left to the programmer.</p>
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
