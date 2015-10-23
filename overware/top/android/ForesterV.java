package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import java.util.List;
import overware.gen.*;


/** A view of a forest by a {@linkplain Forester forester}.
  */
@ThreadRestricted("app main") final class ForesterV
{
    /* * *
    - f-structure as per notebook 2015.1.31
        - heaviest nodes at bottom of peers, as that allows voters to slide into peers
    - peer overflow accessed via page up control atop peers
        - ellipsis form
        - down control appears at bottom of peers, when paged
    - minor peers handled by appendage to final page
        - similar to Votorola
        - as per notebook 2015.6.1, ad b
      */

    static final PolyStator<ForesterV> stators = new PolyStator<>();

///////


    /** Constructs a ForesterV.
      */
    ForesterV( Forester _forester )
    {
        forester = _forester;
        groundWidth = forester.cache().ground().voters().size();
        forester.bell().register( new Auditor<Changed>() // TEST
        {
            private NodeCache cache = forester.cache();
            public void hear( Changed _ding )
            {
                final NodeCache cacheNow = forester.cache();
                if( cacheNow == cache ) return;

                cache = cacheNow;
                groundWidth = cache.ground().voters().size();
                System.err.println( " --- forest cache has changed" );
                show();
            }
        });
        forester.forest().voterListingBell().register( new Auditor<Changed>() // TEST
        {
            public void hear( Changed _ding )
            {
                final int groundWidthNow = forester.cache().ground().voters().size();
                if( groundWidthNow == groundWidth ) return;

                groundWidth = groundWidthNow;
                System.err.println( " --- root list has grown" );
                show();
            }
        });
        System.err.println( " --- ForesterV constructed --------------------------" ); // TEST
        show();
    }



   // --------------------------------------------------------------------------------------------------


    final @Warning("init call") void show() // TEST
    {
        final List<? extends Node> roots = forester.cache().ground().voters();
        System.err.println( " --- root list size=" + roots.size() );
        for( final Node root: roots ) System.err.println( "     root=" + root );
    }



    /** The underlying forester that orients this view.
      */
    Forester forester() { return forester; }


        private final Forester forester;



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private int groundWidth;


///////

    static { stators.seal(); }

}
