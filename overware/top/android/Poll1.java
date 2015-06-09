package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import overware.gen.*;


/** An implementation of a poll.  Currently it is a null implementation because the
  * overguideway lacks a count engine and consequently there are no remote polls to cache.
  * It will be completed when the situation changes.
  */
final class Poll1 implements Poll
{

    static final PolyStator<Poll1> stators = new PolyStator<>();

///////


   // - P o l l --------------------------------------------------------------------------


    public Ground ground() { return ground; }


        private final Ground ground = new Ground( new Position1( this ));



    public Bell<Changed> votersDefinitionBell() { return votersDefinitionBell; }


        private final ReEmitter<Changed> votersDefinitionBell = Changed.newReEmitter();


///////

    static { stators.seal(); }


}
