/** Overware.js - Basic facilities for Overware scripts
  ***************
  * This module publishes basic facilities for Overware scripts.  It defines namespaces
  * such as overware.spec.build in which other modules may publish themselves.  The root
  * namespace "overware" must, however, be predefined as a global property before loading
  * this module.  Typically this is done in each executable script as follows:
  *
  *     this.overware = {}; // preliminary boilerplate required in all executable scripts
  *     load( Java.type('java.nio.file.Paths').get(__DIR__).toUri().resolve(
  *       'RELATIVE-PATH/Overware.js' ).toASCIIString() );
  */
if( !overware.Overware ) {
     overware.Overware = {};
( function()
{
    var our = overware.Overware; // public as overware.Overware

    var Files = Java.type( 'java.nio.file.Files' );
    var System = Java.type( 'java.lang.System' );



    var init = function()
    {
        init = undefined; // singleton
        var ppSys = System.getProperties();
        our.L = ppSys.getProperty( 'line.separator' );
        our.P = ppSys.getProperty( 'path.separator' );
        tmpLoc = our.slashed(ppSys.getProperty('java.io.tmpdir')) + 'overware';

        var Paths = Java.type( 'java.nio.file.Paths' );
        var v = Paths.get(__DIR__).toRealPath();
     // v = v.getRoot().resolve( v.subpath( 0, v.getNameCount() - 1 )); // minus overware
     /// IllegalArgumentException when installed to root, e.g. of mapped drive on Windows, so:
        v = v.getParent(); // e.g. path/install/overware -> path/install
        loc = v.toString();
        outLog = Files.newBufferedWriter( Paths.get(tmpLoc,'log'));
        uri = v.toUri();

      // Initialize OS specifics.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        userHomeLoc = ppSys.getProperty( 'user.home' );
        v = ppSys.getProperty( 'os.name' ).slice( 0, 3 );
        if( v === 'Win' )
        {
            osTag = 'win';
            userConfigLoc = our.slashed(userHomeLoc) + 'AppData' + our.F + 'Local' + our.F
              + 'Overware';
        }
        else if( v === 'Mac' )
        {
            osTag = 'mac';
            userConfigLoc = our.slashed(userHomeLoc) + 'Library' + our.F
              + 'Application Support' + our.F + 'Overware';
        }
        else
        {
            osTag = 'nix';;
            v = $ENV.XDG_CONFIG_HOME;
            userConfigLoc = v? our.slashed(v) + 'overware':
              our.slashed(userHomeLoc) + '.config' + our.F + 'overware';
        }

      // Predefine all namespaces eagerly.  Simpler than defining them lazily, ad hoc.
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        overware.spec = {};
        overware.spec.build = {};
        overware.spec.build.android = {};
        overware.spec.build.clean = {};
        overware.spec.build.release = {};
        overware.spec.build.source = {};
    };



//// P u b l i c /////////////////////////////////////////////////////////////////////////


    /** Ensures that the specified directory exists.
      *
      *     @param dir (java.nio.file.Path)
      *     @return the same directory.
      */
    our.ensureDir = function( dir )
    {
        Files.createDirectories( dir );
        return dir;
    };



    /** Prints a message and aborts the script.  To also print the path and line number of
      * the currently executing script file, instead just throw the message.
      *
      *     @param message (String) The message to print.
      */
    our.exit = function( message )
    {
        System.err.println( message );
        exit( 1 );
    };



    /** The filename separator, which is '/' on Unix.
      *
      *     @see #slashed
      */
    our.F = Java.type('java.nio.file.FileSystems').getDefault().getSeparator();



    /** The line separator, which is '\n' on Unix.
      */
 // our.L = . . .



    /** The absolute filepath of the real Overware installation directory.  Most of its
      * files are located within a subdirectory called 'overware'.
      *
      *     @return String
      *     @see #ulocTo
      */
    our.loc = function() { return loc; };


        var loc;



    /** Logs a command that is being issued through the command-line interface.
      *
      *     @return (String) the same command.
      */
    our.logCommand = function( command )
    {
        outLog.append( command );
        outLog.newLine();
        var cN = command.length();
        if( cN > 90 ) cN = 90;
        for( var c = 0; c < cN; ++c ) outLog.append( '^' );
        outLog.newLine();
        outLog.flush();
        return command;
    };



    /** Logs the result of a command that was issued through the command-line interface
      * via the $EXEC function.
      */
    our.logCommandResult = function()
    {
        outLog.append( '    Exit value = ' ).append( $EXIT.toString() );
        outLog.newLine();
        outLog.newLine();
        if( $OUT )
        {
            outLog.append( '    Out:' );
            outLog.newLine();
            outLog.append( '    ---' );
            outLog.newLine();
            outLog.append( $OUT );
            outLog.newLine();
            if( !$OUT.endsWith( our.L )) outLog.newLine();
        }
        if( $ERR )
        {
            outLog.append( '    Err:' );
            outLog.newLine();
            outLog.append( '    ---' );
            outLog.newLine();
            outLog.append( $ERR );
            outLog.newLine();
            if( !$ERR.endsWith( our.L )) outLog.newLine();
        }
        outLog.flush();
    };



    /** The OS identifier, which is either 'mac', 'win' or 'nix'.  The latter means the OS
      * is assumed (by default) to be a generic variant of Unix.
      *
      *     @return String
      */
    our.osTag = function() { return osTag; };


        var osTag;



    /** The filepath separator, which is ':' on Unix.
      */
 // our.P = . . .



    /** Appends a filename separator (e.g. / or \) to the path if none is yet appended.
      * Appends nothing if the path is completely empty ''.  Returns the resulting path.
      *
      *     @param path (String) The path on which to append the separator.
      */
    our.slashed = function( path )
    {
        if( path )
        {
            var c = path.slice( -1 );
            if( c != '/' && c != '\\' ) path += our.F;
        }
        return path;
    };



    /** The directory for expendable, intermediate output from scripted processes.  It
      * might not exist, so the caller should create it as needed.  Do not delete it.
      *
      *     @return String
      */
    our.tmpLoc = function() { return tmpLoc; };


        var tmpLoc;



    /** Returns the absolute URI of the specified Overware installation file.
      *
      *     @param subLoc (String) The URI path of the file relative to the
      *       Overware installation directory.
      *     @return String
      *     @see #loc
      */
    our.ulocTo = function( subLoc ) { return uri.resolve( subLoc ).toASCIIString(); };



    /** The directory that stores the user's Overware configuration.
      *
      *     @return String
      */
    our.userConfigLoc = function() { return userConfigLoc; };


        var userConfigLoc;



    /** The user's home directory.
      *
      *     @return String
      */
    our.userHomeLoc = function() { return userHomeLoc; };


        var userHomeLoc;



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    var outLog; // java.io.BufferedWriter



    /** The absolute file URI (java.net.URI) of the Overware installation directory.
      */
    var uri;



////////////////////

    init();

}() );
    // still under this module's load guard at top
}


// Copyright 2015, Michael Allan.  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Overware Software"), to deal in the Overware Software without restriction, subject to the following conditions: The preceding copyright notice and this permission notice shall be included in all copies or substantial portions of the Overware Software.  The Overware Software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement.  In no event shall the authors or copyright holders be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the Overware Software or the use or other dealings in the Overware Software.
