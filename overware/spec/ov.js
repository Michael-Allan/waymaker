/** ov.js - Basic utilities for Overware scripts
  *********
  */
if( !this.ov ) { ( function( globe )
{
    var our = globe.ov = {}; // public, our.NAME accessible as ov.NAME
    var my = {}; // private



//// P u b l i c /////////////////////////////////////////////////////////////////////////


    /** Utilities for building Overware.
      *
      *     @see build/build.js
      */
    our.build = {}



    /** Ensures that the specified directory exists.
      *
      *     @param dir (java.nio.file.Path)
      *     @return the same directory.
      */
    our.ensureDir = function( dir )
    {
        Java.type('java.nio.file.Files').createDirectories( dir );
        return dir;
    };



    /** Prints a message and aborts the script.
      *
      *     @param message (String) The message to print, which defaults to 'Abnormal exit'.
      *     @param scriptLoc (String) The path to the aborted script file, or null to skip
      *       printing this information.
      *     @param line (Number) The line number at which the script was aborted, or null
      *       to skip printing this information.
      */
    our.exit = function( message, scriptLoc, line )
    {
        if( !message ) message = 'Abnormal exit';
        var outS = Java.type('java.lang.System').out;
        if( scriptLoc )
        {
            outS.print( scriptLoc );
            outS.print( ':' );
            if( line )
            {
                outS.print( line );
                outS.print( ':' );
            }
            outS.print( ' ' );
        }
        outS.println( message );
        exit( 1 );
    };



    /** The filename separator, which is "/" on Unix.
      *
      *     @see #slashed
      */
    our.F = Java.type('java.nio.file.FileSystems').getDefault().getSeparator();



    /** The line separator, which is "\n" on Unix.
      */
    our.L = Java.type('java.lang.System').getProperties().getProperty( 'line.separator' );



    /** The absolute filepath of the real Overware installation directory.  Most of its
      * files are located within a subdirectory called 'overware'.
      *
      *     @return String
      *     @see #ulocTo
      */
    our.loc = function() { return my.loc; };



    /** Logs a command that is being issued through the command-line interface.
      *
      *     @return (String) the same command.
      */
    our.logCommand = function( command )
    {
        var outLog = my.outLog;
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
        var outLog = my.outLog;
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
    our.osTag = function() { return my.osTag; };



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
    our.tmpLoc = function() { return my.tmpLoc; };



    /** Returns the absolute file URI of the specified Overware installation file.
      *
      *     @param subLoc (String) The URI path of the file relative to the
      *       Overware installation directory.
      *     @return String
      *     @see #loc
      */
    our.ulocTo = function( subLoc ) { return my.uri.resolve( subLoc ).toASCIIString(); };



    /** The directory that stores the user's Overware configuration.
      *
      *     @return String
      */
    our.userConfigLoc = function() { return my.userConfigLoc; };



    /** The user's home directory.
      *
      *     @return String
      */
    our.userHomeLoc = function() { return my.userHomeLoc; };



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    my.init = function()
    {
        var Paths = Java.type( 'java.nio.file.Paths' );
        var v = Paths.get(__DIR__).toRealPath();
     // v = v.getRoot().resolve( v.subpath( 0, v.getNameCount() - 2 )); // minus overware/spec/build
     /// IllegalArgumentException when installed to root (e.g. of mapped drive on Windows) so:
        v = v.getParent().getParent(); // to parent of overware/spec
        my.loc = v.toString();
        my.uri = v.toUri();

        var System = Java.type( 'java.lang.System' );
        my.userHomeLoc = System.getProperties().getProperty( 'user.home' );
        v = System.getProperties().getProperty( 'os.name' ).slice( 0, 3 );
        if( v === 'Win' )
        {
            my.osTag = 'win';
            my.userConfigLoc = our.slashed(my.userHomeLoc) + 'AppData' + our.F + 'Local' + our.F
              + 'Overware';
        }
        else if( v === 'Mac' )
        {
            my.osTag = 'mac';
            my.userConfigLoc = our.slashed(my.userHomeLoc) + 'Library' + our.F
              + 'Application Support' + our.F + 'Overware';
        }
        else
        {
            my.osTag = 'nix';;
            v = $ENV.XDG_CONFIG_HOME;
            my.userConfigLoc = v? our.slashed(v) + 'overware':
              our.slashed(my.userHomeLoc) + '.config' + our.F + 'overware';
        }

        my.tmpLoc = our.slashed(System.getProperties().getProperty('java.io.tmpdir')) + 'overware';
        my.outLog = Java.type('java.nio.file.Files').newBufferedWriter(
          Paths.get(my.tmpLoc,'log'));
        delete my.init; // singleton
    };



    // outLog - java.io.BufferedWriter



    // uri - Absolute file URI (java.net.URI) of real Overware installation directory



////////////////////

    my.init();

}( /*globe*/this ) );
    // still under load guard at top
}
