package waymaker.top.android; // Copyright © 2015 Michael Allan.  Licence MIT.

import android.os.Parcel;
import waymaker.spec.*;


/** A precount-adjustable {@linkplain NodeCache#ground() ground pseudo-node}.
  */
public final class PrecountGround extends PrecountNode
{


    /** Contructs a PrecountGround and {@linkplain UnadjustedNode#precounted(PrecountNode) attaches it}
      * to the given unadjusted base.
      *
      *     @see #unadjusted()
      */
    public PrecountGround( final UnadjustedNode unadjusted )
    {
        super( unadjusted, /*toCopyVoters*/true );
        assert unadjusted.isGround();
    }



   // --------------------------------------------------------------------------------------------------


      @Override/*to allow for "voters" who are barred as such, and so instead roots*/
    public void saveVoter( final PrecountNode1 voter, final Parcel out, final SKit kit )
    {
      // a. Vote.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        AndroidXID.writeUDIDOrNull( voter.rootwardInThis().votedID(), out );

      // b. Voter.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        PrecountNode1.stators.save( voter, out, kit );
    }



    public @Override PrecountNode1 restoreVoter( final UnadjustedNode voterUna, final Parcel in,
      final RKit kit, final RootwardCast<PrecountNode> rootwardHither )
    {
      // a.
      // - - -
        final VotingID votedID = (VotingID)AndroidXID.readUDIDOrNull( in );
        final RootwardCast<PrecountNode> cast;
        if( votedID == null ) cast = rootwardHither; // voter is actually a non-voter
        else cast = new RootwardCastB<PrecountNode>( this, votedID ); // voter is barred

      // b.
      // - - -
        final PrecountNode1 voter = new PrecountNode1( voterUna, cast );
        PrecountNode1.stators.restore( voter, in, kit );
        return voter;
    }



   // - C o u n t - N o d e ----------------------------------------------------------------------------


    public boolean isGround() { return true; }



    public Waynode waynode() { return Waynode.EMPTY_WAYNODE; }


        public void waynode( Waynode1 _waynode ) { throw new UnsupportedOperationException(); }



   // - P r e c o u n t - N o d e ----------------------------------------------------------------------


    public RootwardCast<PrecountNode> rootwardHither_getOrMake() { return rootwardHither; }


        private final RootwardCastU<PrecountNode> rootwardHither =
          new RootwardCastU<PrecountNode>( this );



    public RootwardCast<PrecountNode> rootwardInThis() { return null; }


        public void rootwardInThis( VotingID _votedID, Precounter _precounter )
        {
            throw new UnsupportedOperationException();
        }



   // - P r e c o u n t - N o d e -- s -----------------------------------------------------------------


    public void addVoter( PrecountNode1 _voter ) { enlistVoterIfExtended( _voter ); }



    public void addVoter( final PrecountNode1 _voter, final PrecountNode effectiveGround )
    {
        if( effectiveGround != this ) throw new IllegalArgumentException( "Effective ground is off path" );

        enlistVoterIfExtended( _voter );
    }



    public void removeVoter( PrecountNode1 voter ) { unlistVoter( voter ); }



    public PrecountNode removeVoter( final PrecountNode1 voter, final PrecountNode _candidate, Precounter p_ )
    {
        unlistVoter( voter );
        return this;
    }


}
