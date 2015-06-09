package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import java.util.*;
import overware.gen.*;


/** A representation of a poll implemented as a cache of position snapshots corrected to
  * account for working modifications in the user's guiderepo.
  */
final class PrecountedPoll implements Poll
{

    static final PolyStator<PrecountedPoll> stators = new PolyStator<>();

///////


    /** Constructs a PrecountedPoll.
      *
      *     @see #uncorrectedPoll()
      */
    PrecountedPoll( Poll1 _uncorrectedPoll )
    {
        uncorrectedPoll = _uncorrectedPoll;
        final Ground uncorrectedGround = uncorrectedPoll.ground();
        assert Ground.class.equals( uncorrectedGround.getClass() );
          // implemented entirely by backing position, so refer directly to that:
        ground = new Ground( new CorrectedPosition( uncorrectedGround.back() ));
    }



   // ------------------------------------------------------------------------------------


    /** The underlying, uncorrected poll on which this precounted correction is based.
      */
    Poll1 uncorrectedPoll() { return uncorrectedPoll; }


        private final Poll1 uncorrectedPoll;



   // - P o l l --------------------------------------------------------------------------


    public Ground ground() { return ground; }


        private final Ground ground;



    public Bell<Changed> votersDefinitionBell() { return votersDefinitionBell; }


        private final ReEmitter<Changed> votersDefinitionBell = Changed.newReEmitter();



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    private final Map<String, CorrectedPosition> correctedCache = new HashMap<>();
      // corrected part of poll only


///////

    static { stators.seal(); }


}
