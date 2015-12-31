package waymaker.spec; // Copyright 2015, Michael Allan.  Licence MIT-Waymaker.

import waymaker.gen.ThreadSafe;


/** An identity tag formed of three serial numbers.  Although it has only a single implementation in
  * {@linkplain ID ID}, the interface is separately defined here in order to enable {@linkplain
  * TriSerialUDID useful patterns of subtyping} in future.
  */
public @ThreadSafe interface TriSerialID extends java.io.Serializable {}

    // Not also extending Comparable<TriSerialID>; rather letting TriSerialUDID alone define the natural
    // order by extending Comparable<TriSerialUDID>.  Else would have to uphold Comparable's constraint
    // of symmetry in mixed comparisons of scoped UDID and unscoped ID.

    /* * *
    - intentions
        - unbounded in length - an unlimited number of identities may be tagged
        - no encoding required in URLs
    - non-intentions
        - not used as authentication (login) identification
            - a person will normally authenticate with a less unwieldy, more memorable form of identification
                | email address | OpenID
            - authenticators must therefore maintain a binding between login and tri-serial identification
      */
