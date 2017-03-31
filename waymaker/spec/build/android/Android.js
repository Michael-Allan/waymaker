/** Android.js - Utilities for building the Android parts of Waymaker
  **************
  */
if( !waymaker.spec.build.android.Android ) {
     waymaker.spec.build.android.Android = {};
load( waymaker.Waymaker.ulocTo( 'waymaker/spec/build/Build.js' ));
( function()
{
    var our = waymaker.spec.build.android.Android; // public as waymaker.spec.build.android.Android

    var Build = waymaker.spec.build.Build;
    var Config = waymaker.spec.build.Config;
    var Files = Java.type( 'java.nio.file.Files' );
    var Waymaker = waymaker.Waymaker;
    var Paths = Java.type( 'java.nio.file.Paths' );
    var Pattern = Java.type( 'java.util.regex.Pattern' );
    var QName = Java.type( 'javax.xml.namespace.QName' );

    var F = Waymaker.F;
    var L = Waymaker.L;
    var REPLACE_EXISTING = Java.type( 'java.nio.file.StandardCopyOption' ).REPLACE_EXISTING;



//// P u b l i c ///////////////////////////////////////////////////////////////////////////////////////


    /** Applied once (Matcher.find) to the verbose output of Android asset packaging tool 'aapt
      * package', this pattern captures a single group: 1) the count of files added to the package.
      */
    our.AAPT_PACKAGE_COUNT_PATTERN = Pattern.compile( '^Generated (\\d+) file', Pattern.MULTILINE );



    /** The name of the tmpLoc subdirectory for files that are ready to package as plain runtime assets.
      * The Android asset packaging tool (aapt) describes it under option -A as an “additional directory
      * in which to find raw asset files”.  The Android guide recommends to use it when “you need access
      * to original file names and file hierarchy...  Files in assets/ are not given a resource ID, so
      * you can read them only using [the basic] AssetManager.”  They are not ‘resources’.
      *
      *     @return (String)
      *     @see http://developer.android.com/guide/topics/resources/providing-resources.html
      */
    our.aaptInRelLoc_assets = function() { return 'aaptIn' + F + 'assets'; };



    /** The name of the tmpLoc subdirectory for files that are ready to package as runtime ‘resources’.
      * Its deeper subdirectories are defined by the Android guide.
      *
      *     @return (String)
      *     @see http://developer.android.com/guide/topics/resources/providing-resources.html
      */
    our.aaptInRelLoc_res = function() { return 'aaptIn' + F + 'res'; };



    /** Returns the command for Android build tool 'aapt', first smoke testing it if configuration
      * variable 'androidBuildToolsLoc' is yet untested.
      *
      *     @return (String)
      */
    our.aaptTested = function() { return androidBuildToolTested( 'aapt', 'version' ); };



    /** Returns the path to the Android bootclass jar, first testing that it exists.
      *
      *     @return (java.nio.file.Path)
      */
    our.androidJarTested = function()
    {
        var jar = androidJar;
        if( !jar )
        {
            jar = Paths.get( Config.androidSDKLoc, 'platforms',
              'android-' + Config.androidVersion, 'android.jar' );
            if( !Files.exists( jar ))
            {
                Waymaker.exit( L + 'Missing SDK file: ' + jar + L
                  + 'Does your Config.js correctly set androidSDKLoc and androidVersion?' );
            }
            androidJar = jar; // cache
        }
        return jar;
    };


        var androidJar;



    /** The jars required at both compile time and runtime.
      *
      *     @return (JS Array of java.nio.file.Path)
      */
    our.compileTimeJarArray = function()
    {
     // if( !compileTimeJarArray ) compileTimeJarArray = [ support4JarTested() ];
        if( !compileTimeJarArray ) compileTimeJarArray = [];
        return compileTimeJarArray;
    };


        var compileTimeJarArray;



    /** Returns the path to bytecode translator "jack.jar", first testing that it exists.
      *
      *     @return (java.nio.file.Path)
      */
    our.jackJarTested = function()
    {
        var jar = jackJar;
        if( !jar )
        {
            jar = Paths.get( Config.androidBuildToolsLoc, 'jack.jar' );
            if( !Files.exists( jar ))
            {
                Waymaker.exit( L + 'Missing SDK file: ' + jar + L
                  + 'Does your Config.js correctly set androidBuildToolsLoc?' );
            }
            jackJar = jar; // cache
        }
        return jar;
    };


        var jackJar;



    /** Returns the path to bytecode translator "jill.jar", first testing that it exists.
      *
      *     @return (java.nio.file.Path)
      */
    our.jillJarTested = function()
    {
        var jar = jillJar;
        if( !jar )
        {
            jar = Paths.get( Config.androidBuildToolsLoc, 'jill.jar' );
            if( !Files.exists( jar ))
            {
                Waymaker.exit( L + 'Missing SDK file: ' + jar + L
                  + 'Does your Config.js correctly set androidBuildToolsLoc?' );
            }
            jillJar = jar; // cache
        }
        return jar;
    };


        var jillJar;



    /** Ensures that the image source in fab.file is transformed from SVG to VectorDrawable and readied
      * for packaging as a runtime resource.  File waymaker/top/android/Fubar_bigIcon.svg, for example,
      * would be stored as res/drawable/top_android_fubar_bigicon.xml.
      *
      * This function assumes that any Inkscape source was saved "optimized".  The save options below
      * were tested.  Those marked ! must be specified as shown:
      *
      *     ( )  Shorten color values
      *            No, it omits default values (black) and the default apparently differs between SVG
      *            and VectorDrawable.
      *     (*)  Convert CSS attributes to XML attributes
      *            Yes, use separate XML attributes instead of one composite CSS 'style' attribute.
      *     (*)  Group collapsing
      *     ( )  Create groups for separate attributes
      *            No, it might move a path attribute to a group, which VectorDrawable doesn't allow.
      *     ( )  Embed ratsters
      *     ( )  Keep editor data
      *     (*)  Remove metadata
      *     ( )  Remove comments
      *     ( )  Work around editor bugs
      *     ( )  Enable viewboxing
      *     ( )  Remove the xml declaration
      *
      *     (*)  Remove unused ID names for elements
      *     ( )  Shorten IDs
      *     ( )  Preserve manually created ID names not ending in digits
      *
      * To inspect and debug the immediate transformed output, read its XML.  For example:
      *
      *     <tmp>/waymaker/build/android/aaptIn/res/drawable/top_android_fubar_bigicon.xml
      *
      * To inspect and debug the ultimate output as compiled and packaged, use the "aapt dump" tool:
      *
      *     <androidBuildToolsLoc>/aapt dump xmltree <productLoc>/app.apk res/drawable/top_android_fubar_bigicon.xml
      *
      *     @param fab (Build.fab) The caller.
      *     @return (boolean) Whether any action was taken.  True if the file was readied, false if it
      *       was ready to begin with.
      *
      *     @see http://developer.android.com/reference/android/graphics/drawable/VectorDrawable.html
      *     @see https://androidbycode.wordpress.com/2015/02/27/vector-graphics-in-android-part-1-svg/
      *     @see https://androidbycode.wordpress.com/2015/03/18/vector-graphics-in-android-converting-svg-to-vectordrawable/
      */
    our.readySVG_toVectorDrawable = function( fab )
    {
        // If the transformed source were instead packaged as a plain asset (XML text), then
        // VectorDrawable would fail to read it (API level 23).  Take this code for example:
        //
        //         VectorDrawable drawable = new VectorDrawable();
        //         try( InputStream in = context.getAssets().open( "top/android/Fubar_bigIcon.xml" ); )
        //         {
        //             XmlPullParser p = Xml.newPullParser();
        //             p.setInput( in, /*encoding, self detect*/null );
        //             drawable.inflate( context.getResources(), p, Xml.asAttributeSet(p) );
        //         }
        //
        // The inflate call attempts to cast the attribute set to an XmlBlock.Parser, which fails and
        // throws a ClassCastException.  The cause is documented in the source:
        // https://android.googlesource.com/platform/frameworks/base.git/+/android-6.0.0_r1/core/java/android/content/res/Resources.java#1593
        // It will "only work with compiled XML files" such as resource files packaged by the aapt tool.
        // (posted to http://stackoverflow.com/a/35464667/2402790)
        //
        // Further the compiled XML of a proper resource is probably more efficient at runtime.

        var sourceRoot = Paths.get( Waymaker.loc(), 'waymaker' );
        var sourceFile = Paths.get( fab.fileLoc() );
        var tmpDir = Waymaker.ensureDir( Paths.get( tmpLoc ));
        var targetFile = tmpDir.resolve( our.aaptInRelLoc_res() + F + 'drawable' + F
          + Waymaker.suffixSiblingV(sourceRoot.relativize(sourceFile),'.svg','.xml')
              .toString().replaceAll(F,'_').toLowerCase() ); // aapt rejects anything but [a-z0-9_.]
        if( Files.exists(targetFile)
          && Files.getLastModifiedTime(sourceFile).compareTo(
             Files.getLastModifiedTime(targetFile)) < 0 ) return false; // source unchanged

        var bufferFile = tmpDir.resolve( 'vectorDrawableBuffer.xml' );
        var inX = Build.xmlInputFactory().createXMLEventReader(
          new (Java.type('java.io.BufferedInputStream'))(
            new (Java.type('java.io.FileInputStream'))( sourceFile )));
        var out = Files.newBufferedWriter( bufferFile );
        try
        {
            var factory = Build.xmlEventFactory();
            while( inX.hasNext() )
            {
                var xml = inX.next();
                if( !xml.isStartElement() || xml.getName().getLocalPart() != 'svg' ) continue;

                var parent;
                var parents = []; // SVG
                var _parents = []; // VectorDrawable
                function indentLine()
                {
                    out.newLine();
                    for( var p = parents.length; p > 0; --p ) out.append( '\t' );
                }

              // svg → vector
              // - - - - - - -
                out.append( '<vector xmlns:a="http://schemas.android.com/apk/res/android"' );
                parents.push( parent = 'svg' );
                _parents.push( 'vector' );
                var atts;
                var att;
                var name;
                atts = xml.getAttributes();
                while( atts.hasNext() )
                {
                    att = atts.next();
                    name = att.getName().getLocalPart();
                    if( name == 'height' ) out.append( ' a:height="' ).append( att.getValue() ).append( '"' );
                    else if( name == 'width' )
                    {
                        out.append( ' a:width="' ).append( att.getValue() ).append( '"' );
                    }
                    else if( name == 'viewBox' )
                    {
                      // svg[viewBox] → vector[viewportWidth, viewportHeight]
                      // - - - - - - - - - - - - - - - - - - - - - - - - - - -
                        var b = att.getValue().split( ' ' );
                        if( /*min-x*/b[0] != '0' || /*min-y*/b[1] != '0' )
                        {
                            throw "Cannot transform, viewBox displaced from origin: " + sourceFile;
                        }

                        out.append( ' a:viewportWidth="' ).append( b[2] ).append( '"' )
                           .append( ' a:viewportHeight="' ).append( b[3] ).append( '"' );
                    }
                }
                out.append( '>' );
                while( inX.hasNext() )
                {
                    xml = inX.next();
                    if( xml.isStartElement() )
                    {
                        if( xml.getName().getLocalPart() != 'path' ) continue;

                      // path
                      // - - -
                        indentLine();
                        out.append( '<path' );
                        atts = xml.getAttributes();
                        while( atts.hasNext() )
                        {
                            att = atts.next();
                            name = att.getName().getLocalPart();
                            if( name == 'd' )
                            {
                              // path[d → pathData]
                              // - - - - - - - - - -
                                out.append( ' a:pathData="' ).append( att.getValue() ).append( '"' );
                            }
                            else if( name == 'fill' )
                            {
                              // path[fill → fillColor]
                              // - - - - - - - - - - - -
                                out.append( ' a:fillColor="' ).append( att.getValue() ).append( '"' );
                            }
                        }
                        out.append( '/>' );
                    }
                    else if( xml.isEndElement() )
                    {
                        name = xml.getName().getLocalPart();
                        if( name == parent )
                        {
                            indentLine();
                            out.append( '</' ).append( _parents.pop() ).append( '>' );
                        }
                    }
                }
            }
        }
        finally
        {
            out.close();
            inX.close();
        }
        Waymaker.ensureDir( targetFile.getParent() );
        Files.move( bufferFile, targetFile, REPLACE_EXISTING ); // now that it's fully written
        return true;
    };



    /** Returns the path to the Android 'sdklib.jar', first testing that it exists.
      *
      *     @return (java.nio.file.Path)
      */
    our.sdkLibJarTested = function()
    {
        var jar = sdkLibJar;
        if( !jar )
        {
            jar = Paths.get( Config.androidSDKLoc, 'tools', 'lib', 'sdklib.jar' );
            if( !Files.exists( jar ))
            {
                Waymaker.exit( L + 'Missing SDK file: ' + jar + L
                  + 'Does your Config.js correctly set androidSDKLoc?' );
            }
            sdkLibJar = jar; // cache
        }
        return jar;
    };


        var sdkLibJar;



    /** The directory for expendable, intermediate output from the Android build process, a subdirectory
      * of Build.tmpLoc.
      *
      *     @return (String)
      */
    our.tmpLoc = function() { return tmpLoc; };


        var tmpLoc = Waymaker.slashed(Build.tmpLoc()) + 'android';



    /** Returns the command for Android build tool 'zipalign', first smoke testing it if configuration
      * variable 'androidBuildToolsLoc' is yet untested.
      *
      *     @return (String)
      */
    our.zipalignTested = function() { return androidBuildToolTested( 'zipalign' ); };



//// P r i v a t e /////////////////////////////////////////////////////////////////////////////////////


    /** Returns the command for the named Android build tool, first smoke testing it if configuration
      * variable 'androidBuildToolsLoc' is yet untested.
      *
      *     @param name (String) The name of the command.
      *     @param arg (String) Arguments to pass to it for testing, or null to pass no arguments.
      *
      *     @return (String)
      */
    function androidBuildToolTested( name, arg )
    {
        var command = Waymaker.slashed(Config.androidBuildToolsLoc) + name;
        var testedSet = Build.testedSet();
        if( !testedSet.contains( 'androidBuildToolsLoc' ))
        {
            var testCommand = command;
            if( arg ) testCommand += ' ' + arg;
            try { $EXEC( Waymaker.logCommand( testCommand )); }
            catch( x )
            {
                Waymaker.exit( L + x + L + 'Does your Config.js correctly set androidBuildToolsLoc?' );
            }
            Waymaker.logCommandResult();
            testedSet.add( 'androidBuildToolsLoc' );
        }
        return command;
    }



 // /** Returns the path to the 'android-support-v4.jar', first testing that it exists.
 //   *
 //   *     @return (java.nio.file.Path)
 //   *     @see http://developer.android.com/tools/support-library/setup.html
 //   */
 // function support4JarTested()
 // {
 //     var jar = support4Jar;
 //     if( !jar )
 //     {
 //         jar = Paths.get( Config.androidSDKLoc, 'extras', 'android', 'support', 'v4',
 //           'android-support-v4.jar' );
 //         if( !Files.exists( jar ))
 //         {
 //             Waymaker.exit( L + 'Missing SDK file: ' + jar + L
 //               + 'Does your Config.js correctly set androidSDKLoc?' );
 //         }
 //         support4Jar = jar; // cache
 //     }
 //     return jar;
 // }
 //
 //
 //     var support4Jar;



}() );
    // still under this module's load guard at top
}


// Copyright © 2015 Michael Allan.  Licence MIT.
