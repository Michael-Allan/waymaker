/** Target.js - Definition of the 'javadoc' build target
  *************
  * Builds the Java API documentation.
  *
  *     $ overware/build -- javadoc
  *
  * The product is a directory named 'javadoc' which contains the documentation.
  */
if( !overware.spec.build.javadoc.Target ) {
     overware.spec.build.javadoc.Target = {};
load( overware.Overware.ulocTo( 'overware/spec/build/android/Android.js' ));
load( overware.Overware.ulocTo( 'overware/spec/build/Build.js' ));
( function()
{
    var our = overware.spec.build.javadoc.Target; // public as overware.spec.build.javadoc.Target

    var Android = overware.spec.build.android.Android;
    var Build = overware.spec.build.Build;
    var BuildConfig = overware.spec.build.BuildConfig;
    var Files = Java.type( 'java.nio.file.Files' );
    var Overware = overware.Overware;
    var Paths = Java.type( 'java.nio.file.Paths' );
    var PrintWriter = Java.type( 'java.io.PrintWriter' );
    var StandardCopyOption = Java.type( 'java.nio.file.StandardCopyOption' );

    var COPY_ATTRIBUTES = StandardCopyOption.COPY_ATTRIBUTES;
    var REPLACE_EXISTING = StandardCopyOption.REPLACE_EXISTING;



//// P u b l i c /////////////////////////////////////////////////////////////////////////


    /** Builds this target.
      */
    our.build = function()
    {
        var tmpDir = Overware.ensureDir( Paths.get( Build.tmpLoc(), 'javadoc' ));
        var outDir = Overware.ensureDir( Paths.get(BuildConfig.productLoc) ).resolve( 'javadoc' );

        var argInFile = tmpDir.resolve( 'argIn' );
        Files.deleteIfExists( argInFile );
        var out = new PrintWriter( argInFile );
        {
            out.println( '-breakiterator' );
            var compileTimeJarArray = Android.compileTimeJarArray(); // no others at present
            var jN = compileTimeJarArray.length;
            if( jN > 0 )
            {
                var P = Overware.P;
                out.append( '-classpath ' );
                for( var j = 0;; )
                {
                    var jar = compileTimeJarArray[j];
                    out.append( jar.toString() );
                    ++j;
                    if( j == jN ) break;

                    out.append( P );
                }
                out.println();
            }
            out.append( '-d ' ).println( outDir );
            out.append( '-link http://download.oracle.com/javase/' )
              .append( String(Build.jdkSimpleVersion()) ).println( '/docs/api/' );
            out.println( '-linksource' );
            out.println( '-noqualifier overware.*' );
            out.println( '-package' );
            out.append( '-sourcepath ' ).println( Overware.loc() );
            out.println( '-subpackages overware' );
            out.println( '-use' );
            out.println( "-windowtitle 'Overware Java API'" );
            out.close();
        }
        var command = Build.javadocTested() + ' @' + argInFile;
        $EXEC( Overware.logCommand( command ));
        Overware.logCommandResult();
        if( $EXIT ) Overware.exit( $ERR );

        var styleSheetOutDefault = outDir.resolve( 'stylesheetDefault.css' );
        var styleSheetOut = outDir.resolve( 'stylesheet.css' );
        if( !Files.exists( styleSheetOutDefault )) Files.move( styleSheetOut, styleSheetOutDefault );
        Files.copy( Paths.get(__DIR__).resolve('stylesheet.css'), styleSheetOut, COPY_ATTRIBUTES,
          REPLACE_EXISTING );
    };



}() );
    // still under this module's load guard at top
}


// Copyright 2015, Michael Allan.  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Overware Software"), to deal in the Overware Software without restriction, subject to the following conditions: The preceding copyright notice and this permission notice shall be included in all copies or substantial portions of the Overware Software.  The Overware Software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement.  In no event shall the authors or copyright holders be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the Overware Software or the use or other dealings in the Overware Software.
