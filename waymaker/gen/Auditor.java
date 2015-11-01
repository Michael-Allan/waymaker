package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.


/** A receiver of dings.
  *
  *     @param <D> The type of ding received.
  */
public interface Auditor<D extends Ding<D>>
{


    /** A common instance of an auditor array of length zero.
      */
    public static final @SuppressWarnings("rawtypes") Auditor[] EMPTY_AUDITOR_ARRAY = new Auditor[0];



   // - A u d i t o r ----------------------------------------------------------------------------------


    /** Receives a ding.
      */
    public void hear( D ding );


}
