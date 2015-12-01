/** Target.js - Definition of the 'source' build target
  *************
  * Builds a copy of the source code.
  *
  *     $ waymaker/build -- source
  *
  * The product is a directory named 'waymaker' which contains a copy of the code.
  * You can filter the content by defining sourceMatcher in your Config.js.
  */
if( !waymaker.spec.build.source.Target ) {
     waymaker.spec.build.source.Target = {};
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/Build.js' ));
( function()
{
    var our = waymaker.spec.build.source.Target; // public as waymaker.spec.build.source.Target

    var Build = waymaker.spec.build.Build;
    var Config = waymaker.spec.build.Config;
    var Files = Java.type( 'java.nio.file.Files' );
    var FileVisitResult = Java.type( 'java.nio.file.FileVisitResult' );
    var Paths = Java.type( 'java.nio.file.Paths' );
    var SimpleFileVisitor = Java.type( 'java.nio.file.SimpleFileVisitor' );
    var StandardCopyOption = Java.type( 'java.nio.file.StandardCopyOption' );
    var System = Java.type( 'java.lang.System' );
    var Waymaker = waymaker.Waymaker;

    var CONTINUE = FileVisitResult.CONTINUE;
    var COPY_ATTRIBUTES = StandardCopyOption.COPY_ATTRIBUTES;



//// P u b l i c ///////////////////////////////////////////////////////////////////////////////////////


    /** Builds this target.
      */
    our.build = function()
    {
        var outS = System.out;
        outS.append( Build.indentation() ).append( '(.. ' );
        var inRoot = Paths.get( Waymaker.loc(), 'waymaker' );
        var sourceMatcher = Config.sourceMatcher;
        var outRoot = Waymaker.ensureDir(Paths.get(Config.productLoc)).resolve( 'waymaker' );
        var count = 0;
        Files.walkFileTree( inRoot, new (Java.extend( SimpleFileVisitor ))
        {
            preVisitDirectory: function( inDir, inAtt )
            {
                if( !sourceMatcher.matches( inDir )) return FileVisitResult.SKIP_SUBTREE;

                var outDir = outRoot.resolve( inRoot.relativize( inDir ));
                if( !Files.isDirectory( outDir )) // assume uniprocess for atomic test/act
                {
                    Files.copy( inDir, outDir, COPY_ATTRIBUTES );
                    ++count;
                }
                return CONTINUE;
            },

            visitFile: function( inFile, inAtt )
            {
                if( sourceMatcher.matches( inFile ))
                {
                    var outFile = outRoot.resolve( inRoot.relativize( inFile ));
                    if( !Files.exists(outFile) ||
                      inAtt.lastModifiedTime().compareTo(Files.getLastModifiedTime(outFile)) > 0 )
                    {
                        Files.copy( inFile, outFile, COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING );
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


// Copyright 2015, Michael Allan.  Licence MIT-Waymaker.
