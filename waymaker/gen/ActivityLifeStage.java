package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.


/** Progressive stages in the
  * <a href='http://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle' target='_top'
  * >life of an activity</a>.  These exclude the cyclic operational states such as start and stop.
  */
public enum ActivityLifeStage
{

    /** Entered when the activity instance begins to initialize.
      */
    INITIALIZING,


    /** Entered at the start of
      * <a href='http://developer.android.com/reference/android/app/Activity.html#onCreate(android.os.Bundle)'
      * target='_top'>onCreate</a>.
      */
    CREATING,


    /** Entered at the end of
      * <a href='http://developer.android.com/reference/android/app/Activity.html#onCreate(android.os.Bundle)'
      * target='_top'>onCreate</a>.
      */
    CREATED,


    /** Entered at the start of
      * <a href='http://developer.android.com/reference/android/app/Activity.html#onDestroy()'
      * target='_top'>onDestroy</a>.
      */
    DESTROYING,


    /** Entered at the end of
      * <a href='http://developer.android.com/reference/android/app/Activity.html#onDestroy()'
      * target='_top'>onDestroy</a>.
      */
    DESTROYED;

}
