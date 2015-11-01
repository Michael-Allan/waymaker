/** Target.js - Definition of the 'release' build target
  *************
  * Builds a full release of the Waymaker software.  This is the default target.
  *
  *     $ waymaker/build
  *     $ waymaker/build -- release # the same thing
  */
if( !waymaker.spec.build.release.Target ) {
     waymaker.spec.build.release.Target = {};
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/android/Target.js' ));
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/javadoc/Target.js' ));
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/source/Target.js' ));
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/Build.js' ));
( function()
{
    var our = waymaker.spec.build.release.Target; // public as waymaker.spec.build.release.Target

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
