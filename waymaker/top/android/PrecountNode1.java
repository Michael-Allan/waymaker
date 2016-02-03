package waymaker.top.android; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;
import waymaker.gen.*;
import waymaker.spec.VotingID;

import static waymaker.top.android.Waynode.EMPTY_WAYNODE;


/** An implementation of a precount-adjustable node.
  */
public final class PrecountNode1 extends PrecountNode
{

    static final KittedPolyStatorSR<PrecountNode1,SKit,RKit> stators =
      new KittedPolyStatorSR<>( PrecountNode.stators );

///////


    /** Contructs a PrecountNode1 and {@linkplain UnadjustedNode#precounted(PrecountNode) attaches it}
      * to the given unadjusted base, also copying its initial voters from the unadjusted base, and
      * constructing any of its candidates that are yet unconstructed on the {@linkplain
      * #rootwardInThis() root path}.
      *
      *     @see #unadjusted()
      *     @throws NullPointerException if the given unadjusted base is the ground.  In that case, you
      *       should construct a PrecountGround instead.
      */
    public PrecountNode1( final UnadjustedNode unadjusted )
    {
        super( unadjusted, /*toCopyVoters*/true );
        waynode = unadjusted.waynode();

      // Ensure that all rootward candidates are precount adjustable, too.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final RootwardCast<UnadjustedNode> rootwardInUna = unadjusted.rootwardInThis();
        final PrecountNode candidate = getOrMake( rootwardInUna.candidate() ); // initially same as una
        rootwardInThis = rootwardInUna.isBarred()?
          new RootwardCastB<PrecountNode>( candidate, rootwardInUna.votedID() ):
          candidate.rootwardHither_getOrMake();

      // Replace the unadjusted base in the candidate's voter list with this node.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if( !unadjusted.getClass().equals( UnadjustedNode0.class )) candidate.substituteVoter( this );
        else candidate.addVoter( this ); // nothing to replace, so simply add it
    }



    /** Contructs a PrecountNode1 and {@linkplain UnadjustedNode#precounted(PrecountNode) attaches it}
      * to the given unadjusted base.
      *
      *     @see #unadjusted()
      *     @see #rootwardInThis()
      *     @throws AssertionError if assertions are enabled and the given unadjusted base is the
      *       ground.  In that case, you should construct a PrecountGround instead.
      */
    public PrecountNode1( final UnadjustedNode unadjusted, final RootwardCast<PrecountNode> _rootwardInThis )
    {
        super( unadjusted, /*toCopyVoters*/false );
        assert !unadjusted.isGround(); // else this.isGround is wrong
        waynode = unadjusted.waynode();
        rootwardInThis = _rootwardInThis;
    }



   // - N o d e ----------------------------------------------------------------------------------------


    public boolean isGround() { return false; }



    public Waynode waynode() { return waynode; }


        private Waynode1 waynode;


        static { stators.add( new Stator<PrecountNode1>()
        {
            public void save( final PrecountNode1 node, final Parcel out )
            {
                Waynode1.stators.saveD( node.waynode, out, EMPTY_WAYNODE );
            }
            public void restore( final PrecountNode1 node, final Parcel in )
            {
                node.waynode = Waynode1.makeD( in, EMPTY_WAYNODE );
            }
        });}


        public void waynode( final Waynode1 _waynode )
        {
            if( _waynode == null ) throw new NullPointerException();

            assert !_waynode.equals( waynode );
            waynode = _waynode;
        }



   // - P r e c o u n t - N o d e ----------------------------------------------------------------------


    public RootwardCast<PrecountNode> rootwardHither_getOrMake()
    {
        if( rootwardHither == null ) rootwardHither = new RootwardCastU<PrecountNode>( this );
        return rootwardHither;
    }


        private RootwardCastU<PrecountNode> rootwardHither;



    public RootwardCast<PrecountNode> rootwardInThis() { return rootwardInThis; }


        private RootwardCast<PrecountNode> rootwardInThis;



