/** Target.js - Definition of the 'android' build target
  *************
  * Builds the Android user interface.
  *
  *     $ waymaker/build -- android
  *
  * The product is an APK file (app.apk) ready to install and run on an Android device.  For example:
  *
  *     $ adb install -r waymaker-0.0/app.apk
  *     $ adb shell am start -n com.example.waymaker/waymaker.top.android.Wayranging
  *
  * This assumes a default configuration for productLoc (waymaker-0.0) and appPackageName
  * (com.example.waymaker).  To uninstall the product:
  *
  *     $ adb uninstall com.example.waymaker
  */
if( !waymaker.spec.build.android.Target ) {
     waymaker.spec.build.android.Target = {};
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/android/Android.js' ));
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/Build.js' ));
( function()
{
    var our = waymaker.spec.build.android.Target; // public as waymaker.spec.build.android.Target

    var Android = waymaker.spec.build.android.Android;
    var ArrayList = Java.type( 'java.util.ArrayList' );
    var Build = waymaker.spec.build.Build;
    var Config = waymaker.spec.build.Config;
    var Files = Java.type( 'java.nio.file.Files' );
    var FileVisitResult = Java.type( 'java.nio.file.FileVisitResult' );
    var Paths = Java.type( 'java.nio.file.Paths' );
    var Pattern = Java.type( 'java.util.regex.Pattern' );
    var PrintWriter = Java.type( 'java.io.PrintWriter' );
    var SimpleFileVisitor = Java.type( 'java.nio.file.SimpleFileVisitor' );
    var StandardCopyOption = Java.type( 'java.nio.file.StandardCopyOption' );
    var System = Java.type( 'java.lang.System' );
    var Waymaker = waymaker.Waymaker;

    var CONTINUE = FileVisitResult.CONTINUE;
    var REPLACE_EXISTING = StandardCopyOption.REPLACE_EXISTING;



//// P u b l i c ///////////////////////////////////////////////////////////////////////////////////////


    /** Builds this target.
      */
    our.build = function() // based on [H], q.v. for additional comments
    {
        var outS = System.out;
        var compileTimeJarArray = Android.compileTimeJarArray();
        var waymakerLoc = Waymaker.loc();

        var tmpDir = Paths.get( Build.tmpLoc(), 'android' );
        var F = Waymaker.F;
        var relInLoc = 'waymaker' + F + 'top' + F + 'android';
        var javacOutDir = Waymaker.ensureDir( tmpDir.resolve( 'javacOut' ));
        var javaInArray = Build.arraySourceArguments( [relInLoc + F + 'Wayranging'], javacOutDir );
        if( javaInArray.length > 0 )
        {
          // 1. Translate the source code to Android form.
          // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
            outS.append( Build.indentation() ).append( '(source.. ' );
            var originalSourceRoot = Paths.get( waymakerLoc, 'waymaker' );
            var javacSourceInDir = Waymaker.ensureDir( tmpDir.resolve( 'javacSourceIn' ));
              // temporary build directory for translated source (input for javac)
            var translatedlSourceRoot = javacSourceInDir.resolve( 'waymaker' );
            var count = 0;
            var translator = new (Java.extend( SimpleFileVisitor ))
            {
                assertionMatcher: Pattern.compile(
                   '((?:else )?)assert ([^;:]+)(?:: ([^;]+))?;(.*)' ).matcher( '' ),
                  // ----------         ------       -----     --
                  //     1                2            3       4
                  //
                  // Matches a region of a line (Matcher.match) from the first non-whitespace character
                  // to the end (exclusive), provided it contains an assert statement.  Groups portions
                  // 1) prior to the statement; 2) boolean expression; 3) string expression, or null if
                  // omitted; and 4) after the statement.

                bufferFile: tmpDir.resolve( 'javacSourceInBuffer' ),

                isDirEnsured: false,

                sourceMatcher: Config.sourceMatcher,

                translateMatchedAssertion: ( function()
                {
                    function toEmptyStatement()
                    {
                        var m = this.assertionMatcher;
                        return m.group(1) + '; /*androidAssertTranslation*/' + m.group(4);
                    }
                    function toIfStatement()
                    {
                        var m = this.assertionMatcher;
                        var stringExpression = m.group( 3 );
                        var trans = m.group(1);
                        trans += '{ '; /* Wrapping in block {} to isolate.  Otherwise a bare 'if' might
                          (not sure) wreck a surrounding if-else statement in some cases. */
                        trans += 'if( !( ' + m.group(2) + ' )) ';
                        if( stringExpression == null ) trans += 'throw new AssertionError();'
                        else trans += 'throw new AssertionError( ' + stringExpression + ' );'
                        trans += ' }/*androidAssertTranslation*/';
                        trans += m.group(4);
                        return trans;
                    }
                    var t = Config.androidAssertTranslation;
                    if( t == 'empty' ) return toEmptyStatement;
                    else if( t == 'if' ) return toIfStatement;
                    else Waymaker.exit( Build.badConfigNote( 'androidAssertTranslation', t ));
                }() ),

               // ---
                preVisitDirectory: function( inDir, inAtt )
                {
                    if( !this.sourceMatcher.matches( inDir )) return FileVisitResult.SKIP_SUBTREE;

                    this.isDirEnsured = false; // its existence not yet being tested
                    return CONTINUE;
                },

                visitFile: function( inFile, inAtt )
                {
                    file:
                    {
                        var fileName = inFile.getFileName().toString();
                        if( !fileName.endsWith( '.java' )) break file;

                        if( fileName.startsWith( 'package-' )) break file; // package-info.java

                        if( !this.sourceMatcher.matches( inFile )) break file;

                        var t = inAtt.lastModifiedTime();
                        var toFile = translatedlSourceRoot.resolve( originalSourceRoot.relativize( inFile ));
                        if( Files.exists(toFile) && t.compareTo(Files.getLastModifiedTime(toFile)) < 0 )
                        {
                            break file; // original source unchanged
                        }

                        if( !this.isDirEnsured )
                        {
                            Waymaker.ensureDir( toFile.getParent() );
                            this.isDirEnsured = true;
                        }
                        var _in = Files.newBufferedReader( inFile );
                        var out = Files.newBufferedWriter( this.bufferFile );
                        var line;
                        function lineHasAssert( cStart ) { return line.indexOf('assert',cStart) != -1; }
                        try
                        {
                            for( var lineNumber = 1;; ++lineNumber )
                            {
                                function assertionMismatchNote()
                                {
                                    return 'Unrecognized pattern of assert statement: ' + inFile +
                                      ', line ' + lineNumber;
                                }
                                line = _in.readLine();
                                if( line === null ) break;

                                tr: // translate the line if necessary
                                {
                                    var cN = line.length();
                                    var chFirst; // first non-whitespace character
                                    var cFirst; // index of chFirst
                                    for( var c = 0;; ++c )
                                    {
                                        if( c >= cN ) break tr; // only whitespace

                                        ch = line.charAt( c );
                                        if( ch != ' ' && ch != '\t' )
                                        {
                                            chFirst = ch;
                                            cFirst = c;
                                            break;
                                        }
                                    }

                                    if( chFirst == '/' )
                                    {
                                        var ch = line.charAt( cFirst + 1 );
                                        if( ch == '/' || ch == '*' ) break tr; // likely a comment
                                    }
                                    else if( chFirst == '*' ) break tr; // likely within a document comment

                                    if( !lineHasAssert(cFirst) ) break tr;

                                    var m = this.assertionMatcher.reset( line ).region( cFirst, cN );
                                    if( !m.matches() ) Waymaker.exit( assertionMismatchNote() );

                                    line = line.substring( 0, cFirst );
                                    line += this.translateMatchedAssertion();
                                    if( lineHasAssert(0) ) Waymaker.exit( assertionMismatchNote() );
                                      // an untranslated assertion remains, maybe the line had two
                                }
                                out.append( line );
                                out.newLine();
                            }
                        }
                        finally
                        {
                            out.close();
                            _in.close();
                        }
                        Files.move( this.bufferFile, toFile, REPLACE_EXISTING ); // now that it's complete
                        ++count;
                    }
                    return CONTINUE;
                }
            };

            Files.walkFileTree( originalSourceRoot, translator );
            count = count.intValue(); // [IV]
            outS.append( '\b\b\b ' ).println( count );

          // 2. Write the compilation arguments for javac.
          // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
            outS.append( Build.indentation() ).append( '(javac.. ' );
            var javacArgInFile = tmpDir.resolve( 'javacArgIn' );
            {
                var out = new PrintWriter( javacArgInFile ); // truncates file if it exists
                out.append( '-classpath ' );
                out.append( javacOutDir.toString() );
                var P = Waymaker.P;
                for each( var jar in compileTimeJarArray )
                {
                    out.append( P );
                    out.append( jar.toString() );
                }
                out.println();
                out.close();
            }

          // 3. Write the source arguments for javac.
          // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
            var javaInFile = tmpDir.resolve( 'javaIn' );
            {
                var out = new PrintWriter( javaInFile ); // truncates file if it exists
                for each( var f in javaInArray )
                {
                    f = translatedlSourceRoot.resolve( originalSourceRoot.relativize( f ));
                      // use the translated version of the source file
                    out.append( f.toString() ).println();
                }
                out.close();
            }

          // 4. Compile the source code to Java bytecode (.class files).
          // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
            var command = Build.javacTested()
              + ' -bootclasspath ' + Android.androidJarTested()
              + ' -d ' + javacOutDir
              + ' -encoding UTF-8' // of source; instead of platform default, whatever that might be
              + ' -source 1.7' // Java API version, cannot exceed -target
              + ' -sourcepath ' + waymakerLoc
              + ' -target 1.7' // JVM version, currently limited by Android to 1.7
              + ' -Werror' // terminate compilation when a warning occurs
              + ' -Xdoclint:all,-missing' /* verify all javadoc comments, but allow their omission;
                    changing? change also in ../javadoc/Target.js */
              + ' -Xlint'
              + ' @' + javacArgInFile
              + ' @' + javaInFile;
            var compileTime = Files.getLastModifiedTime( javaInFile );
            $EXEC( Waymaker.logCommand( command ));
            Waymaker.logCommandResult();
            if( $EXIT ) Waymaker.exit( $ERR );

            count = Build.countCompiled( javacOutDir, compileTime );
            outS.append( '\b\b\b ' ).println( count );
        }

      // 5. Translate the output from Java to Android bytecode.
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        count = ' '; // skipped, till proven otherwise
        var dexFile = tmpDir.resolve( 'classes.dex' );
        var args =
            ' --dex'
          + ' --output=' + dexFile
          + ' --verbose';
        dex:
        {
            if( Files.exists( dexFile )) // then update by translating the newly compiled classes
            {
                var msCompileTime = Files.getLastModifiedTime( dexFile ).toMillis();
                msCompileTime += 1; /* Compilation will have occured a considerable time
                  after previous dexFile was created, so it's safe to add something here.
                  Adding 1 ms suffices to prevent re-translating class files that were
                  compiled just before (almost simultaneous with) the last translation. */
                var classesInArray = Build.arrayCompiled( javacOutDir, msCompileTime );
                var cN = classesInArray.length;
                if( cN == 0 ) break dex;

                outS.append( Build.indentation() ).append( '(dx.. ' );
                var classesInFile = tmpDir.resolve( 'classesIn' );
                var out = new PrintWriter( classesInFile ); // truncates file if it exists
                for( var c = 0; c < cN; ++c )
                {
                    var classFile = classesInArray[c];
                    classFile = javacOutDir.relativize( classFile ); // dx expects relative path
                    out.append( classFile.toString() );
                    out.println();
                }
                out.close();
                classesInArray = null; // free memory
                var dir = $ENV.PWD; // dx resolves class files of input-list against working dir
                $ENV.PWD = javacOutDir.toString(); // so change dir to where the class files are
                try
                {
                    var command = Android.dxTested() + args
                      + ' --incremental'
                      + ' --input-list=' + classesInFile;
                    $EXEC( Waymaker.logCommand( command ));
                }
                finally { $ENV.PWD = dir; } // restore working directory
            }
            else // translate all classes
            {
                outS.append( Build.indentation() ).append( '(dx.. ' );
                var dxInFile = tmpDir.resolve( 'dxIn' );
                {
                    var out = new PrintWriter( dxInFile ); // truncates file if it exists
                    out.println( javacOutDir.toString() );
                    for each( var jar in compileTimeJarArray ) out.println( jar.toString() );
                    out.close();
                }
                var command = Android.dxTested() + args + ' --input-list=' + dxInFile;
                $EXEC( Waymaker.logCommand( command ));
            }
            Waymaker.logCommandResult();
            if( $EXIT ) Waymaker.exit( $ERR );

            var count = 0;
            var m = Android.DEXED_TOP_CLASS_PATTERN.matcher( $OUT );
            while( m.find() ) ++count;
            count = count.intValue(); // [IV]
            outS.append( '\b\b\b ' ).println( count );
        }

      // 6. Package up the resource files alone, making a partial APK.
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        var manifestTemplate = Paths.get( waymakerLoc, relInLoc, 'AndroidManifest.xml' );
        var apkPartFile = tmpDir.resolve( 'app.apkPart' );
        if( !Files.exists( apkPartFile )
          || Files.getLastModifiedTime( apkPartFile ).compareTo(
             Files.getLastModifiedTime( manifestTemplate )) < 0 ) // then do package it
        {
            outS.append( Build.indentation() ).append( '(aapt.. ' );

            // 1 //  First generate the manifest from its template
            var xmlInputFactory = Java.type('javax.xml.stream.XMLInputFactory').newFactory();
            var inX = xmlInputFactory.createXMLEventReader(
              new (Java.type('java.io.BufferedInputStream'))(
                new (Java.type('java.io.FileInputStream'))( manifestTemplate )));
            var manifestFile = tmpDir.resolve('AndroidManifest.xml').toFile();
            var xmlOutputFactory = Java.type('javax.xml.stream.XMLOutputFactory').newFactory();
            var outX = xmlOutputFactory.createXMLEventWriter(
              new (Java.type('java.io.BufferedOutputStream'))(
                new (Java.type('java.io.FileOutputStream'))( manifestFile )));
            var xmlEventFactory = Java.type('javax.xml.stream.XMLEventFactory').newFactory();
            try
            {
                while( inX.hasNext() )
                {
                    var xml = inX.next();
                    if( xml instanceof Java.type('javax.xml.stream.events.Comment') )
                    {
                        if( xml.getText() == ' incomplete template ' )
                        {
                            xml = xmlEventFactory.createComment( ' generated by build script ' );
                        }
                    }
                    else if( xml.isStartElement() )
                    {
                        var name = xml.getName();
                        if( name.getLocalPart() == 'manifest' && name.getNamespaceURI() == '' )
                        {
                            var attList = new ArrayList();
                            var aa = xml.getAttributes();
                            while( aa.hasNext() ) attList.add( aa.next() );
                            attList.add( xmlEventFactory.createAttribute( 'package',
                              Config.appPackageName ));
                            xml = xmlEventFactory.createStartElement( name, attList.iterator(),
                              xml.getNamespaces() );
                        }
                    }
                    outX.add( xml );
                }
            }
            finally
            {
                outX.close();
                inX.close();
            }

            // 2 //  Now package up the resources
            var command = Android.aaptTested()
              + ' package'
              + ' -f'
              + ' -F ' + apkPartFile
              + ' -I ' + Android.androidJarTested()
              + ' -M ' + manifestFile
           // + ' -S ' + resDir;
           /// but no resources are needed yet, aside from the mandatory manifest
              + ' -v';
            $EXEC( Waymaker.logCommand( command ));
            Waymaker.logCommandResult();
            if( $EXIT ) Waymaker.exit( $ERR );

            var m = Android.AAPT_PACKAGE_COUNT_PATTERN.matcher( $OUT );
            var count = m.find()? m.group( 1 ): '?';
            outS.append( '\b\b\b ' ).println( count );
        }

      // 7. Make the full APK.
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        var apkFullFile = tmpDir.resolve( 'app.apkUnalign' );
        var toDo = true; // till proven otherwise
        skip:
        {
            if( !Files.exists( apkFullFile ) ) break skip;

            var t = Files.getLastModifiedTime( apkFullFile );
            if( t.compareTo( Files.getLastModifiedTime( dexFile )) < 0
             || t.compareTo( Files.getLastModifiedTime( apkPartFile )) < 0 ) break skip;

            toDo = false;
        }
        if( toDo )
        {
            outS.append( Build.indentation() ).append( '(apk.. ' );
            var command = Build.javaTested()
              + ' -classpath ' + Android.sdkLibJarTested()
              + ' com.android.sdklib.build.ApkBuilderMain' // deprecated as explained in [H]
              + ' ' + apkFullFile
              + ' -d'
              + ' -f ' + dexFile
              + ' -v'
              + ' -z ' + apkPartFile;
            $EXEC( Waymaker.logCommand( command ));
            Waymaker.logCommandResult();
            if( $EXIT ) Waymaker.exit( $ERR );

            outS.append( '\b\b\b ' ).println( ' ' ); // done
        }

      // 8. Optimize the data alignment of the APK.
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        var apkAlignedFile = Waymaker.ensureDir(Paths.get(Config.productLoc))
          .resolve( 'app.apk' );
        if( !Files.exists( apkAlignedFile )
          || Files.getLastModifiedTime( apkAlignedFile ).compareTo(
             Files.getLastModifiedTime( apkFullFile )) < 0 ) // then do align it
        {
            outS.append( Build.indentation() ).append( '(zipalign.. ' );
            var command = Android.zipalignTested()
              + ' -f'
              + ' -v'
              + ' 4'
              + ' ' + apkFullFile
              + ' ' + apkAlignedFile;
            $EXEC( Waymaker.logCommand( command ));
            Waymaker.logCommandResult();
            if( $EXIT ) Waymaker.exit( $ERR );

            outS.append( '\b\b\b ' ).println( ' ' ); // done
        }
    };



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    var ttt = function() { return 'TESTTEST'; };



}() );
    // still under this module's load guard at top
}


// Notes
// -----
//  [H] http://stackoverflow.com/a/29313378/2402790
//
//  [IV] Using explicit intValue conversion here in order to defeat previous ++ operator's
//      implicit conversion to double, for sake of pretty printing.


// Copyright 2015, Michael Allan.  Licence MIT-Waymaker.
