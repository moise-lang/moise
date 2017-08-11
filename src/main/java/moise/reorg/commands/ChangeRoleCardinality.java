
package moise.reorg.commands;

import moise.common.MoiseException;
import moise.oe.OE;
import moise.os.OS;

/**
 * A reorganization command,  a command that changes the OS
 *
 * @author  jomi
 */
public class ChangeRoleCardinality extends ChangeGroup {

    private static final long serialVersionUID = 1L;

    public ChangeRoleCardinality() { }

    //
    // execution of the reorganization commands
    //

    public void execute(OS os, OE oe) throws MoiseException {
        throw new MoiseException("reorganization command "+this+" not implemented!");
    }
}
