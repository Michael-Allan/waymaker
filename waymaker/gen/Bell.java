package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.


/** A source of dings and a registry for auditors.
  *
  *     @param <D> The type of ding.
  */
public interface Bell<D extends Ding<D>>
{


   // - B e l l ----------------------------------------------------------------------------------------


    /** Adds the auditor to this bell’s register, ensuring it will hear future dings from this bell.
      *
      *     @throws IllegalArgumentException if the auditor is already registered.
      */
    public void register( Auditor<D> auditor );



    /** Registers the auditor with this bell and ensures it will be unregistered on destruction.  This
      * is a convenience method that creates a separate destructible for the unregistration and adds it
      * to the given destructor.
      *
      *     @throws IllegalArgumentException if the auditor is already registered.
      */
    public void registerDestructibly( Auditor<D> auditor, Destructor destructor );



    /** Removes the auditor from this bell’s register, if it is registered.
      */
    public void unregister( Auditor<D> auditor );


}
