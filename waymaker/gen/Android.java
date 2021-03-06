package waymaker.gen; // Copyright © 2016 Michael Allan.  Licence MIT.

import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.lang.reflect.Field;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;


/** Utilities for working with Android.
  */
public @ThreadSafe final class Android
{

    private Android() {}



    /** The maximum value of an alpha component, meaning “fully opaque”.
      *
      *     @see <a href='http://developer.android.com/reference/android/graphics/drawable/Drawable.html#getAlpha()'
      *       target='_top'>Drawable.getAlpha</a>
      */
    public static final int ALPHA_OPAQUE = 255;



    /** Calculates the bottom bound (exclusive) of a graphical component from its top position
      * (inclusive) and height.
      *
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getTop()'
      *       target='_top'>View.getTop</a>
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getBottom()'
      *       target='_top'>View.getBottom</a>
      */
    public static int bottom( final int top, final int height ) { return top + height; } // undocumented



    /** Returns the requested service, or throws a NullPointerException if the service is unavailable in
      * the given context.  This is a convenience method.
      *
      *     @see <a href='http://developer.android.com/reference/android/content/Context.html#getSystemService(java.lang.Class%3CT%3E)'
      *       target='_top'>Context.getSystemService</a>
      */
    public static final <T> T ensureSystemService( final Class<T> serviceClass, final Context context )
    {
        final T service = context.getSystemService( serviceClass );
        if( service == null ) throw new IllegalStateException();

        return service;
    }



    /** Calculates the height of a graphical component from its top position (inclusive) and bottom
      * bound (exclusive).
      *
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getTop()'
      *       target='_top'>View.getTop</a>
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getBottom()'
      *       target='_top'>View.getBottom</a>
      */
    public static int height( final int top, final int bottom ) { return bottom - top; } // undocumented



    /** Returns a string representation of the given Intent flags.  Inefficient, this method is meant
      * only for test purposes.
      *
      *     @see <a href='https://developer.android.com/reference/android/content/Intent.html#getFlags()'
      *       target='_top'>getFlags</a>
      */
    public static @Warning("non-API") String intentFlagsToString( final int flags )
    {
        // after joecks, https://gist.github.com/joecks/4559331
        final int countEncoded = Integer.bitCount( flags );
        String s = Integer.toString(countEncoded) + " flags";
        if( flags > 0 )
        {
            s += " including";
            int countNamed = 0;
            for( final Field field: Intent.class.getDeclaredFields() )
            {
                if( !field.getName().startsWith( "FLAG_" )) continue;

                try
                {
                    final int flag = field.getInt( null );
                    if( (flag & flags) == 0 ) continue;

                    s += " ";
                    s += field.getName();
                    if( Integer.bitCount(flag) == 1 ) ++countNamed; // count only 1-bit flags, just in case
                }
                catch( final IllegalAccessException x ) { throw new RuntimeException( x ); }
            }

            for( int c = countNamed; c < countEncoded; ++c ) s += " UNDECLARED";
        }
        return s;
    }



    /** Converts HSV colour components to an ARGB colour.  This convenience method passes the arguments
      * through a common array, restricted to the application main thread.
      *
      *     @see <a href='http://developer.android.com/reference/android/graphics/Color.html#HSVToColor(float[])'
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



    /** Creates a text view with the given text.  This is a convenience method.
      */
    public static TextView newTextView( final String text, final Context context )
    {
        final TextView view = new TextView( context );
        view.setText( text );
        return view;
    }



    /** Sets a uniform padding on the view.  This is a convenience method.
      *
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#setPadding(int,%20int,%20int,%20int)'
      *       target='_top'>View.setPadding</a>
      */
    public static void pad( final View view, final int p ) { view.setPadding( p, p, p, p ); }



    /** Registers the listener with the preference store and ensures it will be unregistered on
      * destruction.  This is a convenience method that creates a separate destructible for the
      * unregistration and adds it to the given destructor.  Thereby it also defeats the {@linkplain
      * ApplicationX#preferences() weak reference} in the store register.
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



    /** Calculates the right bound (exclusive) of a graphical component from its left position
      * (inclusive) and width.
      *
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getLeft()'
      *       target='_top'>View.getLeft</a>
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getRight()'
      *       target='_top'>View.getRight</a>
      */
    public static int right( final int left, final int width ) { return left + width; } // undocumented



    /** Returns the given bundle, or an immutable, empty surrogate if the bundle is null.
      */
    public static Bundle unnull( final Bundle bun ) { return bun == null? Bundle.EMPTY: bun; }
      // Bundle.EMPTY is immutable (undocumented in Android 23) owing to use of ArrayMap.EMPTY



    /** Calculates the width of a graphical component from its left position (inclusive) and right bound
      * (exclusive).
      *
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getLeft()'
      *       target='_top'>View.getLeft</a>
      *     @see <a href='http://developer.android.com/reference/android/view/View.html#getRight()'
      *       target='_top'>View.getRight</a>
      */
    public static int width( final int left, final int right ) { return right - left; } // undocumented



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static @ThreadRestricted("app main") final float[] triFloat = new float[3];


}
