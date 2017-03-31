package waymaker.top.android; // Copyright © 2016 Michael Allan.  Licence MIT.


  @SuppressWarnings("overrides")/* overrides equals, but not hashCode*/
final class WaynodeJig implements Waynode
{


    WaynodeJig() { clear(); }



   // --------------------------------------------------------------------------------------------------


    void clear()
    {
        answer = EMPTY_WAYNODE.answer();
        handle = EMPTY_WAYNODE.handle();
        question = EMPTY_WAYNODE.question();
        questionBackImageLoc = EMPTY_WAYNODE.questionBackImageLoc();
    }



   // - O b j e c t ------------------------------------------------------------------------------------


    public @Override boolean equals( final Object other ) { return Waynode1.equals( this, other ); }



   // - W a y n o d e ----------------------------------------------------------------------------------


    public String answer() { return answer; }


        String answer;



    public String handle() { return handle; }


        String handle;



    public String question() { return question; }


        String question;



    public String questionBackImageLoc() { return questionBackImageLoc; }


        String questionBackImageLoc;


}
