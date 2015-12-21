package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;
import java.util.*;
import waymaker.gen.*;
import waymaker.spec.*;


/** A node that a {@linkplain Precounter precounter} may adjust in order to include changes read from
  * the user’s local wayrepo.
  */
public abstract class PrecountNode implements Node
{

    static final KittedPolyStatorSR<PrecountNode,SKit,RKit> stators = new KittedPolyStatorSR<>();

///////


    /** Super-contructs a PrecountNode and {@linkplain UnadjustedNode#precounted(PrecountNode) attaches
      * it} to the given unadjusted base.
      *
      *     @see #unadjusted()
      *     @param toCopyVoters Whether to copy the initial voters from the unadjusted base, or to leave
      *       the voters initially empty.
      */
    public PrecountNode( UnadjustedNode _unadjusted, final boolean toCopyVoters )
    {
        unadjusted = _unadjusted;

      // Initialize voters.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if( toCopyVoters )
        {
            final List<? extends UnadjustedNode> initialVoters = unadjusted.voters();
            voters = new ArrayList<>( /*initial capacity*/initialVoters.size()
              + /*room for later addition of precount voters*/4 );
            voters.addAll( initialVoters );
        }
        else voters = new ArrayList<>();

      // Attach to unadjusted base.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        unadjusted.precounted( this );
    }



   // --------------------------------------------------------------------------------------------------


    /** Adds the given node as a voter and adjusts the flow registers and their dependencies at this
      * node (future candidate) and each node down the root path.  Changes the content of the voters
      * list if it’s sufficiently extended, and possibly also the content and order of downstream lists.
      *
      *     @throws IllegalArgumentException if either (a) the voter is miscast such that
      *       voter.{@linkplain #rootwardInThis() rootwardInThis}.candidate does not equal this
      *       node, or (b) the voter is already listed.
      *     @throws IllegalArgumentException if the voter is miscast such that voter.{@linkplain
      *       #rootwardInThis() rootwardInThis}.candidate does not equal this node.
      */
    public abstract void addVoter( PrecountNode1 voter );



    /** Adds the given node as a voter and adjusts the flow registers and their dependencies at this
      * node (future candidate) and each node down the root path, stopping the adjustment before the
      * given {@linkplain #removeVoter(PrecountNode1,PrecountNode,Precounter) effective ground}.
      * Changes the content of the voters list if it’s sufficiently extended, and possibly also the
      * content and order of downstream lists.
      *
      *     @param effectiveGround The effective ground for use in the addition, which may be this
      *       node itself.
      *     @throws IllegalArgumentException if either (a) the voter is miscast such that
      *       voter.{@linkplain #rootwardInThis() rootwardInThis}.candidate does not equal this
      *       node, (b) the voter is already listed, or (c) effectiveGround is off the root path.
      */
    public abstract void addVoter( PrecountNode1 voter, PrecountNode effectiveGround );



    /** Adds the given node to the voters list without adjusting any flow registers.
      *
      *     @throws IllegalArgumentException if voter is miscast such that
      *       voter.{@linkplain #rootwardInThis() rootwardInThis}.candidate does not equal this node.
      *     @throws IllegalStateException if the voter list is insufficiently extended to accept the
      *       voter.
      */
    public final void enlistVoter( final PrecountNode voter )
    {
        assert voter.peerOrdinal() == 0; // major voter, okay for initial extension
        if( unadjusted.votersNextOrdinal() == 0 ) throw new IllegalStateException( "Insufficient extension" );

        enlistVoterUnchecked( voter );
    }


        /** Adds the given node to the voters list insofar as it’s sufficiently extended, and without
          * adjusting any flow registers.
          *
          *     @throws IllegalArgumentException if voter is miscast such that voter.{@linkplain
          *       #rootwardInThis() rootwardInThis}.candidate does not equal this node.
          */
        public final void enlistVoterIfExtended( final PrecountNode1 voter )
        {
            if( unadjusted.votersNextOrdinal() == 0 ) return;
              // not yet extended to cover precount (major) voters

            enlistVoterUnchecked( voter );
        }


        private void enlistVoterUnchecked( final PrecountNode voter )
        {
            if( voter.rootwardInThis().candidate() != this ) throw new IllegalArgumentException( "Miscast" );

            int v = Collections.binarySearch( voters, voter, peersComparator ); /* might speed search
              by limiting to zero-ordinal leaders, but would require tracking leader count */
            if( v >= 0 ) throw new IllegalArgumentException( "Already in list" );

            v = -v - 1; // decode to insertion point
            voters.add( v, voter );
        }



