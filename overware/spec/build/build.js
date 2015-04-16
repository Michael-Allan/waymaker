/** build.js - Utilities for building Overware
  ************
  */ if( !ov.build ) { ov.build = 'loading'; // recursion guard

ov.build = ( function()
{
    var our = {}; // public, all our.NAME is globally accessible as ov.build.NAME
    var my = {}; // private

    var CONTINUE = Java.type('java.nio.file.FileVisitResult').CONTINUE;
    var Files = Java.type( 'java.nio.file.Files' );
    var Paths = Java.type( 'java.nio.file.Paths' );
    var Pattern = Java.type( 'java.util.regex.Pattern' );
    var SimpleFileVisitor = Java.type( 'java.nio.file.SimpleFileVisitor' );



//// P u b l i c /////////////////////////////////////////////////////////////////////////


    /** Applied once (Matcher.find) to the verbose output of Android asset packaging tool
      * 'aapt package', this pattern captures a single group: 1) the count of files added
      * to the package.
      */
    our.AAPT_PACKAGE_COUNT_PATTERN = Pattern.compile( '^Generated (\\d+) file', Pattern.MULTILINE );



    /** Returns the command for Android build tool 'aapt', first smoke testing it if
      * config variable 'androidBuildToolsLoc' is yet untested.
      *
      *     @return String
      */
    our.aaptTested = function() { return my.androidBuildToolTested( 'aapt', 'version' ); }



    /** Returns the path to the Android bootclass jar, first testing that it exists.
      *
      *     @return java.nio.file.Path
      */
    our.androidJarTested = function()
    {
        var bc = our.config;
        var jar = my.androidJar;
        if( !jar )
        {
            jar = Paths.get( bc.androidSDKLoc, 'platforms', 'android-' + bc.androidVersion,
              'android.jar' );
            if( !Files.exists( jar ))
            {
                ov.exit( ov.L + 'Missing SDK file: ' + jar + ov.L
                  + 'Does your buildConfig.js correctly set androidSDKLoc and androidVersion?' );
            }

            my.androidJar = jar; // cache
        }
        return jar;
    };



    /** Returns the path to the Android 'sdklib.jar', first testing that it exists.
      *
      *     @return java.nio.file.Path
      */
    our.androidSDKLibJarTested = function()
    {
        var jar = my.androidSDKLibJar;
        if( !jar )
        {
            jar = Paths.get( our.config.androidSDKLoc, 'tools', 'lib', 'sdklib.jar' );
            if( !Files.exists( jar ))
            {
                ov.exit( ov.L + 'Missing SDK file: ' + jar + ov.L
                  + 'Does your buildConfig.js correctly set androidSDKLoc?' );
            }

            my.androidSDKLibJar = jar; // cache
        }
        return jar;
    };



    /** Arrays the class files compiled by javac.
      *
      *     @param dir (java.nio.file.Path) The output directory which contains the
      *       compiled class files.
      *     @param msCompileTime (long) The file time at which the compiler was invoked,
      *       or just before.  Only files modified at msCompileTime or later are included
      *       in the result.  The reliability of this filter depends on msCompileTime
      *       originating with the file system, as other clocks may differ in granularity.
      *
      *     @return JS Array of java.nio.file.Path
      */
    our.arrayCompiled = function( dir, msCompileTime )
    {
        var array = [];
        Files.walkFileTree( dir, new (Java.extend( SimpleFileVisitor ))
        {
            visitFile: function( file, att )
            {
                test:
                {
                    if( !att.isRegularFile() ) break test;

                    if( att.lastModifiedTime().toMillis() < msCompileTime ) break test;
                      // compiled on previous invocation of compiler, not this one

                    array.push( file );
                }
                return CONTINUE;
            }
        });
        return array;
    };



    /** The build configuration.
      */
    our.config = {};



    /** Counts the class files compiled by javac and returns the result.  Counts only the
      * top-level classes, not the member classes.
      *
      *     @param dir (java.nio.file.Path) The output directory which contains the
      *       compiled class files.
      *     @param compileTime (java.nio.file.attribute.FileTime) The time at which the
      *       compiler was invoked, or just before.  Only files modified at compileTime or
      *       later are included in the result.
      *
      *     @return Integer
      */
    our.countCompiled = function( dir, compileTime )
    {
        var count = 0;
        Files.walkFileTree( dir, new (Java.extend( SimpleFileVisitor ))
        {
            visitFile: function( file, att )
            {
                test:
                {
                    if( !att.isRegularFile() ) break test;

                    if( !our.isTopClass( file.getFileName().toString() )) break test;
                      // not a top-level class file

                    if( att.lastModifiedTime().compareTo(compileTime) < 0 ) break test;
                      // compiled on previous invocation of compiler, not this one

                    ++count;
                }
                return CONTINUE;
            }
        });
        count = count.intValue(); // per contract, defeat ++'s conversion to double
        return count;
    };



    /** Applied repeatedly (Matcher.find) to the verbose output of Android translator 'dx
      * --dex', this pattern matches once for each top-level class that was translated.
      * Member classes, recognized by $ characters in their names, are excluded.
      */
    our.DEXED_TOP_CLASS_PATTERN = Pattern.compile(
   // '^processing [^$\R]+\.class\.*$', Pattern.UNICODE_CHARACTER_CLASS/* for \R */ |
   ///// [^\R] fails to exclude line ends as promised, but this works on both Unix and Windows:
      '^processing [^$\n]+\.class\.*$',
      Pattern.MULTILINE );



    /** Returns the command for Android build tool 'dx', first smoke testing it if config
      * variable 'androidBuildToolsLoc' is yet untested.
      *
      *     @return String
      */
    our.dxTested = function() { return my.androidBuildToolTested( 'dx', '--version' ); }



    /** Prints the name of the target with proper indentation, then builds it.
      *
      *     @param target (String) The name of the target.
      */
    our.indentAndBuild = function( target )
    {
        var outS = Java.type('java.lang.System').out;
        outS.print( our.indentation() )
        outS.println( target )
        our.indent();
        try { ov.buildTarget[target](); } // build it
        finally { our.exdent(); }
    };



    /** An indentation string that reflects the current level of target nesting.
      */
    our.indentation = function() { return my.indentation; };


        my.indentationUnit = '  ';
        my.indentation = my.indentationUnit; // initial indentation of 1 unit


        /** Shortens the indentation string by 1 unit.
          */
        our.exdent = function()
        {
            my.indentation = my.indentation.slice( 0, -my.indentationUnit.length );
        };


        /** Lengthens the indentation string by 1 unit.
          */
        our.indent = function() { my.indentation += my.indentationUnit; };



    /** Answers whether the specified short name (File.getName) is correct for a top-level
      * class file, as opposed to a member class.
      */
    our.isTopClass = function( name )
    {
        if( !name.endsWith( '.class' )) return false; // not a class file

        var c = name.lastIndexOf( '$', name.length - '.class'.length - 1 );
        if( c != -1 ) return false; // member class, not top level

        return true;
    }



    /** Returns the command for the Java virtual machine 'java', first smoke testing it if
      * config variable 'jdkBinLoc' is yet untested.
      *
      *     @return String
      */
    our.javaTested = function()
    {
        var command = ov.slashed(our.config.jdkBinLoc) + 'java';
        if( !my.testedSet.contains( 'jdkBinLoc' ))
        {
            try{ $EXEC( ov.logCommand( command + ' -version' )); }
            catch( x )
            {
                ov.exit( ov.L + x + ov.L + 'Does your buildConfig.js correctly set jdkBinLoc?' );
            }

            ov.logCommandResult();
            my.testedSet.add( 'jdkBinLoc' );
        }
        return command;
    };



    /** Returns the command for the Java compiler 'javac', first smoke testing it if
      * config variable 'jdkBinLoc' is yet untested.
      *
      *     @return String
      */
    our.javacTested = function()
    {
        var command = ov.slashed(our.config.jdkBinLoc) + 'javac';
        if( !my.testedSet.contains( 'jdkBinLoc' ))
        {
            try
            {
                $EXEC( ov.logCommand( command + ' -target ' + bc.jdkVersion + ' -version' ));
                ov.logCommandResult();
                if( $EXIT ) throw $ERR; // probably an older javac rejecting the -target option
            }
            catch( x )
            {
                ov.exit( ov.L + x + ov.L +
                  'Does your buildConfig.js correctly set jdkBinLoc?  Is the JDK version '
                  + bc.jdkVersion + ' or later?' );
            }

            my.testedSet.add( 'jdkBinLoc' );
        }
        return command;
    };



    /** Builds Overware.  Call once only.
      */
    our.run = function()
    {
        for( ;; )
        {
            var target = $ARG.shift();
            if( !target ) break;

            our.indentAndBuild( target );
        }
        delete our.run; // singleton
    };



    /** The directory for expendable, intermediate output from the build process.  Target
      * "clean" deletes this directory together with its contents, while others recreate
      * it as needed.
      *
      *     @return String
      */
    our.tmpLoc = function() { return my.tmpLoc; };



    /** Overwrites argFile with the path of each Java source file that needs compiling or
      * recompiling.  If none needs compiling, then instead deletes argFile.  Explicit,
      * comprehensive source arguments of this kind are necessary when recompiling, as
      * opposed to compiling from scratch, because javac's implicit recompilation is
      * unreliable in the case of indirect dependencies.
      *
      *     @param indiClasses (JS Array of String) The relative path to the source file
      *       of each independent class in the compilation.  These are exactly the minimal
      *       set of source paths you would pass to javac during a clean compile (knowing
      *       that javac's default -implicit:class option would pull in the rest) except
      *       here you specify no .java extension.  For example: [fu/bar/One, fu/bar/Two].
      *     @param argFile (java.nio.file.Path) Wherein to write the source arguments.
      *     @param outDir (java.nio.file.Path) The directory in which the javac compiler
      *       outputs the class files.
      */
    our.writeSourceArgs = function( indiClasses, argFile, outDir )
    {
        Files.deleteIfExists( argFile );
        var args = []; // each without the '.java' extension
        for( var i = indiClasses.length - 1; i >= 0; --i )
        {
            var indiClass = indiClasses[i];
            var indiClassFile = outDir.resolve( indiClass + '.class' );
            if( !Files.exists( indiClassFile )) args.push( indiClass );
        }
        var outPrefix = outDir.toString() + ov.F;
        Files.walkFileTree( outDir, new (Java.extend( SimpleFileVisitor ))
        {
            visitFile: function( outFile, att )
            {
                test:
                {
                    if( !att.isRegularFile() ) break test;

                    if( !our.isTopClass( outFile.getFileName().toString() )) break test;
                      // not a top-level class file

                    var arg = outFile.toString();
                    if( !arg.startsWith( outPrefix )) ov.exit( null, __FILE__, __LINE__ ); // must

                    arg = arg.slice( outPrefix.length, -'.class'.length );
                      // relativized to outDir and relieved of .class extension
                    var inFile = Paths.get( ov.loc(), arg + '.java' );
                    if( !Files.exists( inFile )) break test; // source file was deleted

                    if( Files.getLastModifiedTime( inFile ).compareTo(
                        Files.getLastModifiedTime( outFile )) < 0 ) break test; // source unchanged

                    args.push( arg );
                }
                return CONTINUE;
            }
        });
        var aN = args.length;
        if( aN == 0 ) return;

        var out = new (Java.type('java.io.PrintWriter'))( argFile );
        for( var a = 0; a < aN; ++a )
        {
            var arg = args[a];
            out.append( arg );
            out.append( '.java' );
            out.println();
        }
        out.close();
    };



    /** Returns the command for Android build tool 'zipalign', first smoke testing it if
      * config variable 'androidBuildToolsLoc' is yet untested.
      *
      *     @return String
      */
    our.zipalignTested = function() { return my.androidBuildToolTested( 'zipalign' ); }



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    /** Returns the command for the named Android build tool, first smoke testing it if
      * config variable 'androidBuildToolsLoc' is yet untested.
      *
      *     @param (String) The name of the command.
      *     @param (String) Arguments to pass to it for testing, or null to pass no
      *       arguments.
      *
      *     @return String
      */
    my.androidBuildToolTested = function( name, arg )
    {
        var command = ov.slashed(our.config.androidBuildToolsLoc) + name;
        if( !my.testedSet.contains( 'androidBuildToolsLoc' ))
        {
            var testCommand = command;
            if( arg ) testCommand += ' ' + arg;
            try { $EXEC( ov.logCommand( testCommand )); }
            catch( x )
            {
                ov.exit( ov.L + x + ov.L +
                  'Does your buildConfig.js correctly set androidBuildToolsLoc?' );
            }

            ov.logCommandResult();
            my.testedSet.add( 'androidBuildToolsLoc' );
        }
        return command;
    };



    my.init = function()
    {
        if( $ARG.length === 0 ) $ARG.unshift( 'release' ); // default target
        delete my.init; // singleton
    };



    my.testedSet = new (Java.type('java.util.HashSet'))(); // names of smoke-tested config variables



    my.tmpLoc = ov.slashed(ov.tmpLoc()) + 'build';



////////////////////

    my.init();
    return our;

}() );
    // still under guard, if( !ov.build ) {
    ( function() { load( ov.ulocTo( 'overware/spec/build/buildConfigDefault.js' )); }() );
    load( ov.ulocTo( 'overware/spec/build/target.js' )); // dependency of user's config:
    ( function() // load user's build configuration, if any
    {
        var f = Java.type('java.nio.file.Paths').get( ov.userConfigLoc(), 'buildConfig.js' );
        if( Java.type('java.nio.file.Files').exists( f )) load( f.toString() );
    }() );
}


// Copyright 2015, Michael Allan.  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Overware Software"), to deal in the Overware Software without restriction, subject to the following conditions: The preceding copyright notice and this permission notice shall be included in all copies or substantial portions of the Overware Software.  The Overware Software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement.  In no event shall the authors or copyright holders be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the Overware Software or the use or other dealings in the Overware Software.
