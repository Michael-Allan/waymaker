/** Build.js - Utilities for building Overware
  ************
  */
if( !overware.spec.build.Build ) {
     overware.spec.build.Build = {};
     overware.spec.build.BuildConfig = {}; // predefine in order to simplify config file
load( overware.Overware.ulocTo( 'overware/spec/build/Target.js' ));
( function()
{
    var our = overware.spec.build.Build; // public as overware.spec.build.Build

    var BuildConfig = overware.spec.build.BuildConfig;
    var Files = Java.type( 'java.nio.file.Files' );
    var Overware = overware.Overware;
    var Paths = Java.type( 'java.nio.file.Paths' );
    var Pattern = Java.type( 'java.util.regex.Pattern' );
    var SimpleFileVisitor = Java.type( 'java.nio.file.SimpleFileVisitor' );
    var Target = overware.spec.build.Target;

    var CONTINUE = Java.type('java.nio.file.FileVisitResult').CONTINUE;
    var L = Overware.L;



    var init = function()
    {
        init = undefined; // singleton
        if( $ARG.length === 0 ) $ARG.unshift( 'release' ); // default target
    };



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
    our.aaptTested = function() { return androidBuildToolTested( 'aapt', 'version' ); }



    /** Returns the path to the Android bootclass jar, first testing that it exists.
      *
      *     @return java.nio.file.Path
      */
    our.androidJarTested = function() // named by the load guard at top
    {
        var jar = androidJar;
        if( !jar )
        {
            jar = Paths.get( BuildConfig.androidSDKLoc, 'platforms',
              'android-' + BuildConfig.androidVersion, 'android.jar' );
            if( !Files.exists( jar ))
            {
                Overware.exit( L + 'Missing SDK file: ' + jar + L
                  + 'Does your BuildConfig.js correctly set androidSDKLoc and androidVersion?' );
            }
            androidJar = jar; // cache
        }
        return jar;
    };


        var androidJar;



    /** Returns the path to the Android 'sdklib.jar', first testing that it exists.
      *
      *     @return java.nio.file.Path
      */
    our.androidSDKLibJarTested = function()
    {
        var jar = androidSDKLibJar;
        if( !jar )
        {
            jar = Paths.get( BuildConfig.androidSDKLoc, 'tools', 'lib', 'sdklib.jar' );
            if( !Files.exists( jar ))
            {
                Overware.exit( L + 'Missing SDK file: ' + jar + L
                  + 'Does your BuildConfig.js correctly set androidSDKLoc?' );
            }
            androidSDKLibJar = jar; // cache
        }
        return jar;
    };


        var androidSDKLibJar;



    /** Returns the path to the 'android-support-v4.jar', first testing that it exists.
      *
      *     @return java.nio.file.Path
      *     @see http://developer.android.com/tools/support-library/setup.html
      */
    our.androidSupport4JarTested = function()
    {
        var jar = androidSupport4Jar;
        if( !jar )
        {
            jar = Paths.get( BuildConfig.androidSDKLoc, 'extras', 'android', 'support', 'v4',
              'android-support-v4.jar' );
            if( !Files.exists( jar ))
            {
                Overware.exit( L + 'Missing SDK file: ' + jar + L
                  + 'Does your BuildConfig.js correctly set androidSDKLoc?' );
            }
            androidSupport4Jar = jar; // cache
        }
        return jar;
    };


        var androidSupport4Jar;



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
    our.dxTested = function()
    {
        var name = Overware.osTag() == 'win'? 'dx.bat': 'dx';
        return androidBuildToolTested( name, '--version' );
    }



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
        try { Target[target](); } // build it
        finally { our.exdent(); }
    };



    /** An indentation string that reflects the current level of target nesting.
      */
    our.indentation = function() { return indentation; };


        var indentationUnit = '  ';

        var indentation = indentationUnit; // initial indentation of 1 unit


        /** Shortens the indentation string by 1 unit.
          */
        our.exdent = function()
        {
            indentation = indentation.slice( 0, -indentationUnit.length );
        };


        /** Lengthens the indentation string by 1 unit.
          */
        our.indent = function() { indentation += indentationUnit; };



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
        var command = Overware.slashed(BuildConfig.jdkBinLoc) + 'java';
        if( !testedSet.contains( 'jdkBinLoc' ))
        {
            try{ $EXEC( Overware.logCommand( command + ' -version' )); }
            catch( x )
            {
                Overware.exit( L + x + L + 'Does your BuildConfig.js correctly set jdkBinLoc?' );
            }
            Overware.logCommandResult();
            testedSet.add( 'jdkBinLoc' );
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
        var command = Overware.slashed(BuildConfig.jdkBinLoc) + 'javac';
        if( !testedSet.contains( 'jdkBinLoc' ))
        {
            try
            {
                $EXEC( Overware.logCommand( command + ' -target ' + BuildConfig.jdkVersion
                  + ' -version' ));
                Overware.logCommandResult();
                if( $EXIT ) throw $ERR; // probably an older javac rejecting the -target option
            }
            catch( x )
            {
                Overware.exit( L + x + L +
                  'Does your BuildConfig.js correctly set jdkBinLoc?  Is the JDK version '
                  + BuildConfig.jdkVersion + ' or later?' );
            }
            testedSet.add( 'jdkBinLoc' );
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
    our.tmpLoc = function() { return tmpLoc; };


        var tmpLoc = Overware.slashed(Overware.tmpLoc()) + 'build';



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
        var F = Overware.F;
        var args = []; // each without the '.java' extension
        for( var i = indiClasses.length - 1; i >= 0; --i )
        {
            var indiClass = indiClasses[i];
            var indiClassFile = outDir.resolve( indiClass + '.class' );
            if( !Files.exists( indiClassFile ))
            {
                args.push( Overware.loc() + F + indiClass + '.java' );
            }
        }
        var outPrefix = outDir.toString() + F;
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
                    if( !arg.startsWith( outPrefix )) throw 'Impossible state';

                    arg = arg.slice( outPrefix.length, -'.class'.length );
                      // relativized to outDir and relieved of .class extension
                    var inFile = Paths.get( Overware.loc(), arg + '.java' );
                    if( !Files.exists( inFile )) break test; // source file was deleted

                    if( Files.getLastModifiedTime( inFile ).compareTo(
                        Files.getLastModifiedTime( outFile )) < 0 ) break test; // source unchanged

                    args.push( inFile.toString() );
                }
                return CONTINUE;
            }
        });
        var aN = args.length;
        if( aN == 0 ) return;

        var out = new (Java.type('java.io.PrintWriter'))( argFile );
        for( var a = 0; a < aN; ++a ) out.append( args[a] ).println();
        out.close();
    };



    /** Returns the command for Android build tool 'zipalign', first smoke testing it if
      * config variable 'androidBuildToolsLoc' is yet untested.
      *
      *     @return String
      */
    our.zipalignTested = function() { return androidBuildToolTested( 'zipalign' ); }



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
    function androidBuildToolTested( name, arg )
    {
        var command = Overware.slashed(BuildConfig.androidBuildToolsLoc) + name;
        if( !testedSet.contains( 'androidBuildToolsLoc' ))
        {
            var testCommand = command;
            if( arg ) testCommand += ' ' + arg;
            try { $EXEC( Overware.logCommand( testCommand )); }
            catch( x )
            {
                Overware.exit( L + x + L +
                  'Does your BuildConfig.js correctly set androidBuildToolsLoc?' );
            }
            Overware.logCommandResult();
            testedSet.add( 'androidBuildToolsLoc' );
        }
        return command;
    }



    var testedSet = new (Java.type('java.util.HashSet'))(); // names of smoke-tested config variables



