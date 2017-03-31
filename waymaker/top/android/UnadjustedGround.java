package waymaker.top.android; // Copyright © 2015 Michael Allan.  Licence MIT.

import android.os.Parcel;
import waymaker.gen.*;
import waymaker.spec.*;

import static waymaker.top.android.Waynode.EMPTY_WAYNODE;


/** An unadjusted {@linkplain NodeCache#ground() ground pseudo-node}.
  */
public final class UnadjustedGround extends UnadjustedNodeV
{


    /** Contructs an UnadjustedGround.
      */
    public @ThreadSafe UnadjustedGround() { super( null, 0, (RootwardCast<UnadjustedNode>)null ); }



   // --------------------------------------------------------------------------------------------------


      @Override/*to allow for "voters" who are barred as such, and so instead roots*/
    public void saveVoter( final UnadjustedNode1 voter, final Parcel out, final SKit kit )
    {

      // a. Voter ID.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        AndroidXID.writeUDID( voter.id(), out );

      // b. Voter ordinal.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        out.writeInt( voter.peerOrdinal() );

      // c. Waynode.
      // - - - - - - -
        Waynode1.stators.saveD( voter.waynode(), out, EMPTY_WAYNODE );

      // d. Vote.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        AndroidXID.writeUDIDOrNull( voter.rootwardInThis().votedID(), out );

      // e. Voter.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        UnadjustedNode1.stators.save( voter, out, kit );
    }



    /** Restores state to this ground.
      *
      *     @param state The state as marshalled from the {@linkplain stators stators}.
      */
      @ThreadRestricted("further KittedPolyStatorSR.openToThread") // for stators.restore
    public void restore( final byte[] state, final UnadjustedNodeV.RKit kit )
    {
        final Parcel in = Parcel.obtain(); // grep Parcel-TS
        try
        {
            in.unmarshall( state, 0, state.length ); // sic
            in.setDataPosition( 0 ); // undocumented requirement
            stators.restore( this, in, kit );
        }
        finally { in.recycle(); } // grep ParcelReuse
    }



    public @Override UnadjustedNode1 restoreVoter( final VotingID id, final Parcel in, final RKit kit,
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
        final VotingID votedID = (VotingID)AndroidXID.readUDIDOrNull( in );
        final RootwardCast<UnadjustedNode> cast;
        if( votedID == null ) cast = rootwardHither; // voter is actually a non-voter
        else cast = new RootwardCastB<UnadjustedNode>( this, votedID ); // voter is barred

      // e.
      // - - -
        final UnadjustedNode1 voter = new UnadjustedNode1( id, peerOrdinal, cast, waynode );
        kit.encache( voter );
        UnadjustedNode1.stators.restore( voter, in, kit );
        return voter;
    }



   // - N o d e ----------------------------------------------------------------------------------------


    public boolean isGround() { return true; }



    public Waynode1 waynode() { return Waynode.EMPTY_WAYNODE; }



   // - O b j e c t ------------------------------------------------------------------------------------


    public @Override String toString() { return "ground"; }



   // - U n a d j u s t e d - N o d e ------------------------------------------------------------------


    public @Override PrecountGround precounted() { return (PrecountGround)super.precounted(); }


        public @Override void precounted( final PrecountNode _precounted )
        {
            if( !(_precounted instanceof PrecountGround) ) throw new IllegalArgumentException();

            super.precounted( _precounted );
        }


}
