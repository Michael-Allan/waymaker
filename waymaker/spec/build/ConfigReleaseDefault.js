/** ConfigReleaseDefault.js - Configuration defaults for the release variant of the build
  ***************************
  * This file defines the ‘Release’ variant of the build configuration, which is activated by supplying
  * a ‘ConfigRelease’ argument to the build command.  It changes the configuration variables to values
  * suitable for building a public release.
  *
  * Do not edit this file in an attempt to personalize your release builds.  Although personal variants
  * (e.g. ConfigRelease.js) are not yet supported, this particular variant changes only one binary
  * variable.  To undo the change, simply omit the ‘ConfigRelease’ argument from the build command.
  */
load( Java.type('java.nio.file.Paths').get(__DIR__).toUri().resolve( 'ConfigDefault.js' ).toASCIIString() );

    waymaker.spec.build.Config.androidAssertTranslation = 'empty';