    /** Returns the precount-adjusted node that is already
      * {@linkplain UnadjustedNode#precounted(PrecountNode) attached} to the given unadjusted base;
      * or, if none is attached, then a newly constructed and attached one.
      */
    public static PrecountNode getOrMake( final UnadjustedNode una )
    {
        PrecountNode pre = una.precounted();
        if( pre == null ) pre = make( una );
        return pre;
    }



    /** Returns the identified precount node from the cache; or a newly constructed one, which may
      * entail connecting to the remote count server.
      */
    public static PrecountNode getOrMake( final VotingID id, final Precounter precounter )
    {
        UnadjustedNode una = precounter.getOrFetchUnadjusted( id );
        if( una == null ) return UnadjustedNode0.makeMappedPrecounted(id,precounter).precounted();

        return getOrMake( una );
    }



    /** Returns the identified precount node from the cache; or a newly constructed and cached one,
      * which may entail connecting to the remote count server; or null if _votedID is discovered to be the
      * identifier of the current vote.
      *
      *     @param toForceNode Ensures that the node is cached.  If missing from the original,
      *       unadjusted count, then toForceNode will construct and cache it.  This is done even if the
      *       vote is unchanged (_votedID is null) and consequently this method will return null (see
      *       _votedID below).
      *     @param _votedID An identifier that might differ from that of the current vote.  If actually
      *       the identifiers are discovered to be identical, then this method returns null.
      */
    public static PrecountNode getOrMakeIfVoteChanged( final VotingID id, final Precounter precounter,
      final boolean toForceNode, final VotingID _votedID )
    {
        UnadjustedNode una = precounter.getOrFetchUnadjusted( id );
        if( una == null )
        {
            if( _votedID == null )
            {
                if( toForceNode )
                {
                    UnadjustedNode0.makeMappedPrecounted( id, precounter );
                    logger.info( "(poll " + precounter.pollName() + ") Precounting " + id + " as bare root" );
                }
                return null;
            }

            una = UnadjustedNode0.makeMappedPrecounted( id, precounter );
            return una.precounted();
        }

        PrecountNode pre = una.precounted();
        if( pre == null )
        {
            if( !ObjectX.equals( _votedID, una.rootwardInThis().votedID() )) pre = make( una );
        }
        else if( ObjectX.equals( _votedID, pre.rootwardInThis().votedID() )) pre = null;
        return pre;
    }



    /** Removes the given node as a voter and adjusts the flow registers and their dependencies at
      * this node (present candidate) and each node down the root path.  Changes the content of
      * the voters list insofar as it’s complete, and possibly the content and order of downstream
      * lists.
      *
      *     @throws IllegalArgumentException if the voter is miscast such that voter.{@linkplain
      *       #rootwardInThis() rootwardInThis}.candidate still equals this node.
      *     @throws NoSuchElementException if the list is known to be complete and the voter is absent
      *       from it.
      */
    public abstract void removeVoter( PrecountNode1 voter );



    /** Removes the given node as a voter and adjusts the flow registers and their dependencies at
      * this node (present candidate) and each node down the root path, stopping the adjustment
      * before the {@linkplain EffectiveGrounder effective ground}.  There the vote change can no longer
      * affect flow volumes, making any further adjustment sub-optimal.  Changes the content of the
      * voters list insofar as it’s complete, and possibly the content and order of downstream lists.
      *
      * <p>This optimized method requires that the voter be part of no barred cycle at present, and none
      * after the vote shift.  Use it like this:</p><pre>
      *
      *   // knowing the voter is not, nor will result to be, part of a barred cycle:
      *   PrecountNode effectiveGround =
      *     candidate.removeVoter( voter, futureCandidate, precounter ); //
      *   futureCandidate.addVoter( voter, effectiveGround );           // a complete hand off</pre>
      *
      *     @param futureCandidate The node of the voter’s future candidate.
      *     @return The effective ground to use in the subsequent addition of the voter to the future
      *       candidate.  The effective ground may be this node itself, which happens when the voter
      *       is shifting leafward; or the node of the future candidate, which happens when the
      *       voter is shifting rootward; or some other node.  The future candidate may also be
      *       returned when the outflow of votes from the voter is zero.
      *
      *     @throws IllegalArgumentException if the voter is miscast such that voter.{@linkplain
      *       #rootwardInThis() rootwardInThis}.candidate still equals this node.
      *     @throws NoSuchElementException if the list is known to be complete and the voter is absent
      *       from it.
      */
    public abstract PrecountNode removeVoter( PrecountNode1 voter, PrecountNode futureCandidate,
      Precounter precounter );

