package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.


/** A source of dings and a registry for auditors.
  *
  *     @param <D> The type of ding.
  */
public interface Bell<D extends Ding<D>>
{


   // - B e l l ----------------------------------------------------------------------------------------


    /** Ensures the auditor is entered in this bell’s register so that it will receive
      * future dings from this bell.
      */
    public void register( Auditor<D> auditor );



    /** Removes the auditor from this bell’s register, if registered.
      */
    public void unregister( Auditor<D> auditor );


}
