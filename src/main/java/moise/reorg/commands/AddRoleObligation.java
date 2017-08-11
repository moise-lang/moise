package moise.reorg.commands;

import moise.common.MoiseException;
import moise.oe.OE;
import moise.os.OS;
import moise.os.fs.Mission;
import moise.os.ns.Norm;
import moise.os.ns.NS.OpTypes;
import moise.os.ss.Role;

/**
 * A reorganization command,  a command that changes the OS
 *
 * @author  jomi
 */
public class AddRoleObligation extends ChangeDS {

    private static final long serialVersionUID = 1L;

    String roleId;
    String missionId;
    OpTypes type;

    public AddRoleObligation() { }

    public AddRoleObligation(String roleId, String missionId, OpTypes type) {
        this.roleId = roleId;
        this.missionId = missionId;
        this.type = type;
    }

    //
    // execution of the reorganization commands
    //

    public void execute(OS os, OE oe) throws MoiseException {
        Role r = os.getSS().getRoleDef( roleId );
        Mission m = os.getFS().findMission( missionId );

        Norm dr = new Norm(r, m, os.getNS(), type);

        os.getNS().addNorm(dr);

        return;
    }


    public String toString() {
        String args = "(roleId="+roleId+", type="+type+", missionId="+missionId+")";
        return this.getClass().getName() + args;
    }

}