      // Cycles are excluded because (a) the "effective ground" optimization might be difficult to code
      // in the presence of cycles; and (b) cyclic shifts are likely too uncommon to need optimization.



    /** Returns the shared instance of an unbarred cast to this node, first creating it if necessary.
      */
    public abstract RootwardCast<PrecountNode> rootwardHither_getOrMake();



    /** Saves state from the precount-adjusted voter, writing out to the parcel.
      */
    public void saveVoter( final PrecountNode1 voter, final Parcel out, final SKit kit )
    {
        PrecountNode1.stators.save( voter, out, kit );
    }


        /** Reconstructs a precount-adjusted voter and restores its state, reading in from the parcel.
          */
        public PrecountNode1 restoreVoter( final UnadjustedNode voterUna, final Parcel in, final RKit kit,
          final RootwardCast<PrecountNode> rootwardHither )
        {
            final PrecountNode1 voter = new PrecountNode1( voterUna, rootwardHither );
            PrecountNode1.stators.restore( voter, in, kit );
            return voter;
        }



    /** Finds voterSub.{@linkplain #unadjusted() unadjusted} if the voter list is sufficiently extended,
      * then replaces it with the given voterSub and reorders the list to accomodate any change of
      * {@linkplain #peerOrdinal() ordinal}.  Assumes that otherwise voterSub is a faithful copy of its
      * unadjusted counterpart, such that no further changes are needed.
      *
      *     @throws NoSuchElementException if the list is complete and the unadjusted counterpart is
      *       absent from the list.
      */
    public final void substituteVoter( final PrecountNode1 voterSub )
    {
        final UnadjustedNode unadjusted = voterSub.unadjusted();
        int vUna = voters.indexOf( unadjusted );
        if( vUna < 0 )
        {
            if( !votersMaybeIncomplete() ) throw new NoSuchElementException( unadjusted.toString() );

            return;
        }

        if( vUna != 0 && unadjusted.peerOrdinal() != 0 )
        {
            assert voterSub.peerOrdinal() != unadjusted.peerOrdinal(); // so placement might need adjusting

          // Maybe adjust placement in voters list.
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            int vMinor = vUna;
            do // decrement vMinor unto first minor voter in list
            {
                --vMinor;
                if( voters.get(vMinor).peerOrdinal() == 0 )  // then actually that's a major voter
                {
                    ++vMinor; // undo
                    break;
                }
            } while( vMinor != 0 );
            while( vUna > vMinor ) voters.set( vUna, voters.get(--vUna) );
              // decrement vUna unto vMinor, shifting forward all voters in between
        }

      // Replace unadjusted.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        voters.set( vUna, voterSub );
        assert isCorrectlyOrdered( vUna, voterSub );
    }



    /** The original node on which this precount-adjustable version is based.
      */
    public final UnadjustedNode unadjusted() { return unadjusted; }


        private final UnadjustedNode unadjusted;



    /** Removes the given node from the voters list insofar as the list is complete, and without
      * adjusting any flow registers.
      *
      *     @throws IllegalArgumentException if voter is miscast such that voter.{@linkplain
      *       #rootwardInThis() rootwardInThis}.candidate still equals this node.
      *     @throws NoSuchElementException if the list is known to be complete and the voter is absent
      *       from it.
      */
    public final void unlistVoter( final PrecountNode1 voter )
    {
        if( voter.rootwardInThis().candidate() == this ) throw new IllegalArgumentException( "Miscast" );

        if( !voters.remove( voter ))
        {
            if( !votersMaybeIncomplete() ) throw new NoSuchElementException( voter.toString() );
        }
    }



   // - N o d e ----------------------------------------------------------------------------------------


    public final VotingID id() { return unadjusted.id(); }



    /** {@inheritDoc} The ordinal number is always forced to zero for a precount adjusted node such as
      * this, helping to ensure its adjustments are {@linkplain #peersComparator visible} to the user
      * who authored them.
      */
    public final int peerOrdinal() { return 0; }


