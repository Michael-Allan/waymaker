package waymaker.gen; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.content.SharedPreferences;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;


/** Utilities for working with Android.
  */
public @ThreadSafe final class Android
{

    private Android() {}



    /** Calculates the height of a graphical component from its top position (inclusive) and bottom
      * bound (exclusive).
      *
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getTop%28%29'
      *       target='_top'>getTop</a>
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getBottom%28%29'
      *       target='_top'>getBottom</a>
      */
    public static int height( final int top, final int bottom ) { return bottom - top; } // undocumented



    /** Registers the listener with the preference store and ensures it will be unregistered on
      * destruction.  This is a convenience method that creates a separate destructible for the
      * unregistration and adds it to the given destructor.  Thereby it also defeats the {@linkplain
      * Application#preferences() weak reference} in the store register.
      */
    public static void registerDestructibly( final SharedPreferences preferenceStore,
      final OnSharedPreferenceChangeListener listener, final Destructor destructor )
    {
        preferenceStore.registerOnSharedPreferenceChangeListener( listener );
        destructor.add( new Destructible()
        {
            public void close() { preferenceStore.unregisterOnSharedPreferenceChangeListener( listener ); }
        });
    }



    /** Calculates the width of a graphical component from its left position (inclusive) and right bound
      * (exclusive).
      *
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getLeft%28%29'
      *       target='_top'>getLeft</a>
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getRight%28%29'
      *       target='_top'>getRight</a>
      */
    public static int width( final int left, final int right ) { return right - left; } // undocumented


}
