package waymaker.top.android; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.content.Intent;
import android.os.*;
import android.view.Window;
import waymaker.gen.*;
import waymaker.spec.*;

import static waymaker.gen.ActivityLifeStage.*;


/** Exploring and elaborating an <a href='../../../../way' target='_top'>ultimate way</a>, which is the
  * principle activity of this {@linkplain WaykitUI waykit UI}.
  *
  *     @extra waymaker.top.android.Wayranging.toIntroducePolls (boolean)
  *       Whether to {@linkplain PollIntroducer introduce polls}.  A false value is useful when testing
  *       because the initial introduction slows the start of the application.  The default is true.
  */
public @ThreadRestricted("app main") final class Wayranging extends android.app.Activity
{

    private static final PolyStator<Wayranging> stators = new PolyStator<>();

///////


    { lifeStage = INITIALIZING; }



   // ` c r e a t i o n ````````````````````````````````````````````````````````````````````````````````


    protected @Override void onCreate( final Bundle inB )
    {
        isCreatedAnew = inB == null;
        assert lifeStage.compareTo(CREATING) < 0: "One creation per instance, no colliding creations";
        lifeStage = CREATING;
        lifeStageBell.ring();
        super.onCreate( inB );
        create1();
        if( isCreatedAnew ) create2( null );
        else
        {
            logger.info( "Restoring activity state from bundle: " + inB );
            final byte[] state = inB.getByteArray( Wayranging.class.getName() ); // get state from bundle
            // Not following the Android convention of using the bundle to save and restore each complex
            // object as a whole Parcelable, complete with its references to external dependencies.  Rather
            // restoring the complex whole as originally created using constructors and/or initializers to
            // inject its external dependencies.  All that remains therefore is to restore the state of the
            // internal variables of each reconstructed object.  All state is restored from a single parcel:
            final Parcel inP = Parcel.obtain();
            try
            {
                inP.unmarshall( state, 0, state.length ); // (sic) form state into parcel
                inP.setDataPosition( 0 ); // (undocumented requirement)
                create2( inP );
            }
            finally { inP.recycle(); }
        }
        create3();
        lifeStage = CREATED;
        lifeStageBell.ring();
    }



    private void create1()
    {
        final Window w = getWindow(); // note: w.requestFeature wants to precede setContentView
        w.requestFeature( Window.FEATURE_NO_TITLE ); // omit activity bar, or whatever else shows title
    }



    /** @param inP The parceled state to restore, or null to restore none.
      */
    private void create2( final Parcel inP/*grep CtorRestore*/ ) // see Recreating an Activity [RA]
    {
        final boolean toInitClass;
        if( wasConstructorCalled ) toInitClass = false;
        else
        {
            toInitClass = true;
            wasConstructorCalled = true;
        }
        if( inP != null )
        {
            KittedPolyStatorSR.openToThread();
            stators.restore( this, inP ); // saved by stators in static inits further below
        }

      // Actor identifier.
      // - - - - - - - - - -
        if( toInitClass ) stators.add( new StateSaver<Wayranging>()
        {
            public void save( final Wayranging wr, final Parcel out )
            {
                AndroidXID.writeUDIDOrNull( wr.actorIdentifier.get(), out );
            }
        });
        actorIdentifier = new BelledVariable<VotingID>( inP == null? null:
          (VotingID)AndroidXID.readUDIDOrNull(inP) ); // CtorRestore to cleanly construct with restored state

      // Forests.
      // - - - - -
        if( toInitClass ) stators.add( new StateSaver<Wayranging>()
        {
            public void save( final Wayranging wr, final Parcel out )
            {
                ForestCache.stators.save( wr.forests, out );
            }
        });
        forests = new ForestCache( inP/*by CtorRestore*/ );
        if( inP == null ) forests.startRefreshFromWayrepo( wk.wayrepoTreeLoc() );

      // Poll namer.
      // - - - - - - -
        if( toInitClass ) stators.add( new StateSaver<Wayranging>()
        {
            public void save( final Wayranging wr, final Parcel out )
            {
                out.writeString( wr.pollNamer.get() );
            }
        });
        pollNamer = new BelledNonNull<String>( inP == null? "end": inP.readString() );
          // CtorRestore to cleanly construct with restored state

      // - - -
        if( toInitClass ) stators.seal();
    }



    private void create3()
    {
        forester = new Forester( this );
        final boolean toIntroducePolls = Android.unnull( getIntent().getExtras() )
          .getBoolean( Wayranging.class.getName() + ".toIntroducePolls", /*default*/true );
        if( toIntroducePolls ) new PollIntroducer( this );
        setContentView( new WayrangingV( this ));
    }



   // --------------------------------------------------------------------------------------------------


    /** The identifier of the wayranging actor, if any.
      *
      *     @see #position(String,VotingID)
      */
    public BelledVariable<VotingID> actorIdentifier() { return actorIdentifier;}


        private BelledVariable<VotingID> actorIdentifier; // final after create2, which adds stator



    /** The forester of this wayranging activity.
      */
    public Forester forester() { return forester; }


        private Forester forester; // final after create3



    /** The pollar forests of this wayranging activity.
      */
    public ForestCache forests() { return forests; }


        private ForestCache forests; // final after create2, which adds stator



    /** Answers whether this activity’s creation is a creation from scratch, as opposed to {@linkplain
      * #onRestoreInstanceState(Bundle) saved state}.
      *
      *     @throws IllegalStateException if the life stage is less than CREATING.
      */
    public boolean isCreatedAnew()
    {
        if( lifeStage.compareTo(CREATING) < 0 ) throw new IllegalStateException();

        return isCreatedAnew;
    }


        private boolean isCreatedAnew;



    /** The life stage of this activity.  Initially set to INITIALIZING, any subsequent change to the
      * return value will be signalled by the life stage bell.
      */
    public ActivityLifeStage lifeStage() { return lifeStage; }


        private ActivityLifeStage lifeStage;



    /** A bell that rings when the life stage changes.
      */
    public Bell<Changed> lifeStageBell() { return lifeStageBell; }


        private final ReRinger<Changed> lifeStageBell = Changed.newReRinger();



    /** The namer of the poll on which wayranging now focuses.
      *
      *     @see #position(String,VotingID)
      */
    public BelledNonNull<String> pollNamer() { return pollNamer;}


        private BelledNonNull<String> pollNamer; // final after create2, which adds stator



    /** Atomically sets the poll position on which wayranging now focuses by setting,
      * in effect simultaneously, both the {@linkplain #pollNamer() poll name}
      * and {@linkplain #actorIdentifier() actor identity}.
      */
    public void position( final String pollName, final VotingID actorID )
    {
        final boolean p = pollNamer.setSilently( pollName );
        final boolean a = actorIdentifier.setSilently( actorID );
        if( p ) pollNamer.bell().ring();
        if( a ) actorIdentifier.bell().ring();
    }



    /** Launches an activity that returns a result to the given receiver.  Use this method in preference
      * to its namesake alternatives, which afford no means of passing the result to the caller.
      *
      *     @see <a href='http://developer.android.com/reference/android/app/Activity.html#startActivityForResult(android.content.Intent,%20int)'
      *       target='_top'>startActivityForResult</a>(Intent,int)
      *     @see <a href='http://developer.android.com/reference/android/app/Activity.html#startActivityForResult(android.content.Intent,%20int,%20android.os.Bundle)'
      *       target='_top'>startActivityForResult</a>(Intent,int,Bundle)
      */
    public void startActivityForResult( final Intent request, final ActivityResultReceiver resultReceiver )
    {
        if( startActivity_resultReceiver != null )
        {
            throw new IllegalStateException( "Start of new activity when old is still pending" );
              // implied impossible, http://developer.android.com/guide/components/tasks-and-back-stack.html
        }

        startActivity_resultReceiver = resultReceiver;
        startActivityForResult( request, startActivity_requestCode ); // to continue in onActivityResult
    }


        private ActivityResultReceiver startActivity_resultReceiver; /* For pending request, or null if none.
          Must persist by stator, as calling an activity may happen to entail save/restore of this one. */

        private int startActivity_requestCode = 0; /* That of next result to be returned.
          Must be ≥ zero or no result will be returned; see #startActivityForResult(Intent,int,Bundle). */

            static { stators.add( new Stator<Wayranging>()
            {
                public void save( final Wayranging wr, final Parcel out )
                {
                    ParcelX.writeParcelable( wr.startActivity_resultReceiver, out );
                    out.writeInt( wr.startActivity_requestCode );
                }
                public void restore( final Wayranging wr, final Parcel in )
                {
                    wr.startActivity_resultReceiver = ParcelX.readParcelable( in );
                    wr.startActivity_requestCode = in.readInt();
                }
            });}



    /** The wayscope zoomer of this wayranging activity.
      */
    public WayscopeZoomer wayscopeZoomer() { return wayscopeZoomer; }


        private final WayscopeZoomer wayscopeZoomer = new WayscopeZoomer();


        static { stators.add( new Stator<Wayranging>()
        {
            public void save( final Wayranging wr, final Parcel out )
            {
                WayscopeZoomer.stators.save( wr.wayscopeZoomer, out );
            }
            public void restore( final Wayranging wr, final Parcel in )
            {
                WayscopeZoomer.stators.restore( wr.wayscopeZoomer, in );
            }
        });}



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final java.util.logging.Logger logger = LoggerX.getLogger( Wayranging.class );



    private static boolean wasConstructorCalled;
      // more correctly 'wasCreateCalled', but here following the usual pattern of CtorRestore



    private static final WaykitUI wk = WaykitUI.i();



   // - A c t i v i t y --------------------------------------------------------------------------------


      @Override
    protected void onActivityResult( final int requestCode, final int resultCode, final Intent result )
    {
System.err.println( " --- onActivityResult wk.isMainThread()=" + wk.isMainThread() ); // TEST
        final ActivityResultReceiver receiver = startActivity_resultReceiver;
        guard:
        {
            final String expected;
            if( receiver == null ) expected = "*none*";
            else
            {
                if( requestCode == startActivity_requestCode ) break guard;

                expected = Integer.toString( startActivity_requestCode );
            }
            throw new IllegalStateException( "Expecting result from activity request " + expected +
              ", but received result from " + requestCode );
        }

        startActivity_resultReceiver = null;
        startActivity_requestCode = MathX08.incrementExact( startActivity_requestCode ); // "Must be ≥ zero"
        receiver.receive( resultCode, result );
    }



    protected @Override void onDestroy()
    {
        assert lifeStage.compareTo(DESTROYING) < 0: "One destruction per instance, no colliding destructions";
        lifeStage = DESTROYING;
        lifeStageBell.ring();
        super.onDestroy();
        lifeStage = DESTROYED;
        lifeStageBell.ring();
    }



    protected @Override void onSaveInstanceState( final Bundle outB ) // see Recreating an Activity [RA]
    {
        logger.info( "Saving activity state to bundle" );
        super.onSaveInstanceState( outB ); // at least in order to reopen any open dialogues
        KittedPolyStatorSR.openToThread(); // (a) before stators.save (b)
        final byte[] state;
        final Parcel outP = Parcel.obtain();
        try
        {
            stators.save( this, outP ); // save all state variables to parcel, (b) after (a)
            state = outP.marshall(); // (sic) form parcel into state
        }
        finally { outP.recycle(); }
        outB.putByteArray( Wayranging.class.getName(), state ); // put state into bundle
    }


}


// Notes
// -----
//  [RA] Recreating an Activity
//      http://developer.android.com/training/basics/activity-lifecycle/recreating.html
