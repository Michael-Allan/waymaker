/** Target.js - Definition of the 'source' build target
  *************
  * Builds a copy of the source code.
  *
  *     $ overware/build -- source
  *
  * The product is a directory named 'overware' which contains a copy of the code.
  * You can filter the content by defining sourceMatcher in your BuildConfig.js.
  */
if( !overware.spec.build.source.Target ) {
     overware.spec.build.source.Target = {};
load( overware.Overware.ulocTo( 'overware/spec/build/Build.js' ));
( function()
{
    var our = overware.spec.build.source.Target; // public as overware.spec.build.source.Target

    var Build = overware.spec.build.Build;
    var BuildConfig = overware.spec.build.BuildConfig;
    var Files = Java.type( 'java.nio.file.Files' );
    var FileVisitResult = Java.type( 'java.nio.file.FileVisitResult' );
    var Paths = Java.type( 'java.nio.file.Paths' );
    var SimpleFileVisitor = Java.type( 'java.nio.file.SimpleFileVisitor' );
    var StandardCopyOption = Java.type( 'java.nio.file.StandardCopyOption' );
    var System = Java.type( 'java.lang.System' );

    var CONTINUE = FileVisitResult.CONTINUE;
    var COPY_ATTRIBUTES = StandardCopyOption.COPY_ATTRIBUTES;



//// P u b l i c /////////////////////////////////////////////////////////////////////////


    /** Builds this target.
      */
    our.build = function()
    {
        var outS = System.out;
        outS.append( Build.indentation() ).append( '(.. ' );
        var sourceMatcher = BuildConfig.sourceMatcher;
        var fromRoot = Paths.get( Overware.loc(), 'overware' );
        var toRoot = Overware.ensureDir(Paths.get(BuildConfig.productLoc)).resolve( 'overware' );
        var count = 0;
        Files.walkFileTree( fromRoot, new (Java.extend( SimpleFileVisitor ))
        {
            preVisitDirectory: function( fromDir, fromAtt )
            {
                if( !sourceMatcher.matches( fromDir )) return FileVisitResult.SKIP_SUBTREE;

                var toDir = toRoot.resolve( fromRoot.relativize( fromDir ));
                if( !Files.isDirectory( toDir )) // assume uniprocess for atomic test/act
                {
                    Files.copy( fromDir, toDir, COPY_ATTRIBUTES );
                    ++count;
                }
                return CONTINUE;
            },

            visitFile: function( fromFile, fromAtt )
            {
                if( sourceMatcher.matches( fromFile ))
                {
                    var toFile = toRoot.resolve( fromRoot.relativize( fromFile ));
                    if( !Files.isRegularFile(toFile) ||
                      fromAtt.lastModifiedTime().compareTo(Files.getLastModifiedTime(toFile)) > 0 )
                    {
                        Files.copy( fromFile, toFile, COPY_ATTRIBUTES,
                          StandardCopyOption.REPLACE_EXISTING );
                        ++count;
                    }
                }
                return CONTINUE;
            }
        });
        count = count.intValue(); // [2]
        outS.append( '\b\b\b ' ).println( count );
    };



}() );
    // still under this module's load guard at top
}


// Copyright 2015, Michael Allan.  Licence MIT-Overware.
