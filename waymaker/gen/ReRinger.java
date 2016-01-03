package waymaker.gen; // Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.

import java.util.Set;
import waymaker.gen.CopyOnResizeArraySet;

import static waymaker.gen.Auditor.EMPTY_AUDITOR_ARRAY;


/** A bell that re-emits the same ding.
  *
  *     @param <D> The type of ding emitted.
  */
public final class ReRinger<D extends Ding<D>> implements Bell<D>
{


    /** Constructs a ReRinger.
      *
      *     @see #ding()
      */
    public @ThreadSafe ReRinger( D _ding ) { ding = _ding; }



   // --------------------------------------------------------------------------------------------------


    /** The ding that is emitted on each ring of the bell.
      */
    public @ThreadSafe D ding() { return ding; }


        private final D ding;



    /** Emits a ding to all registered auditors.
      */
    public void ring() { for( Auditor<D> auditor: register ) auditor.hear( ding ); }



   // - B e l l ----------------------------------------------------------------------------------------


    public void register( final Auditor<D> auditor ) { register.add( auditor ); }



    public void registerDestructibly( final Auditor<D> auditor, final Destructor destructor )
    {
        register.add( auditor );
        destructor.add( new Destructible() { public void close() { unregister( auditor ); } });
    }



    public void unregister( final Auditor<D> auditor ) { register.remove( auditor ); }



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    private final Set<Auditor<D>> register = new CopyOnResizeArraySet<Auditor<D>>()
    {
        public @SuppressWarnings("unchecked") Auditor<D>[] emptyArray() { return EMPTY_AUDITOR_ARRAY; }
        public @SuppressWarnings({"rawtypes","unchecked"}) Auditor<D>[] newArray( final int length )
        {
            return new Auditor[length];
        }
    };


}
