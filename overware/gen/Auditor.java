package overware.gen; // Copyright 2015, Michael Allan.  Licence MIT-Overware.


/** A receiver of dings.
  */
public interface Auditor<D extends Ding<D>>
{


    /** Receives a ding.
      */
    public void hear( D ding );


}
