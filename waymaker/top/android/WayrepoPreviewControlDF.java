package waymaker.top.android; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import android.app.Dialog;
import android.os.Bundle;
import android.view.*;
import waymaker.gen.*;


/** A one-shot dialogue to show a {@linkplain WayrepoPreviewController wayrepo preview controller}.
  */
  @ThreadRestricted("app main")
public final class WayrepoPreviewControlDF extends android.app.DialogFragment // grep AutoRestore-public
{


   // - F r a g m e n t --------------------------------------------------------------------------------


    public @Override Dialog onCreateDialog( final Bundle in )
    {
        final Dialog dialog = super.onCreateDialog( in );
        dialog.setTitle( WayrepoPreviewController.TITLE );
        return dialog;
    }



    public @Override View onCreateView( LayoutInflater _inf, ViewGroup _group, Bundle _in )
    {
        return new WayrepoPreviewController( (Wayranging)getActivity(), destructor );
    }



    public @Override void onDestroyView()
    {
        destructor.close();
        super.onDestroyView();
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final Destructor destructor = new Destructor1();


}
