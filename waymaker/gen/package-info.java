/** Library code general to all domains.
  *
  * <h3>Android</h3>
  *
  * <p>Code annotated &#064;{@linkplain waymaker.gen.ThreadRestricted ThreadRestricted}("app main") is
  * restricted to the application’s {@linkplain Application#isMainThread() main thread} under the
  * Android runtime.  Much of the library code for Android is likewise restricted, but with no
  * annotation or other indication.</p>
  *
  * <h3 id='AutoRestore-public'>Automatic restoration by public access (AutoRestore-public)</h3>
  *
  * <p>Automatic restoration of an open dialogue will throw IllegalAccessException if the declaring
  * subclass of DialogFragment is not public.</p>
  *
  * <h3 id='ContentProviderClient-TS'>ContentProviderClient thread safety (ContentProviderClient-TS)</h3>
  *
  * <p><a href='http://developer.android.com/reference/android/content/ContentProviderClient.html'
  *  target='_top'>ContentProviderClient</a> is thread restricted, but not restricted to the
  * application’s main thread in particular.  An instance is safely accessible from any
  * single thread.  “Note that you should ... create a new ContentProviderClient instance
  * for each thread that will be performing operations.”</p>
  *
  * <h3 id='ContentResolver-TS'>ContentResolver thread safety (ContentResolver-TS)</h3>
  *
  * <p><a href='http://developer.android.com/reference/android/content/ContentResolver.html'
  *  target='_top'>ContentResolver</a> is indirectly documented as thread safe.
  * “Unlike ContentResolver, the methods here [in <a href=
  * 'http://developer.android.com/reference/android/content/ContentProviderClient.html'
  * target='_top'>ContentProviderClient</a>] ... are not thread safe”.</p>
  *
  * <h3 id='DocumentsContract-TS'>DocumentsContract thread safety (DocumentsContract-TS)</h3>
  *
  * <p><a href='http://developer.android.com/reference/android/provider/DocumentsContract.html'
  *  target='_top'>DocumentsContract</a> build<var>X</var>Uri() methods appear to be thread safe
  * judging by inspection of the source code.  Likewise for its parsing methods
  * get<var>X</var>(Uri).</p>
  *
  * <h3 id='ParcelReuse'>Parcel reuse (ParcelReuse)</h3>
  *
  * <p><a href='http://developer.android.com/reference/android/os/Parcel.html' target='_top'>Parcel</a>.recycle
  * clears the parcel by parcel.freeBuffer, which unfortunately is private.
  * Otherwise it might be possible to reuse a parcel instead of making repeated calls to obtain/recycle.
  * Alternatively it might be possible to clear it for reuse by calling parcel.setDataSize(0),
  * but that method is too poorly documented to rely on.</p>
  *
  * <h3 id='Parcel-TS'>Parcel thread safety (Parcel-TS)</h3>
  *
  * <p><a href='http://developer.android.com/reference/android/os/Parcel.html' target='_top'
  * >Parcel</a>.obtain and .recycle methods appear to be thread safe judging by inspection
  * of the source code.</p>
  *
  * <h3 id='FreezeSync'>Thread synchronization by final freezing (FreezeSync)</h3>
  *
  * <p>“A thread that can only see a reference to an object after that object has been completely initialized
  * is guaranteed to see the correctly initialized values for that object's final fields...
  * [including] versions of any object or array referenced by those final fields
  * that are at least as up-to-date as the final fields are.”
  * (<a href='http://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.5' target='_top'>17.5</a>)
  * That’s an attempt to summarize the ‘freeze’ rule for final fields
  * (<a href='http://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.5.1' target='_top'>17.5.1</a>),
  * which is difficult to parse.  See also
  * <a href='http://tech.puredanger.com/2008/11/26/jmm-and-final-field-freeze' target='_top'>JMM and final field freeze</a>.</p>
  *
  * <p>In other words, given a fully constructed and properly referenced instance, a read from one of
  * its final fields <i>happens-after</i> all initialization of values accessible through that field.
  * The initial values of a composite member such as an array or collection, for example, are guaranteed
  * visible to any reader who accesses it through a final field.</p>
  *
  * <h3 id='StartSync'>Thread synchronization by starting (StartSync)</h3>
  *
  * <p>“A call to start() on a thread <i>happens-before</i> any actions in the started thread.”
  * (<a href='http://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.4.5' target='_top'>17.4.5</a>)
  * Therefore when thread T1 starts thread T2, it thereby ensures that every previous
  * action of T1 is visible to all subsequent actions of T2.</p>
  *
  * <h3 id='TermSync'>Thread synchronization by termination detecting (TermSync)</h3>
  *
  * <p>“The final action in a thread T1 <i>synchronizes-with</i> any action in another
  * thread T2 that detects that T1 has terminated.”  (Java language specification,
  * <a href='http://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.4.4' target='_top'>17.4.4</a>)
  * “T2 may accomplish this by calling T1.isAlive() or T1.join().”  So T2 ensures that
  * every previous action of T1 <i>happens-before</i> the moment of detection and is
  * thereby visible to all subsequent actions of T2.</p>
  *
  * <p>“All actions in a thread <i>happen-before</i> any other thread successfully returns
  * from a join() on that thread.”
  * (<a href='http://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.4.5' target='_top'>17.4.5</a>)
  * </p>
  */
package waymaker.gen;
