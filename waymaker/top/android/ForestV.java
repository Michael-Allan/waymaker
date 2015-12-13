package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import java.util.List;
import waymaker.gen.*;


/** A forest view that is oriented by the {@linkplain Wayranging#forester() forester}.
  */
@ThreadRestricted("app main") final class ForestV
{
    /* * *
    - laid out in thin, vertical line
        ( like f-structure of notebook 2015.1.31, but with vertical voters
        - voters replaced with quantity indicator if screen space too small
    [ vote control
      / - to right of horizontal voters atop view
      // no longer horizontal, vote control placement is now unknown
        - when on-path, subject node is either directly there, or upstream
            - because waypath crosses via transnorm of first pipe downstream of subject
    - peer overflow accessed via page up control atop peers
        - ellipsis form
        - down control appears at bottom of peers, when paged
    - minor peers handled by appendage to final page
        - similar to Votorola
        ( notebook 2015.6.1, ad b
      */


    /** Constructs a ForestV.
      */
    ForestV( final Wayranging _wr )
    {
        wr = _wr;
        final Forester forester = wr.forester();
        groundWidth = forester.nodeCache().ground().voters().size();
        forester.bell().register( new Auditor<Changed>() // TEST
        {
            private NodeCache cache = forester.nodeCache();
            public void hear( Changed _ding )
            {
                final NodeCache _cache = wr.forester().nodeCache();
                if( _cache == cache ) return;

                cache = _cache;
                groundWidth = cache.ground().voters().size();
                System.err.println( " --- forest cache has changed" );
                show();
            }
        });
        wr.forests().voterListingBell().register( new Auditor<Changed>() // TEST
        {
            public void hear( Changed _ding )
            {
                final int _groundWidth = wr.forester().nodeCache().ground().voters().size();
                if( _groundWidth == groundWidth ) return;

                groundWidth = _groundWidth;
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
        final List<? extends Node> roots = wr.forester().nodeCache().ground().voters();
        System.err.println( " --- root list size=" + roots.size() );
        for( final Node root: roots ) System.err.println( "     root=" + root );
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private int groundWidth;



    private final Wayranging wr; // TEST, till can use overridden getActivity


}
