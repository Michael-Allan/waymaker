/** BuildConfig.js - Configuration for the Waymaker build
  ******************
  *
  *   This configuration file should be copied to one of the following locations:
  *
  *       Mac OS X: HOME/Library/Application Support/Waymaker/BuildConfig.js
  *       Windows:  HOME\AppData\Local\Waymaker\BuildConfig.js
  *       Others:   XDG_CONFIG_HOME/waymaker/BuildConfig.js
  *                 HOME/.config/waymaker/BuildConfig.js
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
  *   waymaker/spec/build/BuildConfig_default.js.  Set them like this:
  *
  *       bc.NAME = VALUE;
  *
  *   Variables may be overridden from the command line as follows.  This works reliably
  *   only for variables that have string-form values.
  *
  *     $ waymaker/build -Dwaymaker.spec.build.BuildConfig.NAME=VALUE
  *
  *
  * Defining custom build targets
  * -----------------------------
  *   Define a custom build target like this:
  *
  *       waymaker.spec.build.TARGET = {};
  *       waymaker.spec.build.TARGET.Target = {};
  *       waymaker.spec.build.TARGET.Target.build = function() { CUSTOM CODE };
  *
  *   For the coding of standard targets, see files waymaker/spec/build/TARGET/Target.js.
  *   Once defined, you can build the target using the command "waymaker/build -- NAME".
  */
var bc = waymaker.spec.build.BuildConfig;


    bc.jdkBinLoc = '/opt/jdk/bin'; // example of a configuration setting


    waymaker.spec.build.foo = {};
    waymaker.spec.build.foo.Target = {};
    waymaker.spec.build.foo.Target.build = function() // example of custom build target 'foo',
    {                                                // build it with "waymaker/build -- foo"
        print( 'Building target foo' );
        // put your custom code here
    };
