package overware.gen; // Copyright 2015, Michael Allan.  Licence MIT-Overware.


/** A ding that signifies a change.
  */
public @ThreadSafe final class Changed implements Ding<Changed>
{


    private Changed() { source = new ReRinger<Changed>( this ); }



   // --------------------------------------------------------------------------------------------------


    /** Constructs a ReRinger for signifying changes.
      */
    public static ReRinger<Changed> newReRinger()
    {
        return (ReRinger<Changed>)new Changed().source;
    }



   // - D i n g ----------------------------------------------------------------------------------------


    public Bell<Changed> source() { return source; }


        private final Bell<Changed> source;


}
