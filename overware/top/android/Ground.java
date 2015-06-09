package overware.top.android; // Copyright 2015, Michael Allan.

import java.util.List;
import overware.gen.*;


/** A pseudo-position to represent the ground beneath the root candidates, which are here
  * modeled as the ground’s ‘{@linkplain #voters() voters}’.  This modeling simplifies the
  * forest structure of the poll to a single ‘tree’ rooted in the ground, which in turn
  * simplifies algorithms for navigating the structure.
  *
  *     @see <a href='../../../../overware/spec/forest' target='_top'>‘forest’</a>
  */
final class Ground extends Position2
{


    /** Constructs a Ground based on a backing position.
      *
      *     @see #back()
      */
    Ground( Position _back ) { super( _back ); }



   // ------------------------------------------------------------------------------------


    /** The backing position on which this ground is based.
      */
    Position back() { return back; }

      // PrecountedPoll depends on Ground being "implemented entirely by backing position"



    /** Demands further resolution of a possibly incomplete root candidate list.  This
      * method is effectively the same as {@linkplain #fetchVoters(int) fetchVoters}().
      */
    void fetchRootCandidates( int _threshold ) { back.fetchVoters( _threshold ); }



    /** A dynamic list of the root candidates of the poll.  The return value is identical
      * to that of {@linkplain #voters() voters}.
      */
    List<Position> rootCandidates() { return back.voters(); }



    /** A bell that sounds whenever the {@linkplain #rootCandidates() root candidate list}
      * becomes better defined.  The return value is identical to that of {@linkplain
      * #poll() poll}.{@linkplain Poll#votersDefinitionBell() votersDefinitionBell}.
      */
    Bell<Changed> rootCandidatesDefinitionBell() { return poll().votersDefinitionBell(); }



    /** Answers whether the root candidate list is possibly incomplete.  The return value
      * is identical to {@linkplain #votersMaybeIncomplete() votersMaybeIncomplete}.
      */
    boolean rootCandidatesMaybeIncomplete() { return back.votersMaybeIncomplete(); }



   // - P o s i t i o n ------------------------------------------------------------------


    /** Returns null.
      */
    public Position candidate() { return null; }


}
