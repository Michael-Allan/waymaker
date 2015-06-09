package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import android.content.*;
import android.net.Uri;
import android.os.*;
import android.support.v4.provider.DocumentFile;
import android.widget.TextView;
import overware.gen.*;

import static android.content.Intent.ACTION_OPEN_DOCUMENT_TREE;


public final class Overguidance extends android.app.Activity
{

    private static final PolyStator<Overguidance> stators = new PolyStator<>();

///////


    protected @Override void onCreate( Bundle _in )
    {
        super.onCreate( _in );
        final TextView v = new TextView( this );
        v.setText( "This will be a UI for overguideways." ); // TEST
        setContentView( v );
    }



    protected @Override void onRestoreInstanceState( final Bundle in )
    {
        System.err.println( " --- onRestoreInstanceState" ); // TEST
        super.onRestoreInstanceState( in );
        if( in != null )
        {
            StatorsAdapter.i = this;
            in.getParcelable( "StatorsAdapter" ); // restores by side effect
        }
    }



    protected @Override void onPostCreate( Bundle _in )
    {
        super.onPostCreate( _in );
        requestGuiderepo(); // TEST
    }



   // ------------------------------------------------------------------------------------


    protected @Override void onActivityResult( final int requestCode, final int resultCode,
      final Intent intent )
    {
        if( requestCode == 99 ) // TEST
        {
            if( resultCode == RESULT_OK )
            {
                final Uri uri = intent.getData();
                System.err.println( " --- uri=" + uri );
            }
            requestGuiderepo();
            return;
        }
        if( requestCode != GUIDEREPO_REQUEST ) return;

        if( resultCode == RESULT_OK )
        {
            if( intent == null ) throw new NullPointerException(); // not expected

            final Uri treeUri = intent.getData();
            final DocumentFile rootDir = DocumentFile.fromTreeUri( Overguidance.this, treeUri );
            for( DocumentFile file: rootDir.listFiles() )
            {
                if( "_autoindex-summary.html".equals( file.getName() ))
                {
                    try( final java.io.BufferedReader in = new java.io.BufferedReader( new java.io.InputStreamReader( getContentResolver().openInputStream( file.getUri() ))); )
                    {
                        System.err.println( " --- lastModified=" + java.text.DateFormat.getDateTimeInstance().format(file.lastModified()) ); // TEST
                        for( ;; )
                        {
                            final String l = in.readLine();
                            if( l == null ) break;

                            System.err.println( l ); // TEST
                        }
                    }
                    catch( final java.io.IOException x ) { throw new RuntimeException( x ); } // not expected
                }
            }
        }
        else System.err.println( " --- resultCode=" + resultCode ); // TEST, but deal with cancel too
        requestGuiderepo(); // TEST again
    }



    protected @Override void onSaveInstanceState( final Bundle out )
    {
        System.err.println( " --- onSaveInstanceState" ); // TEST
        super.onSaveInstanceState( out );
        out.putParcelable( "StatorsAdapter", new StatorsAdapter( this ) );
    }



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    private static final int GUIDEREPO_REQUEST = 1; // adding more?  change to enum



    private void requestGuiderepo() // TEST
    {
        System.out.println( " --- seeking documents"  );
        try
        {
            final Intent i = new Intent( android.content.Intent.ACTION_OPEN_DOCUMENT );
            i.addCategory( android.content.Intent.CATEGORY_OPENABLE );
            i.setType( "*/*" ); // must set type, or throws ActivityNotFoundException
            startActivityForResult( i, 99 ); // resume at onActivityResult
         /// rather want a directory, not any single document:
         // final Intent i = new Intent( ACTION_OPEN_DOCUMENT_TREE );
         // startActivityForResult( i, GUIDEREPO_REQUEST ); // resume at onActivityResult
        }
        catch( final ActivityNotFoundException x )
        {
            System.out.println( " --- no provider found: " + x );
        }
    }



    private final PollTouristV touristV = new PollTouristV( new PollTourist(
      new PrecountedPoll( new Poll1() )));


        static { stators.add( new Stator<Overguidance>()
        {
            public void get( final Overguidance i, final Parcel in )
            {
                Poll1.stators.get( i.touristV.tourist().poll().uncorrectedPoll(), in );
                PrecountedPoll.stators.get( i.touristV.tourist().poll(), in );
                PollTourist.stators.get( i.touristV.tourist(), in );
                PollTouristV.stators.get( i.touristV, in );
            }
            public void put( final Overguidance i, final Parcel out )
            {
                Poll1.stators.put( i.touristV.tourist().poll().uncorrectedPoll(), out );
                PrecountedPoll.stators.put( i.touristV.tourist().poll(), out );
                PollTourist.stators.put( i.touristV.tourist(), out );
                PollTouristV.stators.put( i.touristV, out );
            }
        });}



   // ====================================================================================


    /** A device to enable this activity’s stators to save a single, large parcel (Parcel)
      * of primitives and simple objects in a bundle instead of separate complex objects
      * (each a Parcelable).  Complex objects are problematic because often they contain
      * references to external dependencies that are difficult to recreate or reconnect
      * during restoration.  By instead using a StatorsAdapter, the activity can restore
      * its complex objects by re-constructing them in the usual manner, as from scratch,
      * using constructors and initializers to inject their dependencies.  Each object can
      * then restore the less problematic parts of its state from the parcel as usual.
      */
    private static final class StatorsAdapter implements Parcelable
    {

        // The parcel could alternatively be stored in the bundle by marshalling it into
        // bytes then unmarshalling the bytes back to a parcel on retrieval.  But that's
        // probably less efficient than using the parcel that the bundle already provides
        // to each Parcelable, which is what this StatorsAdapter, by masquerading as a
        // Parcelable, does.
        //
        // Alternatively the primitives and simple objects could be directly hashed into
        // the bundle rather than serialized into the parcel.  But they are numerous and
        // each would require a separate key and lookup, thus pointlessly reducing the
        // speed and increasing the complexity of the code.


        StatorsAdapter( Overguidance _i ) { i = _i; }


        static final Creator<StatorsAdapter> CREATOR = new Creator<StatorsAdapter>()
        {
            public StatorsAdapter createFromParcel( final Parcel in )
            {
                stators.get( i, in );
                return new StatorsAdapter( null ); // dummy construction, not actually used
            }

            public StatorsAdapter[] newArray( int _size )
            {
                throw new UnsupportedOperationException();
            }
        };


        static Overguidance i; // current or last Overguidance instance using a StatorsAdapter


       // - P a r c e l a b l e ----------------------------------------------------------


        public int describeContents() { return 0; }


        public void writeToParcel( final Parcel out, int _flags ) { stators.put( i, out ); }

    }


///////

    static { stators.seal(); }

}
