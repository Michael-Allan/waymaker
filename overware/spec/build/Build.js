/** Build.js - Utilities for building Overware
  ************
  */
if( !overware.spec.build.Build ) {
     overware.spec.build.Build = {};
     overware.spec.build.BuildConfig = {}; // predefine in order to simplify config file
( function()
{
    var our = overware.spec.build.Build; // public as overware.spec.build.Build

    var BuildConfig = overware.spec.build.BuildConfig;
    var Files = Java.type( 'java.nio.file.Files' );
    var Overware = overware.Overware;
    var Paths = Java.type( 'java.nio.file.Paths' );
    var SimpleFileVisitor = Java.type( 'java.nio.file.SimpleFileVisitor' );

    var CONTINUE = Java.type('java.nio.file.FileVisitResult').CONTINUE;
    var L = Overware.L;



//// P u b l i c /////////////////////////////////////////////////////////////////////////


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



    /** Prints the target name with proper indentation, then builds the target.
      *
      *     @param targetName (String)
      */
    our.indentAndBuild = function( targetName )
    {
        var outS = Java.type('java.lang.System').out;
        outS.print( our.indentation() )
        outS.println( targetName )
        our.indent();
        try { overware.spec.build[targetName].Target.build(); }
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
      * configuration variable 'jdkBinLoc' is yet untested.
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
      * configuration variable 'jdkBinLoc' or 'jdkVersion' is yet untested.
      *
      *     @return String
      */
    our.javacTested = function()
    {
        var command = Overware.slashed(BuildConfig.jdkBinLoc) + 'javac';
        if( !testedSet.contains('jdkBinLoc') || !testedSet.contains('jdkVersion') )
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
                  'Does your BuildConfig.js correctly set jdkBinLoc?  Is your JDK version '
                  + BuildConfig.jdkVersion + ' or later?' );
            }
            testedSet.add( 'jdkBinLoc' );
            testedSet.add( 'jdkVersion' );
        }
        return command;
    };



    /** Returns the command for the Java API documenter 'javadoc', first smoke testing it
      * if configuration variable 'jdkBinLoc' is yet untested.
      *
      *     @return String
      */
    our.javadocTested = function()
    {
        var command = Overware.slashed(BuildConfig.jdkBinLoc) + 'javadoc';
        if( !testedSet.contains( 'jdkBinLoc' ))
        {
            try{ $EXEC( Overware.logCommand( command + ' -help' )); }
            catch( x )
            {
                Overware.exit( L + x + L + 'Does your BuildConfig.js correctly set jdkBinLoc?' );
            }
            Overware.logCommandResult();
            testedSet.add( 'jdkBinLoc' );
        }
        return command;
    };



    /** The required minimum version of the JDK expressed in the form of a single number.
      *
      *     @return Integer
      */
    our.jdkSimpleVersion = function()
    {
        if( BuildConfig.jdkVersion == '1.8' ) return 8;

        throw( 'Unable to determine simple form of JDK version: ' + BuildConfig.jdkVersion );
    };



    /** Builds Overware.  Call once only.
      */
    our.run = function()
    {
        delete our.run; // singleton
        var tN = $ARG.length;
        for( var t = 0; t < tN; ++t ) our.indentAndBuild( $ARG[t] );
    };



    /** The names of all smoke-tested configuration variables.
      *
      *     @return java.util.Set<String>
      */
    our.testedSet = function() { return testedSet; };


        var testedSet = new (Java.type('java.util.HashSet'))();



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



}() );
    // still under this module's load guard at top
    ( function()
    {
        load( overware.Overware.ulocTo( 'overware/spec/build/BuildConfig_default.js' ));
    }() );
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
    ( function()
    {
        // Load the requested standard target modules
        var Files = Java.type( 'java.nio.file.Files' );
        var Paths = Java.type( 'java.nio.file.Paths' );
        if( $ARG.length === 0 ) $ARG.unshift( 'release' ); // default target
        for( var t = $ARG.length - 1; t >= 0; --t )
        {
            var moduleFile = Paths.get( overware.Overware.loc(), 'overware', 'spec', 'build',
              $ARG[t], 'Target.js' );
            if( Files.exists(moduleFile) ) load( moduleFile.toString() );
            // else assume a custom target defined by the user
        }
    }() );
}


// Copyright 2015, Michael Allan.  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Overware Software"), to deal in the Overware Software without restriction, subject to the following conditions: The preceding copyright notice and this permission notice shall be included in all copies or substantial portions of the Overware Software.  The Overware Software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement.  In no event shall the authors or copyright holders be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the Overware Software or the use or other dealings in the Overware Software.
