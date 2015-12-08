package waymaker.top.android; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.os.Bundle;
import android.view.*;
import waymaker.gen.ThreadRestricted;


/** A dialogue for the {@linkplain WayrepoPreviewController control of wayrepo previews}.
  */
  @ThreadRestricted("app main")
public final class WayrepoPreviewControlDF extends android.app.DialogFragment // grep AutoRestore-public
{


   // - F r a g m e n t --------------------------------------------------------------------------------


    public @Override View onCreateView( LayoutInflater _inf, ViewGroup _group, Bundle _in )
    {
        return new WayrepoPreviewController( (Wayranging)getActivity() );
    }


}
