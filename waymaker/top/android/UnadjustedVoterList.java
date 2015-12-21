package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import waymaker.gen.*;


/** A voter list for an unadjusted node.
  */
public final class UnadjustedVoterList extends CopyOnResizeArrayList<UnadjustedNode1>
{


    public @ThreadSafe UnadjustedVoterList() {}



   // --------------------------------------------------------------------------------------------------


    public UnadjustedNode1[] emptyArray() { return EMPTY_ARRAY; }


        private static final UnadjustedNode1[] EMPTY_ARRAY = new UnadjustedNode1[0];



    public UnadjustedNode1[] newArray( final int length ) { return new UnadjustedNode1[length]; }



   // - L i s t ----------------------------------------------------------------------------------------


    public @Override/*for speed*/ void add( final int e, final UnadjustedNode1 element )
    {
        throw new UnsupportedOperationException( "Rather use bulk methods addAll or array." );
    }


}