        /* * *
        - a precount might leave a candidate with a strangely misordered voter list in certain edge cases
            - problem
                - the precount engine generally avoids adjusting peer ordinals in response to vote shifts
                    - all it does is force the peer ordinal to zero in the nodes it changes
                - does not recalculate a candidate's major/minor threshold and adjust its voters' peer ordinals
                    - unlike the server count engine
                - so a precount shift of voters *away* from a candidate might leave it with minor voters only
                - with no major voters at the head of the voter list
                    - list completely unsorted
                    - might appear strange to the user
            - outlook
                - it was always possible for peers to be exclusively minor
                    - when large in number and low in vote flow
                - occurences are unlikely anyway
                - occurences are survivable
                - no need to correct this
          */



    public final RootwardCast<PrecountNode> rootwardInPrecount() { return rootwardInThis(); }



    public abstract @Override RootwardCast<PrecountNode> rootwardInThis();


        /** Recasts to reflect a vote change for the identified candidate and, if the cast is unbarred,
          * adjusts the flow registers at each node on the root path.
          *
          *     @see RootwardCast#votedID()
          *
          *     @throws AssertionError if assertions are enabled and the vote change would be redundant,
          *       votedID being the identifier of the current vote.  The caller is expected to guard
          *       against such an inefficient call.
          *     @throws UnsupportedOperationException if this node is the ground.
          */
        abstract void rootwardInThis( VotingID votedID, Precounter precounter );



    public final List<Node> voters() { return voters; }


        private final ArrayList<Node> voters;


