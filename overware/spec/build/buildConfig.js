/** buildConfig.js - Configuration for the Overware build
  ******************
  * This configuration file should be copied to one of the following locations:
  *
  *     Mac OS X: HOME/Library/Application Support/overware/buildConfig.js
  *     Windows:  HOME\AppData\Local\overware\buildConfig.js
  *     Others:   XDG_CONFIG_HOME/overware/buildConfig.js
  *               HOME/.config/overware/buildConfig.js
  *
  * XDG_CONFIG_HOME is an environment variable that defaults to the value "HOME/.config".
  * HOME stands for the Java property user.home, which can be displayed by running the
  * Nashorn JavaScript engine (jjs) in interactive mode:
  *
  *     $ jjs
  *     jjs> java.lang.System.getProperties().getProperty( 'user.home' )
  *      - here it displays the value of user.home -
  *     jjs> quit()
  *
  * For the full catalogue of configuration variables that can be set, see installation
  * file overware/spec/build/buildConfigDefault.js.  Set them like this:
  *
  *     bc.NAME = VALUE;
  *
  * You can also define a custom build target, or redefine a standard one, like this:
  *
  *     ov.buildTarget.NAME = function() { CUSTOM CODE };
  *
  * For the coding of the standard targets, see file overware/spec/build/target.js.  Once
  * the target is defined, you can build it using the command "overware/build -- NAME".
  */
var bc = ov.build.config;


    bc.jdkBinLoc = '/opt/jdk/bin'; // example of a configuration setting


    ov.buildTarget.myTarget = function() // example of a custom build target,
    {                                   // build it with "overware/build -- myTarget"
        print( 'Building my target' );
        // put your custom code here
    };
