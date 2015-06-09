package overware.top.android; // Copyright 2015, Michael Allan.

import overware.gen.*;


/** A agent to move incrementally through the forest structure of a poll.
  *
  *     @see <a href='../../../../overware/spec/forest' target='_top'>‘forest’</a>
  */
final class PollTourist
{

    static final PolyStator<PollTourist> stators = new PolyStator<>();

///////


    /** Creates a PollTourist.
      *
      *     @see #poll()
      */
    PollTourist( PrecountedPoll _poll )
    {
        poll = _poll;
        candidate = poll.ground();
    }



   // ------------------------------------------------------------------------------------



    /** Commands this tourist to move leafward to one of the immediate voters of the
      * current position, namely one of position.{@linkplain Position#voters() voters}.
      * Ultimately this will change the value of the {@linkplain #position() position} and
      * {@linkplain #candidate() candidate}.
      *
      *     @param target The immediate voter to move to.
      *
      *     @throws IllegalArgumentException if the current position is null, or the
      *       target is not among its immediate voters.
      */
    void ascendToVoter( final Position target ) { throw new UnsupportedOperationException(); }



    /** The immediate formal candidate on which the position of this tourist is based.
      * Any change to the return value will be signalled by the {@linkplain #motionBell()
      * motion bell}.  The value is identical to position.{@linkplain Position#candidate()
      * candidate} when the position is non-null.  However the value is never null,
      * because this tourist will not base itself on an unknown candidate.
      *
      *     @return Either a real position or the {@linkplain Poll#ground() ground}
      *       pseudo-position.
      */
    Position candidate() { return candidate; }


        private Position candidate;



    /** Commands this tourist to move rootward to one of the candidates on which its
      * position is based.
      *
      *     @throws IllegalArgumentException if the target is not identical to the value
      *       of {@linkplain #candidate() candidate}, nor candidate.{@linkplain
      *       Position#candidate() candidate}, and so on recursively.
      */
    void descendToCandidate( final Position target ) { throw new UnsupportedOperationException(); }



    /** A bell that sounds whenever this tourist moves.  Movements are commanded by the
      * following methods:
      *
      * <ul>
      *     <li>{@linkplain #ascendToVoter(Position) ascendToVoter}</li>
      *     <li>{@linkplain #descendToCandidate(Position) descendToCandidate}</li>
      *     <li>{@linkplain #moveToPeer(Position) moveToPeer}</li>
      *     </ul>
      */
    Bell<Changed> motionBell() { return motionBell; }


        private final ReEmitter<Changed> motionBell = Changed.newReEmitter();



    /** Commands this tourist to move laterally to one of the peers of the current
      * position, namely one of candidate.{@linkplain Position#voters() voters}.
      * Ultimately this will change the value of the {@linkplain #position() position}.
      *
      *     @param target The peer to move to.
      *
      *     @throws IllegalArgumentException if the target is not among the immediate
      *       voters of the candidate.
      */
    void moveToPeer( final Position target ) { throw new UnsupportedOperationException(); }



    /** The poll whose forest structure is toured.
      */
    PrecountedPoll poll() { return poll; }


        private final PrecountedPoll poll;



    /** The exact position of this tourist in the poll, or null if it is not exactly
      * positioned.  Any change to the return value will be signalled by the {@linkplain
      * #motionBell() motion bell}.
      */
    Position position() { return position; }


        private Position position;


///////

    static { stators.seal(); }


}
