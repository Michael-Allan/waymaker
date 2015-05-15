/** BuildConfig.js - Configuration for the Overware build
  ******************
  *
  *   This configuration file should be copied to one of the following locations:
  *
  *       Mac OS X: HOME/Library/Application Support/Overware/BuildConfig.js
  *       Windows:  HOME\AppData\Local\Overware\BuildConfig.js
  *       Others:   XDG_CONFIG_HOME/overware/BuildConfig.js
  *                 HOME/.config/overware/BuildConfig.js
  *
  *   XDG_CONFIG_HOME is an environment variable that defaults to the value
  *   "HOME/.config".  HOME stands for the Java property user.home, which can be displayed
  *   by running the Nashorn JavaScript engine (jjs) in interactive mode:
  *
  *       $ jjs
  *       jjs> java.lang.System.getProperties().getProperty( 'user.home' )
  *        - here it displays the value of user.home -
  *       jjs> quit()
  *
  *
  * Setting variables
  * -----------------
  *   For the full catalogue of configuration variables, see installation file
  *   overware/spec/build/BuildConfig_default.js.  Set them like this:
  *
  *       bc.NAME = VALUE;
  *
  *   Variables may be overridden from the command line as follows.  This works reliably
  *   only for variables that have string-form values.
  *
  *     $ overware/build -Doverware.spec.build.BuildConfig.NAME=VALUE
  *
  *
  * Defining build targets
  * ----------------------
  *   Define a custom build target, or redefine a standard one, like this:
  *
  *       overware.spec.build.Target.NAME = function() { CUSTOM CODE };
  *
  *   For the coding of the standard targets, see file overware/spec/build/Target.js.
  *   Once defined, you can build a target using the command "overware/build -- NAME".
  */
var bc = overware.spec.build.BuildConfig;


    bc.jdkBinLoc = '/opt/jdk/bin'; // example of a configuration setting


    overware.spec.build.Target.myTarget = function() // example of a custom build target,
    {                                               // build it with "overware/build -- myTarget"
        print( 'Building my target' );
        // put your custom code here
    };
