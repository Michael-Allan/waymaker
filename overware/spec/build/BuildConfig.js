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
  * Defining custom build targets
  * -----------------------------
  *   Define a custom build target like this:
  *
  *       overware.spec.build.TARGET = {};
  *       overware.spec.build.TARGET.Target = {};
  *       overware.spec.build.TARGET.Target.build = function() { CUSTOM CODE };
  *
  *   For the coding of standard targets, see files overware/spec/build/TARGET/Target.js.
  *   Once defined, you can build the target using the command "overware/build -- NAME".
  */
var bc = overware.spec.build.BuildConfig;


    bc.jdkBinLoc = '/opt/jdk/bin'; // example of a configuration setting


    overware.spec.build.foo = {};
    overware.spec.build.foo.Target = {};
    overware.spec.build.foo.Target.build = function() // example of custom build target 'foo',
    {                                                // build it with "overware/build -- foo"
        print( 'Building target foo' );
        // put your custom code here
    };
