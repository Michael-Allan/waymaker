package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import java.util.*;
import waymaker.gen.*;


/** A position controller for forest views.  It declares a {@linkplain #forest() forest} variable that
  * automatically tracks the {@linkplain Wayranging#pollName poll name}, and a {@linkplain #position()
  * nodal position} within the forest that obeys the following controls:
  *
  * <ul>
  *     <li>{@linkplain #ascendTo(CountNode) ascendTo}</li>
  *     <li>{@linkplain #descend() descend}</li>
  *     </ul>
  */
  @ThreadRestricted("app main") // in particular, for use of ForestCache
public final class Forester
{


    /** Constructs a Forester.
      *
      *     @see #forest()
      */
    public @Warning("wr co-construct") Forester( final Wayranging wr )
    {
        final ForestCache forests = wr.forests();
        forest = forests.getOrMakeForest( wr.pollName().get() );
        nodeCache( forest.nodeCache() );
        wr.pollName().bell().register( new Auditor<Changed>()
        {
            public void hear( Changed _ding )
            {
                final String _pollName = wr.pollName().get();
                if( _pollName.equals(forest.pollName()) ) return;

                forest = wr.forests().getOrMakeForest( _pollName );
                nodeCache( forest.nodeCache() );
                bell.ring();
            }
        }); // no need to unregister from wr co-construct
        forests.nodeCacheBell().register( new Auditor<Changed>()
        {
            public void hear( Changed _ding )
            {
                final NodeCache _nodeCache = forest.nodeCache();
                if( _nodeCache == nodeCache ) return;

                nodeCache( _nodeCache );
                bell.ring();
            }
        }); // no need to unregister from wr co-construct
    }



   // --------------------------------------------------------------------------------------------------



    /** Commands this forester to step leafward to one of the immediate ‘voters’ of the nodal position,
      * viz. one of position.voters.  This changes the value of {@linkplain #position() position}.
      *
      *     @param _position The new position to move to.
      *
      *     @throws NoSuchElementException if _position is not in position.voters.
      */
    public void ascendTo( CountNode _position ) { throw new UnsupportedOperationException(); }



    /** A bell that rings when this forester moves or replaces its node cache.
      */
    public Bell<Changed> bell() { return bell; }


        private final ReRinger<Changed> bell = Changed.newReRinger();



    /** Commands this forester to move down to position.rootwardInPrecount.  This changes the value of
      * {@linkplain #position() position}.
      *
      *     @throws NoSuchElementException if the position is already grounded.
      */
    public void descend() { throw new UnsupportedOperationException(); }



    /** The forest in which this forester now moves.  It automatically switches the forest based on the
      * {@linkplain Wayranging#pollName poll name}.  Any change in the return value will be signalled by
      * the {@linkplain #bell() bell}.
      */
    public Forest forest() { return forest; }


        private Forest forest;



    /** The height of this forester above ground.  This is the number of nodes on the path
      * position.rootwardInPrecount, or zero if the position is ground,
      */
    public int height() { return height; }


        private int height;



    /** The cache of nodes that defines the forest structure.  No content is ever removed or replaced,
      * but the whole cache may be replaced at any time by an instance with different content.  Each
      * replacement will be signalled by the {@linkplain #bell() bell}.
      */
    public NodeCache nodeCache() { return nodeCache; }


        private NodeCache nodeCache; // may temporarily lag forest.nodeCache instance till change reaches here


        private void nodeCache( final NodeCache _nodeCache )
        {
            nodeCache = _nodeCache;
            position = _nodeCache.ground(); // pending code to position from "concrete" node of waypath
            height = 0;
        }



    /** The nodal position of this forester within the forest.  Any change in the return value will be
      * signalled by the {@linkplain #bell() bell}.  When the position is ground, then the forester is
      * said to be ‘grounded’ at the default position.
      *
      *     @return Either a real node or the {@linkplain NodeCache#ground() ground pseudo-node}.
      */
    public CountNode position() { return position; }


        private CountNode position;


}
