package overware.spec; // Copyright 2015, Michael Allan.  Licence MIT-Overware.


/** A universally unique identifier.  Although it has only a single implementation in {@linkplain UUID
  * UUID}, the interface is separately defined here in order to enable useful patterns of subtyping,
  * such as the diamond pattern shown here.<pre>
  *
  *             TriSerialID
  *                  |
  *                  |
  *            TriSerialUUID
  *                /   \
  *               /     \
  *              /       \
  *         VotingID  ImpersonalID
  *          /   \      /   \
  *         /     \    /     \
  *        /       \  /       \
  *   PersonID    PipeID    AnotherID</pre>
  *
  * Here a VotingID or ImpersonalID <em>as such</em> is also a TriSerialUUID, complete with all its
  * methods.  This is possible only because of the separate interface for TriSerialUUID.
  */
public interface TriSerialUUID extends Comparable<TriSerialUUID>, TriSerialID
{


   // - T r i - S e r i a l - U U I D ------------------------------------------------------------------


    /** The nominal scope of uniqueness for this identifier.  If i.scope and j.scope are unequal for
      * identifiers i and j, then i and j too are unequal <em>regardless of their serial numbers</em>;
      * <code>i.{@linkplain ID#equalsNumerically(ID) equalsNumerically}(j)</code> may be true, but
      * <code>i.equals(j)</code> will be false.
      */
    public String scope();


}
