/** Config.js - Configuration for the Waymaker build
  *************
  *
  *   This configuration file should be copied to one of the following locations:
  *
  *       Mac OS X: HOME/Library/Application Support/Waymaker/Build/Config.js
  *       Windows:  HOME\AppData\Local\Waymaker\Build\Config.js
  *       Others:   XDG_CONFIG_HOME/waymaker/build/Config.js
  *                 HOME/.config/waymaker/build/Config.js
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
  *   Set the value of a configuration variable using an assignment operation in this form:
  *
  *       waymaker.spec.build.Config.NAME = VALUE;
  *
  *   For more information on configuration variables, see these files in the installation directory:
  *
  *       waymaker/spec/build/ConfigDefault.js   Catalogue of configuration variables
  *       waymaker/build                         Command-line configuration arguments and options
  *
  * Defining custom targets
  * -----------------------
  *   Define a custom build target with variable assignments in this form:
  *
  *       waymaker.spec.build.TARGET = {};
  *       waymaker.spec.build.TARGET.Target = {};
  *       waymaker.spec.build.TARGET.Target.build = function() { CUSTOM CODE };
  *
  *   Use the predefined targets as coding examples.  See these files in the installation directory:
  *
  *       waymaker/spec/build/TARGET/Target.js
  *
  *   Once the custom target is coded, build it like any other:
  *
  *       $ waymaker/build -- TARGET
  */
var waymaker = arguments[0];

    var c = waymaker.spec.build.Config;


    c.jdkBinLoc = '/opt/jdk/bin'; // example of a configuration setting


    waymaker.spec.build.foo = {};
    waymaker.spec.build.foo.Target = {};
    waymaker.spec.build.foo.Target.build = function() // example of custom build target 'foo',
    {                                                // build it with "waymaker/build -- foo"
        print( 'Building target foo' );
        // put your custom code here
    };
