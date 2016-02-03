package waymaker.top.android; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;
import java.util.*;
import waymaker.gen.*;
import waymaker.spec.*;

import static waymaker.top.android.Waynode.EMPTY_WAYNODE;


/** A skeletal implementation of a votable unadjusted node.
  */
public abstract class UnadjustedNodeV extends UnadjustedNode
{

    static final KittedPolyStatorSR<UnadjustedNodeV,SKit,RKit> stators = new KittedPolyStatorSR<>();

///////


    /** Contructs an UnadjustedNodeV.
      *
      *     @see #id()
      *     @see #peerOrdinal()
      *     @see #rootwardInThis()
      */
      @ThreadSafe
    public UnadjustedNodeV( VotingID _id, int _peerOrdinal, RootwardCast<UnadjustedNode> _rootwardInThis )
    {
        id = _id;
        peerOrdinal = _peerOrdinal;
        rootwardInThis = _rootwardInThis;
    }



   // --------------------------------------------------------------------------------------------------


    /** Saves state from the voter, writing out to the parcel.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread") // for stators.save
    public void saveVoter( final UnadjustedNode1 voter, final Parcel out, final SKit kit )
    {
      // a. Voter ID.
      // - - - - - - -
        AndroidXID.writeUDID( voter.id(), out );

      // b. Voter ordinal.
      // - - - - - - - - - -
        out.writeInt( voter.peerOrdinal() );

      // c. Waynode.
      // - - - - - - -
        Waynode1.stators.saveD( voter.waynode(), out, EMPTY_WAYNODE );

      // d. Voter.
      // - - - - - -
        UnadjustedNode1.stators.save( voter, out, kit );
    }



    /** Reconstructs a voter and restores its state, reading in from the parcel.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread") // for stators.restore
    public UnadjustedNode1 restoreVoter( final VotingID id, final Parcel in, final RKit kit,
      final RootwardCast<UnadjustedNode> rootwardHither )
    {
      // b.
      // - - -
        final int peerOrdinal = in.readInt();

      // c.
      // - - -
        final Waynode1 waynode = Waynode1.makeD( in, EMPTY_WAYNODE );

      // d.
      // - - -
        final UnadjustedNode1 voter = new UnadjustedNode1( id, peerOrdinal, rootwardHither, waynode );
        kit.encache( voter );
        UnadjustedNode1.stators.restore( voter, in, kit );
        return voter;
    }



   // - N o d e ----------------------------------------------------------------------------------------


    public final VotingID id() { return id; }


        private final VotingID id;



    public final int peerOrdinal() { return peerOrdinal; }


        private final int peerOrdinal;



    public final RootwardCast<UnadjustedNode> rootwardInThis() { return rootwardInThis; }


        private final RootwardCast<UnadjustedNode> rootwardInThis;



    public final List<UnadjustedNode1> voters() { return voters; }


        private final UnadjustedVoterList voters = new UnadjustedVoterList();


        static { stators.add( new KittedStatorSR<UnadjustedNodeV,SKit,RKit>()
        {
            // Saves upstream voters recursively, both inlying (listed) and outlying (yet unlisted).
            // Changing?  Maybe also change for PrecountNode.voters.

            public void save( final UnadjustedNodeV node, final Parcel out, final SKit kit )
            {
              // 1. Inlying voters.
              // - - - - - - - - - -
                {
                    final List<UnadjustedNode1> inlyingVoters = node.voters;
                    final int vN = inlyingVoters.size();
                    out.writeInt( vN );
                    for( int v = 0; v < vN; ++v ) node.saveVoter( inlyingVoters.get(v), out, kit );
                }

              // 2. Outlying voters.
              // - - - - - - - - - - -
                for( final UnadjustedNode1 voter: kit.outlyingVotersUna() )
                {
                    if( voter.rootwardInThis().candidate() == node ) { node.saveVoter( voter, out, kit ); }
                }
                AndroidXID.writeUDIDNull( out ); // mark the end of this node's outlying voters
            }

            public void restore( final UnadjustedNodeV node, final Parcel in, final RKit kit )
            {
              // 1.
              // - - -
                final int vN = in.readInt();
                if( vN != 0 )
                {
                    final RootwardCast<UnadjustedNode> rootwardHither = node.rootwardHither_getOrMake();
                    final UnadjustedNode1[] inlyingVoters = new UnadjustedNode1[vN];
                    int v = 0;
                    do
                    {
                      // a.
                      // - - -
                        final VotingID id = (VotingID)AndroidXID.readUDID( in );

                      // - - -
                        inlyingVoters[v++] = node.restoreVoter( id, in, kit, rootwardHither );
                    }
                    while( v < vN );
                    node.voters.array( inlyingVoters );
                }

              // 2.
              // - - -
                VotingID id = (VotingID)AndroidXID.readUDIDOrNull( in );
                if( id == null ) return; // no outlying voters

                final RootwardCast<UnadjustedNode> rootwardHither = node.rootwardHither_getOrMake();
                do
                {
                    kit.enlistOutlyingVoter( node.restoreVoter( id, in, kit, rootwardHither ));

                  // a.
                  // - - -
                    id = (VotingID)AndroidXID.readUDIDOrNull( in );
                }
                while( id != null );
            }

        });}



    public final boolean votersMaybeIncomplete() { return votersNextOrdinal != Integer.MAX_VALUE; }



    public final int votersNextOrdinal() { return votersNextOrdinal; }


        private int votersNextOrdinal;


        static { stators.add( new Stator<UnadjustedNodeV>()
        {
            public void save( final UnadjustedNodeV una, final Parcel out )
            {
                out.writeInt( una.votersNextOrdinal );
            }
            public void restore( final UnadjustedNodeV una, final Parcel in )
            {
                una.votersNextOrdinal = in.readInt();
            }
        });}


        /** Sets the peer ordinal of the next voter in order to mark an extension or completion of the
          * voter list.
          *
          *     @throws IllegalArgumentException if the voter list is already complete, or if newValue
          *        does not register an actual change that extends or properly completes it.
          */
        public final void votersNextOrdinal( final int newValue )
        {
            if( votersNextOrdinal == Integer.MAX_VALUE )
            {
                throw new IllegalArgumentException( "Already complete" );
            }

            if( newValue <= votersNextOrdinal )
            {
                throw new IllegalArgumentException( "Improper advance or completion" );
            }

            votersNextOrdinal = newValue;
        }



