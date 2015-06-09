package overware.gen; // Copyright 2015, Michael Allan.  Licence MIT-Overware.


/** A source of dings and a registry for auditors.
  */
public interface Bell<D extends Ding<D>>
{


   // - B e l l --------------------------------------------------------------------------


    /** Ensures the auditor is registered to receive future dings from this bell.
      */
    public void register( Auditor<D> auditor );



    /** Removes the auditor from this bell’s register, if registered.
      */
    public void unregister( Auditor<D> auditor );


}
