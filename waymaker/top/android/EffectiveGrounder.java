package waymaker.top.android; // Copyright © 2015 Michael Allan.  Licence MIT.

import waymaker.gen.*;


/** A searcher for an effective ground between two candidate nodes.  An effective ground is a
  * node common to the root paths of both candidates, inclusive of those candidates.  It may
  * therefore be either of the candidate nodes, or the node of a common candidate farther
  * downstream, or the actual ground.  Effective grounds have the property of flow invariance during
  * vote shifts between the two candidates; a shift from one candidate to the other is guaranteed to
  * leave the vote flow at the effective ground unchanged.  More all candidates of the effective
  * ground are themselves effective grounds, so the root path as a whole is flow invariant.
  *
  * <p>This guarantee of flow invariance rests on the assumption that the would-be voter is part of no
  * barred cycle at present, and none after the vote shift.  The consequences of invalidating this
  * assumption are unknown.</p>
  *
  * <p>Do not hold an instance of EffectiveGrounder indefinitely.  It retains the contents of its path
  * buffers for sake of speed, which may cause old forests to be witheld from the garbage collector.</p>
  */
public final @Warning("no hold") class EffectiveGrounder
{


    /** Returns an effective ground between the two candidate nodes.  If the two nodes are
      * actually the same node, then normally that node is returned.  The returned node is
      * not guaranteed, however, to be the leafmost effective ground; if the two paths are very long,
      * then a more rootward one may be returned instead.
      */
    public PrecountNode effectiveGround( final PrecountNode candidateA, final PrecountNode candidateB )
    {
        try
        {
            store( candidateA, pathA, pA ); // store root paths to enable reverse, leafward traversal
            PrecountNode gE = store( candidateB, pathB, pB ); /* init gE (effective ground) to
              actual ground; it's certainly common to both paths, so begin there */
            while( pA.size() > 0 && pB.size() > 0 )
            {
                final PrecountNode nod = pathA[pA.unwriting()]; // pop the rootmost remaining nod
                if( nod != pathB[pB.unwriting()] ) break; // nod missing from B, paths have diverged

                gE = nod; // nod is common to both, paths are still converged
            }
            return gE;
        }
        finally { pA.clear(); pB.clear(); } /* Clearing indeces alone for speed.  Retaining garbage in
          buffers, which may cause old forests to be witheld.  Hence "no hold". */
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final int PATH_CAPACITY = 15; // guess, uncertain what tree heights to expect in practice



    private final PrecountNode[] pathA = new PrecountNode[PATH_CAPACITY]; // ring buffer

        private final CircularIndeces pA = new CircularIndeces( PATH_CAPACITY ); // to use as overflowing stack

        // not using ArrayList for storage because it clears laboriously, nulling each element



    private final PrecountNode[] pathB = new PrecountNode[PATH_CAPACITY];

        private final CircularIndeces pB = new CircularIndeces( PATH_CAPACITY );



    /** Stores the candidate path from nod (inclusive) to the root (inclusive) and returns the actual
      * ground beneath the root; or, if the path length would exceed PATH_CAPACITY, then stores just the
      * rootward part.  Stores nothing (length zero) if the node itself is the actual ground.
      *
      *     @return The actual ground.
      */
    private static PrecountNode store( PrecountNode nod, final PrecountNode[] path,
      final CircularIndeces p )
    {
        assert p.size() == 0;
        for( ;; ) // each node on root path
        {
            final RootwardCast<PrecountNode> cast = nod.rootwardInThis();
            if( cast == null ) return nod; // actual ground

            path[p.writingOver()] = nod; // push, overflowing leafmost node if necessary
            nod = cast.candidate(); // move rootward
        }
    }


}



