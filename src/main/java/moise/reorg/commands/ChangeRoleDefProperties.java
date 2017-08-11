package moise.reorg.commands;

import moise.common.MoiseException;
import moise.oe.OE;
import moise.os.OS;
import moise.os.ss.Role;

/**
 * A reorganization command,  a command that changes the OS
 *
 * @author  jomi
 */
public class ChangeRoleDefProperties extends ChangeRolesDef {

    private static final long serialVersionUID = 1L;

    String target;
    String object;
    String property;
    String value;

    public ChangeRoleDefProperties() { }

    public ChangeRoleDefProperties(String target, String object, String property, String value) {
        this.target = target;
        this.object = object;
        this.property = property;
        this.value = value;
    }

    //
    // execution of the reorganization commands
    //

    public void execute(OS os, OE oe) throws MoiseException {
        if ( target.equals("roleDef")) {
            Role r = os.getSS().getRoleDef( object );
            r.setProperty( property, value );
            return;
        }
        throw new MoiseException("reorganization command "+this+" not implemented!");
    }

    public String toString() {
        String args = "(target="+target + ", object="+object + ", property="+property + ", value="+value+")";
        return this.getClass().getName() + args;
    }

}
