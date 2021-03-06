package waymaker.top.android; // Copyright © 2015 Michael Allan.  Licence MIT.


/** A partial store of the nodes that contribute to a {@linkplain Forest pollar forest}.  Although it
  * might initially comprise a mere {@linkplain #ground() ground pseudo-node}, it can grow on demand
  * by {@linkplain ServerCount#enqueuePeersRequest(VotingID,PeersReceiver,int) requesting} further nodal
  * data from the count server.
  */
public interface NodeCache
{
    /* * *
    - cache inconsistency due to lazy growth is detected by repocaster serial number (RepocastSer)
        ( notebook 2015.5.27
        - to detect
            - remote count server (re)stamps with current repocaster serial number
                - each (re)counted node
                - overall count (continually updated and restamped)
            - local code stamps new cache with count serial once
                - never updating it
            - then as cache grows by receiving new nodes:
                - the stamp of each received node is tested against the cache stamp
                - if higher, then cache inconsistency (with received node) is detected
        - in response to detection, invalidate cache, which is then replaced
            - thus also replacing precount cache
            - may cause churn
                - re-request at high threshold as UI recovers
                - infrequent enough to be non-problematic (let it churn)
                    - only when UI deeply paged into voters
    - staleness
        - overall cache
            ( some staleness is tolerable
            - time limit (perhaps lengthy) on cache validity
        - user node
            ( no staleness is tolerable
            - serial number of user node is returned for all requests
                - any change (e.g. by parallel modification) invalidates cache
      */


    /** Extra node capacity for newly constructed caches: {@value}.  This addition of space is traded
      * for a reduction of time in rehashes.
      */
    public static final int INITIAL_HEADROOM = 30;



   // - N o d e - C a c h e ----------------------------------------------------------------------------


    /** A pseudo-node that represents a snapshot of the forest floor beneath the roots, whom it models
      * as its ‘{@linkplain CountNode#voters() voters}’.  Their {@linkplain CountNode#rootwardInThis()
      * rootward casts} lead to this ground, which serves formally as the ultimate ‘root’ and
      * ‘candidate’.  Meanwhile the rootward cast of the ground itself is null.  This modeling
      * simplifies the forest structure by reducing it to a single super-tree having the ground
      * as its root, which in turn simplifies the algorithms for navigating that structure.
      */
    public CountNode ground();



    /** The forest’s first root in {@linkplain CountNode#peersComparator peer order}, or the ground if
      * the forest is without roots, or the roots are incomplete.  Any subsequent change to the return
      * value will be signalled by the {@linkplain ForestCache#nodeCacheBell() node cache bell}.  It
      * will change at most once as the roots are completed.
      *
      *     @see CountNode#votersMaybeIncomplete()
      */
    public CountNode leader();


}
