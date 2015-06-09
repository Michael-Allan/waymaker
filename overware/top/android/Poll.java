package overware.top.android;

import overware.gen.*;


/** A representation of a remote poll based on a local cache of position snapshots.
  *
  *     @see <a href='../../../../overware/gen/poll' target='_top'>‘poll’</a>
  */
interface Poll
{


   // - P o l l --------------------------------------------------------------------------


    /** A cached snapshot of the ground at the remote poll.
      */
    public Ground ground();



    /** A bell that sounds whenever the {@linkplain Position#voters() voter list} at a
      * position snapshot becomes better defined by additional information that was
      * obtained from the remote poll; in other words, whenever the list lengthens or
      * becomes {@linkplain Position#votersMaybeIncomplete() definitely complete}.
      */
    public Bell<Changed> votersDefinitionBell();


}
