package waymaker.gen; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.*;
import android.view.View;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;


/** Utilities for working with Android.
  */
public @ThreadSafe final class Android
{

    private Android() {}



    /** The maximum value of an alpha component, meaning “fully opaque”.
      *
      *     @see <a href='http://developer.android.com/reference/android/graphics/drawable/Drawable.html#getAlpha%28%29'
      *       target='_top'>Drawable.getAlpha</a>
      */
    public static final int ALPHA_OPAQUE = 255;



    /** Calculates the height of a graphical component from its top position (inclusive) and bottom
      * bound (exclusive).
      *
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getTop%28%29'
      *       target='_top'>View.getTop</a>
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getBottom%28%29'
      *       target='_top'>View.getBottom</a>
      */
    public static int height( final int top, final int bottom ) { return bottom - top; } // undocumented



    /** Converts HSV colour components to an ARGB colour.  This convenience method passes the arguments
      * through a common array, restricted to the application main thread.
      *
      *     @see <a href='http://developer.android.com/reference/android/graphics/Color.html#HSVToColor%28float[]%29'
      *       target='_top'>Color.HSVToColor</a>
      */
      @ThreadRestricted("app main")
    public static int HSVToColor( final float hue, final float saturation, final float value )
    {
        triFloat[0] = hue;
        triFloat[1] = saturation;
        triFloat[2] = value;
        return Color.HSVToColor( ALPHA_OPAQUE, triFloat );
    }



    /** Sets a uniform padding on the view.  This is a convenience method.
      *
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#setPadding%28int,%20int,%20int,%20int%29'
      *       target='_top'>View.setPadding</a>
      */
    public static void pad( final View view, final int p ) { view.setPadding( p, p, p, p ); }



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



    /** Returns the given bundle, or an immutable, empty surrogate if the bundle is null.
      */
    public static Bundle unnull( final Bundle bun ) { return bun == null? Bundle.EMPTY: bun; }
      // Bundle.EMPTY is immutable (undocumented in Android 23) owing to use of ArrayMap.EMPTY



    /** Calculates the width of a graphical component from its left position (inclusive) and right bound
      * (exclusive).
      *
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getLeft%28%29'
      *       target='_top'>View.getLeft</a>
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getRight%28%29'
      *       target='_top'>View.getRight</a>
      */
    public static int width( final int left, final int right ) { return right - left; } // undocumented



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static @ThreadRestricted("app main") final float[] triFloat = new float[3];


}
