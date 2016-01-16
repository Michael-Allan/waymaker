package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;
import waymaker.gen.*;

import static waymaker.top.android.WayscopeZoom.POLL;
import static waymaker.top.android.WayscopeZoom.FORESTER;
import static waymaker.top.android.WayscopeZoom.NODE;


/** A zoom controller for a wayscope.
  */
public @ThreadRestricted("app main") final class WayscopeZoomer
{

    static final PolyStator<WayscopeZoomer> stators = new PolyStator<>();

///////


    /** A bell that rings when the zoom level changes.
      */
    public Bell<Changed> bell() { return bell; }


        private final ReRinger<Changed> bell = Changed.newReRinger();



    /** Increments the zoom level.
      *
      *     @throws IllegalStateException if in-zoom is not enabled.
      */
    public void inZoom()
    {
        if( !inZoomEnabled() ) throw new IllegalStateException();

        if( zoom == POLL ) zoom = FORESTER;
        else
        {
            assert zoom == FORESTER;
            zoom = NODE;
        }
        bell.ring();
    }



    /** Returns true if an in-zoom is possible, false if the zoom is already maximized.
      */
    public boolean inZoomEnabled() { return zoom != NODE; }



    /** Decrements the zoom level.
      *
      *     @throws IllegalStateException if out-zoom is not enabled.
      */
    public void outZoom()
    {
        if( !outZoomEnabled() ) throw new IllegalStateException();

        if( zoom == FORESTER ) zoom = POLL;
        else
        {
            assert zoom == NODE;
            zoom = FORESTER;
        }
        bell.ring();
    }



    /** Returns true if an out-zoom is possible, false if the zoom is already minimized.
      */
    public boolean outZoomEnabled() { return zoom != POLL; }



    /** The level of this zoomer.  Any change in the return value will be signalled
      * by the {@linkplain #bell() bell}.
      */
    public WayscopeZoom zoom() { return zoom; }


        private WayscopeZoom zoom = FORESTER;


        static { stators.add( new Stator<WayscopeZoomer>()
        {
            public void save( final WayscopeZoomer z, final Parcel out ) { out.writeString( z.zoom.name() ); }
            public void restore( final WayscopeZoomer z, final Parcel in )
            {
                z.zoom = WayscopeZoom.valueOf( in.readString() );
            }
        });}


///////

    static { stators.seal(); }

}
