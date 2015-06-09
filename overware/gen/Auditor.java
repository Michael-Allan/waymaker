package overware.gen;


/** A receiver of dings.
  */
public interface Auditor<D extends Ding<D>>
{


    /** Receives a ding.
      */
    public void hear( D ding );


}
