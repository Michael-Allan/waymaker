package waymaker.gen; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.content.SharedPreferences;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;


/** Utilities for working with Android.
  */
public @ThreadSafe final class Android
{

    private Android() {}



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


}
