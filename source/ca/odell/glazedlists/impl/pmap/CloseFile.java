/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.pmap;

// NIO is used for CTP
import ca.odell.glazedlists.impl.nio.*;
import ca.odell.glazedlists.impl.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.text.ParseException;
import java.io.*;
// logging
import java.util.logging.*;

/**
 * Closes the file for reading and writing a persistent map.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class CloseFile implements Runnable {
     
    /** logging */
    private static Logger logger = Logger.getLogger(OpenFile.class.toString());
    
    /** the host map */
    private PersistentMap persistentMap = null;

    /**
     * Create a new CloseFile.
     */
    public CloseFile(PersistentMap persistentMap) {
        this.persistentMap = persistentMap;
    }
    
    /**
     * Close the file.
     */
    public void run() {
        FileChannel fileChannel = persistentMap.getFileChannel();
        
        try {
            fileChannel.close();
        } catch(IOException e) {
            persistentMap.fail(e, "Failed to close file " + persistentMap.getFile().getPath());
        }
    }
}
