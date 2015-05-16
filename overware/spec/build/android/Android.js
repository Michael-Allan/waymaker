/** Android.js - Utilities for building the Android parts of Overware
  **************
  */
if( !overware.spec.build.android.Android ) {
     overware.spec.build.android.Android = {};
load( overware.Overware.ulocTo( 'overware/spec/build/Build.js' ));
( function()
{
    var our = overware.spec.build.android.Android; // public as overware.spec.build.android.Android

    var Build = overware.spec.build.Build;
    var BuildConfig = overware.spec.build.BuildConfig;
    var Files = Java.type( 'java.nio.file.Files' );
    var Overware = overware.Overware;
    var Paths = Java.type( 'java.nio.file.Paths' );
    var Pattern = Java.type( 'java.util.regex.Pattern' );



//// P u b l i c /////////////////////////////////////////////////////////////////////////


    /** Applied once (Matcher.find) to the verbose output of Android asset packaging tool
      * 'aapt package', this pattern captures a single group: 1) the count of files added
      * to the package.
      */
    our.AAPT_PACKAGE_COUNT_PATTERN = Pattern.compile( '^Generated (\\d+) file', Pattern.MULTILINE );



    /** Returns the command for Android build tool 'aapt', first smoke testing it if
      * configuration variable 'androidBuildToolsLoc' is yet untested.
      *
      *     @return String
      */
    our.aaptTested = function() { return androidBuildToolTested( 'aapt', 'version' ); };



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



    /** The jars required at compile time.
      *
      *     @return JS Array of java.nio.file.Path
      */
    our.compileTimeJarArray = function()
    {
        if( !compileTimeJarArray ) compileTimeJarArray = [ support4JarTested() ];
        return compileTimeJarArray;
    };


        var compileTimeJarArray;



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
    };



    /** Returns the path to the Android 'sdklib.jar', first testing that it exists.
      *
      *     @return java.nio.file.Path
      */
    our.sdkLibJarTested = function()
    {
        var jar = sdkLibJar;
        if( !jar )
        {
            jar = Paths.get( BuildConfig.androidSDKLoc, 'tools', 'lib', 'sdklib.jar' );
            if( !Files.exists( jar ))
            {
                Overware.exit( L + 'Missing SDK file: ' + jar + L
                  + 'Does your BuildConfig.js correctly set androidSDKLoc?' );
            }
            sdkLibJar = jar; // cache
        }
        return jar;
    };


        var sdkLibJar;



    /** Returns the command for Android build tool 'zipalign', first smoke testing it if
      * configuration variable 'androidBuildToolsLoc' is yet untested.
      *
      *     @return String
      */
    our.zipalignTested = function() { return androidBuildToolTested( 'zipalign' ); };



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    /** Returns the command for the named Android build tool, first smoke testing it if
      * configuration variable 'androidBuildToolsLoc' is yet untested.
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
        var testedSet = Build.testedSet();
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



    /** Returns the path to the 'android-support-v4.jar', first testing that it exists.
      *
      *     @return java.nio.file.Path
      *     @see http://developer.android.com/tools/support-library/setup.html
      */
    function support4JarTested()
    {
        var jar = support4Jar;
        if( !jar )
        {
            jar = Paths.get( BuildConfig.androidSDKLoc, 'extras', 'android', 'support', 'v4',
              'android-support-v4.jar' );
            if( !Files.exists( jar ))
            {
                Overware.exit( L + 'Missing SDK file: ' + jar + L
                  + 'Does your BuildConfig.js correctly set androidSDKLoc?' );
            }
            support4Jar = jar; // cache
        }
        return jar;
    }


        var support4Jar;



}() );
    // still under this module's load guard at top
}
