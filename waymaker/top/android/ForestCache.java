package waymaker.top.android; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.provider.DocumentsContract; // grep DocumentsContract-TS
import java.util.*;
import waymaker.gen.*;

import static android.provider.DocumentsContract.Document.MIME_TYPE_DIR;
import static java.util.logging.Level.WARNING;


/** A store of pollar forests.  Its main purpose is to speed execution.  It does this by persistent
  * storage and singleton referencing forest instances, which together reduce the frequency of slow
  * communications with the remote data source (count server).
  */
  @ThreadRestricted("app main") // effectively so by "joins" into "app main"
public final class ForestCache
{

    static final PolyStator<ForestCache> stators = new PolyStator<>();

///////


    /** Constructs a ForestCache.
      *
      *     @param inP The parceled state to restore, or null to restore none, in which case the
      *       openToThread restriction is lifted.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread") // for stators.restore
    public ForestCache( final Parcel inP/*grep CtorRestore*/ )
    {
        // A CtorRestore is a state-component restoration that is coded within a constructor or factory
        // method, as opposed to the restore method of a stator.  Coordinating it with the whole save
        // and restore procedure complicates its code.  CtorRestore is therefore used only where reason
        // demands.  The reason in each case is documented by a comment labeled "CtorRestore".
        final boolean toInitClass;
        if( wasConstructorCalled ) toInitClass = false;
        else
        {
            toInitClass = true;
            wasConstructorCalled = true;
        }
        if( inP != null ) stators.restore( this, inP ); // saved by stators in static inits further below

      // Forest map.
      // - - - - - - -
        if( toInitClass ) stators.add( new StateSaver<ForestCache>()
        {
            public void save( final ForestCache c, final Parcel out )
            {
              // 1. Size.
              // - - - - -
                final Collection<Forest> forests = c.forestMap.values();
                out.writeInt( forests.size() );

              // 2. Map.
              // - - - - -
                for( final Forest forest: forests )
                {
                  // 2a. Key.
                  // - - - - -
                    out.writeString( forest.pollName() );

                  // 2b. Value.
                  // - - - - - -
                    Forest.stators.save( forest, out );
                }

            }
        });
        if( inP != null ) // restore
        {
          // 1.
          // - - -
            final int size = inP.readInt();

          // 2.
          // - - -
            forestMap = new HashMap<>( MapX.hashCapacity(size + /*room to grow*/10), MapX.HASH_LOAD_FACTOR );
              // CtorRestore for this optimization of initial capacity based on saved state
            for( int f = 0; f < size; ++f )
            {
              // 2a.
              // - - -
                final String pollName = inP.readString();

              // 2b.
              // - - -
                final Forest forest = new Forest( pollName, this, inP );
                forestMap.put( pollName, forest );
            }
        }
        else forestMap = new HashMap<>( 10, MapX.HASH_LOAD_FACTOR );

      // - - -
        if( toInitClass ) stators.seal();
    }



   // --------------------------------------------------------------------------------------------------


    /** Returns the named forest either by retrieving it from this cache, or by constructing and caching
      * a new one.
      *
      *     @see Forest#pollName()
      */
    public Forest getOrMakeForest( final String pollName )
    {
        Forest forest = forestMap.get( pollName );
        if( forest == null )
        {
            forest = new Forest( pollName, this );
            forestMap.put( pollName, forest );
        }
        return forest;
    }



    /** A bell that rings on each replacement of a forest {@linkplain Forest#nodeCache() node cache}, or
      * change of a node cache {@linkplain NodeCache#leader() leader}.
      */
    public ReRinger<Changed> nodeCacheBell() { return nodeCacheBell; }


        private final ReRinger<Changed> nodeCacheBell = Changed.newReRinger();



    /** A bell that rings when a note is changed.
      */
    public Bell<Changed> notaryBell() { return notaryBell; }


        private final ReRinger<Changed> notaryBell = Changed.newReRinger();



    /** A note for the user on the ultimate result of the latest refresh attempt.  Any change in the
      * return value will be signalled by the {@linkplain #notaryBell() notary bell}.
      */
    public String refreshNote() { return refreshNote; }


        private String refreshNote = "Not yet refreshed";


        static { stators.add( new Stator<ForestCache>()
        {
            public void save( final ForestCache c, final Parcel out ) { out.writeString( c.refreshNote ); }
            public void restore( final ForestCache c, final Parcel in ) { c.refreshNote = in.readString(); }
        });}



    /** Initiates the clearance of all cached data, plus a {@linkplain #startRefreshFromWayrepo(String)
      * refresh from the local wayrepo}.  Eventually replaces the node cache of each forest and rings
      * the {@linkplain #nodeCacheBell() node cache bell}.  Posts user feedback as a single {@linkplain
      * #refreshNote() refresh note}.
      *
      *     @see WaykitUI#wayrepoTreeLoc()
      */
    public void startRefresh( final String wayrepoTreeLoc ) { r1( /*toClear*/true, wayrepoTreeLoc ); }



    /** Initiates a refresh by {@linkplain Precounter precounting} from the user’s local wayrepo,
      * eventually replacing the node cache of each affected forest and ringing the {@linkplain
      * #nodeCacheBell() node cache bell}.  Skips the bell ringing if no forest was affected.  Posts
      * user feedback as a single {@linkplain #refreshNote() refresh note}.
      *
      *     @see WaykitUI#wayrepoTreeLoc()
      */
    public void startRefreshFromWayrepo( final String wayrepoTreeLoc ) { r1( /*toClear*/false, wayrepoTreeLoc ); }



    /** A bell that rings when a nodal {@linkplain CountNode#voters() voter list} is extended.
      */
    public ReRinger<Changed> voterListingBell() { return voterListingBell; }

        /* * *
        - after hearing a ring, client may re-test extension of precount node's voter list
            - it may happen that the extension that caused the ring
              covered one or more voters who are missing there, having been shifted away in the precount
                - so precount list might not extend as much as expected, or not at all
            - client may then issue another extension request
          */

        private final ReRinger<Changed> voterListingBell = Changed.newReRinger();



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final HashMap<String,Forest> forestMap; // keyed by poll name



    private static final java.util.logging.Logger logger = LoggerX.getLogger( ForestCache.class );



    private static Thread newWorkerThread( final String name, final int serial, final Runnable runnable )
    {
        final Thread t = new Thread( runnable, name + " forest cache worker " + serial );
        t.setPriority( Thread.NORM_PRIORITY ); // or to limit of group
        t.setDaemon( true );
        return t;
    }



    private static boolean wasConstructorCalled;



   // ` r e f r e s h ``````````````````````````````````````````````````````````````````````````````````


    private void r1( final boolean toClear, final String wayrepoTreeLoc )
    {
      // Coordinate with refresh series.
      // - - - - - - - - - - - - - - - - -
        final int serial = ++refreshSerialLast; // raise signal to any prior refresh, "you're superceded"
        if( tRefresh != null ) tRefresh.interrupt(); // tap on shoulder, "no longer wanted"

      // Scope the general refresh demands that are determinable from "app main".
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final ArrayList<RefreshDemand> demands = new ArrayList<>();
        KittedPolyStatorSR.openToThread(); // (a) before snapOriginalState (b) now in r1 and later in r3
        final boolean toStrip = !toClear; /* No point stripping if cache to be entirely cleared.  The
          only type of demand yet determinable is one that will later be converted to a precount demand
          if precounting is possible, or will otherwise default to a strip demand.  Therefore scope no
          demand here if stripping will not actually occur. */
        if( toStrip ) for( final Forest forest: forestMap.values() )
        {
            final NodeCache1 nC = forest.nodeCache1();
            if( nC.groundUna().precounted() != null ) // then must refresh, whether by precount or strip
            {
                final RefreshDemand demand = new RefreshDemand( forest.pollName() );
                demand.snapOriginalState( nC ); // (b) after (a)
                demands.add( demand );
            }
        }

      // Maybe skip straight to r5.
      // - - - - - - - - - - - - - -
        if( wayrepoTreeLoc == null && demands.size() == 0 ) // then can't precount & there's nothing to strip
        {
            // can only clear if requested, and finally post the obligatory note for user feedback:
            r5( toClear, serial, Collections.<PrecountDemand>emptyList(), Collections.<StripDemand>emptyList(),
              /*failure*/null );
            return;
        }

      // Else make reference for use outside "app main".
      // - - - - - - - - - - - - - - - - - - - - - - - - -
        final ContentResolver cResolver = Application.i().getContentResolver(); // grep ContentResolver-TS

      // Start worker thread.
      // - - - - - - - - - - -
        tRefresh = newWorkerThread( "r2t", serial, new Runnable() // grep StartSync
        {
            public void run() { r2t( toClear, wayrepoTreeLoc, serial, demands, cResolver ); }
        });
        tRefresh.start();
    }


        private int refreshSerialLast; // write at start of refresh, thence test to prevent conflict

            static { stators.add( new Stator<ForestCache>()
            {
                public void save( final ForestCache c, final Parcel out )
                {
                    out.writeInt( c.refreshSerialLast );
                }
                public void restore( final ForestCache c, final Parcel in )
                {
                    c.refreshSerialLast = in.readInt();
                }
            });}


        private Thread tRefresh;
          // Reference of worker thread as interrupt handle.  Interrupted only to synchronize (TermSync)
          // and to conserve resources.  Otherwise, running a worker thread to completion is harmless.
          // On thread termination, null this reference to enable garbage collection.



    /** Spend effort of stage 2 using a worker thread.
      */
    private @ThreadSafe void r2t( final boolean toClear, final String wayrepoTreeLoc, final int serial,
      final ArrayList<RefreshDemand> demands, final ContentResolver cResolver )
    {
        final Thread t = Thread.currentThread();
        final Holder1<Exception> failureH = new Holder1<>();

      // Demand a precount for any poll that might have data in wayrepo (expensive test).
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final ArrayList<PrecountDemand> precountDemands = new ArrayList<>();
        int statelessDemandCount = 0; // precount demands that still require a snapshot of ground state
        if( wayrepoTreeLoc != null )
        {
            final Uri wayrepoTreeUri = Uri.parse( wayrepoTreeLoc );
            try( final WayrepoReader inWr = new WayrepoReader( wayrepoTreeUri, cResolver ))
            {
                String docID;
                docID = DocumentsContract.getTreeDocumentId( wayrepoTreeUri );
                docID = inWr.findDirectory( "poll", docID );
                if( docID == null ) throw new WayrepoAccessFailure( "Missing 'poll' directory" );

                try( final Cursor c/*proID_NAME_TYPE*/ = inWr.queryChildren( docID ); )
                {
                    while( c.moveToNext() )
                    {
                        if( !MIME_TYPE_DIR.equals( c.getString(2) )) continue;

                        final String dirName = c.getString( 1 );
                        if( !ServerCount.isPollNameForm( dirName ))
                        {
                            logger.info( "Skipping wayrepo poll directory with malformed name: '" + dirName + "'" );
                            continue;
                        }

                        // it may contain poll position files, so ensure a precount is demanded:
                        for( int d = demands.size() - 1;; --d )
                        {
                            if( d < 0 ) // then not already demanded
                            {
                                precountDemands.add( new PrecountDemand( /*pollName*/dirName ));
                                ++statelessDemandCount;
                                break;
                            }

                            final RefreshDemand demand = demands.get( d );
                            if( demand.pollName.equals( dirName )) // then already demanded
                            {
                                demands.remove( d ); // instead convert to precount demand:
                                precountDemands.add( new PrecountDemand( demand ));
                                assert demand.groundUnaState != null;
                                  // so statelessDemandCount unchanged by this
                                break;
                            }
                        }
                    }
                }
            }
            catch( final WayrepoAccessFailure x )
            {
                logger.log( WARNING, "", x );
                failureH.set( x );
            }
            catch( InterruptedException _x )
            {
                logger.info( "Aborting interrupted thread: " + t.getName() );
                Thread.currentThread().interrupt(); // pass it on, just to be correct
                return;
            }
        }

      // Convert any remaining demands to strip demands and finish stripping their grounds (expensive).
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final int sN = demands.size(); // count of strip demands
        final List<StripDemand> stripDemands;
        if( sN == 0 ) stripDemands = Collections.emptyList();
        else
        {
            final ArrayList<StripDemand> strips = new ArrayList<>( /*initialCapacity*/sN );
            KittedPolyStatorSR.openToThread(); // (a) before UnadjustedGround.restore (b)
            for( int s = 0; s < sN; ++s )
            {
                final StripDemand strip = new StripDemand( demands.get( s ));
                strips.add( strip );
                final NodeCache1 nC = new NodeCache1( strip.originalUnaCount,
                  /*hasPrecountAdjustments*/false/*they being stripped*/ );
                strip.newNodeCache = nC;
                nC.groundUna().restore( strip.groundUnaState, /*kit*/nC ); // (b) after (a)
            }
            stripDemands = strips;
        }

      // Maybe skip straight to r4t.
      // - - - - - - - - - - - - - - -
        final boolean r3Wanted;
        final boolean r4tWanted;
        if( !toClear && statelessDemandCount > 0 )
        {
            r3Wanted = true;
            assert precountDemands.size() > 0;
            r4tWanted = true;
        }
        else
        {
            r3Wanted = false;
            r4tWanted = precountDemands.size() > 0;
        }
        if( !r3Wanted && r4tWanted )
        {
            r4t( toClear, wayrepoTreeLoc, serial, precountDemands, stripDemands, cResolver, failureH );
            return;
        }

      // Else must join back into "app main" thread.
      // - - - - - - - - - - - - - - - - - - - - - - -
        Application.i().handler().post( new MainJoin( /*threadToJoin*/t, serial )
        {
            public void runAfterJoin() // on "app main", reading r2t variables above by TermSync
            {
                if( r3Wanted ) r3( wayrepoTreeLoc, serial, precountDemands, stripDemands, failureH );
                else
                {
                    assert !r4tWanted; // only r5 is left
                    r5( toClear, serial, precountDemands, stripDemands, failureH.get() );
                }
            }
        });
    }



    private void r3( final String wayrepoTreeLoc, final int serial, final List<PrecountDemand> precountDemands,
      final List<StripDemand> stripDemands, final Holder1<Exception> failureH )
    {
      // Take snapshot of unadjusted ground state for each precount demand.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
     // KittedPolyStatorSR.openToThread(); // (a) before snapOriginalState (b)
     /// already called in r1
        for( final PrecountDemand demand: precountDemands )
        {
            if( demand.groundUnaState != null ) continue; // cached forest, snapshot already taken in r1

            final Forest forest = forestMap.get( demand.pollName );
            if( forest == null ) continue; // will precount it from scratch

            demand.snapOriginalState( forest.nodeCache1() ); // (b) after (a)
        }

      // Make reference for use outside "app main".
      // - - - - - - - - - - - - - - - - - - - - - -
        final ContentResolver cResolver = Application.i().getContentResolver(); // grep ContentResolver-TS

      // Start worker thread.
      // - - - - - - - - - - -
        tRefresh = newWorkerThread( "r4t", serial, new Runnable() // grep StartSync
        {
            public void run()
            {
                r4t( /*toClear*/false, wayrepoTreeLoc, serial, precountDemands, stripDemands, cResolver,
                  failureH );
            }
        });
        tRefresh.start();
    }



    /** Spend effort of stage 4 using a worker thread.
      */
    private @ThreadSafe void r4t( final boolean toClear, final String wayrepoTreeLoc, final int serial,
      final List<PrecountDemand> precountDemands, final List<StripDemand> stripDemands,
      final ContentResolver cResolver, final Holder1<Exception> failureH )
    {
        final Thread t = Thread.currentThread();

      // Precount (expensive).
      // - - - - - - - - - - - -
        if( !toClear) KittedPolyStatorSR.openToThread(); // (a) before (b)
        for( final PrecountDemand demand: precountDemands )
        {
            final String pollName = demand.pollName;
            final byte[] groundUnaState = demand.groundUnaState;
            final int originalUnaCount = demand.originalUnaCount;
            assert groundUnaState == null && originalUnaCount == 0 || !toClear;
              // ground state is null when clearing, and when skipping restriction (a), as Precounter expects
            final Precounter precounter = new Precounter( pollName, groundUnaState, originalUnaCount,
              cResolver, wayrepoTreeLoc ); // (b) after (a), as per Precounter
            try { precounter.precount(); }
            catch( final CountFailure x )
            {
                logger.log( WARNING, "Unable to precount poll '" + pollName + "' from local wayrepo", x );
                if( failureH.get() == null ) failureH.set( x ); // to inform user
                continue;
            }
            catch( InterruptedException _x )
            {
                logger.info( "Aborting interrupted thread: " + t.getName() );
                Thread.currentThread().interrupt(); // pass it on, just to be correct
                return;
            }

            demand.newNodeCache = new NodeCache1( precounter ); // collate results of precount
        }

      // Join back into "app main" thread.
      // - - - - - - - - - - - - - - - - - -
        Application.i().handler().post( new MainJoin( /*threadToJoin*/t, serial )
        {
            public void runAfterJoin() // on "app main", reading r4t variables above by TermSync
            {
                r5( toClear, serial, precountDemands, stripDemands, failureH.get() );
            }
        });
    }



    private void r5( final boolean toClear, final int serial, final List<PrecountDemand> precountDemands,
      final List<StripDemand> stripDemands, final Exception failure )
    {
      // Apply results to forests.
      // - - - - - - - - - - - - - -
        boolean replacedNodeCache = false;
        if( toClear ) // then apply results to all forests: cached AND precounted but yet uncached:
        {
            assert stripDemands.size() == 0; /* no point to stripping (see toStrip further above),
              therefore no point in demanding it */
            final Collection<Forest> forests = forestMap.values();
            if( forests.size() > 0 )
            {
                final Iterator<Forest> f = forests.iterator();
                forests: do
                {
                    final Forest forest = f.next();
                    final String pollName = forest.pollName();
                    for( final PrecountDemand demand: precountDemands )
                    {
                        if( demand.wereResultsApplied ) continue;

                        if( !demand.pollName.equals(pollName) ) continue;

                        demand.wereResultsApplied = true;
                        final NodeCache1 newNodeCache = demand.newNodeCache;
                        if( newNodeCache == null ) break; // precount failed, so default to clearing node cache

                        forest.nodeCache( newNodeCache ); // precount succeeded, so set resulting node cache
                        continue forests;
                    }

                    forest.nodeCache( new NodeCache1( /*originalUnaCount*/0,
                      /*hasPrecountAdjustments*/false )); // thus entirely clearing the forest
                } while( f.hasNext() );
                replacedNodeCache = true; // all were replaced
                assert forests.size() > 0; // and all is at least one
            }
            for( final PrecountDemand demand: precountDemands )
            {
                if( demand.wereResultsApplied ) continue; // results were applied above

                final NodeCache1 newNodeCache = demand.newNodeCache;
                if( newNodeCache == null ) continue; // precount failed

                final String name = demand.pollName;
                forestMap.put( name, new Forest(name,this,newNodeCache) ); /* Eagerly caching a new forest in
                  order to persist the precount, which otherwise would be lost.  Precounts happen only
                  during a general refresh such as this, and not in a subsequent getOrMakeForest. */
            }
        }
        else if( r5_apply(precountDemands) | r5_apply(stripDemands) ) replacedNodeCache = true;
          // applying results only where specifically demanded
        if( replacedNodeCache ) nodeCacheBell.ring();

      // Inform user.
      // - - - - - - -
        if( failure == null ) refreshNote = "Refresh " + serial + " done";
        else
        {
            final StringBuilder b = Application.i().stringBuilderClear();
            b.append( "Refresh " ).append( serial ).append( ": " );
            ThrowableX.toStringDeeply( failure, b );
            refreshNote = b.toString();
        }
        notaryBell.ring();
    }


        private boolean r5_apply( final List<? extends RefreshDemand> demands )
        {
            boolean replacedNodeCache = false;
            for( final RefreshDemand demand: demands )
            {
                final NodeCache1 newNodeCache = demand.newNodeCache;
                if( newNodeCache == null ) continue;

                final String name = demand.pollName;
                final Forest forest = forestMap.get( name );
                if( forest == null ) forestMap.put( name, new Forest(name,this,newNodeCache) );
                else
                {
                    forest.nodeCache( newNodeCache );
                    replacedNodeCache = true;
                }
            }
            return replacedNodeCache;
        }



   // ==================================================================================================


    private abstract class MainJoin extends GuardedJointRunnable
    {

        @ThreadSafe MainJoin( final Thread threadToJoin, final int serial )
        {
            super( threadToJoin );
            this.serial = serial;
        }


        private final int serial;


        public boolean toProceed()
        {
            if( serial != refreshSerialLast ) return false; // refresh superceded, abort to avoid collision

            assert threadToJoin().equals( tRefresh );
            tRefresh = null; // release to garbage collector
            return true;
        }

    }



   // ==================================================================================================


    /** A demand to precount a poll and cache the result.
      */
    private static final class PrecountDemand extends RefreshDemand
    {

        PrecountDemand( final String pollName ) { super( pollName ); }


        PrecountDemand( final RefreshDemand other ) { super( other ); } // retype constructor


        boolean wereResultsApplied; // temporary variable

    }



   // ==================================================================================================


    /** A demand to refresh the forest cache in regard to a particular poll.
      */
    private static class RefreshDemand
    {

        RefreshDemand( final String pollName ) { this.pollName = pollName; }


        RefreshDemand( final RefreshDemand other ) // copy constructor
        {
            groundUnaState = other.groundUnaState;
            newNodeCache = other.newNodeCache;
            originalUnaCount = other.originalUnaCount;
            pollName = other.pollName;
        }


       // ----------------------------------------------------------------------------------------------

        NodeCache1 newNodeCache; // after refresh, or null if PrecountDemand and precount failed


        final String pollName;


          @ThreadRestricted("KittedPolyStatorSR.openToThread") // for stators.save
        final void snapOriginalState( final NodeCache1 nC ) // sets groundUnaState & originalUnaCount
        {
            final UnadjustedGround ground = nC.groundUna();
            final Parcel out = Parcel.obtain();
            try
            {
                UnadjustedGround.stators.save( ground, out, /*kit*/nC );
                groundUnaState = out.marshall(); // sic
            }
            finally { out.recycle(); } // grep ParcelReuse
            originalUnaCount = nC.nodeMap.size();
        }

            byte[] groundUnaState; // before refresh, or null to demand a precount from scratch

            int originalUnaCount; // including UnadjustedNode0s excluded from groundUnaState

    }



   // ==================================================================================================


    /** A demand to strip a potentially obsolete precount and its adjusted nodes from a cached forest.
      */
    private static final class StripDemand extends RefreshDemand
    {

        StripDemand( final RefreshDemand other ) { super( other ); } // retype constructor

    }


}
