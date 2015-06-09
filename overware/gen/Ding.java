package overware.gen;


/** A signal sent through a bell.
  */
public interface Ding<D extends Ding<D>>
{


   // - D i n g --------------------------------------------------------------------------


    /** The original bell that emitted this ding.
      */
    public Bell<D> source();


}
