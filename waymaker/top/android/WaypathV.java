package waymaker.top.android; // Copyright 2016, Michael Allan.  Licence MIT-Waymaker.

import android.widget.*;
import waymaker.gen.*;


/** A temporary placeholder for a waypath view.
  */
public @ThreadRestricted("app main") final class WaypathV extends LinearLayout
{
    /* * *
    - narrow view of subject's way, showing only one waypath at a time
    - basis of waypath view
        - models
            - waypath
                - types of origin
                    | temporal
                        - originating as nascent waypath
                    | positional
                        - originating in position document
                - identification
                    ( as per 2015.12.12
                    - using only string form, "name"
            - Wayranging.pollNamer
            - Wayranging.waypathNamer
        - controls
            - waypath chooser, summoner (=)
    - choice of waypath
        | explicitly by chooser
            - waypaths that may be chosen
                ( functional categories, orthogonal to original typology (positional|temporal)
                | nascent (0..1)
                    - incomplete, missing act
                    - type of origin: temporal
                | adopted (0..*)
                    - determined by action vote
                        - leafmost positional waypath on action vote path, including self
                    - type of origin: positional
                | stowed (0..*)
                    - waypath (nascent or adopted) that was replaced implicitly by action vote
                    - removed from storage either automatically when it matches an adopted waypath,
                      or manually by user
        | implicitly by link travel
            ( forest travel cannot change the waypath choice, only introduce a break
            | exploring into an established waypath
                - bringing a nascent waypath to match the endward (leftmost) subpath
                  of just one established waypath chooses that waypath
            | breaking out of an established waypath
                - travel to an off-path waynode chooses a nascent path through to that waynode
        | implicitly by action vote
            - vote (or vote shift) in action poll chooses the adopted waypath
            - previously chosen waypath becomes stowed, if not already stowed, and user is told of this
    - waynodes
        - end [ norm* [ act ]]
            - one end, any number of norms, and one act (established way only)
        - abstract definition as series of polls made concrete (as positions) by way of (in priority order)
            | recent node of forester travel
                - temporarily stored in order to stabilize link backtracking and re-traversal
                - temporary, so not to confuse user by masking the following:
            | subject's vote
            | default position, which is part of the waypath definition
                - in case of nascent waypath, default position is last traversed during exploration
    - parallel subpaths
        ( notebook 2015.11.4-5 (Q1, A1)
        - e.g. for electoral action, diverging endward into multiple laws before converging again
    - breaks
        - caused by forest travel (node choice) that breaks end/means relation
        | in established path
            - between any two waynodes
        | in nascent path
            - at most one intermediate break between the two rightmost waynodes:

                    end ← norm ← norm ··· norm ···     *act*
                                       /
                                      /
                        intermediate break
                           /
                          /
                    end ··· norm ···                    *act*

            - final break (always present) to waynode placeholder *act*
                - so here is default nascent waypath for virgin user

                    end ···                             *act*

    - truncation of nascent waypath
        ( leaving aside placeholder *act*, which is not a proper waynode
        - right side is truncated to the waynode choice
            - moving leftward therefore retracts the waypath
            - except when waypath is broken
                - then either of the two right waynodes may be chosen
                - link traversal rightward from either replaces the rightmost waynode
      */


    /** Constructs a WaypathV.
      */
    public @Warning("wr co-construct") WaypathV( final Wayranging wr )
    {
        super( /*context*/wr );

      // Poll indicator. (in temporary stopgap form)
      // - - - - - - - - -
        {
            final TextView view = new TextView( wr );
            addView( view );
            wr.pollNamer().bell().register( new Auditor<Changed>()
            {
                { sync(); } // init
                private void sync() { view.setText( wr.pollNamer().get() ); }
                public void hear( Changed _ding ) { sync(); }
            }); // no need to unregister from wr co-construct
        }
    }


}
