package overware.gen; // Copyright 2005, Brian Goetz and Tim Peierls; 2006-2008, 2012, Michael Allan.  Released under the Creative Commons Attribution License (http://creativecommons.org/licenses/by/2.5).  Official home: http://www.jcip.net.  Any republication or derived work distributed in source code form must include this copyright and license notice.

import java.lang.annotation.*;


/** Indicates thread safety of fields, constructors and methods.  Access to thread-safe
  * fields and calls to thread-safe constructors and methods will never put the program
  * into an invalid state, regardless of how the runtime interleaves those actions, and
  * without requiring any additional synchronization or coordination on the part of the
  * caller.
  *
  * <p>The indication of thread safety applies to a field, constructor or method.  It
  * never applies to an object read from the field, or created by the constructor, or
  * returned by the method.  (Thread-safe fields, constructors and methods are not
  * constrained to dispense only thread-safe objects.)  Each object's own thread safety is
  * specified by its own API documentation.</p>
  *
  * <p>The opposite of ThreadSafe is {@linkplain ThreadRestricted ThreadRestricted}.</p>
  *
  * <h3>Applied to fields</h3>
  *
  * <p>An unannotated field is assumed to be thread safe only if it is final.  For
  * non-final fields, the safety of access (read or write) is specified by the rules of
  * the language (particularly by the <a
  * href='http://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.4'>memory
  * model</a>).  Strictly speaking, only certain types of volatile field (and their
  * equivalents in java.util.concurrent.{@linkplain java.util.concurrent.atomic atomic})
  * can be thread safe.  All others are subject to possibile read/write caching of field
  * values, by threads (caches being flushed at synchronization points).</p>
  *
  * <h3>Applied to constructors</h3>
  *
  * <p>An unannotated constructor is assumed to be thread safe.</p>
  *
  * <h3 id='method-test'>Applied to methods</h3>
  *
  * <p>The thread safety of a call, such as object.method(), depends on the object's type
  * (T), where T = object.getClass().  To determine the thread safety of the call:</p>
  *
  * <ol>
  *
  *     <li>Look at the API documentation (javadoc page) of type T.</li>
  *
  *     <li>Find the method declaration on that page.  If the method is not found on that
  *     page (it is inherited, and not overridden), then look at the javadoc page of the
  *     supertype (or its supertype, and so on, until you find the method).  Refer to the
  *     thread-safety annotation of the method.  <br>Or, if the method is
  *     unannotated:</li>
  *
  *     <li>Refer to the annotation of the method's <em>declaring</em> type (top of that
  *     same page). <br>Or, if that type is unannotated:</li>
  *
  *     <li>Return to the original, javadoc page of type T (if you had left it), and refer
  *     to the annotation of type T. <br>Failing that, unless you know otherwise:</li>
  *
  *     <li>Assume the method is {@linkplain ThreadRestricted ThreadRestricted}.</li>
  *
  *     </ol>
  *
  * <h3>Applied to types</h3>
  *
  * <p>Annotation of a class or interface specifies the default thread safety for its
  * public methods.  Only methods have such defaults, fields, constructors and static
  * member classes do not.  See the <a href='#method-test'>rules above</a> for determining
  * the thread safety of a method call.</p>
  *
  * <p>An unannotated Throwable is assumed to be thread safe.</p>
  *
  *     @see ThreadRestricted
  */
  @Documented @Retention(RetentionPolicy.SOURCE)
  @Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
public @interface ThreadSafe {}
