package waymaker.top.android; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.widget.TextView;
import waymaker.gen.*;


/** A view of a counting node.
  */
public @ThreadRestricted("app main") final class NodeV extends TextView
{


    /** Constructs a NodeV with a null node.  Set a node before using it.
      */
    public @Warning("wr co-construct") NodeV( final Wayranging wr ) { this( wr, null ); }



    /** Constructs a NodeV.
      *
      *     @see #node()
      */
    public @Warning("wr co-construct") NodeV( final Wayranging wr, final Node _node )
    {
        super( /*context*/wr );
        setPadding( 0, 0, PX_ACTOR_MARKER_WIDTH * 2, 0 ); // LTRB
        node( _node );
    }



    private static final WaykitUI wk = WaykitUI.i(); // early def



   // --------------------------------------------------------------------------------------------------


    /** The node to view, or null if there is none.  Avoid using this view without a node; it may throw
      * an exception or otherwise fail.
      */
    public Node node() { return node; }


        private Node node;


        /** Sets the node.
          */
        public void node( final Node _node )
        {
            if( _node == node ) return;

            node = _node;
            final String text;
            if( node == null )
            {
                assert getParent() == null;
                text = ""; // default (source level 23)
            }
            else text = node.id().toString();
            setText( text );
            isActor_sync();
        }


//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private static final OnClickListener actorElector = new OnClickListener()
    {
        public void onClick( final View src )
        {
            final NodeV nodeV = (NodeV)src;
            nodeV.wr().actorIdentifier().set( nodeV.node.id() );
        }
    };

        { setOnClickListener( actorElector ); }



    private boolean isActor;


        {
         // isActor_sync(); // init
         /// effectively done by node init
            wr().actorIdentifier().bell().register( new Auditor<Changed>()
            {
                public void hear( Changed _ding ) { isActor_sync(); }
            }); // no need to unregister from wr co-construct
        }


        private void isActor_sync()
        {
            final boolean _isActor = node != null && node.id().equals(wr().actorIdentifier().get());
            if( _isActor == isActor ) return;

            isActor = _isActor;
            invalidate();
        }



    private static final int PX_ACTOR_MARKER_WIDTH = Math.max( Math.round(wk.pxSP()), /*at least*/1 );



    private Wayranging wr() { return (Wayranging)getContext(); }



   // - V i e w ----------------------------------------------------------------------------------------


    protected @Override void onAttachedToWindow() { assert node != null; }



    protected @Override void onDraw( final Canvas canvas )
    {
        super.onDraw( canvas );
        if( !isActor ) return;

        final Rect bounds = wk.rect();
        if( !canvas.getClipBounds( bounds )) return;

        bounds.left = bounds.right - PX_ACTOR_MARKER_WIDTH; // shrink to thin right border
        final Paint paint = onDraw_paint;
        paint.setColor( getCurrentTextColor() );
        canvas.drawRect( bounds, paint );
    }


        private static final Paint onDraw_paint = new Paint(); // fill styled, for isolated reuse


}
