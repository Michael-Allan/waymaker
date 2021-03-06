package waymaker.top.android; // Copyright © 2016 Michael Allan.  Licence MIT.

import waymaker.gen.*;


abstract @ThreadRestricted("app main") class QuestionSyncher implements Auditor<Changed>
{


    @Warning("wr co-construct") QuestionSyncher( final Wayranging wr )
    {
        this.wr = wr;
        wr.pollName().bell().register( this ); // no need to unregister from wr co-construct
        wr.forests().nodeCacheBell().register( this ); // "
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    final Waynode leaderWaynode( final Wayranging wr )
    {
        return wr.forests().getOrMakeForest(wr.pollName().get()).nodeCache().leader().waynode();
    }



    abstract void sync();



    final Wayranging wr;


}
