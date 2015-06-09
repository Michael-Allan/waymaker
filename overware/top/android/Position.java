package overware.top.android;

import java.util.List;


/** A position snapshot in a {@linkplain Poll Poll}.
  */
interface Position
{


   // - P o s i t i o n ------------------------------------------------------------------


    /** The formal candidate immediately to the rootward of this position snapshot, or
      * null if the formal candidate is unknown.
      *
      *     @return Either a real position, the {@linkplain Poll#ground() ground}
      *       pseudo-position, or null.
      */
    public Position candidate();



    /** Demands further resolution of a possibly incomplete voter list.  Ultimately this
      * may change the content of the {@linkplain #voters() voters list}, or the value of
      * {@linkplain #votersMaybeIncomplete() votersMaybeIncomplete}, or both.
      *
      *     @param threshold The minimum acceptable size for the voters list should it
      *       remain incomplete after this demand is serviced.
      */
    public void fetchVoters( final int threshold );



    /** The poll representation in which this position snapshot is cached.
      */
    public Poll poll();



    /** A dynamic list of the immediate voters of this position snapshot.  The list is
      * known to be complete only if {@linkplain #votersMaybeIncomplete()
      * votersMaybeIncomplete} returns false.  Otherwise more information can be demanded
      * through {@linkplain #fetchVoters(int) fetchVoters}.  Any change to the membership
      * of the list will be signalled by the {@linkplain Poll#votersDefinitionBell()
      * voters definition bell}.  All changes will be pure extensions, leaving the number
      * and order of previous members unchanged.
      */
    public List<Position> voters();



    /** Answers whether the voter list is possibly incomplete.  Moreover, if the list
      * contains at least one voter, then this method answers definitely as though it were
      * named “voters<em>Are</em>Incomplete”.  Any change to the return value will be
      * signalled by the {@linkplain Poll#votersDefinitionBell() voters definition bell}.
      * It will change only from true to false, never the opposite.
      */
    public boolean votersMaybeIncomplete();


}
