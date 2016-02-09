package waymaker.gen; // Copyright 2006, 2012, Michael Allan.  Licence MIT-Waymaker.


/** A universal container with a read accessor.
  *
  *     @param <C> The type of content held.
  */
public interface Holder<C>
{

    // Differs from alternatives such as javax.xml.ws.Holder or single element arrays in not necessarily
    // exposing the content to be overwritten,


   // - H o l d e r ------------------------------------------------------------------------------------


    /** The content held, or null if there is none.
      */
    public C get();


}