////////////////////

    init();

}() );
    // still under this module's load guard at top
    ( function()
    {
        load( overware.Overware.ulocTo( 'overware/spec/build/BuildConfig_default.js' ));
    }() );
 // load( overware.Overware.ulocTo( 'overware/spec/build/Target.js' )); // dependency of user config:
 /// already loaded above
    ( function()
    {
        // Load user's build configuration, if any
        var f = Java.type('java.nio.file.Paths').get( overware.Overware.userConfigLoc(),
          'BuildConfig.js' );
        if( Java.type('java.nio.file.Files').exists( f )) load( f.toString() );

        // Apply any command-line overrides
        var BuildConfig = overware.spec.build.BuildConfig;
        var properties = Java.type('java.lang.System').getProperties();
        var keyPrefix = 'overware.spec.build.BuildConfig.';
        for each( var key in properties.keySet() ) // plain 'for' fails, not an array
        {
            if( !key.startsWith( keyPrefix )) continue;

            var name = key.slice( keyPrefix.length );
            BuildConfig[name] = properties.get( key ); // override
        }
    }() );
}


// Copyright 2015, Michael Allan.  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Overware Software"), to deal in the Overware Software without restriction, subject to the following conditions: The preceding copyright notice and this permission notice shall be included in all copies or substantial portions of the Overware Software.  The Overware Software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement.  In no event shall the authors or copyright holders be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the Overware Software or the use or other dealings in the Overware Software.
