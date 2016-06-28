/** Target.js - Definition of the 'android' build target
  *************
  * Builds the Android user interface.
  *
  *     $ waymaker/build -- android
  *
  * Tests for and calls the following optional function on each script (fab) of
  * Build.fabArray:
  *
  *     fab.readyAndroidAsset( Android )
  *             Ensures that this script's associated file is ready for packaging as a runtime asset.
  *         @param Android (waymaker.spec.build.android.Android)
  *         @return (boolean) Whether any action was taken.  True if the file was readied, false if it
  *           was ready to begin with.
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
    var System = Java.type( 'java.lang.System' );
    var Waymaker = waymaker.Waymaker;

    var CONTINUE = FileVisitResult.CONTINUE;
    var F = Waymaker.F;
    var P = Waymaker.P;
    var REPLACE_EXISTING = Java.type( 'java.nio.file.StandardCopyOption' ).REPLACE_EXISTING;



//// P u b l i c ///////////////////////////////////////////////////////////////////////////////////////


    /** Builds this target.
      */
    our.build = function() // based on [H], q.v. for additional comments
    {
        var tmpDir = Paths.get( Android.tmpLoc() );
        var outS = System.out;

      // 1. Ready the assets.
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        outS.append( Build.indentation() ).append( '(fab assets.. ' );
        var modifiedAssetCount = 0;
        for each( var fab in Build.fabArray() )
        {
            var func = fab.readyAndroidAsset;
            if( func )
            {
                var wasAssetModified = func( Android );
                if( wasAssetModified ) ++modifiedAssetCount;
            }
        }
        modifiedAssetCount = modifiedAssetCount.intValue(); // [IV]
        outS.append( '\b\b\b ' ).println( modifiedAssetCount );

      // 2. Package the assets alone, making a partial APK.
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        var aaptSourceOutDir = Waymaker.ensureDir( tmpDir.resolve( 'aaptSourceOut' ));
        var waymakerLoc = Waymaker.loc();
        var androidPackageRelLoc = 'waymaker' + F + 'top' + F + 'android';
        var manifestTemplate = Paths.get( waymakerLoc, androidPackageRelLoc, 'AndroidManifest.xml' );
        var apkPartFile = tmpDir.resolve( 'app.apkPart' );
        if( modifiedAssetCount > 0 || !Files.exists(apkPartFile)
          || Files.getLastModifiedTime(apkPartFile).compareTo(
             Files.getLastModifiedTime(manifestTemplate)) < 0 ) // then do package it
        {
            outS.append( Build.indentation() ).append( '(aapt.. ' );

          // Generate the manifest from its template.
          // - - - - - - - - - - - - - - - - - - - - -
            var manifestFile = tmpDir.resolve('AndroidManifest.xml').toFile();
            var inX = Build.xmlInputFactory().createXMLEventReader(
              new (Java.type('java.io.BufferedInputStream'))(
                new (Java.type('java.io.FileInputStream'))( manifestTemplate )));
            var outX = Build.xmlOutputFactory().createXMLEventWriter(
              new (Java.type('java.io.BufferedOutputStream'))(
                new (Java.type('java.io.FileOutputStream'))( manifestFile )));
            try
            {
                var factory = Build.xmlEventFactory();
                while( inX.hasNext() )
                {
                    var xml = inX.next();
                    if( xml instanceof Java.type('javax.xml.stream.events.Comment') )
                    {
                        if( xml.getText() == ' incomplete template ' )
                        {
                            xml = factory.createComment( ' generated by build script ' );
                        }
                    }
                    else if( xml.isStartElement() )
                    {
                        var name = xml.getName();
                        if( name.getLocalPart() == 'manifest' && name.getNamespaceURI() == '' )
                        {
                            var attList = new ArrayList();
                            var atts = xml.getAttributes();
                            while( atts.hasNext() ) attList.add( atts.next() );
                            attList.add( factory.createAttribute( 'package', Config.appPackageName ));
                            xml = factory.createStartElement( name, attList.iterator(), xml.getNamespaces() );
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

          // Package the assets.
          // - - - - - - - - - - -
            var command = Android.aaptTested()
              + ' package'
           // +  ' -A ' + Android.tmpLoc() + F + Android.aaptInRelLoc_assets()
              + ' --custom-package waymaker.top.android' // where it "generates R.java"
              +  ' -f'
              +  ' -F ' + apkPartFile
           // + ' --generate-dependencies' // generates R.java.d, purpose of which is unclear
              +  ' -I ' + Android.androidJarTested()
              +  ' -J ' + Waymaker.ensureDir( aaptSourceOutDir.resolve( androidPackageRelLoc ))
              +  ' -M ' + manifestFile
              +  ' -S ' + Android.tmpLoc() + F + Android.aaptInRelLoc_res()
              +  ' -v';
            $EXEC( Waymaker.logCommand( command ));
            Waymaker.logCommandResult();
            if( $EXIT ) Waymaker.exit( $ERR );

            var m = Android.AAPT_PACKAGE_COUNT_PATTERN.matcher( $OUT );
            var count = m.find()? m.group( 1 ): '?';
            outS.append( '\b\b\b ' ).println( count );
        }

      // 3. Compile the source code to Java bytecode.
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        var compileTimeJarArray = Android.compileTimeJarArray();
        var javacOutDir = Waymaker.ensureDir( tmpDir.resolve( 'javacOut' ));
        var javacSourceArgArray = Build.arraySourceArguments( [androidPackageRelLoc + F + 'Wayranging'],
          javacOutDir );
        if( javacSourceArgArray.length > 0 )
        {
          // Translate the Java source to Android form.
          // - - - - - - - - - - - - - - - - - - - - - -
            outS.append( Build.indentation() ).append( '(jtrans.. ' );
            var originalSourceRoot = Paths.get( waymakerLoc, 'waymaker' );
            var jtransSourceOutDir = Waymaker.ensureDir( tmpDir.resolve( 'jtransSourceOut' ));
            var translatedSourceRoot = jtransSourceOutDir.resolve( 'waymaker' );
            var count = 0;
            var translator = new (Java.extend( SimpleFileVisitor ))
            {
                assertionMatcher: Pattern.compile(
                  '((?:(?:(?:else\\s+)?if\\s*\\([^)]*\\)|else|.*[{;])\\s*)?)' // 1
                      //  ------------------------------ ---- ------
                      //                a                 b     c
                      //
                  + 'assert ([^;:]+)(?:: ([^;]+))?;(.*)' ).matcher( '' ),
                      //     ------       -----     --
                      //       2            3       4
                  //
                  // A matcher of an assert line.  Takes all characters from the first non-whitespace to
                  // the line terminator (exclusive) as input.  Matches the whole input (Matcher.match)
                  // if it contains a recognizable form of assert statement.  Assumes (caller assures)
                  // at most one assert statement, and the 'assert' is not part of a comment.
                  //
                  // The assert statement may lead the line.  Otherwise the leader must be either (a) an
                  // 'if' or 'else if' clause in a recognizable form; (b) an 'else' clause; or (c) any
                  // characters followed by a block opening '{' or semicolon ';'.  These restrictions
                  // help to avoid false matches in string literals and comments.
                  //
                  // A successful match forms groups (1) prior to the assert statement; (2) boolean
                  // expression; (3) string expression, or null if omitted; and (4) after the statement.

                bufferFile: tmpDir.resolve( 'jtransSourceOutBuffer' ),

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
                        var toFile = translatedSourceRoot.resolve( originalSourceRoot.relativize( inFile ));
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
                                    return inFile + '( ' + lineNumber +
                                      ' )\n  Unrecognized pattern of assert statement:\n' + line;
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
                        Files.move( this.bufferFile, toFile, REPLACE_EXISTING ); // now that it's fully written
                        ++count;
                    }
                    return CONTINUE;
                }
            };

            Files.walkFileTree( originalSourceRoot, translator );
            count = count.intValue(); // [IV]
            outS.append( '\b\b\b ' ).println( count );

          // Write the compilation arguments for javac.
          // - - - - - - - - - - - - - - - - - - - - - -
            outS.append( Build.indentation() ).append( '(javac.. ' );
            var javacArgFile = tmpDir.resolve( 'javacArg' );
            {
                var out = new PrintWriter( javacArgFile ); // truncates file if it exists
                out.append( '-classpath ' );
                out.append( javacOutDir.toString() );
                for each( var jar in compileTimeJarArray )
                {
                    out.append( P );
                    out.append( jar.toString() );
                }
                out.println();
                out.close();
            }

          // Write the source arguments for javac.
          // - - - - - - - - - - - - - - - - - - - -
            var javacSourceArgFile = tmpDir.resolve( 'javacSourceArg' );
            {
                var out = new PrintWriter( javacSourceArgFile ); // truncates file if it exists
                for each( var f in javacSourceArgArray )
                {
                    f = translatedSourceRoot.resolve( originalSourceRoot.relativize( f ));
                      // use the translated version of the source file
                    out.append( f.toString() ).println();
                }
                out.close();
            }

          // Compile the source code to Java bytecode (.class files).
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            var command = Build.javacTested()
              + ' -bootclasspath ' + Android.androidJarTested()
              + ' -d ' + javacOutDir
              + ' -encoding UTF-8' // of source; instead of platform default, whatever that might be
              + ' -source 1.7' // Java API version, cannot exceed -target
              + ' -sourcepath ' + jtransSourceOutDir + P + aaptSourceOutDir
              + ' -target 1.7' // JVM version, limited by Android to 1.7
              + ' -Werror' // terminate compilation when a warning occurs
              + ' -Xdoclint:all,-missing' /* verify all javadoc comments, but allow their omission;
                    changing?  change also in ../javadoc/Target.js */
              + ' -Xlint'
              + ' @' + javacArgFile
              + ' @' + javacSourceArgFile;
            var compileTime = Files.getLastModifiedTime( javacSourceArgFile );
            $EXEC( Waymaker.logCommand( command ));
            Waymaker.logCommandResult();
            if( $EXIT ) Waymaker.exit( $ERR );

            count = Build.countCompiled( javacOutDir, compileTime );
            outS.append( '\b\b\b ' ).println( count );
        }

      // 4. Translate the Java bytecode to Android form.
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        var dexFile = tmpDir.resolve( 'classes.dex' );
        var args =
            ' --dex'
          + ' --output=' + dexFile
          + ' --verbose';
        dx:
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
                if( cN == 0 ) break dx;

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

      // 5. Make the full APK.
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

      // 6. Optimize the data alignment of the APK.
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



}() );
    // still under this module's load guard at top
}


// Notes
// -----
//  [H] http://stackoverflow.com/a/29313378/2402790
//      http://reluk.ca/project/waymaker/spec/build/android/Hello_world_using_Android_SDK_alone.txt
//
//  [IV] Using explicit intValue conversion here in order to defeat previous ++ operator's
//      implicit conversion to double, if only for sake of pretty printing.


// Copyright 2015-2016, Michael Allan.  Licence MIT-Waymaker.
