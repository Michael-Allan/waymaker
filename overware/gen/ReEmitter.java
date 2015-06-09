package overware.gen; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import java.util.Set;
import overware.gen.CopyOnResizeArraySet;


/** A bell that always emits the same ding.
  */
public final class ReEmitter<D extends Ding<D>> implements Bell<D>
{


    /** Creates a ReEmitter.
      *
      *     @see #ding()
      */
    public ReEmitter( D _ding ) { ding = _ding; }



   // ------------------------------------------------------------------------------------


    /** The ding that is emitted.
      */
    public D ding() { return ding; }


        private final D ding;



    /** Emits a ding to all registered auditors.
      */
    public void emit() { for( Auditor<D> auditor: register ) auditor.hear( ding ); }



   // - B e l l --------------------------------------------------------------------------


    public void register( final Auditor<D> auditor ) { register.add( auditor ); }



    public void unregister( final Auditor<D> auditor ) { register.remove( auditor ); }



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    private final Set<Auditor<D>> register = new CopyOnResizeArraySet<Auditor<D>>();


}
