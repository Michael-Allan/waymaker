package overware.top.android;

import java.util.List;
import overware.gen.*;


/** A view of a poll by a {@linkplain PollTourist tourist}.
  */
final class PollTouristV
{

    //  - f-structure per notebook 2015.1.31
    //      - heaviest positions at bottom of peers, as that allows voters to slide into peers
    //  - peer overflow accessed via page up control atop peers
    //      - ellipsis form
    //      - down control appears at bottom of peers, when paged
    //  - mosquitos handled by appendage to final page
    //      - similar to Votorola
    //      - per notebook 2015.6.1, ad b

    static final PolyStator<PollTouristV> stators = new PolyStator<>();

///////


    /** Creates a PollTouristV.
      */
    PollTouristV( PollTourist _tourist )
    {
        tourist = _tourist;
        final Ground ground = tourist.poll().ground();
        groundWidth = ground.rootCandidates().size();
        ground.rootCandidatesDefinitionBell().register( new Auditor<Changed>() // TEST
        {
            public void hear( Changed _ding )
            {
                final int groundWidthNow = ground.rootCandidates().size();
                if( groundWidthNow == groundWidth ) return;

                groundWidth = groundWidthNow;
                System.err.println( " --- root list has grown" );
                show();
            }
        });
        System.err.println( " --- PollTouristV constructed" ); // TEST
        show();
    }



   // ------------------------------------------------------------------------------------


    final @Warning("init call") void show() // TEST
    {
        final List<Position> roots = tourist.poll().ground().rootCandidates();
        System.err.println( " --- root list size=" + roots.size() );
        for( final Position root: roots ) System.err.println( "     root=" + root );
    }



    /** The underlying ‘tourist’ that orients this view.
      */
    PollTourist tourist() { return tourist; }


        private final PollTourist tourist;



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    private int groundWidth; // TEST


///////

    static { stators.seal(); }


}
