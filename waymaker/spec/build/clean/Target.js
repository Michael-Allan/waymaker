/** Target.js - Definition of the 'clean' build target
  *************
  * Deletes all files generated by previous builds.
  *
  *     $ waymaker/build -- clean
  *     $ waymaker/build -- clean TARGET...
  */
if( !waymaker.spec.build.clean.Target ) {
     waymaker.spec.build.clean.Target = {};
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/Build.js' ));
( function()
{
    var our = waymaker.spec.build.clean.Target; // public as waymaker.spec.build.clean.Target

    var Build = waymaker.spec.build.Build;
    var Files = Java.type( 'java.nio.file.Files' );
    var FileVisitResult = Java.type( 'java.nio.file.FileVisitResult' );
    var Paths = Java.type( 'java.nio.file.Paths' );
    var SimpleFileVisitor = Java.type( 'java.nio.file.SimpleFileVisitor' );
    var System = Java.type( 'java.lang.System' );

    var CONTINUE = FileVisitResult.CONTINUE;



//// P u b l i c ///////////////////////////////////////////////////////////////////////////////////////


    /** Builds this target.
      */
    our.build = function()
    {
        var outS = System.out;
        var locs = { tmp: Build.tmpLoc(), product: waymaker.spec.build.Config.productLoc };
        for( var l in locs )
        {
            var dir = Paths.get( locs[l] );
            if( !Files.exists( dir )) continue;

            outS.append( Build.indentation() ).append( '(' ).append( l ).append( '.. ' );
            var count = 0;
            Files.walkFileTree( dir, new (Java.extend( SimpleFileVisitor ))
            {
                postVisitDirectory: function( dir, x )
                {
                    if( x ) throw x;

                    Files.delete( dir );
                    ++count;
                    return CONTINUE;
                },

                visitFile: function( file, att )
                {
                    Files.delete( file );
                    ++count;
                    return CONTINUE;
                }
            });
            count = count.intValue(); // [IV]
            outS.append( '\b\b\b ' ).println( count );
        }
    };



}() );
    // still under this module's load fence at top
}


// Note
// ----
//  [IV] Using explicit intValue conversion here in order to defeat previous ++ operator's
//      implicit conversion to double, if only for sake of pretty printing.


// Copyright © 2015 Michael Allan.  Licence MIT.
