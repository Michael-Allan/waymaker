package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import waymaker.gen.*;


final class PollIntroducer implements Auditor<Changed>
{


    @Warning("_wr co-construct") PollIntroducer( final Wayranging _wr )
    {
        wr = _wr;
        syncPoll();
        wr.pollNamer().bell().register( this ); // no need to unregister from wr co-construct
        wr.wayscopeZoomer().bell().register( new Auditor<Changed>()
        {
            public void hear( Changed _ding ) { ++stateOrdinal; } // avoid collision between zoom agents
        }); // no need to unregister from wr co-construct
    }


   // - A u d i t o r ----------------------------------------------------------------------------------


    public void hear( Changed _ding )
    {
        ++stateOrdinal; // prevent overlapping or obsolete intro
        syncPoll();
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final java.util.logging.Logger logger = LoggerX.getLogger( PollIntroducer.class );



    private int stateOrdinal; // increment to invalidate any running intro



    private void syncPoll()
    {
        if( wr.actorIdentifier().get() != null ) return; // skip intro, pollar forest not entered at ground

        final WayscopeZoomer zoomer = wr.wayscopeZoomer();
        if( zoomer.zoom() != WayscopeZoom.FORESTER )
        {
            logger.warning( "Skipping introduction, unexpected zoom on entry to forest ground" );
            return;
        }

        zoomer.outZoom(); // temporarily to POLL level, drawing attention to the question
        Application.i().handler().postDelayed( new Runnable()
        {
            private final int stateOrdinalWas = stateOrdinal;
            public void run() // later
            {
                if( stateOrdinal != stateOrdinalWas ) return; // invalidated by subsequent state change

                assert wr.wayscopeZoomer().zoom() == WayscopeZoom.POLL;
                wr.wayscopeZoomer().inZoom(); // back to FORESTER, showing answers
            }
        }, 8000/*ms*/ );
    }



    private final Wayranging wr;


}
