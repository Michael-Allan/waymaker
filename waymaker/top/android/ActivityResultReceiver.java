package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.content.Intent;
import android.os.*;
import waymaker.gen.*;


/** A receiver of a result from a
  * {@linkplain Wayranging#startActivityForResult(Intent,ActivityResultReceiver) purpose-launched} activity.
  * Each implementation must declare a creator, which may be a {@linkplain SimpleCreator simple creator}.
  */
public @ThreadRestricted("app main") abstract class ActivityResultReceiver implements Parcelable
{
    /* * *
    - receivers will often have a problem of recovering context at time of reception
        - being serializeable, they cannot (or ought not) solve it by being coded as inner classes,
          with a reference to the outer as context
            - rather they will normally be coded as top-level or static-member classes
        - to handle the (likely) common case of a receiver wanting an Android view as context:
            = code a generalized view walker
                - inspecting first:
                    - source code of View.findViewWithTag
                    < http://stackoverflow.com/questions/13887003/android-how-to-find-views-by-type
                    < http://android-wtf.com/2013/06/how-to-easily-traverse-any-view-hierarchy-in-android/?ckattempt=1
      */


    /** Receives the result.
      *
      *     @param resultCode An indication of communication state such as
      *       <a href='http://developer.android.com/reference/android/app/Activity.html#RESULT_OK'
      *         target='_top'>RESULT_OK</a> or
      *       <a href='http://developer.android.com/reference/android/app/Activity.html#RESULT_CANCELED'
      *         target='_top'>RESULT_CANCELED</a>.
      *     @param result The result of the activity in data form.
      *
      *     @see <a href='http://developer.android.com/reference/android/app/Activity.html#onActivityResult(int,+int,+android.content.Intent)'
      *       target='_top'>onActivityResult</a>(int,int,Intent)
      */
    public abstract void receive( int resultCode, Intent result );



   // - P a r c e l a b l e ----------------------------------------------------------------------------


    public final int describeContents() { return 0; }



    public final void writeToParcel( Parcel _out, int _flags ) {}


}

