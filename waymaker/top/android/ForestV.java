package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import java.util.List;
import waymaker.gen.*;


/** A forest view that is oriented by a {@linkplain Forester forester}.
  */
@ThreadRestricted("app main") final class ForestV
{
    /* * *
    - f-structure notebook 2015.1.31
        - heaviest nodes at bottom of peers, as that allows voters to slide into peers
    [ vote control
        - to right of horizontal voters atop view
        - when on-path, subject node is either directly there, or upstream
            - because waypath crosses via transnorm of first pipe downstream of subject
    - peer overflow accessed via page up control atop peers
        - ellipsis form
        - down control appears at bottom of peers, when paged
    - minor peers handled by appendage to final page
        - similar to Votorola
        - notebook 2015.6.1, ad b
      */

    static final PolyStator<ForestV> stators = new PolyStator<>();

///////


    /** Constructs a ForestV.
      *
      *     @see #forester()
      */
    ForestV( Forester _forester )
    {
        forester = _forester;
        groundWidth = forester.nodeCache().ground().voters().size();
        forester.bell().register( new Auditor<Changed>() // TEST
        {
            private NodeCache cache = forester.nodeCache();
            public void hear( Changed _ding )
            {
                final NodeCache cacheNow = forester.nodeCache();
                if( cacheNow == cache ) return;

                cache = cacheNow;
                groundWidth = cache.ground().voters().size();
                System.err.println( " --- forest cache has changed" );
                show();
            }
        });
        forester.forestCache().voterListingBell().register( new Auditor<Changed>() // TEST
        {
            public void hear( Changed _ding )
            {
                final int groundWidthNow = forester.nodeCache().ground().voters().size();
                if( groundWidthNow == groundWidth ) return;

                groundWidth = groundWidthNow;
                System.err.println( " --- root list has grown" );
                show();
            }
        });
        System.err.println( " --- ForestV constructed --------------------------" ); // TEST
        show();
    }



   // --------------------------------------------------------------------------------------------------


    final @Warning("init call") void show() // TEST
    {
        final List<? extends Node> roots = forester.nodeCache().ground().voters();
        System.err.println( " --- root list size=" + roots.size() );
        for( final Node root: roots ) System.err.println( "     root=" + root );
    }



    /** The forester that orients this view.
      */
    Forester forester() { return forester; }


        private final Forester forester;



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private int groundWidth;


///////

    static { stators.seal(); }

}
