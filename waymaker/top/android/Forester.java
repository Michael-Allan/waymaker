package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import java.util.List;
import waymaker.gen.*;


/** An agent to move incrementally through forests.  Motion in a particular forest is commanded by the
  * following methods:
  *
  * <ul>
  *     <li>{@linkplain #ascendToVoter(Node) ascendToVoter}</li>
  *     <li>{@linkplain #descendToCandidate(Node) descendToCandidate}</li>
  *     <li>{@linkplain #moveToPeer(Node) moveToPeer}</li>
  *     </ul>
  *
  *     @see <a href='../../../../forest' target='_top'>‘forest’</a>
  */
@ThreadRestricted("app main") final class Forester
{

    static final PolyStator<Forester> stators = new PolyStator<>();

///////


    /** Constructs a Forester.
      *
      *     @see #forest()
      */
    Forester( final Forest forest )
    {
        this.forest = forest;
        forestCache = forest.forestCache();
        nodeCache = forest.nodeCache();
        candidate = nodeCache.ground();
        forestCache.nodeCacheBell().register( new Auditor<Changed>() // TEST
        {
            public void hear( Changed _ding )
            {
                final NodeCache _nodeCache = forest.nodeCache();
                if( _nodeCache == nodeCache ) return;

                nodeCache = _nodeCache;
                candidate = nodeCache.ground(); // pending code to attempt something less disruptive
                node = null;
                bell.ring();
            }
        });
    }



   // --------------------------------------------------------------------------------------------------



    /** Commands this forester to move leafward to one of the immediate voters of the current node,
      * viz. one of node.{@linkplain Node#voters() voters}.  This changes the value of the {@linkplain
      * #node() current node} and {@linkplain #candidate() candidate}.
      *
      *     @param target The voter to move to.
      *
      *     @throws IllegalArgumentException if the current node is null, or the target is not among its
      *       immediate voters.
      */
    void ascendToVoter( Node target ) { throw new UnsupportedOperationException(); }



    /** A bell that rings when this forester moves or experiences a node cache change.
      */
    Bell<Changed> bell() { return bell; }


        private final ReRinger<Changed> bell = Changed.newReRinger();



    /** The node directly rootward of this forester’s specific position ({@linkplain #node() node}).
      * Any change in the return value will be signalled by the {@linkplain #bell() bell}.  The value is
      * identical to {@linkplain #node() node}.rootwardInPrecount.candidate when {@linkplain #node()
      * node} is non-null, and is never itself null.
      *
      *     @return Either a real node or the {@linkplain NodeCache#ground() ground pseudo-node}.
      */
    Node candidate() { return candidate; }


        private Node candidate;



    /** Commands this forester to move down the path candidate.{@linkplain Node#rootwardInPrecount()
      * rootwardInPrecount}.  This changes the value of {@linkplain #candidate() candidate}.
      *
      *     @throws IllegalArgumentException if the target is not among the candidates.
      */
    void descendToCandidate( Node target ) { throw new UnsupportedOperationException(); }



    /** The forest in which this forester now moves.
      */
    Forest forest() { return forest; }


        private final Forest forest;



    /** The store of each forest.
      */
    ForestCache forestCache() { return forestCache; }


        private final ForestCache forestCache;



    /** Commands this forester to move laterally to one of the peers of the current node, viz. one of
      * candidate.{@linkplain Node#voters() voters}, or to unspecify its position.  This
      * changes the value of the {@linkplain #node() current node}.
      *
      *     @param target The peer to move to, or null to unspecify the position.
      *
      *     @throws IllegalArgumentException if the target is not null, and not among the immediate
      *       voters of the candidate.
      */
    void moveToPeer( Node target ) { throw new UnsupportedOperationException(); }



    /** The specific position of this forester in the forest, or null if the position is unspecified.
      * Any change in the return value will be signalled by the {@linkplain #bell() bell}.  The general
      * position ({@linkplain #candidate() candidate}) is always non-null regardless.  When the specific
      * position is null and the {@linkplain #candidate() candidate} is ground, then the forester is
      * said to be ‘grounded’ at its default position.
      */
    Node node() { return node; }


        private Node node;



    /** The current cache of nodes that defines the forest structure.  Any change in the return value
      * will be signalled by the {@linkplain #bell() bell}.
      */
    NodeCache nodeCache() { return nodeCache; }


        private NodeCache nodeCache; // may temporarily lag forest.nodeCache instance till change reaches here


///////

    static { stators.seal(); }

}
