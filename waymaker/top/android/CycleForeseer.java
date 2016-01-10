package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import waymaker.spec.VotingID;


/** A detector of cycles in proposed vote casts.
  */
public final class CycleForeseer implements AutoCloseable
{


    /** The node on the cyclic path to be {@linkplain RootwardCast#isBarred() barred} by the
      * proposed cast, or null if no cycle was detected.  It is the node with the {@linkplain
      * waymaker.spec.TriSerialUDID#compareTo(Object) highest identity tag} in the cycle.
      */
    public PrecountNode1 barNode() { return barNode; }


        private PrecountNode1 barNode;


        /* * *
        - probably the exact rule of bar placement matters little, as long as the rule is stable
        - reason (slight as it is) for barring highest ID
            - no person is barred in a cycle that also involves a pipe
            - no person is forced to root when a pipe might be forced instead
            - so a natural distribution of nodes (persons leafward, pipes rootward) is not upset
          */



    /** The future {@linkplain RootwardCast#candidate() cast candidate}, which may be the ground, and
      * definitely will be if the original voter is to be barred.
      */
    public PrecountNode candidate() { return candidate; }


        private PrecountNode candidate;



    /** Looks at the proposed vote, reopens this foreseer, and stores here the properties of any cycle
      * it foresees.  It foresees a cycle by testing whether the chosen candidate is either a voter of
      * the origin (direct or indirect), or the origin itself.  In other words, it tests whether the
      * proposed vote would flow to the origin (i.e. whether it would cycle) if it encountered no bar
      * along the way.  Call {@linkplain #close close}() when finished reading the stored properties.
      *
      * <p>If the origin is already in a barred cycle with the chosen candidate, then the origin must be
      * downstream of that candidate, reachable on its rootward cast path.  Otherwise foresight will
      * silently fail.  A simple form of insurance is to first uncast the origin.</p>
      *
      *     @param o The node ("origin") that would cast the proposed vote.
      *     @param oVotedID The proposed {@linkplain RootwardCast#votedID() voted identity tag},
      *       identifying the chosen candidate.
      *
      *     @throws IllegalStateException if this foreseer is already open.
      */
    public void foresee( final PrecountNode1 o, final VotingID oVotedID, final Precounter precounter )
    {
        if( isOpen ) throw new IllegalStateException();

        isOpen = true;
        if( oVotedID == null ) // no vote, ∴ no cycle
        {
         // barNode = null; // which it already is
            candidate = precounter.ground().precounted(); // not null, ctor of o ensures
            return;
        }

        final VotingID oID = o.id();
        final int oComp = oID.compareTo( oVotedID );
        if( oComp == 0 ) // self vote, ∴ a cycle
        {
            barNode = o;
            candidate = precounter.ground().precounted(); // not null, ctor of o ensures
            return;
        }

        UnadjustedNode oVotedUna = precounter.getOrFetchUnadjusted( oVotedID );
        if( oVotedUna == null ) oVotedUna = UnadjustedNode0.makeMapped( oVotedID, precounter );
        CountNode highestNod = oComp > 0? o: oVotedUna; // thus far
        for( CountNode nod = oVotedUna;; ) // begin with chosen candidate
        {
            // Trace path of would-be vote.  Trace by cast candidates, not by voted candidates, and so
            // avoid becoming trapped downstream in a pre-existing cycle among the candidates.
            final CountNode nextNod = nod.rootwardInPrecount().candidate();
            final VotingID nextID = nextNod.id();
            if( nextID == null ) // this candidate nod casts for ground, ∴ no cycle:
            {
                barNode = null;
                candidate = PrecountNode.getOrMake( oVotedUna );
                return;
            }

            if( nextID.equals( oID )) // this candidate nod casts for original voter, ∴ a cycle
            {
                if( highestNod == o ) // then should bar the original voter:
                {
                    barNode = o;
                    candidate = precounter.ground().precounted(); // not null, ctor of o ensures
                }
                else // should bar the candidate with highest ID:
                {
                    barNode = highestNod instanceof PrecountNode1? (PrecountNode1)highestNod:
                      (PrecountNode1)PrecountNode.getOrMake((UnadjustedNode)highestNod);
                    candidate = PrecountNode.getOrMake( oVotedUna );
                }
                return;
            }

            nod = nextNod; // next on root path
            if( nextID.compareTo(highestNod.id()) > 0 ) highestNod = nod;
        }
    }



   // - A u t o - C l o s e a b l e --------------------------------------------------------------------


    public void close() // resets this foreseer
    {
        barNode = null;
        candidate = null;

        isOpen = false;
    }


        private boolean isOpen;


}



