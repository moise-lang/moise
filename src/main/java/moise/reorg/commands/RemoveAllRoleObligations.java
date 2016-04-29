package moise.reorg.commands;

import moise.common.MoiseException;
import moise.oe.MissionPlayer;
import moise.oe.OE;
import moise.oe.OEAgent;
import moise.os.OS;
import moise.os.ss.Role;

/**
 * A reorganisation command,  a command that changes the OS
 *
 * @author  jomi
 */
public class RemoveAllRoleObligations extends ChangeDS {
    
    private static final long serialVersionUID = 1L;

    String roleId;
    
    public RemoveAllRoleObligations() {}
    
    public RemoveAllRoleObligations(String roleId) {
        this.roleId = roleId;
    }
    
    //
    // execution of the reorganisation commands
    //
    
    public void execute(OS os, OE oe) throws MoiseException {
        Role r = os.getSS().getRoleDef( roleId );
        os.getNS().removeNorms( r );
        
        // remove the agents missions
        for (OEAgent ag: oe.getAgents( null, r)) {
            for (MissionPlayer mp: ag.getMissions()) {
                ag.abortMission( mp.getMission().getId(), mp.getScheme() );
            }
        }
        return;
    }
    
    
    public String toString() {
        String args = "(roleId="+roleId+")";
        return this.getClass().getName() + args;
    }
    
}
