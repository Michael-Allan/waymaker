package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.content.Context;


/** A view of a counting node.
  */
public @waymaker.gen.ThreadRestricted("app main") final class NodeV extends android.widget.TextView
{


    /** Constructs a NodeV.
      */
    public NodeV( final Context context ) { this( context, null ); }



    /** Constructs a NodeV.
      *
      *     @see #node()
      */
    public NodeV( final Context context, final Node _node )
    {
        super( context );
        node( _node );
    }



   // --------------------------------------------------------------------------------------------------


    /** The counting node to view.
      */
    public Node node() { return node; }


        private Node node;


        /** Sets the counting node to view.
          */
        public void node( final Node _node )
        {
            node = _node;
            setText( node == null? "∅": node.id().toString() );
        }


}
