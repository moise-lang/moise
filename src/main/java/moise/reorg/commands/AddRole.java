package moise.reorg.commands;

import moise.common.MoiseException;
import moise.oe.OE;
import moise.os.OS;
import moise.os.ss.Group;

/**
 * A reorganization command, a command that changes the OS
 *
 * @author  jomi
 */
public class AddRole extends ChangeGroup {

    // TODO: use java annotations to define the focus

    private static final long serialVersionUID = 1L;

    String grSpecId;
    String roleId;

    public AddRole() { }

    public AddRole(String grSpecId, String roleId) {
        this.grSpecId = grSpecId;
        this.roleId = roleId;
    }


    //
    // execution of the reorganization commands
    //

    public void execute(OS os, OE oe) throws MoiseException {
        Group grSpec = os.getSS().getRootGrSpec().findSubGroup( grSpecId );
        grSpec.addRole( roleId );
    }

    public String toString() {
        String args = "(grSpecId="+grSpecId + ", roleId="+roleId+")";
        return this.getClass().getName() + args;
    }
}
