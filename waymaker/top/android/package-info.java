/** A waykit user interface in the form of an Android application.
  * The top component is {@linkplain WayrangingV WayrangingV}
  *
  * <p>Code annotated &#064;{@linkplain waymaker.gen.ThreadRestricted ThreadRestricted}("app main") is
  * restricted to the application’s {@linkplain waymaker.gen.Application#isMainThread() main thread}
  * under the Android runtime.  Much of the library code for Android is likewise restricted, but with no
  * annotation or other indication.</p>
  */
package waymaker.top.android;
