package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.content.Context;
import android.graphics.*;
import android.widget.TextView;
import waymaker.gen.ThreadRestricted;


@ThreadRestricted("app main") final class HandleV extends TextView // of waynode handle in count node
{


    HandleV( final Context context )
    {
        super( context );
        setPadding( 0, 0, PX_ACTOR_MARKER_WIDTH * 2, 0 ); // LTRB
    }



    private static final WaykitUI wk = WaykitUI.i(); // early def



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private NodeV parentNodeV() { return (NodeV)getParent(); }



    private static final int PX_ACTOR_MARKER_WIDTH = Math.max( Math.round(wk.pxSP()), /*at least*/1 );



   // - V i e w ----------------------------------------------------------------------------------------


    protected @Override void onDraw( final Canvas canvas )
    {
        super.onDraw( canvas );
        final NodeV nodeV = parentNodeV();
        if( !nodeV.isActor ) return;

        final Rect bounds = wk.rect();
        if( !canvas.getClipBounds( bounds )) return;

        bounds.left = bounds.right - PX_ACTOR_MARKER_WIDTH; // shrink to thin right border
        final Paint paint = onDraw_paint;
        paint.setColor( getCurrentTextColor() );
        canvas.drawRect( bounds, paint );
    }


        private static final Paint onDraw_paint = new Paint(); // fill styled, for isolated reuse


}
