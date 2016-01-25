package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.os.Parcel;
import waymaker.gen.*;

import static waymaker.top.android.Waynode.EMPTY_WAYNODE;


/** An implementation of a waynode.
  */
  @SuppressWarnings("overrides")/* overrides equals, but not hashCode*/
public @ThreadSafe final class Waynode1 implements Waynode
{

    static final PolyStator<Waynode1> stators = new PolyStator<>();

///////


    /** Constructs a Waynode1.
      *
      *     @see #handle()
      *     @see #answer()
      *     @see #question()
      *     @throws NullPointerException if any argument is null.
      */
    public Waynode1( final String _handle, final String _answer, final String _question,
      final String _questionBackImageLoc )
    {
        this( _handle, _answer, _question, _questionBackImageLoc, /*inP*/null );
        if( _handle == null || _answer == null || _question == null ) throw new NullPointerException();
    }



    /** Constructs a Waynode1 by copying another waynode.
      */
    public Waynode1( final Waynode wn )
    {
        this( wn.handle(), wn.answer(), wn.question(), wn.questionBackImageLoc(), /*inP*/null );
    }



    /** Constructs a Waynode1 from stored state.
      *
      *     @param inP The parceled state to restore, or null to restore none, in which case the
      *       openToThread restriction is lifted.
      */
      @ThreadRestricted("KittedPolyStatorSR.openToThread") // for stators.restore
    private Waynode1( final String _handle, final String _answer, final String _question,
      final String _questionBackImageLoc, final Parcel inP/*grep CtorRestore*/ )
    {
        final boolean toInitClass;
        if( wasConstructorCalled ) toInitClass = false;
        else
        {
            toInitClass = true;
            wasConstructorCalled = true;
        }
        if( inP != null ) stators.restore( this, inP ); // saved by stators in static inits further below
          // at coding time this had no effect, the only stator being the StateSaver added just below

      // Final fields.
      // - - - - - - - -
        if( toInitClass ) stators.add( new StateSaver<Waynode1>()
        {
            public void save( final Waynode1 wn, final Parcel out )
            {
                out.writeString( wn.handle() );
                ParcelX.writeString( wn.answer(), out, DEFAULT_ANSWER );
                ParcelX.writeString( wn.question(), out, DEFAULT_QUESTION );
                out.writeString( wn.questionBackImageLoc() );
            }
        });
        if( inP == null )
        {
            handle = _handle;
            answer = _answer;
            question = _question;
            questionBackImageLoc = _questionBackImageLoc;
        }
        else
        {
            handle = inP.readString(); // CtorRestore to restore these final fields
            answer = ParcelX.readString( inP, DEFAULT_ANSWER );
            question = ParcelX.readString( inP, DEFAULT_QUESTION );
            questionBackImageLoc = inP.readString();
        }

      // - - -
        if( toInitClass ) stators.seal();
    }



   // --------------------------------------------------------------------------------------------------


    /** Reconstructs a Waynode1, or recreates a reference to the {@linkplain EMPTY_WAYNODE empty
      * waynode}, by reading in from the parcel.
      */
      @ThreadRestricted("KittedPolyStatorSR.openToThread") // for Waynode1(Parcel)
    public static Waynode1 restoreEmptily( final Parcel in )
    {
      // 1.
      // - - -
        if( ParcelX.readBoolean( in )) return EMPTY_WAYNODE;

      // 2.
      // - - -
        return new Waynode1( null, null, null, null, in );
    }



    /** Saves either the state of a Waynode1, or a reference to the {@linkplain EMPTY_WAYNODE empty
      * waynode}, by writing out to the parcel.
      */
      @ThreadRestricted("KittedPolyStatorSR.openToThread") // for stators.save
    public static void saveEmptily( final Waynode1 wn, final Parcel out )
    {
      // 1. Is empty?
      // - - - - - - -
        final boolean isEmpty = wn == EMPTY_WAYNODE; // == for speed, not equals
        ParcelX.writeBoolean( isEmpty, out );

      // 2. Waynode
      // - - - - - -
        if( !isEmpty ) stators.save( wn, out );
    }



   // - O b j e c t ------------------------------------------------------------------------------------


    public @Override boolean equals( final Object other ) { return equals( this, other ); }



   // - W a y n o d e ----------------------------------------------------------------------------------


    public String answer() { return answer; }


        static final String DEFAULT_ANSWER = "This is the answer";


        private final String answer;



    public String handle() { return handle; }


        private final String handle;



    public String question() { return question; }


        static final String DEFAULT_QUESTION = "What is the answer?";


        private final String question;



    public String questionBackImageLoc() { return questionBackImageLoc; }


        private final String questionBackImageLoc;



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    static boolean equals( final Waynode w1, final Object o2 )
    {
        if( w1 == o2 ) return true; // a frequent case in Precounter

        if( !(o2 instanceof Waynode) /*or if null*/ ) return false;

        final Waynode w2 = (Waynode)o2;
        return w1.handle().equals(w2.handle())
            && w1.answer().equals(w2.answer())
            && w1.question().equals(w2.question())
            && w1.questionBackImageLoc().equals(w2.questionBackImageLoc());
    }



    private static volatile boolean wasConstructorCalled;


}