    public void rootwardInThis( final VotingID _votedID, final Precounter precounter )
    {
        // the tense of _VARNAME is future, while VARNAME is present
        final VotingID thisID = id();
        final VotingID votedID = rootwardInThis.votedID();
        logger.info( "(poll " + precounter.pollName() + ") Precounting " + thisID + ", changing vote from " + votedID + " to " + _votedID );
        if( votedID == null )
        {
            if( _votedID == null )
            {
                assert false: NON_REDUNDANCY_ASSERTION;
                return;
            }

            rootwardInThis_b_presentUncast( _votedID, precounter );
            return;
        }
        assert !ObjectX.equals( votedID, _votedID ): NON_REDUNDANCY_ASSERTION;

      // (a1) Detect any present, barred cycle that involves this node.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final CycleForeseer cycleForeseer = precounter.cycleForeseer();
        final PrecountGround ground = precounter.ground().precounted(); // not null, ctor of this ensures
        for( PrecountNode nod = this;; ) // each node on root path
        {
            final RootwardCast<PrecountNode> cast = nod.rootwardInThis();
            if( cast.isBarred() ) // then here's a barred cycle, but does it involve this node?
            {
                try( final CycleForeseer cF = cycleForeseer )
                {
                    cF.foresee( this, cast.votedID(), precounter ); /* Would the barred cast flow to
                      this node, meaning it's now involved in the cycle?  Only if the cast is for
                      this node, or one of its leafward voters.  This test will tell. */
                    if( cF.barNode() == null ) break; // no; cycle must be separate, downstream

                    rootwardInThis_c_presentCycle( _votedID, /*barNode*/(PrecountNode1)nod, precounter );
                    return;
                }
            }

            nod = cast.candidate(); // next on path
            if( nod == ground ) break; // end of path, no cycle detected
        }

      // (a2) Detect any future, barred cycle that would involve this node.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final PrecountNode candidate = rootwardInThis.candidate();
        final PrecountNode _candidate;
        try( final CycleForeseer cF = cycleForeseer )
        {
            cF.foresee( this, _votedID, precounter );
            if( cF.barNode() != null )
            {
              // (a3') Uncast the old vote.
              // - - - - - - - - - - - - - -
                rootwardInThis = ground.rootwardHither_getOrMake();

              // (a4') Unflow the old vote.
              // - - - - - - - - - - - - - -
                candidate.removeVoter( this ); //
                ground.addVoter( this );      // a complete hand off

              // - - -
                rootwardInThis_d_presentUncast_futureCycle( _votedID, cF );
                return;
            }

            _candidate = cF.candidate();
        }

        if( _votedID == null )
        {
          // (a3'') Uncast and unflow the old vote, leaving this node uncast.
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            rootwardInThis = ground.rootwardHither_getOrMake();
            candidate.removeVoter( this ); //
            ground.addVoter( this );      // a complete hand off
            return;
        }

      // (a3) Uncast the old vote, recasting the new.
      // - - - - - - - - - - - - - - - - - - - - - - -
        rootwardInThis = _candidate.rootwardHither_getOrMake();

      // (a4) Unflow the old vote and reflow the new.
      // - - - - - - - - - - - - - - - - - - - - - - -
        _candidate.addVoter( this, candidate.removeVoter(this,_candidate,precounter) ); // a complete hand off
    }



   // - P r e c o u n t - N o d e -- s -----------------------------------------------------------------


    public void addVoter( final PrecountNode1 voter )
    {
        enlistVoterIfExtended( voter );
        final int voterOutflow = VOTER_OUTFLOW;
        if( voterOutflow != 0 ) registerInflowChange( voterOutflow, /*effectiveGroundID*/null );
    }



    public void addVoter( final PrecountNode1 voter, final PrecountNode effectiveGround )
    {
        enlistVoterIfExtended( voter );
        if( effectiveGround == this ) return; // vote flow is unaffected

        final int voterOutflow = VOTER_OUTFLOW;
        if( voterOutflow != 0 ) registerInflowChange( voterOutflow, effectiveGround.id() );
    }



    public void removeVoter( final PrecountNode1 voter )
    {
        unlistVoter( voter );
        final int voterOutflow = VOTER_OUTFLOW;
        if( voterOutflow != 0 ) registerInflowChange( -voterOutflow, /*effectiveGroundID*/null );
    }



