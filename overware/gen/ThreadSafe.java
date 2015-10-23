package overware.gen; // Copyright 2005, Brian Goetz and Tim Peierls; 2006-2008, 2012, Michael Allan.  Released under the Creative Commons Attribution License (http://creativecommons.org/licenses/by/2.5).  Official home: http://www.jcip.net.  Any republication or derived work distributed in source code form must include this copyright and license notice.

import java.lang.annotation.*;


/** Indicates thread safety of fields, constructors and methods.  Access to thread-safe
  * fields and calls to thread-safe constructors and methods will never put the program
  * into an invalid state, regardless of how the runtime interleaves those actions, and
  * without requiring any additional synchronization or coordination on the part of the
  * caller.
  *
  * <p>The indication of thread safety applies only to a formal member, such as a field,
  * constructor or method.  It never applies to any actual object read from the field, or
  * created by the constructor, or returned by the method.  (Thread-safe members are not
  * constrained to dispense only thread-safe objects.)  Each object’s own thread safety is
  * specified by the API documentation of its type.</p>
  *
  * <p>The opposite of ThreadSafe is {@linkplain ThreadRestricted ThreadRestricted}.</p>
  *
  * <h3>Applied to a field</h3>
  *
  * <p>An unannotated field is assumed to be thread safe only if it is final.  For
  * non-final fields, the safety of access (read or write) is specified by the rules of
  * the language (particularly by the <a
  * href='http://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.4'
  * target='_top'>memory model</a>).  Strictly speaking, only certain types of volatile
  * field (and their equivalents in java.util.concurrent.{@linkplain
  * java.util.concurrent.atomic atomic}) can be thread safe.  All others are subject to
  * possibile read/write caching of field values, by threads (caches being flushed at
  * synchronization points).</p>
  *
  * <h3 id='ctor-method-test'>Applied to a constructor or method</h3>
  *
  * <p>The thread safety of a call to a non-private constructor or method of an object o
  * depends on type T = o.getClass().  To determine the thread safety of the call:</p>
  *
  * <ol>
  *
  *     <li>Look at the API documentation (javadoc) page for type T.</li>
  *
  *     <li>Find the declaration of the constructor or method on that page.  If it is not
  *     declared on that page (in other words it is inherited and not overridden), then
  *     look at the javadoc page of the supertype, and so on through all ancestor types
  *     until you find the declaration.  Refer to the annotation of thread safety in that
  *     declaration.  <br>Or, if the declaration is unannotated:</li>
  *
  *     <li>Using the link at the top of that page, refer to the declaration of the same
  *     constructor or method in the <em>declaring</em> type. <br>Or, if that declaration
  *     too is unannotated:</li>
  *
  *     <li>On the original javadoc page of type T, refer to the annotation of T itself.
  *     <br>Failing that, unless you know otherwise:</li>
  *
  *     <li>Assume the call is {@linkplain ThreadRestricted ThreadRestricted}.</li>
  *
  *     </ol>
  *
  * <h3>Applied to a type</h3>
  *
  * <p>Annotation of a class, interface or other type specifies the default thread safety
  * of all non-private constructors and methods of that type.  This default specification
  * applies only to constructors and methods, not to fields or static member classes.
  *
  * <p>Some types are assumed to be thread safe.  These include:</p>
  * <ul>
  *     <li>Basic immutable types in the Java library, such as java.lang.String</li>
  *     <li>Class java.lang.Throwable and its subclasses
  *         <!-- including init methods, based on source inspection --></li>
  *     </ul>
  *
  *     @see ThreadRestricted
  */
  @Documented @Retention(RetentionPolicy.SOURCE)
  @Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
public @interface ThreadSafe {}
