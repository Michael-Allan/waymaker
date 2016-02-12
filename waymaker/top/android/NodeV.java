package waymaker.top.android; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.content.Context;
import android.view.View;
import android.widget.*;
import waymaker.gen.*;

import static waymaker.top.android.Waynode.EMPTY_WAYNODE;


/** <p>A view of a count node in a {@linkplain ForestV forest view}.  Though situated in a count-based
  * forest, its main subviews are modeled not on properties of the count node itself, but rather its
  * associated waynode:</p>
  * <pre>
  *      handle
  *       /
  *      /
  *    lo  Lorem ipsum dolor sit amet
  *                /
  *               /
  *            answer
  * </pre>
  */
public @ThreadRestricted("app main") final class NodeV extends LinearLayout
{
    /* * *
    - wayscope to somehow eclipse the answer subview when zoomed in
        - leaving handle alone visible
      */


    /** Constructs a NodeV with a null node.  Set a node before using it.
      */
    public @Warning("forestV.wr co-construct") NodeV( final ForestV forestV ) { this( forestV, null ); }



    /** Constructs a NodeV.
      *
      *     @see #node()
      */
    public @Warning("forestV.wr co-construct") NodeV( final ForestV forestV, final CountNode _node )
    {
        super( forestV.getContext() );
        assert getChildCount() == C_HANDLE;
        addView( new HandleV(forestV) ); // forestV.wr co-construct
        assert getChildCount() == C_ANSWER;
        addView( new TextView(getContext()) );
        node( _node );
    }



   // --------------------------------------------------------------------------------------------------


    /** The node to view, or null if there is none.  Avoid using this view without a node; it may throw
      * an exception or otherwise fail.
      */
    public CountNode node() { return node; }


        private CountNode node;


        /** Sets the node.
          */
        public void node( final CountNode _node )
        {
            if( _node == node ) return;

            node = _node;
            final Waynode waynode;
            if( node == null )
            {
                assert getParent() == null;
                waynode = EMPTY_WAYNODE;
            }
            else waynode = node.waynode();
            handleV().setText( waynode.handle() );
            answerV().setText( waynode.answer() );
            isActor_sync();
        }


//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final OnClickListener actorElector = new OnClickListener()
    {
        public void onClick( final View src )
        {
            final NodeV nodeV = (NodeV)src;
            nodeV.wr().actorID().set( nodeV.node.id() );
        }
    };

        { setOnClickListener( actorElector ); }



    private TextView answerV() { return (TextView)getChildAt( C_ANSWER ); }



    private static final int C_HANDLE = 0; // child index

    private static final int C_ANSWER = 1;



    private HandleV handleV() { return (HandleV)getChildAt( C_HANDLE ); }



    boolean isActor;


        {
         // isActor_sync(); // init
         /// effectively done by node init
            wr().actorID().bell().register( new Auditor<Changed>()
            {
                public void hear( Changed _ding ) { isActor_sync(); }
            }); // no need to unregister from wr co-construct
        }


        private void isActor_sync()
        {
            final boolean _isActor = node != null && node.id().equals(wr().actorID().get());
            if( _isActor == isActor ) return;

            isActor = _isActor;
            handleV().invalidate();
        }



    ForestV parentForestV() { return (ForestV)getParent(); }



    private Wayranging wr() { return (Wayranging)getContext(); }



   // - V i e w ----------------------------------------------------------------------------------------


    protected @Override void onAttachedToWindow() { assert node != null; }


}
