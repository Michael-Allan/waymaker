package waymaker.gen; // Copyright © 2015 Michael Allan.  Licence MIT.


/** A signal sent from a source bell.
  *
  *     @param <D> The type of ding emitted by the source bell.
  */
public interface Ding<D extends Ding<D>>
{


   // - D i n g ----------------------------------------------------------------------------------------


    /** The original bell that emitted this ding.
      */
    public Bell<D> source();


}