        static { stators.add( new KittedStatorSR<PrecountNode,SKit,RKit>()
        {
            // Saves upstream precount voters recursively, both inlying (listed) and outlying (yet unlisted).
            // Changing?  Maybe also change for UnadjustedNodeV.voters.

            public void save( final PrecountNode node, final Parcel out, final SKit kit )
            {
              // 1. Inlying voters.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                {
                    final List<Node> inlyingVoters = node.voters;
                    final int vN = inlyingVoters.size();
                    out.writeInt( vN );
                    for( int v = 0; v < vN; ++v )
                    {
                        final Node voter = inlyingVoters.get( v );

                      // 1a. Voter ID.
                      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                        AndroidXID.writeUUID( voter.id(), out );
                        if( voter.getClass().equals( PrecountNode1.class ))
                        {
                          // 1b. Is voter a precount voter?
                          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                            ParcelX.writeBoolean( true, out );

                          // 1c. Is its unadjusted counterpart empty?
                          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                            final PrecountNode1 voterPre = (PrecountNode1)voter;
                            ParcelX.writeBoolean(
                              voterPre.unadjusted().getClass().equals(UnadjustedNode0.class), out );

                          // 1d. Voter.
                          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                            node.saveVoter( voterPre, out, kit );
                        }
                        else // unadjusted voter
                        {
                          // 1b. Is voter a precount voter?
                          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                            ParcelX.writeBoolean( false, out );
                        }
                    }
                }

              // 2. Outlying voters.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                for( final PrecountNode1 voter: kit.outlyingVotersPre() )
                {
                    if( voter.rootwardInThis().candidate() == node )
                    {

                      // 2a. Voter ID.
                      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                        AndroidXID.writeUUID( voter.id(), out );

                      // 2b. Is its unadjusted counterpart empty?
                      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                        ParcelX.writeBoolean(
                          voter.unadjusted().getClass().equals(UnadjustedNode0.class), out );

                      // 2c. Voter.
                      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                        node.saveVoter( voter, out, kit );
                    }
                }
                AndroidXID.writeUUIDNull( out ); // mark the end of this node's outlying voters
            }

            public void restore( final PrecountNode node, final Parcel in, final RKit kit )
            {
              // 1.
              // - - -
                final int vN = in.readInt();
                if( vN != 0 )
                {
                    final RootwardCast<PrecountNode> rootwardHither = node.rootwardHither_getOrMake();
                    final ArrayList<Node> inlyingVoters = node.voters;
                    inlyingVoters.ensureCapacity( vN );
                    int v = 0;
                    do
                    {
                      // 1a.
                      // - - -
                        final VotingID id = (VotingID)AndroidXID.readUUID( in );

                      // 1b.
                      // - - -
                        final boolean isPrecountVoter = ParcelX.readBoolean( in );
                        final Node voter;
                        if( isPrecountVoter )
                        {
                          // 1c.
                          // - - -
                            final boolean is0 = ParcelX.readBoolean( in );

                          // 1d.
                          // - - -
                            voter = node.restoreVoter( una(id,is0,kit), in, kit, rootwardHither );
                        }
                        else voter = kit.certainlyCached( id );
                        inlyingVoters.add( voter );
                        ++v;
                    }
                    while( v < vN );
                }

              // 2.
              // - - -
                VotingID id = (VotingID)AndroidXID.readUUIDOrNull( in );
                if( id == null ) return; // no outlying voters

                final RootwardCast<PrecountNode> rootwardHither = node.rootwardHither_getOrMake();
                do
                {
                  // 2b.
                  // - - -
                    final boolean is0 = ParcelX.readBoolean( in );

                  // 2c.
                  // - - -
                    kit.enlistOutlyingVoter( node.restoreVoter( una(id,is0,kit), in, kit, rootwardHither ));

                  // 2a.
                  // - - -
                    id = (VotingID)AndroidXID.readUUIDOrNull( in );
                }
                while( id != null );
            }

            private UnadjustedNode una( final VotingID id, final boolean is0, final RKit kit )
            {
                final UnadjustedNode una;
                if( is0 )
                {
                    una = new UnadjustedNode0( id, kit.groundUna() );
                    kit.encache( una );
                }
                else una = kit.certainlyCached( id );
                return una;
            }

        });}



    public final boolean votersMaybeIncomplete() { return unadjusted.votersMaybeIncomplete(); }



    public final int votersNextOrdinal() { return unadjusted.votersNextOrdinal(); }



   // - O b j e c t ------------------------------------------------------------------------------------


    public @Override final String toString() { return unadjusted.toString(); }



   // ==================================================================================================


    /** Resources for restoring the persisted state of precount adjusted nodes.
      */
    public static interface RKit
    {

       // - P r e c o u n t - N o d e . R - K i t ------------------------------------------------------

        /** Retrieves a node from the {@linkplain Forest#nodeCache() node cache} that is certainly there.
          *
          *     @throws IllegalStateException if the node is not actually cached.
          */
        public UnadjustedNode certainlyCached( VotingID id );
          // cached by the time of original saving, it should also be cached by now, the time of restoration


        /** Stores a restored node in the {@linkplain Forest#nodeCache() node cache}.
          */
        public void encache( final UnadjustedNode node ); // really an UnadjustedNode0 placeholder in this case


        /** Adds a restored node to the list of {@linkplain SKit#outlyingVotersPre() outlying
          * voters}, thus partially restoring it, too.
          */
        public void enlistOutlyingVoter( PrecountNode1 node );


        /** The unadjusted {@linkplain NodeCache#ground() ground}.
          */
        public UnadjustedGround groundUna();

    }


   // ==================================================================================================


    /** Resources for saving the persisted state of precount adjusted nodes.
      */
    public static interface SKit
    {

       // - P r e c o u n t - N o d e . S - K i t ------------------------------------------------------

        /** An unordered list of precount voters that are cached but unlisted by their candidates owing
          * to incomplete extension of their {@linkplain #voters() internal voter lists}.  When a
          * candidate saves its voters, it will scan this list for any outliers and save them too.
          */
        public List<PrecountNode1> outlyingVotersPre();

    }


//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private boolean isCorrectlyOrdered( final int v, final PrecountNode1 voter )
    {
        if( v != 0 && peersComparator.compare(voters.get(v-1),voter) >= 0 ) return false;

        if( v != voters.size() && peersComparator.compare(voter,voters.get(v+1)) >= 0 ) return false;

        return true;
    }



    private static final java.util.logging.Logger logger = LoggerX.getLogger( PrecountNode.class );



    /** Contructs a PrecountNode and {@linkplain UnadjustedNode#precounted(PrecountNode) attaches it} to
      * the given unadjusted base.
      *
      *     @see #unadjusted()
      *     @throws IllegalStateException if a precount node is already attached to una.
      */
    private static PrecountNode make( final UnadjustedNode una )
    {
        return una.isGround()? new PrecountGround(una): new PrecountNode1(una);
    }


///////

    static { stators.seal(); }

}
