package overware.spec; // Copyright 2015, Michael Allan.  Licence MIT-Overware.

import overware.gen.ThreadSafe;


/** An identifier made of three serial numbers.  Although it has only a single implementation in
  * {@linkplain ID ID}, the interface is separately defined here in order to enable {@linkplain
  * TriSerialUUID useful patterns of subtyping} in future.
  */
@ThreadSafe interface TriSerialID extends java.io.Serializable {}

    // Not also extending Comparable<TriSerialID>; rather letting TriSerialUUID alone define the natural
    // order by extending Comparable<TriSerialUUID>.  Else would have to uphold Comparable's constraint
    // of symmetry in mixed comparisons of scoped UUID and unscoped ID.

    /* * *
    - intentions
        - unbounded in length - an unlimited number identifiers may be generated
        - no encoding required in URLs
    - non-intentions
        - not used as authentication (login) identifier
            - a person will normally authenticate with a less unwieldy, more memorable form of identifier
                | email address | OpenID
            - authenticators must therefore maintain a binding between login and tri-serial ID
    - reserved identifiers
        - domain serial 0 is reserved for network use
      / - standard 0 generators are:
      /     [ 00p-INSTANCE
      /         ( referring to poll (p), not issue, per overware/gen/poll
      /         - reserved for issues
      /             - all issues have an identifier in this form
      /                 ( examples: 00p-0 | 00p-torM
      /         - the instance serial number is largely unregulated in this case
      /             - i.e. collision avoidance is not enforced
      /             - only the following instance serial is predefined:
      /                 [ 1
      /                     - universally collective end
      /                     - predefinition required by top/android issue train
      // not clear how 00p-* form will be useful
      */
