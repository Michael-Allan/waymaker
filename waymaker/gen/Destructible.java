package waymaker.gen; // Copyright © 2016 Michael Allan.  Licence MIT.


/** A closeable that unregisters a callback, detaches from an external resource. or otherwise unwinds a
  * construction in whole or part.
  */
public interface Destructible extends AutoCloseable
{


   // - A u t o - C l o s e a b l e --------------------------------------------------------------------


    public void close(); // redefined to throw no checked exception


}
