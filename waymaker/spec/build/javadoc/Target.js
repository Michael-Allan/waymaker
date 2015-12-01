/** Target.js - Definition of the 'javadoc' build target
  *************
  * Builds the Java API documentation.
  *
  *     $ waymaker/build -- javadoc
  *
  * The product is a directory named 'waymaker/spec/javadoc' that contains the
  * documentation.  It includes HTML links to the source code and therefore depends on
  * ../source/Target.js.
  */
if( !waymaker.spec.build.javadoc.Target ) {
     waymaker.spec.build.javadoc.Target = {};
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/android/Android.js' ));
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/Build.js' ));
( function()
{
    var our = waymaker.spec.build.javadoc.Target; // public as waymaker.spec.build.javadoc.Target

    var Android = waymaker.spec.build.android.Android;
    var Build = waymaker.spec.build.Build;
    var Config = waymaker.spec.build.Config;
    var Files = Java.type( 'java.nio.file.Files' );
    var Waymaker = waymaker.Waymaker;
    var Paths = Java.type( 'java.nio.file.Paths' );
    var PrintWriter = Java.type( 'java.io.PrintWriter' );
    var StandardCopyOption = Java.type( 'java.nio.file.StandardCopyOption' );

    var COPY_ATTRIBUTES = StandardCopyOption.COPY_ATTRIBUTES;
    var REPLACE_EXISTING = StandardCopyOption.REPLACE_EXISTING;



//// P u b l i c ///////////////////////////////////////////////////////////////////////////////////////


    /** Builds this target.
      */
    our.build = function()
    {
        var tmpDir = Waymaker.ensureDir( Paths.get( Build.tmpLoc(), 'javadoc' ));
        var outDir = Waymaker.ensureDir( Paths.get( Config.productLoc, 'waymaker', 'spec',
          'javadoc' ));

        var argInFile = tmpDir.resolve( 'argIn' );
//      Files.deleteIfExists( argInFile );
        var out = new PrintWriter( argInFile ); // truncates file if it exists
        {
            var P = Waymaker.P;
         // out.append( '-bootclasspath ' ).println( Android.androidJarTested() );
         /// "class file for java.lang.FunctionalInterface not found" under JDK 1.8 + SDK 22,
         /// which lacks lambda (functional) expressions, so include JDK bootclass jar too:
            out.append( '-bootclasspath ' ).append( Build.rtJarTested().toString() )
              .append( P ).println( Android.androidJarTested() );
            out.println( '-breakiterator' );
            var compileTimeJarArray = Android.compileTimeJarArray(); // no others at present
            var jN = compileTimeJarArray.length;
            if( jN > 0 )
            {
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
            out.println( '-charset UTF-8' );
              // Make explicit in generated HTML, because otherwise Firefox falls back to
              // ISO-8859-1 (which it calls "windows-1252").
              // https://developer.mozilla.org/en-US/docs/Web/Guide/Localizations_and_character_encodings
            out.append( '-d ' ).println( outDir );
            out.println( '-encoding UTF-8' );
              // Of source; UTF-8 instead of platform default, whatever that might be.
              // This also governs the output encoding (-docencoding).
            out.append( '-link http://download.oracle.com/javase/' )
              .append( String(Build.jdkSimpleVersion()) ).println( '/docs/api/' );
         // out.append( '-linkoffline http://developer.android.com/reference/ ' )
         //   .println( Paths.get(Config.androidSDKLoc,'docs','reference') );
         /// But the resulting links often have broken IDs, because doclets are incompatible.
          // E.g. "@see Activity#runOnUiThread(Runnable)" generates JDK-1.8-style IDs:
          // http://developer.android.com/reference/android/app/Activity.html#runOnUiThread-java.lang.Runnable-
          // while Android continues to use the old style IDs:
          // http://developer.android.com/reference/android/app/Activity.html#runOnUiThread(java.lang.Runnable)
          // It's hard to correct this by compiling standard javadocs for Android because javadoc
          // tags in source are often incompatible too, using the syntax of the custom doclet.
         // out.println( '-linksource' );
         /// instead link to built source (../source/Target.js) consistent with javadoc comments
            out.println( '-noqualifier waymaker.*' );
            out.println( '-package' );
            out.append( '-sourcepath ' ).println( Waymaker.loc() );
            out.println( '-subpackages waymaker' );
            out.println( '-use' );
            out.println( "-windowtitle 'Waymaker Java API'" );
            out.println( ' -Xdoclint:all,-missing' ); /* verify all javadoc comments, but allow their omission;
              changing? change also in ../android/Target.js */
            out.close();
        }
        var command = Build.javadocTested() + ' @' + argInFile;
        $EXEC( Waymaker.logCommand( command ));
        Waymaker.logCommandResult();
        if( $EXIT ) Waymaker.exit( $ERR );

        var styleSheetOutDefault = outDir.resolve( 'stylesheetDefault.css' );
        var styleSheetOut = outDir.resolve( 'stylesheet.css' );
        if( !Files.exists( styleSheetOutDefault )) Files.move( styleSheetOut, styleSheetOutDefault );
        Files.copy( Paths.get(__DIR__).resolve('stylesheet.css'), styleSheetOut, COPY_ATTRIBUTES,
          REPLACE_EXISTING );
    };



}() );
    // still under this module's load guard at top
}


// Copyright 2015, Michael Allan.  Licence MIT-Waymaker.
