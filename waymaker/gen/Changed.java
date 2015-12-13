package waymaker.gen; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.


/** A ding that signifies a change.
  */
public @ThreadSafe final class Changed implements Ding<Changed>
{


    private Changed() { source = new ReRinger<Changed>( this ); }



   // --------------------------------------------------------------------------------------------------


    /** Constructs a Changed and returns a re-ringer that emits it.
      */
    public static ReRinger<Changed> newReRinger() { return (ReRinger<Changed>)new Changed().source; }



   // - D i n g ----------------------------------------------------------------------------------------


    public Bell<Changed> source() { return source; }


        private final Bell<Changed> source;


}
