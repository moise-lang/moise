package moise.reorg.commands;

import moise.common.MoiseException;
import moise.oe.GroupInstance;
import moise.oe.OE;
import moise.oe.RolePlayer;
import moise.os.OS;
import moise.os.ss.Group;

/**
 * A reorganisation command, a command that changes the OS
 *
 * @author  jomi
 */
public class RemoveRole extends ChangeGroup {
    
    private static final long serialVersionUID = 1L;

    String grSpecId;
    String roleId;
    
    public RemoveRole() {}
    
    public RemoveRole(String grSpecId, String roleId) {
        this.grSpecId = grSpecId;
        this.roleId = roleId;
    }
    
    
    //
    // execution of the reorganisation commands
    //
    
    public void execute(OS os, OE oe) throws MoiseException {
        Group grSpec = os.getSS().getRootGrSpec().findSubGroup( grSpecId );
        grSpec.removeRole( roleId );
        
        // remove all agents roles
        for (GroupInstance gr: oe.findInstancesOf(grSpec)) {
            
            // for roleId players
            for (RolePlayer rp: gr.getPlayers()) {
                if (rp.getRole().getId().equals( roleId )) {
                    rp.getPlayer().abortRole( rp );
                }
            }
        }
    }
    
    public String toString() {
        String args = "(grSpecId="+grSpecId + ", roleId="+roleId+")";
        return this.getClass().getName() + args;
    }
}
