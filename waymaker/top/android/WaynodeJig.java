package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.


  @SuppressWarnings("overrides")/* overrides equals, but not hashCode*/
final class WaynodeJig implements Waynode
{


    void clear()
    {
        handle = EMPTY_WAYNODE.handle();
        summary = EMPTY_WAYNODE.summary();
    }



   // - O b j e c t ------------------------------------------------------------------------------------


    public @Override boolean equals( final Object other ) { return Waynode1.equals( this, other ); }



   // - W a y n o d e ----------------------------------------------------------------------------------


    public String handle() { return handle; }


        String handle = EMPTY_WAYNODE.handle();



    public String summary() { return summary; }


        String summary = EMPTY_WAYNODE.summary();


}
