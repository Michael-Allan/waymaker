package waymaker.spec; // Copyright © 2015 Michael Allan.  Licence MIT.


/** A universally decisive, tri-serial identity tag.  Although it has only a single implementation in
  * {@linkplain UDID UDID}, the interface is separately defined here in order to enable useful patterns
  * of subtyping, such as the diamond pattern shown here.<pre>
  *
  *             TriSerialID
  *                  |
  *                  |
  *            TriSerialUDID
  *                /   \
  *               /     \
  *              /       \
  *         VotingID  ImpersonalID
  *          /   \      /   \
  *         /     \    /     \
  *        /       \  /       \
  *   PersonID    PipeID    AnotherID</pre>
  *
  * Here a VotingID or ImpersonalID <em>as such</em> is also a TriSerialUDID, complete with all its
  * methods.  This is possible only because of the separate interface for TriSerialUDID.
  */
public interface TriSerialUDID extends Comparable<TriSerialUDID>, TriSerialID
{


   // - T r i - S e r i a l - U D I D ------------------------------------------------------------------


    /** The nominal scope of decision for this identity tag.  Explicitly incorporating the scope is what
      * makes this tag universally decisive across all scopes.  If i.scope and j.scope are unequal for
      * identity tags i and j, then i and j too are unequal <em>regardless of their serial numbers</em>;
      * <code>i.{@linkplain ID#equalsNumerically(ID) equalsNumerically}(j)</code> may be true, but
      * <code>i.equals(j)</code> will be false.
      */
    public String scope();


}
