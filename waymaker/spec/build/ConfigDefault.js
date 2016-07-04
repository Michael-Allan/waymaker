/** ConfigDefault.js - Configuration defaults for the Waymaker build
  ********************
  * Do not edit this file in an attempt to personalize your build.  It assigns default values to all
  * configuration variables.  Instead edit your own configuration file as instructed in Config.js.
  */
var waymaker = arguments[0];

    var c = waymaker.spec.build.Config;
    var version = '0.0';



 // androidAssertTranslation
 // ------------------------
 // The translation to apply to Java assert statements before compiling the source code.
 //
 //     empty  Translate each assert to an empty statement, so disabling assertions.
 //     if     Translate each assert to an 'if' statement, so enabling assertions.
 //
 // This is a workaround for a deficiency in the Android runtime: it gives no reliable support for
 // assertions.  http://stackoverflow.com/a/34027607/2402790

    c.androidAssertTranslation = 'if';



 // androidBuildToolsLoc
 // ---------------------
 // The pathname of the build-tools directory of the Android SDK, where tools such as aapt
 // and dx reside.  For example:
 //
 //     c.androidBuildToolsLoc = c.androidSDKLoc + '/build-tools/22.0.1';
 //
 // If set to an empty string '', the tools will instead be sought on the execution PATH.

    c.androidBuildToolsLoc = '';



 // androidSDKLoc
 // --------------
 // The pathname of the Android SDK installation directory.

    var v = $ENV.ANDROID_HOME; // default to environment variable ANDROID_HOME
    if( !v )
    {
        var Waymaker = waymaker.Waymaker;
        var osTag = Waymaker.osTag();
        if( osTag == 'mac' ) v = '/usr/local/Cellar/android-sdk'; // Homebrew installation
        else if( osTag == 'win' )
        {
            v = Waymaker.slashed(Waymaker.userHomeLoc()) + 'AppData\\Local\\Android\\android-sdk';
              // default for the Windows installer
        }
        else v = '/opt/android-sdk'; // Arch Linux and (if I recall) Gentoo
    }
    c.androidSDKLoc = v;



 // androidVersion
 // ---------------
 // The numerical version of the Android API to build against.  It should normally be the latest
 // installed by the developer, e.g. using the SDK manager.  Look for this installation directory:
 //
 //     {androidSDKLoc} / platforms / android-{androidVersion}

    c.androidVersion = 24; // changing?  change also in top/android/AndroidManifest.xml



 // appPackageName
 // --------------
 // The unique name under which you intend to publish your build of the Android app.  The
 // build script will write this name into the 'package' attribute of the app manifest.
 // http://developer.android.com/guide/topics/manifest/manifest-element.html#package
 //
 // Although it looks like a Java-style package name, this name need not (and should not)
 // match the Java source package "waymaker.top.android".  Instead read the instructions
 // linked above, and set a unique name here before publishing your build.

    c.appPackageName = 'com.example.waymaker';



 // jdkBinLoc
 // ----------
 // The pathname of the Oracle JDK bin directory, where tools such as javac reside.  If
 // this is set to an empty string '', then the tools are sought on the execution PATH.
 //
 // Some underlying build tools such as Android dx will always seek the Java virtual
 // machine launcher ('java') on the execution PATH regardless of the value of jdkBinLoc.
 // It is therefore prudent when setting jdkBinLoc to also execute 'java -version' or
 // 'which java' from the command line in order to test that the launcher is on the PATH.
 // Its version need not always be identical to the version at jdkBinLoc.
 //
 // The pathname may contain a space, as for example 'C:\\Program Files\\Java\\bin'.
 // Beware however that mistyping a spaced pathname ('C:\\Program Files\\Typo\\bin') may
 // result in a misleading error report:
 //
 //     Cannot run program "C:\Program": CreateProcess error=2,
 //     The system cannot find the file specified
 //
 // This does not mean a problem with the space character.  Instead it means:
 //
 //     Cannot run program "C:\Program Files\Typo\bin": CreateProcess error=2,
 //     The system cannot find the file specified

    c.jdkBinLoc = '';



 // jdkVersion
 // ----------
 // The required minimum version of the JDK.

    c.jdkVersion = 1.8;



 // productLoc
 // ----------
 // The output directory for the build process.  Target "clean" deletes this directory
 // together with its contents, while others recreate it as needed.

    c.productLoc = 'waymaker-' + version;



 // sourceMatcher
 // -------------
 // Returns true if the file is a proper Waymaker source file.  Use this as necessary to
 // screen the build process from personal files added to the Waymaker installation.

    c.sourceMatcher = new (Java.type('java.nio.file.PathMatcher'))(
    {
        matches: function( path ) { return true; } // by default, no filtering is needed
    });



 // version
 // -------
 // The version of Waymaker.

    c.version = version;
