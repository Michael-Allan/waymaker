package waymaker.gen; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import java.util.ArrayList;


/** An implementation of a destructor.
  */
public final class Destructor1 implements Destructor
{


    /** Creates a Destructor1.
      */
    public @ThreadSafe Destructor1() {}



   // --------------------------------------------------------------------------------------------------


    public boolean add( final Destructible d )
    {
        if( isClosing )
        {
            d.close();
            return false;
        }

        destructibles.add( d );
        return true;
    }



    public boolean isClosing() { return isClosing; }


        private boolean isClosing;



   // - A u t o - C l o s e a b l e --------------------------------------------------------------------


    public void close()
    {
        if( isClosing ) return;

        isClosing = true;
        for( Destructible d: destructibles ) d.close();
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final ArrayList<Destructible> destructibles = new ArrayList<>();


}
