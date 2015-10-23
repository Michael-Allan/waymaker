package overware.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import android.os.Parcel;
import overware.gen.ThreadSafe;
import overware.spec.*;


/** An unadjusted {@linkplain NodeCache#ground() ground pseudo-node}.
  */
final class UnadjustedGround extends UnadjustedNodeV
{


    /** Contructs an UnadjustedGround.
      */
    @ThreadSafe UnadjustedGround() { super( null, 0, (RootwardCast<UnadjustedNode>)null ); }



   // --------------------------------------------------------------------------------------------------


      @Override/*to allow for voters that are barred*/
    void saveVoter( final UnadjustedNode1 voter, final Parcel out, final SKit kit )
    {

      // a. Voter ID.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        AndroidXID.writeUUID( voter.id(), out );

      // b. Voter ordinal.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        out.writeInt( voter.peerOrdinal() );

      // c. Vote.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        AndroidXID.writeUUIDOrNull( voter.rootwardInThis().votedID(), out );

      // d. Voter.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        UnadjustedNode1.stators.save( voter, out, kit );
    }



    @Override UnadjustedNode1 restoreVoter( final VotingID id, final Parcel in, final RKit kit,
      final RootwardCast<UnadjustedNode> rootwardHither )
    {
      // b.
      // - - -
        final int peerOrdinal = in.readInt();

      // c.
      // - - -
        final VotingID votedID = (VotingID)AndroidXID.readUUIDOrNull( in );
        final RootwardCast<UnadjustedNode> cast;
        if( votedID == null ) cast = rootwardHither; // voter is actually a non-voter
        else cast = new RootwardCastB<UnadjustedNode>( this, votedID ); // voter is barred

      // d.
      // - - -
        final UnadjustedNode1 voter = new UnadjustedNode1( id, peerOrdinal, cast );
        kit.cache( voter );
        UnadjustedNode1.stators.restore( voter, in, kit );
        return voter;
    }



   // - N o d e ----------------------------------------------------------------------------------------


    public boolean isGround() { return true; }



   // - O b j e c t ------------------------------------------------------------------------------------


    public @Override String toString() { return "ground"; }



   // - U n a d j u s t e d - N o d e ------------------------------------------------------------------


    @Override PrecountGround precounted() { return (PrecountGround)super.precounted(); }


        @Override void precounted( final PrecountNode _precounted )
        {
            if( !(_precounted instanceof PrecountGround) ) throw new IllegalArgumentException();

            super.precounted( _precounted );
        }


}
