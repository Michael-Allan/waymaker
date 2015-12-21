package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import java.util.*;
import waymaker.gen.*;


/** An agent to move incrementally through the {@linkplain Wayranging#forests() forests}.  It positions
  * among the forests as {@linkplain #forest() forest}, {@linkplain #node() node} and {@linkplain
  * #candidate() candidate}.  It moves within a given forest by the following methods:
  *
  * <ul>
  *     <li>{@linkplain #ascendToVoter(Node) ascendToVoter}</li>
  *     <li>{@linkplain #moveToPeer(Node) moveToPeer}</li>
  *     <li>{@linkplain #descendToCandidate() descendToCandidate}</li>
  *     </ul>
  */
public @ThreadRestricted("app main"/*uses ForestCache*/) final class Forester
{


    /** Constructs a Forester.
      *
      *     @see #forest()
      */
    public Forester( final Wayranging wr )
    {
        final ForestCache forests = wr.forests();
        forest = forests.getOrMakeForest( wr.pollNamer().get() );
        nodeCache( forest.nodeCache() );
        wr.pollNamer().bell().register( new Auditor<Changed>()
        {
            public void hear( Changed _ding )
            {
                final String _pollName = wr.pollNamer().get();
                if( _pollName.equals(forest.pollName()) ) return;

                forest = wr.forests().getOrMakeForest( _pollName );
                nodeCache( forest.nodeCache() );
                bell.ring();
            }
        });
        forests.nodeCacheBell().register( new Auditor<Changed>()
        {
            public void hear( Changed _ding )
            {
                final NodeCache _nodeCache = forest.nodeCache();
                if( _nodeCache == nodeCache ) return;

                nodeCache( _nodeCache );
                bell.ring();
            }
        });
    }



   // --------------------------------------------------------------------------------------------------



    /** Commands this forester to step leafward to one of the immediate voters of the specific node,
      * viz. one of node.voters.  This changes the value of {@linkplain #node() node} and {@linkplain
      * #candidate() candidate}.
      *
      *     @param target The voter to move to.
      *
      *     @throws NoSuchElementException if the specific node is null, or the target is not among its
      *       immediate voters.
      */
    public void ascendToVoter( Node target ) { throw new UnsupportedOperationException(); }



    /** A bell that rings when this forester moves or experiences a node cache change.
      */
    public Bell<Changed> bell() { return bell; }


        private final ReRinger<Changed> bell = Changed.newReRinger();



    /** The general node at which this forester is positioned, directly rootward of any specific node
      * ({@linkplain #node() node}).  Any change in the return value will be signalled by the
      * {@linkplain #bell() bell}.  The value is identical to node.rootwardInPrecount.candidate when
      * node is non-null, and is never itself null.
      *
      *     @return Either a real node or the {@linkplain NodeCache#ground() ground pseudo-node}.
      */
    public Node candidate() { return candidate; }


        private Node candidate;



    /** Commands this forester to step down the path candidate.rootwardInPrecount.  This changes the
      * value of {@linkplain #candidate() candidate}.
      *
      *     @throws NoSuchElementException if the candidate is the
      *       {@linkplain NodeCache#ground() ground pseudo-node}.
      */
    public void descendToCandidate() { throw new UnsupportedOperationException(); }



    /** The forest in which this forester now moves.  It automatically switches to the forest named by
      * the {@linkplain Wayranging#pollNamer poll namer}.  Any change in the return value will be
      * signalled by the {@linkplain #bell() bell}.
      */
    public Forest forest() { return forest; }


        private Forest forest;



    /** Commands this forester to move laterally to one of the peers of the specific node, viz. one of
      * candidate.voters, or to unspecify the node.  This changes the value of the {@linkplain #node()
      * node}.
      *
      *     @param target The peer to move to, or null to unspecify the node.
      *
      *     @throws NoSuchElementException if the target is not null, and not among the immediate voters
      *       of the candidate.
      */
    public void moveToPeer( Node target ) { throw new UnsupportedOperationException(); }



    /** The specific node at which this forester is positioned, or null if the node is unspecified.  Any
      * change in the return value will be signalled by the {@linkplain #bell() bell}.  The general node
      * ({@linkplain #candidate() candidate}) is always non-null regardless.  When the specific node is
      * null and the candidate is ground, then the forester is said to be ‘grounded’ at the default
      * position.
      */
    public Node node() { return node; }


        private Node node;



    /** The cache of nodes that now defines the forest structure.  Any change in the return value to a
      * different cache instance will be signalled by the {@linkplain #bell() bell}.
      */
    public NodeCache nodeCache() { return nodeCache; }


        private NodeCache nodeCache; // may temporarily lag forest.nodeCache instance till change reaches here


        private final @Warning("init call") void nodeCache( final NodeCache _nodeCache )
        {
            nodeCache = _nodeCache;
            candidate = _nodeCache.ground(); // pending code to position from "concrete" node of waypath
            node = null;
        }


}
