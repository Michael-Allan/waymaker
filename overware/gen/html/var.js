/**                                                                             -*- coding:utf-8; -*-
  * HTML variables.  Rough notes follow:
  *
  *   - variables
  *       - formed as var elements
  *           - refs may also be sup elements (to be understood in what follows)
  *       - named by var content
  *       - namespaced by var#class preceding class def|ref
  *       - e.g. <var class='step def'>x</var> is a variable named step.x, where 'step' is namespace
  *   - will re-order (on request) var.def elements of a specified namespace
  *       - to match order of corresponding var.ref
  *       - reordering includes contextual dl structure (of endnotes)
  *   - will numerically rename (on request) var.def elements of a specified namespace
  *       - and duplicate the change in each var.ref
  *       - each namespace getting its own number series for this purpose
  *   - will insert previews (on request) of each sup.ref
  *       - actually hover or (for sake of touch screens) click
  */
