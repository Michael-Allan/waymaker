package waymaker.gen; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.app.Activity;
import android.os.Bundle;


/** An Android activity with member extensions.
  */
public @ThreadRestricted("app main") class ActivityX extends Activity
{


    /** The size of a scale-independent pixel as measured in physical pixels.  Returns the value of
      * getResources.getDisplayMetrics.<a href='http://developer.android.com/reference/android/util/DisplayMetrics.html#scaledDensity' target='_top'>scaledDensity</a>
      * cached (for speed) when this activity was created.
      * The value is {@linkplain ApplicationX#pxDP() pxDP} “scaled by the user’s font size”,
      * larger or smaller according to preference.  A change of font size, e.g. by the Settings app,
      * will recreate the activity and thereby refresh the value.
      *
      *     @see <a href='http://developer.android.com/guide/topics/resources/more-resources.html#Dimension'
      *       target='_top'>Resources § Dimension</a>
      *     @see <a href='http://developer.android.com/guide/topics/resources/runtime-changes.html'
      *       target='_top'>Handling runtime changes</a>
      */
    public final float pxSP() { return pxSP; }


        private float pxSP; /* Final after onCreate.  Bound here to activity because change of font size
          by test user (Settings app, 2016) does not recreate ApplicationX. */



   // - A c t i v i t y --------------------------------------------------------------------------------


    protected @Override void onCreate( final Bundle inB )
    {
        super.onCreate( inB ); // obeying API
        pxSP = getResources().getDisplayMetrics().scaledDensity;
    }


}
