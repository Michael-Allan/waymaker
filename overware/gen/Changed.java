package overware.gen;


/** A ding that signifies a change.
  */
public final class Changed implements Ding<Changed>
{


    private Changed() { source = new ReEmitter<Changed>( this ); }



   // ------------------------------------------------------------------------------------


    /** Creates a ReEmitter for signifying changes.
      */
    public static ReEmitter<Changed> newReEmitter()
    {
        return (ReEmitter<Changed>)new Changed().source;
    }



   // - D i n g --------------------------------------------------------------------------


    public Bell<Changed> source() { return source; }


        private final Bell<Changed> source;


}
