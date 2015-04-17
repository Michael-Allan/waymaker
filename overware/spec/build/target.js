/** target.js - Definition of build targets
  *************
  */
if( !ov.build.target ) { ( function()
{
    var our = ov.build.target = {}; // public, our.NAME accessible as ov.build.target.NAME

    var FileVisitResult = Java.type( 'java.nio.file.FileVisitResult' );
    var Files = Java.type( 'java.nio.file.Files' );
    var Paths = Java.type( 'java.nio.file.Paths' );
    var SimpleFileVisitor = Java.type( 'java.nio.file.SimpleFileVisitor' );
    var System = Java.type( 'java.lang.System' );

    var CONTINUE = FileVisitResult.CONTINUE;



//// P u b l i c /////////////////////////////////////////////////////////////////////////


    /** Builds the Android user interface.
      *
      *     $ overware/build -- android
      *
      * The product is an APK file (app.apk) ready to install and run on an Android
      * device.  The relevant commands, assuming default configuration for productLoc
      * (overware-0.0) and appPackageName (com.example.overware), are:
      *
      *     $ adb install -r overware-0.0/app.apk
      *     $ adb shell am start -n com.example.overware/overware.top.android.Overguidance
      *
      * To uninstall:
      *
      *     $ adb uninstall com.example.overware
      */
    our.android = function() // based on [1], q.v. for additional comments
    {
        var outS = System.out;
        var build = ov.build;

      // Compile the source code to Java bytecode (.class files).
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        var tmpDir = Paths.get( build.tmpLoc(), 'android' );
        var javacOutDir = ov.ensureDir( tmpDir.resolve( 'javacOut' ));
        var javaInFile = tmpDir.resolve( 'javaIn' );
        var relInLoc = 'overware' + ov.F + 'top' + ov.F + 'android';
        build.writeSourceArgs( [relInLoc + ov.F + 'Overguidance'], javaInFile, javacOutDir );
        var bc = build.config;
        if( Files.exists( javaInFile ))
        {
            outS.append( build.indentation() ).append( '(javac.. ' );
            var command = build.javacTested()
              + ' -bootclasspath ' + build.androidJarTested()
              + ' -classpath ' + javacOutDir
              + ' -d ' + javacOutDir
              + ' -source 1.7' // Java API version, cannot exceed -target
              + ' -sourcepath ' + ov.loc()
              + ' -target 1.7' // JVM version, currently limited by Android build tools to 1.7
              + ' -Werror' // terminate compilation when a warning occurs
              + ' -Xdoclint:all,-missing' // verify all javadocs, but allow their omission
              + ' -Xlint'
              + ' @' + javaInFile;
            var compileTime = Files.getLastModifiedTime( javaInFile );
            $EXEC( ov.logCommand( command ));
            ov.logCommandResult();
            if( $EXIT ) ov.exit( $ERR );

            var count = build.countCompiled( javacOutDir, compileTime );
            outS.append( '\b\b\b ' ).println( count );
        }

      // Translate the output from Java to Android bytecode.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        var classesInFile = tmpDir.resolve( 'classesIn' );
        Files.deleteIfExists( classesInFile );
        count = ' '; // skipped, till proven otherwise
        var dexFile = tmpDir.resolve( 'classes.dex' );
        var args =
            ' --dex'
          + ' --output=' + dexFile
          + ' --verbose';
        dex:
        {
            if( Files.exists( dexFile ))
            {
                var msCompileTime = Files.getLastModifiedTime( dexFile ).toMillis();
                msCompileTime += 1; /* Compilation will have occured a considerable time
                  after previous dexFile was created, so it's safe to add something here.
                  Adding 1 ms suffices to prevent re-translating class files that were
                  compiled just before (almost simultaneous with) the last translation. */
                var classesInArray = build.arrayCompiled( javacOutDir, msCompileTime );
                var cN = classesInArray.length;
                if( cN == 0 ) break dex;

                outS.append( build.indentation() ).append( '(dx.. ' );
                var out = new (Java.type('java.io.PrintWriter'))( classesInFile );
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
                    var command = build.dxTested() + args
                      + ' --incremental'
                      + ' --input-list=' + classesInFile; // translate newly compiled classes
                    $EXEC( ov.logCommand( command ));
                }
                finally{ $ENV.PWD = dir; } // restore working directory
            }
            else
            {
                outS.append( build.indentation() ).append( '(dx.. ' );
                var command = build.dxTested() + args + ' ' + javacOutDir; // translate all classes
                $EXEC( ov.logCommand( command ));
            }
            ov.logCommandResult();
            if( $EXIT ) ov.exit( $ERR );

            var count = 0;
            var m = build.DEXED_TOP_CLASS_PATTERN.matcher( $OUT );
            while( m.find() ) ++count;
            count = count.intValue(); // [2]
            outS.append( '\b\b\b ' ).println( count );
        }

      // Package up the resource files alone, making a partial APK.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        var manifestTemplate = Paths.get( ov.loc(), relInLoc, 'AndroidManifest.xml' );
        var apkPartFile = tmpDir.resolve( 'app.apkPart' );
        if( !Files.exists( apkPartFile )
          || Files.getLastModifiedTime( apkPartFile ).compareTo(
             Files.getLastModifiedTime( manifestTemplate )) < 0 ) // then do package it
        {
            outS.append( build.indentation() ).append( '(aapt.. ' );

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
                            var attList = new (Java.type('java.util.ArrayList'))();
                            var aa = xml.getAttributes();
                            while( aa.hasNext() ) attList.add( aa.next() );
                            attList.add( xmlEventFactory.createAttribute( 'package',
                              bc.appPackageName ));
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
            var command = build.aaptTested()
              + ' package'
              + ' -f'
              + ' -F ' + apkPartFile
              + ' -I ' + build.androidJarTested()
              + ' -M ' + manifestFile
           // + ' -S ' + resDir;
           /// but no resources are needed yet, aside from the mandatory manifest
              + ' -v';
            $EXEC( ov.logCommand( command ));
            ov.logCommandResult();
            if( $EXIT ) ov.exit( $ERR );

            var m = build.AAPT_PACKAGE_COUNT_PATTERN.matcher( $OUT );
            var count = m.find()? m.group( 1 ): '?';
            outS.append( '\b\b\b ' ).println( count );
        }

      // Make the full APK.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
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
            outS.append( build.indentation() ).append( '(apk.. ' );
            var command = build.javaTested()
              + ' -classpath ' + build.androidSDKLibJarTested()
              + ' com.android.sdklib.build.ApkBuilderMain' // deprecated as explained in [1]
              + ' ' + apkFullFile
              + ' -d'
              + ' -f ' + dexFile
              + ' -v'
              + ' -z ' + apkPartFile;
            $EXEC( ov.logCommand( command ));
            ov.logCommandResult();
            if( $EXIT ) ov.exit( $ERR );

            outS.append( '\b\b\b ' ).println( ' ' ); // done
        }

      // Optimize the data alignment of the APK.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        var apkAlignedFile = ov.ensureDir(Paths.get(bc.productLoc)).resolve( 'app.apk' );
        if( !Files.exists( apkAlignedFile )
          || Files.getLastModifiedTime( apkAlignedFile ).compareTo(
             Files.getLastModifiedTime( apkFullFile )) < 0 ) // then do align it
        {
            outS.append( build.indentation() ).append( '(zipalign.. ' );
            var command = build.zipalignTested()
              + ' -f'
              + ' -v'
              + ' 4'
              + ' ' + apkFullFile
              + ' ' + apkAlignedFile;
            $EXEC( ov.logCommand( command ));
            ov.logCommandResult();
            if( $EXIT ) ov.exit( $ERR );

            outS.append( '\b\b\b ' ).println( ' ' ); // done
        }
    };



    /** Deletes all files generated by previous builds.
      *
      *     $ overware/build -- clean
      *     $ overware/build -- clean TARGET...
      */
    our.clean = function()
    {
        var build = ov.build;
        var outS = System.out;
        var locs = { tmp: build.tmpLoc(), release: build.config.productLoc };
        for( var l in locs )
        {
            var dir = Paths.get( locs[l] );
            if( !Files.exists( dir )) continue;

            outS.append( build.indentation() ).append( '(' ).append( l ).append( '.. ' );
            var count = 0;
            Files.walkFileTree( dir, new (Java.extend( SimpleFileVisitor ))
            {
                postVisitDirectory: function( dir, x )
                {
                    if( x ) throw x;

                    Files.delete( dir );
                    ++count;
                    return CONTINUE;
                },

                visitFile: function( file, att )
                {
                    Files.delete( file );
                    ++count;
                    return CONTINUE;
                }
            });
            count = count.intValue(); // [2]
            outS.append( '\b\b\b ' ).println( count );
        }
    };



    /** Includes a copy of the source directory "overware".
      *
      *     $ overware/build -- source
      *
      * You can filter the copy by defining sourceMatcher in your buildConfig.js.
      */
    our.source = function()
    {
        var build = ov.build;
        var outS = System.out;
        outS.append( build.indentation() ).append( '(' ).append( '.. ' );
        var bc = build.config;
        var sourceMatcher = bc.sourceMatcher;
        var fromRoot = Paths.get( ov.loc(), 'overware' );
        var toRoot = ov.ensureDir(Paths.get(bc.productLoc)).resolve( 'overware' );
        var StandardCopyOption = Java.type( 'java.nio.file.StandardCopyOption' );
        var COPY_ATTRIBUTES = StandardCopyOption.COPY_ATTRIBUTES;
        var count = 0;
        Files.walkFileTree( fromRoot, new (Java.extend( SimpleFileVisitor ))
        {
            preVisitDirectory: function( fromDir, fromAtt )
            {
                if( !sourceMatcher.matches( fromDir )) return FileVisitResult.SKIP_SUBTREE;

                var toDir = toRoot.resolve( fromRoot.relativize( fromDir ));
                if( !Files.isDirectory( toDir )) // assume uniprocess for atomic test/act
                {
                    Files.copy( fromDir, toDir, COPY_ATTRIBUTES );
                    ++count;
                }
                return CONTINUE;
            },

            visitFile: function( fromFile, fromAtt )
            {
                if( sourceMatcher.matches( fromFile ))
                {
                    var toFile = toRoot.resolve( fromRoot.relativize( fromFile ));
                    if( !Files.isRegularFile(toFile) ||
                      fromAtt.lastModifiedTime().compareTo(Files.getLastModifiedTime(toFile)) > 0 )
                    {
                        Files.copy( fromFile, toFile, COPY_ATTRIBUTES,
                          StandardCopyOption.REPLACE_EXISTING );
                        ++count;
                    }
                }
                return CONTINUE;
            }
        });
        count = count.intValue(); // [2]
        outS.append( '\b\b\b ' ).println( count );
    };



    /** Builds a full release of the Overware software.  This is the default target.
      *
      *     $ overware/build
      *     $ overware/build -- release # the same thing
      */
    our.release = function()
    {
        var b = ov.build;
        b.indentAndBuild( 'source' );
        b.indentAndBuild( 'android' );
    };



}() );
    // still under load guard at top
    load( ov.ulocTo( 'overware/spec/build/build.js' ));
}


// Notes
// -----
//   [1] http://stackoverflow.com/a/29313378/2402790
//
//   [2] Using explicit intValue conversion here in order to defeat previous ++ operator's
//       implicit conversion to double, for sake of pretty printing.


// Copyright 2015, Michael Allan.  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Overware Software"), to deal in the Overware Software without restriction, subject to the following conditions: The preceding copyright notice and this permission notice shall be included in all copies or substantial portions of the Overware Software.  The Overware Software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement.  In no event shall the authors or copyright holders be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the Overware Software or the use or other dealings in the Overware Software.
