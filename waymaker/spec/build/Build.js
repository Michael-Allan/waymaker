/** Build.js - General utilities for building Waymaker
  ************
  */
if( !waymaker.spec.build.Build ) {
     waymaker.spec.build.Build = {};
( function()
{
    var our = waymaker.spec.build.Build; // public as waymaker.spec.build.Build

    var Config = waymaker.spec.build.Config;
    var Files = Java.type( 'java.nio.file.Files' );
    var FileVisitResult = Java.type( 'java.nio.file.FileVisitResult' );
    var Paths = Java.type( 'java.nio.file.Paths' );
    var SimpleFileVisitor = Java.type( 'java.nio.file.SimpleFileVisitor' );
    var Waymaker = waymaker.Waymaker;

    var CONTINUE = FileVisitResult.CONTINUE;
    var L = Waymaker.L;
    var SKIP_SUBTREE = FileVisitResult.SKIP_SUBTREE;



//// P u b l i c ///////////////////////////////////////////////////////////////////////////////////////


    /** Arrays the class files compiled by javac.
      *
      *     @param dir (java.nio.file.Path) The output directory containing the compiled class files.
      *     @param msCompileTime (long) When the compiler was invoked, or just before.  Only files
      *       modified at msCompileTime or later are included in the result.  The reliability of this
      *       filter depends on msCompileTime originating with the file system clock, because other
      *       clocks may differ in granularity.
      *
      *     @return (JS Array of java.nio.file.Path)
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
                  //if( !att.isRegularFile() ) break test;
                 /// visitFile visits no directories
                    if( att.lastModifiedTime().toMillis() < msCompileTime ) break test;
                      // compiled on previous invocation of compiler, not this one

                    array.push( file );
                }
                return CONTINUE;
            }
        });
        return array;
    };



    /** Arrays the necessary source arguments for the javac compiler, each the absolute path of a Java
      * source file that needs compiling or recompiling.  Explicit, comprehensive source arguments are
      * necessary when recompiling as opposed to compiling from scratch, because javac's implicit
      * recompilation is unreliable in the case of indirect dependencies.
      *
      *     @param indiClasses (JS Array of String) An array containing the relative path to the source
      *       file of each independent class in the compilation.  These are exactly the minimal source
      *       paths you would pass to javac for a clean compilation, knowing that javac's default
      *       '-implicit:class' option would pull in the rest, except here you specify the paths without
      *       a .java extension.  For example: [waymaker/blah/One, waymaker/blah/Two].
      *     @param javacOutDir (java.nio.file.Path) The directory in which the javac compiler outputs
      *       the class files.
      *
      *     @return (JS Array of java.nio.file.Path)
      */
    our.arraySourceArguments = function( indiClasses, javacOutDir )
    {
        var args = [];
        var F = Waymaker.F;
        var waymakerDir = Paths.get( Waymaker.loc() );
        for( var i = indiClasses.length - 1; i >= 0; --i )
        {
            var indiClass = indiClasses[i];
            var indiClassFile = javacOutDir.resolve( indiClass + '.class' );
            if( !Files.exists( indiClassFile )) args.push( waymakerDir.resolve( indiClass + '.java' ));
        }
        Files.walkFileTree( javacOutDir, new (Java.extend( SimpleFileVisitor ))
        {
            visitFile: function( classFile, classAtt )
            {
                test:
                {
                 // if( !classAtt.isRegularFile() ) break test;
                 /// visitFile visits no directories
                    var fileName = classFile.getFileName().toString();
                    if( !our.isTopClass( fileName )) break test;

                    var sourceFile = Waymaker.suffixSibling( classFile, fileName, '.class', '.java' );
                    sourceFile = waymakerDir.resolve( javacOutDir.relativize( sourceFile ));
                    if( !Files.exists( sourceFile )) break test; // source file was deleted

                    var t = Files.getLastModifiedTime( sourceFile );
                    if( t.compareTo(classAtt.lastModifiedTime()) < 0 ) break test; // source unchanged

                    args.push( sourceFile );
                }
                return CONTINUE;
            }
        });
        return args;
    };



    /** Returns a message to inform the user of a bad value in a configuration variable.
      *
      *     @param name (String) The name of the configuration variable.
      *     @param value (String)
      */
    our.badConfigNote = function( name, value ) { return 'Bad config value for ' + name + ': ' + value; };



    /** Counts the class files compiled by javac and returns the result.  Counts only the top-level
      * classes, not the member classes.
      *
      *     @param dir (java.nio.file.Path) The output directory containing the compiled class files.
      *     @param compileTime (java.nio.file.attribute.FileTime) When the compiler was invoked, or just
      *       before.  Only files modified at compileTime or later are included in the result.
      *
      *     @return (Integer)
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
                 // if( !att.isRegularFile() ) break test;
                 /// visitFile visits no directories
                    if( !our.isTopClass( file.getFileName().toString() )) break test;
                      // not a top-level class file

                    if( att.lastModifiedTime().compareTo(compileTime) < 0 ) break test;
                      // compiled on previous invocation of compiler, not this one

                    ++count;
                }
                return CONTINUE;
            }
        });
        count = count.intValue(); // as per contract, defeat ++'s conversion to double
        return count;
    };



    /** Registers a file-associated build script and appends it to fabArray.  This function is called by
      * each file-associated build script as it loads.  It defines the following mandatory function on
      * the registered script (fab):
      *
      *     fab.fileLoc()
      *             The path to the file associated with this script.
      *         @return (String)
      *
      *     @param scriptFILE (String) The Nashorn __FILE__ of the file-associated build script.
      *     @return The registered script.
      */
    our.fab = function( scriptFILE )
    {
        if( !scriptFILE.endsWith( '.fab' )) throw( "Missing '.fab' extension: " + scriptFILE );

        var fab = {};
        var scriptLoc = new (Java.type('java.net.URI'))( scriptFILE ).getPath(); /* convert __FILE__
          from a file URI, maybe formed so because script was loaded by URI (grep PortableLoad) */
        var fileLoc = scriptLoc.substring( 0, scriptLoc.length - '.fab'.length );
        fab.fileLoc = function() { return fileLoc; };
        fabArray.push( fab );
        return fab;
    };



    /** The file-associated build scripts of this build.
      *
      *     @return (JS Array of Build.fab)
      */
    our.fabArray = function()
    {
        if( !fabArray )
        {
            fabArray = [];

          // Load each script.
          // - - - - - - - - - -
            var inRoot = Paths.get( Waymaker.loc(), 'waymaker' );
            var sourceMatcher = Config.sourceMatcher;
            Files.walkFileTree( inRoot, new (Java.extend( SimpleFileVisitor ))
            {
                preVisitDirectory: function( inDir, inAtt )
                {
                    return sourceMatcher.matches(inDir)? CONTINUE: SKIP_SUBTREE;
                },

                visitFile: function( inFile, inAtt )
                {
                    if( sourceMatcher.matches(inFile) && inFile.getFileName().toString().endsWith('.fab') )
                    {
                        load( inFile.toUri().toASCIIString() );
                          // it will register with Build.fab and thereby populating the array
                    }
                    return CONTINUE;
                }
            });
        }
        return fabArray;
    };


        var fabArray;



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
        try { waymaker.spec.build[targetName].Target.build(); }
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



    /** Answers whether the given file name (Path.getFileName.toString) is correct for the class file of
      * a top-level class, as opposed to a member class.
      */
    our.isTopClass = function( name )
    {
        if( !name.endsWith( '.class' )) return false; // not a class file

        var c = name.lastIndexOf( '$', name.length - '.class'.length - 1 );
        if( c != -1 ) return false; // member class, not top level

        return true;
    }



    /** Returns the command for the Java virtual machine 'java', first smoke testing it if configuration
      * variable 'jdkBinLoc' is yet untested.
      *
      *     @return (String)
      */
    our.javaTested = function()
    {
        var command = Waymaker.slashed(Config.jdkBinLoc) + 'java';
        if( !testedSet.contains( 'jdkBinLoc' ))
        {
            try { $EXEC( Waymaker.logCommand( command + ' -version' )); }
            catch( x ) { Waymaker.exit( L + x + L + 'Does your Config.js correctly set jdkBinLoc?' ); }

            Waymaker.logCommandResult();
            testedSet.add( 'jdkBinLoc' );
        }
        return command;
    };



    /** Returns the command for the Java compiler 'javac', first smoke testing it if configuration
      * variable 'jdkBinLoc' or 'jdkVersion' is yet untested.
      *
      *     @return (String)
      */
    our.javacTested = function()
    {
        var command = Waymaker.slashed(Config.jdkBinLoc) + 'javac';
        if( !testedSet.contains('jdkBinLoc') || !testedSet.contains('jdkVersion') )
        {
            try
            {
                $EXEC( Waymaker.logCommand( command + ' -target ' + Config.jdkVersion + ' -version' ));
                Waymaker.logCommandResult();
                if( $EXIT ) throw $ERR; // probably an older javac rejecting the -target option
            }
            catch( x )
            {
                Waymaker.exit( L + x + L +
                  'Does your Config.js correctly set jdkBinLoc?  Is your JDK version '
                  + Config.jdkVersion + ' or later?' );
            }

            testedSet.add( 'jdkBinLoc' );
            testedSet.add( 'jdkVersion' );
        }
        return command;
    };



    /** Returns the command for the Java API documenter 'javadoc', first smoke testing it if
      * configuration variable 'jdkBinLoc' is yet untested.
      *
      *     @return (String)
      */
    our.javadocTested = function()
    {
        var command = Waymaker.slashed(Config.jdkBinLoc) + 'javadoc';
        if( !testedSet.contains( 'jdkBinLoc' ))
        {
            try { $EXEC( Waymaker.logCommand( command + ' -help' )); }
            catch( x ) { Waymaker.exit( L + x + L + 'Does your Config.js correctly set jdkBinLoc?' ); }

            Waymaker.logCommandResult();
            testedSet.add( 'jdkBinLoc' );
        }
        return command;
    };



    /** The required minimum version of the JDK expressed in the form of a single number.
      *
      *     @return (Integer)
      */
    our.jdkSimpleVersion = function()
    {
        if( Config.jdkVersion == '1.8' ) return 8;

        throw( 'Unable to determine simple form of JDK version: ' + Config.jdkVersion );
    };



    /** Returns the path to the JDK bootclass jar, first testing that it exists.
      *
      *     @return (java.nio.file.Path)
      */
    our.rtJarTested = function()
    {
        var jar = rtJar;
        if( !jar )
        {
            jar = Paths.get( Config.jdkBinLoc, '..', 'jre', 'lib', 'rt.jar' ).normalize();
              // this abuse of jdkBinLoc (along with need of rt.jar) is expected to be temporary
            if( !Files.exists( jar ))
            {
                Waymaker.exit( L + 'Missing JDK file: ' + jar + L
                  + 'Does your Config.js correctly set jdkBinLoc?' + L
                  + 'It must explicitly be set in this (hopefully temporary) case.' );
            }
            rtJar = jar; // cache
        }
        return jar;
    };


        var rtJar;



    /** Builds Waymaker.  Call once only.
      */
    our.run = function()
    {
        delete our.run; // singleton

      // Load each requested target module.
      // - - - - - - - - - - - - - - - - - -
        if( $ARG.length === 0 ) $ARG.unshift( 'whole' ); // default target
        for( var t = $ARG.length - 1; t >= 0; --t )
        {
            var arg = $ARG[t];
            if( arg.startsWith( 'Config' ))
            {
                Waymaker.exit( "Misplaced configuration variant argument: " + arg );
                  // should be leading argument, shifted off earlier
            }

            var moduleFile = Paths.get( Waymaker.loc(), 'waymaker', 'spec', 'build', arg, 'Target.js' );
            if( Files.exists(moduleFile) ) load( moduleFile.toString() ); // grep NashornLoadedContext
            // else assume it's a custom target, already loaded in user configuration
        }

      // Build each requested target.
      // - - - - - - - - - - - - - - -
        var tN = $ARG.length;
        for( var t = 0; t < tN; ++t ) our.indentAndBuild( $ARG[t] );
    };



    /** The names of all smoke-tested configuration variables.
      *
      *     @return (java.util.Set<String>)
      */
    our.testedSet = function() { return testedSet; };


        var testedSet = new (Java.type('java.util.HashSet'))();



    /** The directory for expendable, intermediate output from the build process, a subdirectory of
      * Waymaker.tmpLoc.  Target "clean" deletes this directory together with its contents, while others
      * recreate it as needed.
      *
      *     @return (String)
      */
    our.tmpLoc = function() { return tmpLoc; };


        var tmpLoc = Waymaker.slashed(Waymaker.tmpLoc()) + 'build';



    /** The XML event factory for this build.
      *
      *     @return (javax.xml.stream.XMLEventFactory)
      */
    our.xmlEventFactory = function()
    {
        if( !xmlEventFactory ) xmlEventFactory = Java.type('javax.xml.stream.XMLEventFactory').newFactory();
        return xmlEventFactory;
    };


        var xmlEventFactory;



    /** The XML input factory for this build.
      *
      *     @return (javax.xml.stream.XMLInputFactory)
      */
    our.xmlInputFactory = function()
    {
        if( !xmlInputFactory ) xmlInputFactory = Java.type('javax.xml.stream.XMLInputFactory').newFactory();
        return xmlInputFactory;
    };


        var xmlInputFactory;



    /** The XML output factory for this build.
      *
      *     @return (javax.xml.stream.XMLOutputFactory)
      */
    our.xmlOutputFactory = function()
    {
        if( !xmlOutputFactory ) xmlOutputFactory = Java.type('javax.xml.stream.XMLOutputFactory').newFactory();
        return xmlOutputFactory;
    };


        var xmlOutputFactory;



}() );
    // still under this module's load guard at top
}


// Copyright © 2015 Michael Allan.  Licence MIT.
