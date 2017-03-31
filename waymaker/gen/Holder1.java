package waymaker.gen; // Copyright © 2006 Michael Allan.  Licence MIT.


/** An implementation of a holder.
  *
  *     @param <C> The type of content held.
  */
public final class Holder1<C> implements Holder<C>
{


    /** Creates a Holder1.
      */
    public @ThreadSafe Holder1() {}



    /** Creates a Holder1 and sets the initial content.
      */
    public Holder1( final C content ) { this.content = content; }



   // --------------------------------------------------------------------------------------------------


    /** Sets the content held.
      */
    public void set( final C content ) { this.content = content; }



   // - H o l d e r ------------------------------------------------------------------------------------


    public C get() { return content; }


        private C content;


}
