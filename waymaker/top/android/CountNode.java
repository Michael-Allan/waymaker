package waymaker.top.android; // Copyright © 2015 Michael Allan.  Licence MIT.

import java.util.Comparator;
import java.util.List;
import waymaker.spec.*;


/** A formal element in a {@linkplain Forest pollar forest}.  It summarizes the formal contribution of
  * {@linkplain #id() one actor’s} position to the poll, particularly his (or her, or its) cast
  * relations to {@linkplain #rootwardInThis() rootward} candidates and the leafward {@linkplain
  * #voters() voters}.  These cast relations combine with those of other nodes and harden to form an
  * arboreal skeleton for the poll, suited for carrying its material content: internally its coalescent
  * flow of vote sums, and externally its survey of the available {@linkplain #waynode() ways}.
  */
public interface CountNode
{


    /** A comparator that sorts nodes by {@linkplain #peerOrdinal() peer ordinal} (low to high), then by
      * vote inflow (high to low), and finally {@linkplain UDID#comparatorUniversal by identity tag}.  It
      * is the comparator used to sort each node’s {@linkplain #voters() voter list}.  It effectively
      * places major (zero ordinal) peers in front according to their inflow, followed by minor peers
      * according to their ordinals.  This matches the order in which data pages are received from the
      * {@linkplain ServerCount#enqueuePeersRequest(VotingID,PeersReceiver,int) server count}.
      */
    public static final Comparator<CountNode> peersComparator = new Comparator<CountNode>()
    {
        public int compare( final CountNode n, final CountNode m )
        {
            if( n == m ) return 0; // short cut, optimizing for a common case

            int result = Integer.compare( n.peerOrdinal(), m.peerOrdinal() ); // or throws NullPointerException
            if( result == 0 ) result = UDID.comparatorUniversal.compare( n.id(), m.id() );
            return result;
        }
    };



   // - N o d e ----------------------------------------------------------------------------------------


    /** The identity of the actor who holds the position behind this count node, or null if this is the
      * {@linkplain NodeCache#ground() ground node}.
      */
    public VotingID id();



    /** Answers whether this count node is the {@linkplain NodeCache#ground() ground node}.
      */
    public boolean isGround();



    /** This count node’s ordinal number as a peer.  The ordinal number serves to page the data received
      * from the {@linkplain ServerCount#enqueuePeersRequest(VotingID,PeersReceiver,int) server count}.
      *
      *     @see <a href='../../../../peer' target='_top'>‘peer’</a>
      *     @see ServerCount#enqueuePeersRequest(VotingID,PeersReceiver,int,int)
      */
    public int peerOrdinal();



    /** The {@linkplain #rootwardInThis() cast rootward} from this count node in the precount-adjusted
      * version of the forest.  If this node is itself a {@linkplain PrecountNode precount node} (pre),
      * or has a {@linkplain UnadjustedNode#precounted() precounted version} (pre), then this method
      * returns pre.rootwardInThis.  Otherwise it returns this.rootwardInThis.
      *
      * <p>Most rootward travellers want exactly this switching behaviour.  The forest may diverge
      * rootward between its adjusted and unadjusted versions — picture a railway track forking — and
      * most travellers want only the adjusted version.  Certainly the reverse switch
      * ‘rootwardInUnadjusted’ is unwanted, and indeed none is coded.</p>
      *
      * <p>Leafward travellers have no choice at all because both versions of the forest converge in
      * that direction on the unadjusted version.  This navigational asymmetry (rootward divergence and
      * leafward convergence) arises because the precount is determined by vote changes that flow
      * exclusively rootward in their effects; the leafward part is unaffected and remains equal to that
      * of the unadjusted version.  The data modeling keeps it equal (indeed identical) by physically
      * sharing it between the two versions of the forest.  This is optimal because the shared leafward
      * part may be very large.</p>
      */
    public RootwardCast<? extends CountNode> rootwardInPrecount();



    /** The cast rootward from this count node, or null if this node is the ground.
      */
    public RootwardCast<? extends CountNode> rootwardInThis();



    /** An {@linkplain #peersComparator ordered}, extensible listing of the peer group directly leafward
      * of this count node.  Its members are either the immediate voters (if this is a proper node) or
      * the roots of the forest (if this is the ground node).  Any change to the list will be signalled
      * by the {@linkplain ForestCache#voterListingBell() listing bell}.  All changes will be pure
      * extensions that leave the number and order of previously listed members unchanged.
      *
      *     @see <a href='../../../../peer' target='_top'>‘peer’</a>
      */
    public List<? extends CountNode> voters();



    /** Answers whether the {@linkplain #voters() voter list} might be incomplete.  A change from true
      * to false will be signalled by the {@linkplain ForestCache#voterListingBell() listing bell}.  No
      * other change is possible.
      */
    public boolean votersMaybeIncomplete();



    /** The peer ordinal of the next voter to request on further extending the voter list, or
      * {@linkplain Integer#MAX_VALUE MAX_VALUE} if the list is fully extended.
      */
    public int votersNextOrdinal();



    /**  The way contributions of the position behind this count node.
      */
    public Waynode waynode();


}
