package waymaker.gen; // Copyright 2009, 2011, 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import java.lang.annotation.*;


/** Conveys one or more warnings.  The following specific warnings are documented:
  *
  * <h3>Co-construction</h3>
  * <pre>
  *     &#064;Warning("<var>co-extant</var> co-construct")
  *         // on constructor or factory method</pre>
  *
  * <p>Warns not to discard the construct and create a replacement during the life of the named
  * co-extant, because currently the construct does not unregister its callbacks from the co-extant, or
  * otherwise clean up after itself.</p>
  *
  * <h3>Dead code</h3>
  * <pre>
  *     &#064;Warning("unused code")</pre>
  *
  * <p>Informs that the code is not in actual use.</p>
  *
  * <h3>Init call</h3>
  * <pre>
  *     &#064;Warning("init call")
  *         // on final method of non-final class</pre>
  *
  * <p>Warns that the method is called from a constructor or other initializer where the call cannot be
  * bound dynamically.  In addition to the warning, the method should also have a final modifier to
  * explicitly bar overriding, even if it happens to be private in the current revision of the code.</p>
  *
  * <h3>No hold</h3>
  * <pre>
  *     &#064;Warning("no hold")
  *         // on class</pre>
  *
  * <p>Warns not to indefinitely hold an instance.  In addition to the warning, comments in the source
  * code should also reference the "no hold" annotation and explain the reason for it.</p>
  *
  * <h3>Non-API</h3>
  * <pre>
  *     &#064;Warning("non-API")
  *         // on public member</pre>
  *
  * <p>Warns that the member is not part of the general application programming interface.  It is
  * exposed for internal use only; it should not be used by ordinary API clients.</p>
  *
  * <h3>Thread restricted</h3>
  * <pre>
  *     &#064;Warning("thread restricted object")
  *         // on field, constructor, method
  *     &#064;Warning("thread restricted elements")
  *         // on field, constructor, method that dispenses an array
  *         // or collection of elements</pre>
  *
  * <p>The first version warns that objects read from the field, or created by the constructor, or
  * returned by the method are not thread safe.  Though the field, constructor or method itself may be
  * thread safe, the objects it dispenses are not.  The programmer is warned to consult the objects’ own
  * type API for the detailed restrictions.  (See also &#064;{@linkplain ThreadRestricted
  * ThreadRestricted}.)  The second version provides the same warning, but with regard to the elements
  * of the object.</p>
  *
  * <pre>
  *     &#064;Warning("thread restricted object, <var>restriction</var>")
  *         // on field, constructor, method
  *     &#064;Warning("thread restricted elements, <var>restriction</var>")
  *         // on field, constructor, method that dispenses an array
  *         // or collection of elements</pre>
  *
  * <p>These are like the previous warnings, except they specify the restriction.</p>
  *
  * <h3>Untested</h3>
  * <pre>
  *     &#064;Warning("untested")</pre>
  *
  * <p>Warns that a particular piece of code has never been tested.</p>
  */
  @Documented @Retention(RetentionPolicy.SOURCE)
  @Target({ ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD,
    ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PACKAGE, ElementType.PARAMETER,
    ElementType.TYPE })
public @interface Warning
{


    /** The literal form of the warnings, one for each.
      */
    public String[] value();


}