   // - U n a d j u s t e d - N o d e ------------------------------------------------------------------


    public final RootwardCast<UnadjustedNode> rootwardHither_getOrMake()
    {
        if( rootwardHither == null ) rootwardHither = new RootwardCastU<UnadjustedNode>( this );
        return rootwardHither;
    }


        private RootwardCastU<UnadjustedNode> rootwardHither;



   // ==================================================================================================


    /** Resources for restoring the persisted state of votable unadjusted nodes.
      */
    public static interface RKit
    {

       // - U n a d j u s t e d - N o d e - V . R - K i t ----------------------------------------------

        /** Stores a restored node in the {@linkplain Forest#nodeCache() node cache}.
          */
        public void encache( final UnadjustedNode node );


        /** Adds a restored node to the list of {@linkplain SKit#outlyingVotersUna()
          * outlying voters}, thus partially restoring it, too.
          */
        public void enlistOutlyingVoter( UnadjustedNode1 node );

    }


   // ==================================================================================================


    /** Resources for saving the persisted state of votable unadjusted nodes.
      */
    public static interface SKit
    {

       // - U n a d j u s t e d - N o d e - V . S - K i t ----------------------------------------------

        /** An unordered list of unadjusted voters that are cached but unlisted by their candidates
          * owing to incomplete extension of their {@linkplain #voters() internal voter lists}.  When a
          * candidate saves its voters, it will scan this list for any outliers and save them too.
          */
        public List<UnadjustedNode1> outlyingVotersUna();

    }


///////

    static { stators.seal(); }

}
