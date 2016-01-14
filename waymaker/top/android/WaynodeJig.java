package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.


  @SuppressWarnings("overrides")/* overrides equals, but not hashCode*/
final class WaynodeJig implements Waynode
{


    void clear()
    {
        answer = EMPTY_WAYNODE.answer();
        handle = EMPTY_WAYNODE.handle();
        question = EMPTY_WAYNODE.question();
    }



   // - O b j e c t ------------------------------------------------------------------------------------


    public @Override boolean equals( final Object other ) { return Waynode1.equals( this, other ); }



   // - W a y n o d e ----------------------------------------------------------------------------------


    public String answer() { return answer; }


        String answer = EMPTY_WAYNODE.answer();



    public String handle() { return handle; }


        String handle = EMPTY_WAYNODE.handle();



    public String question() { return question; }


        String question = EMPTY_WAYNODE.question();


}
