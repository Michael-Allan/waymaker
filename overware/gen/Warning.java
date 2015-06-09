package overware.gen; // Copyright 2009, 2011, 2015, Michael Allan.

import java.lang.annotation.*;


/** Conveys one or more warnings.  The following specific warnings are documented:
  *
  * <h3>Dead code</h3>
  * <pre>
  *     &#064;Warning("dead code")</pre>
  *
  * <p>Warns that a particular piece of code is no longer used, or never was used.  It
  * should be tested and possibly modified before use.</p>
  *
  * <h3>Init call</h3>
  * <pre>
  *     &#064;Warning("init call")
  *         // final method</pre>
  *
  * <p>Warns that the method is called from a constructor or other initializer where the
  * call is not bound dynamically.  Often the method will have an final modifier to
  * explicitly preclude overriding, even if it happens to be private or implicitly final
  * in the current revision of the code.</p>
  *
  * <h3>Untested</h3>
  * <pre>
  *     &#064;Warning("untested")</pre>
  *
  * <p>Warns that a particular piece of code has never been tested.</p>
  *
  * <h3>Non-API</h3>
  * <pre>
  *     &#064;Warning("non-API")
  *         // non-private member</pre>
  *
  * <p>Warns that the member is not part of the general application programming interface.
  * It is exposed for the use of particular clients only; it should not be used by other
  * API clients.</p>
  *
  * <h3>Thread restricted</h3>
  * <pre>
  *     &#064;Warning("thread restricted object")
  *         // field, constructor, method
  *     &#064;Warning("thread restricted elements")
  *         // field, constructor, method that dispenses an array
  *         // or collection of elements</pre>
  *
  * <p>The first version warns that objects read from the field, or created by the
  * constructor, or returned by the method are not thread safe.  Though the field,
  * constructor or method itself may be thread safe, the objects it dispenses are not.
  * The programmer is warned to consult the objects’ own type API for the detailed
  * restrictions.  (See also &#064;{@linkplain ThreadRestricted ThreadRestricted}.)  The
  * second version provides the same warning, but with regard to the elements of the
  * object.</p>
  *
  * <pre>
  *     &#064;Warning("thread restricted object, <em>restriction</em>")
  *         // field, constructor, method
  *     &#064;Warning("thread restricted elements, <em>restriction</em>")
  *         // field, constructor, method that dispenses an array
  *         // or collection of elements</pre>
  *
  * <p>These are the same as the previous warnings, only they specify the restriction.</p>
  */
  @Documented @Retention(RetentionPolicy.SOURCE)
  @Target({ ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
    ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD,
    ElementType.PACKAGE, ElementType.PARAMETER, ElementType.TYPE })
public @interface Warning
{


    /** The literal form of the warnings, one for each.
      */
    public String[] value();


}
