package waymaker.gen; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.


/** A composite destructible.  Unlike destructibles in general, it is guaranteed idempotent; multiple
  * calls to {@linkplain #close() close} have the same effect as one call.
  */
public interface Destructor extends Destructible
{


    /** Adds the given destructible to this destructor as a component, or closes it immediately if this
      * destructor is already closing.
      *
      *     @return True if the destructible was added, false if it was immediately closed instead.
      */
    public boolean add( Destructible d );



    /** Answers whether this destructor has commenced closing.
      */
    public boolean isClosing();



   // - A u t o - C l o s e a b l e --------------------------------------------------------------------


    /** Closes all component destructibles, or does nothing if this destructor is already closing.
      */
    public void close();


}
