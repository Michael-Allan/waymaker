/** Target.js - Definition of the 'whole' build target
  *************
  * Builds all targets required for a release of the Waymaker software.  This is the default target.
  * These commands have the same effect:
  *
  *     $ waymaker/build
  *     $ waymaker/build -- whole
  #
  */
if( !waymaker.spec.build.whole.Target ) {
     waymaker.spec.build.whole.Target = {};
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/android/Target.js' ));
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/javadoc/Target.js' ));
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/source/Target.js' ));
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/Build.js' ));
( function()
{
    var our = waymaker.spec.build.whole.Target; // public as waymaker.spec.build.whole.Target

    var Build = waymaker.spec.build.Build;



//// P u b l i c ///////////////////////////////////////////////////////////////////////////////////////


    /** Builds this target.
      */
    our.build = function()
    {
        Build.indentAndBuild( 'source' );
        Build.indentAndBuild( 'android' );
        Build.indentAndBuild( 'javadoc' );
    };



}() );
    // still under this module's load guard at top
}


// Copyright 2015, Michael Allan.  Licence MIT-Waymaker.
