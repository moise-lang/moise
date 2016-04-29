
package moise.reorg.commands;

import moise.common.MoiseException;
import moise.oe.OE;
import moise.os.OS;

/**
 * A reorganization command,  a command that changes the OS
 *
 * @author  jomi
 */
public class ChangeOS implements java.io.Serializable {

    //
    // execution of the reorganization commands
    //
    
    private static final long serialVersionUID = 1L;

    public void execute(OS os, OE oe) throws MoiseException {
    }
    
    public String toString() {
        return this.getClass().getName(); //+args;
    }
}
