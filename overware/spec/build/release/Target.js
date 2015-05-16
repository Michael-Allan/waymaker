/** Target.js - Definition of the 'release' build target
  *************
  * Builds a full release of the Overware software.  This is the default target.
  *
  *     $ overware/build
  *     $ overware/build -- release # the same thing
  */
if( !overware.spec.build.release.Target ) {
     overware.spec.build.release.Target = {};
load( overware.Overware.ulocTo( 'overware/spec/build/android/Target.js' ));
load( overware.Overware.ulocTo( 'overware/spec/build/javadoc/Target.js' ));
load( overware.Overware.ulocTo( 'overware/spec/build/source/Target.js' ));
load( overware.Overware.ulocTo( 'overware/spec/build/Build.js' ));
( function()
{
    var our = overware.spec.build.release.Target; // public as overware.spec.build.release.Target

    var Build = overware.spec.build.Build;



//// P u b l i c /////////////////////////////////////////////////////////////////////////


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
