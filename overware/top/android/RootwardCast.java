package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import overware.spec.VotingID;


/** An immutable record of a step in the transitive vote flow of a {@linkplain Forest forest}, from one
  * node (reference) to another (candidate).
  *
  *     @param <C> The type of candidate node that is cast to.
  */
public interface RootwardCast<C extends Node>
{


   // - R o o t w a r d - C a s t ----------------------------------------------------------------------


    /** The cast candidate, which is either the node immediately rootward to which the reference
      * successfully casts its vote, or the {@linkplain NodeCache#ground() ground pseudo-node}
      * if the reference successfully casts no vote and is therefore itself a root.
      */
    public C candidate();



    /** Answers whether this cast is barred.  When barred, the {@linkplain #votedID() voted candidate}
      * is always a real, non-ground node (<code>votedID != null</code>), but the vote is uncounted
      * and therefore the {@linkplain #candidate() cast candidate} is the ground (<code>candidate.id ==
      * null</code>).  When unbarred, the two nodes (voted and cast) are identical.  Casts are
      * barred in order to break vote cycles, each bar forcing the would-be caster to serve instead as
      * the root candidate of its tree.
      *
      *     @see CycleForeseer#barNode()
      */
    public boolean isBarred();


        /* * *
        - why break vote cycles?
            - they are difficult to present in the UI
            - they complicate the code with difficult edge cases
            - they have no known utility
                - Votorola allows for cycles, but nothing useful came of it
          */



 // /** The [reference] node from which this cast proceeds.
 //   */
 // public Node node();
 //
 /// unmodeled, else cannot have rootwardHither_getOrMake optimization



    /** The identifier of the voted candidate at time of counting, which is the candidate chosen by the
      * reference; or null if the reference chose none.  The voted candidate is identical to the
      * {@linkplain #candidate() cast candidate} unless the cast {@linkplain #isBarred() is barred}.
      */
    public VotingID votedID();


}
