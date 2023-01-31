package ora4mas.simple;

import java.util.logging.Logger;

import cartago.OPERATION;
import cartago.OperationException;
import jason.util.Config;
import moise.common.MoiseException;
import moise.os.OS;
import moise.os.ns.NS;
import moise.os.ss.SS;
import npl.parser.ParseException;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.JasonTermWrapper;
import ora4mas.nopl.WebInterface;
import ora4mas.nopl.oe.Group;

/**
 * Artifact to manage a group instance.
 * <br/><br/>
 *
 * <b>Operations</b> (see details in method list below):
 * <ul>
 * <li>adoptRole
 * <li>leaveRole
 * <li>addScheme
 * <li>removeScheme
 * <li>destroy
 * </ul>
 *
 * <b>Observable properties</b>:
 * <ul>
 * <li>play(ag,role,group): agent ag is playing the role in the group.
 * <li>schemes: a list of schemes identification the group is responsible for.
 * <li>formationStatus: whether the group is well-formed (values are ok and nok)
 * </ul>
 *
 * @navassoc - specification - moise.os.ss.Group
 * @see moise.os.ss.Group
 * @author Jomi
 */
public class SimpleGroupBoard extends GroupBoard {

    
    protected Logger logger = Logger.getLogger(SimpleGroupBoard.class.getName());

    /**
     * Initialises the group board
     */
    public void init() throws ParseException, MoiseException, OperationException {
        final String grId = getId().getName();
        orgState   = new Group(grId);

        // create  untype group
        OS os = new OS();
        SS ss = new SS(os);
        os.setSS(ss);
        spec = new moise.os.ss.Group("untyped",ss);        
        ss.setRootGrSpec(spec);

        NS ns = new NS(os);
        ns.setProperty("default_management", "ignore");
        os.setNS(ns);

        oeId = getCreatorId().getWorkspaceId().getName();

        // observable properties
        defineObsProperty(obsPropSchemes, getGrpState().getResponsibleForAsProlog());
        defineObsProperty(obsWellFormed, new JasonTermWrapper("nok"));
        defineObsProperty(obsPropSpec, new JasonTermWrapper(spec.getAsProlog()));

        // load normative program
        initNormativeEngine(os, "group(untyped)");
        installNormativeSignaler();

        // install monitor of agents quitting the system
        initWspRuleEngine();

        if (! "false".equals(Config.get().getProperty(Config.START_WEB_OI))) {
            WebInterface w = WebInterface.get();
            try {
                w.registerOEBrowserView(oeId, "/group/",grId, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        grBoards.add(this);
    }

    /**
     * The agent executing this operation tries to adopt a role in the group
     *
     * @param role                        the role being adopted
     * @throws ParseException 
     * @throws MoiseException 
     */
    @OPERATION public void adoptRole(String role) {
        if (spec.getSS().getRoleDef(role) == null) {
            try {
                addRole(role);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        adoptRole(getOpUserName(), role);
    }
}
