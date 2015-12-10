package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import android.widget.RelativeLayout;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/** <p>A device to hold the parameters of a relative layout during their formation.  It serves for
  * convenience and to improve the clarity of code.  For example:</p>
  * <pre>
  *     RelativeLayout.LayoutParams p =
  *       new RelativeLayout.LayoutParams( WRAP_CONTENT, WRAP_CONTENT );
  *     p.addRule( ALIGN_PARENT_RIGHT );
  *     p.addRule( ALIGN_PARENT_BOTTOM );
  *     addView( view, p );</pre>
  *
  * <p>The same layout may be expressed with less clutter by using a jig:</p>
  * <pre>
  *    addView( view, jigRelative().rule(ALIGN_PARENT_BOTTOM)
  *      .rule(ALIGN_PARENT_RIGHT).unjig() );</pre>
  */
public final class RelativeLayoutJig
{

    private RelativeLayoutJig() {}



    /** The single instance of RelativeLayoutJig.
      */
    public static RelativeLayoutJig i() { return instance; }


        private static final RelativeLayoutJig instance = new RelativeLayoutJig();



    /** Constructs a set of layout parameters with default dimensions, lays them in the {@linkplain #i()
      * single instance} of the jig, and returns the jig.  Specifies both the width and height as <a
      * href='http://developer.android.com/reference/android/view/ViewGroup.LayoutParams.html#WRAP_CONTENT'
      * target='_top'>WRAP_CONTENT</a>.
      */
    public static RelativeLayoutJig jigRelative()
    {
        instance.params = new RelativeLayout.LayoutParams( WRAP_CONTENT, WRAP_CONTENT );
        return instance;
    }



    /** Adds a rule to the parameters held in this jig, and returns the jig.
      *
      *     @see <a href='http://developer.android.com/reference/android/widget/RelativeLayout.LayoutParams.html#addRule(int)'
      *       target='_top'>addRule(int)</a>
      *     @throws NullPointerException if this jig {@linkplain #jigRelative() holds no parameters}.
      */
    public RelativeLayoutJig rule( int verb )
    {
        params.addRule( verb );
        return this;
    }



    /** Removes and returns the layout parameters from this jig.
      */
    public RelativeLayout.LayoutParams unjig()
    {
        final RelativeLayout.LayoutParams p = params;
        params = null; // release to garbage collector
        return p;
    }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private RelativeLayout.LayoutParams params;


}