    public PrecountNode removeVoter( final PrecountNode1 voter, final PrecountNode futureCandidate,
      final Precounter precounter )
    {
        unlistVoter( voter );
        final int voterOutflow = VOTER_OUTFLOW;
        final PrecountNode effectiveGround;
        if( voterOutflow == 0 ) effectiveGround = futureCandidate; // vote flow is unaffected here, and there
        else
        {
            effectiveGround = precounter.grounder().effectiveGround( this, futureCandidate );
            if( effectiveGround != this ) registerInflowChange( -voterOutflow, effectiveGround.id() );
        }
        return effectiveGround;
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final java.util.logging.Logger logger = LoggerX.getLogger( PrecountNode1.class );



    private static final String NON_REDUNDANCY_ASSERTION = "No redundant vote change";



    /** @param effectiveGroundID The identity of the effective ground for the change, which must
      *    either be null, or be obtained from another node in the same cache.  It is tested for
      *    equality with candidate identity tags using the == operator.  Do not call this method if the
      *    effective ground is this node itself; that is an unexpected no-op case.
      */
    private void registerInflowChange( final int inflowChange, final VotingID effectiveGroundID )
    {
        assert inflowChange != 0; // not wasting time in no-op calls
        assert effectiveGroundID != id(); // unexpected no-op case
        for( PrecountNode node = this;; ) // each node on root path, including this node
        {
            // will here apply inflowChange to flow registers at node (not yet coded)
            final PrecountNode candidate;
            try{ candidate = node.rootwardInThis().candidate(); }
            catch( NullPointerException _x )
            {
                throw new IllegalArgumentException( "Effective ground is off path: " + effectiveGroundID );
            }

            if( 1 == 1 ) throw new UnsupportedOperationException( "Reorder node in candidate, as necessary" );
              // deferred implementation, similar to initial ordering in substituteVoter

            if( candidate.id() == effectiveGroundID ) break; // effective end of path

            node = candidate; // next on path
        }
    }



    /** Casts to reflect a vote change when this node has no present vote.
      */
    private void rootwardInThis_b_presentUncast( final VotingID _votedID, final Precounter precounter )
    {
        assert rootwardInThis.votedID() == null;
        assert _votedID != null: NON_REDUNDANCY_ASSERTION;

      // (b1) Detect any future, barred cycle that would involve this node.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final PrecountNode _candidate;
        try( final CycleForeseer cF = precounter.cycleForeseer() )
        {
            cF.foresee( this, _votedID, precounter );
            if( cF.barNode() != null )
            {
                rootwardInThis_d_presentUncast_futureCycle( _votedID, cF );
                return;
            }

            _candidate = cF.candidate();
        }

      // (b2) Cast the new vote.
      // - - - - - - - - - - - - -
        rootwardInThis = _candidate.rootwardHither_getOrMake();

      // (b3) Flow the new vote.
      // - - - - - - - - - - - - -
        final PrecountGround ground = precounter.ground().precounted(); // not null, ctor of this ensures
        ground.removeVoter( this );   //
        _candidate.addVoter( this ); // a complete hand off
    }



    /** Recasts to reflect a vote change when this node is already in a barred cycle.
      *
      *     @param barNode The node with the barred cast, which may be this node.
      */
    private void rootwardInThis_c_presentCycle( final VotingID _votedID, final PrecountNode1 barNode,
      final Precounter precounter )
    {
      // (c1) Uncast the old vote of this node, breaking the cycle.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final PrecountNode candidate = rootwardInThis.candidate();
        final PrecountGround ground = precounter.ground().precounted(); // not null, ctor of this ensures
        rootwardInThis = ground.rootwardHither_getOrMake();
        if( candidate != ground )
        {
          // (c2) Unflow the old vote.
          // - - - - - - - - - - - - - -
            candidate.removeVoter( this ); //
            ground.addVoter( this );      // a complete hand off

          // (c3) Recast the vote of the barred node, unbarring it.
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            final PrecountNode barCandidate = getOrMake( barNode.rootwardInThis.votedID(), precounter );
            barNode.rootwardInThis = barCandidate.rootwardHither_getOrMake();

          // (c4) Reflow the vote of the newly unbarred node.
          // - - - - - - - - - - - - - - - - - - - - - - - - -
            ground.removeVoter( barNode );     //
            barCandidate.addVoter( barNode ); // a complete hand off
        }
        else assert barNode == this; // so (c2) no flow change, and (c3-4) will instead recast below

      // (c5) Detect any future, barred cycle that would involve this node.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final PrecountNode _candidate;
        try( final CycleForeseer cF = precounter.cycleForeseer() )
        {
            cF.foresee( this, _votedID, precounter );
            if( cF.barNode() != null )
            {
                rootwardInThis_d_presentUncast_futureCycle( _votedID, cF );
                return;
            }

            _candidate = cF.candidate();
        }

      // (c6) Recast the new vote of this node.
      // - - - - - - - - - - - - - - - - - - - -
        rootwardInThis = _candidate.rootwardHither_getOrMake();

      // (c7) Reflow the new vote.
      // - - - - - - - - - - - - - -
        ground.removeVoter( this );   //
        _candidate.addVoter( this ); // a complete hand off
    }



    /** Casts to reflect a vote change when this node has no present vote, but will enter a barred
      * cycle on casting.
      *
      *     @param cF A cycle foreseer that already records the properties of the cycle.
      */
    private void rootwardInThis_d_presentUncast_futureCycle( final VotingID _votedID, final CycleForeseer cF )
    {
        final PrecountGround ground = (PrecountGround)rootwardInThis.candidate();

      // (d1) Uncast the old vote of the future barred node, and bar it.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final PrecountNode1 _barNode = cF.barNode();
        if( _barNode == this ) // i.e. _votedID is self
        {
            rootwardInThis = new RootwardCastB<PrecountNode>( ground, _votedID );
            return; // no d2, nothing to unflow, already uncast; no d3-4, nothing to recast, now barred
        }

        final PrecountNode _barCandidate = _barNode.rootwardInThis.candidate();
        _barNode.rootwardInThis = new RootwardCastB<PrecountNode>( ground, _barCandidate.id() );

      // (d2) Unflow the old vote.
      // - - - - - - - - - - - - - -
        _barCandidate.removeVoter( _barNode ); //
        ground.addVoter( _barNode );          // a complete hand off

      // (d3) Cast the new vote of this node.
      // - - - - - - - - - - - - - - - - - - -
        final PrecountNode _candidate = cF.candidate();
        rootwardInThis = _candidate.rootwardHither_getOrMake();

      // (d4) Flow the new vote.
      // - - - - - - - - - - - - -
        ground.removeVoter( this );   //
        _candidate.addVoter( this ); // a complete hand off
    }



    private static final int VOTER_OUTFLOW = 0; // not yet coded


///////

    static { stators.seal(); }

}
