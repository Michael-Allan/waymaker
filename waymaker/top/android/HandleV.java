package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.graphics.*;
import android.widget.TextView;
import waymaker.gen.ThreadRestricted;


@ThreadRestricted("app main") final class HandleV extends TextView // of waynode handle in count node
{


    HandleV( final ForestV forestV )
    {
        super( forestV.getContext() );
        setPadding( 0, 0, forestV.pxActorMarkerWidth * 2, 0 ); // LTRB
    }



    private static final WaykitUI wk = WaykitUI.i(); // early def



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private NodeV parentNodeV() { return (NodeV)getParent(); }



   // - V i e w ----------------------------------------------------------------------------------------


    protected @Override void onDraw( final Canvas canvas )
    {
        super.onDraw( canvas );
        final NodeV nodeV = parentNodeV();
        if( !nodeV.isActor ) return;

        final Rect bounds = wk.rect();
        if( !canvas.getClipBounds( bounds )) return;

        bounds.left = bounds.right - nodeV.parentForestV().pxActorMarkerWidth; // shrink to thin right border
        final Paint paint = onDraw_paint;
        paint.setColor( getCurrentTextColor() );
        canvas.drawRect( bounds, paint );
    }


        private static final Paint onDraw_paint = new Paint(); // fill styled, for isolated reuse


}
