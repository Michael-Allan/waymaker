package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.


/** A thing that may be refreshed.
  */
public interface Refreshable
{


   // - R e f r e s h a b l e --------------------------------------------------------------------------


    /** Widely refreshes from all sources in case any have changed.
      */
    public void refreshFromAllSources();



    /** Narrowly refreshes from the user’s local wayrepo in case it was modified.
      */
    public void refreshFromLocalWayrepo();


}
