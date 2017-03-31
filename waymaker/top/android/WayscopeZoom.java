package waymaker.top.android; // Copyright © 2016 Michael Allan.  Licence MIT.


/** Wayscope zoom levels ordered from least to most.
  */
public enum WayscopeZoom
{

    /** The whole poll.
      */
    POLL,


    /** Multiple waynodes near to the forester’s position.
      */
    FORESTER,


    /** One waynode in whole or part.
      */
    NODE; // part zooms (into wayscript element) to be modeled separately, e.g. as "sublevel"

}
