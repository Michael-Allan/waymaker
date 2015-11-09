/** Target.js - Definition of the 'android' build target
  *************
  * Builds the Android user interface.
  *
  *     $ waymaker/build -- android
  *
  * The product is an APK file (app.apk) ready to install and run on an Android device.  The relevant
  * commands, assuming default configuration for productLoc (waymaker-0.0) and appPackageName
  * (com.example.waymaker), are:
  *
  *     $ adb install -r waymaker-0.0/app.apk
  *     $ adb shell am start -n com.example.waymaker/waymaker.top.android.Wayranging
  *
  * To uninstall it:
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
    var BuildConfig = waymaker.spec.build.BuildConfig;
    var Files = Java.type( 'java.nio.file.Files' );
    var Waymaker = waymaker.Waymaker;
    var Paths = Java.type( 'java.nio.file.Paths' );
    var PrintWriter = Java.type( 'java.io.PrintWriter' );
    var System = Java.type( 'java.lang.System' );



//// P u b l i c ///////////////////////////////////////////////////////////////////////////////////////


    /** Builds this target.
      */
    our.build = function() // based on [H], q.v. for additional comments
    {
        var outS = System.out;
        var compileTimeJarArray = Android.compileTimeJarArray();

      // Compile the source code to Java bytecode (.class files).
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        var tmpDir = Paths.get( Build.tmpLoc(), 'android' );
        var javacOutDir = Waymaker.ensureDir( tmpDir.resolve( 'javacOut' ));
        var javacArgInFile = tmpDir.resolve( 'javacArgIn' );
        {
            Files.deleteIfExists( javacArgInFile );
            var out = new PrintWriter( javacArgInFile );
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

        var F = Waymaker.F;
        var relInLoc = 'waymaker' + F + 'top' + F + 'android';
        var javaInFile = tmpDir.resolve( 'javaIn' );
        Build.writeSourceArgs( [relInLoc + F + 'Wayranging'], javaInFile, javacOutDir );

        if( Files.exists( javaInFile ))
        {
            outS.append( Build.indentation() ).append( '(javac.. ' );
            var command = Build.javacTested()
              + ' -bootclasspath ' + Android.androidJarTested()
              + ' -d ' + javacOutDir
              + ' -encoding UTF-8' // of source; instead of platform default, whatever that might be
              + ' -source 1.7' // Java API version, cannot exceed -target
              + ' -sourcepath ' + Waymaker.loc()
              + ' -target 1.7' // JVM version, currently limited by Android build tools to 1.7
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

            var count = Build.countCompiled( javacOutDir, compileTime );
            outS.append( '\b\b\b ' ).println( count );
        }

      // Translate the output from Java to Android bytecode.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
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
                Files.deleteIfExists( classesInFile );
                var out = new PrintWriter( classesInFile );
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
                    Files.deleteIfExists( dxInFile );
                    var out = new PrintWriter( dxInFile );
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

      // Package up the resource files alone, making a partial APK.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        var manifestTemplate = Paths.get( Waymaker.loc(), relInLoc, 'AndroidManifest.xml' );
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
                              BuildConfig.appPackageName ));
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

      // Make the full APK.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
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

      // Optimize the data alignment of the APK.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        var apkAlignedFile = Waymaker.ensureDir(Paths.get(BuildConfig.productLoc))
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
//
//  [IV] Using explicit intValue conversion here in order to defeat previous ++ operator's
//      implicit conversion to double, for sake of pretty printing.


// Copyright 2015, Michael Allan.  Licence MIT-Waymaker.
