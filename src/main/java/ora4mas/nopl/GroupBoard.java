package ora4mas.nopl;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import moise.common.MoiseException;
import moise.oe.GroupInstance;
import moise.oe.RolePlayer;
import moise.os.OS;
import moise.tools.os2dot;
import moise.xml.DOMUtils;
import npl.NormativeFailureException;
import npl.parser.ParseException;
import ora4mas.nopl.oe.CollectiveOE;
import ora4mas.nopl.oe.Group;
import ora4mas.nopl.oe.Player;
import ora4mas.nopl.tools.os2nopl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.CartagoException;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OperationException;

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
 * <li>setParentGroup
 * <li>destroy
 * </ul>
 * 
 * <b>Observable properties</b>:
 * <ul>
 * <li>play(ag,role,group): agent ag is playing the role in the group.
 * <li>schemes: a list of schemes identification the group is responsible for.
 * <li>specification: the specification of the group in the OS (a prolog like representation)
 * <li>subgroups: a list of subgroups ids
 * <li>parent group: the id of the parent group (used in subgroups)
 * <li>formationStatus: whether the group is well-formed (values are ok and nok)
 * </ul>
 * 
 * <b>Signals</b> (obligations has the form: obligation(to whom, maintenance condition, what, deadline)):
 * <ul>
 * <li>oblCreated(o): the obligation <i>o</i> is created
 * <li>oblFulfilled(o): the obligation <i>o</i> is fulfilled
 * <li>oblUnfulfilled(o): the obligation <i>o</i> is unfulfilled (e.g. by timeout)
 * <li>oblInactive(o): the obligation <i>o</i> is inactive (e.g. its maintenance condition does not hold anymore)
 * <li>normFailure(f): the failure <i>f</i> has happened (e.g. due some regimentation)
 * </ul>
 * 
 * @navassoc - specification - moise.os.ss.Group
 * @see moise.os.ss.Group
 * @author Jomi
 */
public class GroupBoard extends OrgArt {

    private moise.os.ss.Group  spec;
    private Set<ArtifactId>    schemes     = new HashSet<ArtifactId>();
    private Set<ArtifactId>    listeners   = new HashSet<ArtifactId>();
    private ArtifactId         parentGroup = null;
    
    private List<String>       futureSchemes = new LinkedList<String>(); // schemes to be responsible to when well formed
        
    public static final String obsPropSpec        = "specification";
    public static final String obsPropPlay        = Group.playPI.getFunctor();
    public static final String obsPropSchemes     = "schemes";
    public static final String obsPropSubgroups   = "subgroups";
    public static final String obsPropParentGroup = "parentGroup";
    public static final String obsWellFormed      = "formationStatus";

    private static boolean httpMsg = false;
    
    protected Logger logger = Logger.getLogger(GroupBoard.class.getName());

    private Group getGrpState() {
        return (Group)orgState;
    }
    
    
    /**
     * Initialises the group board
     * 
     * @param osFile            the organisation specification file (path and file name)
     * @param grType            the type of the group (as defined in the OS)
     * @param createMonitoring  whether a monitoring scheme will be created and attached
     * @param hasGUI            whether a GUI have to be created for the artifact
     * @throws ParseException   if the OS file is not correct
     * @throws MoiseException   if grType was not specified
     * @throws OperationException if parentGroupId doesn't exit
     */
    public void init(final String osFile, final String grType, final boolean createMonitoring, final boolean hasGUI) throws ParseException, MoiseException, OperationException {
        final String grId = getId().getName();
        orgState   = new Group(grId);
       
        final OS os = OS.loadOSFromURI(osFile);
        
        spec = os.getSS().getRootGrSpec().findSubGroup(grType);
        
        if (spec == null)
            throw new MoiseException("group "+grType+" does not exist!");

        // observable properties
        defineObsProperty(obsPropSchemes, getGrpState().getResponsibleForAsProlog());
        defineObsProperty(obsWellFormed, new JasonTermWrapper("nok"));
        defineObsProperty(obsPropSpec, new JasonTermWrapper(spec.getAsProlog()));
        defineObsProperty(obsPropSubgroups, getGrpState().getSubgroupsAsProlog());
        defineObsProperty(obsPropParentGroup, new JasonTermWrapper(getGrpState().getParentGroup()));

        // load normative program
        initNormativeEngine(os, "group("+grType+")");
        installNormativeSignaler();

        // install monitor of agents quiting the system
        initWspRuleEngine();
        
        //final ArtifactId myArtId = getId();

        // use a thread to create GUI/Monitor (to not block the init)
        new Thread() {
            @Override public void run() {
                try {
                    
                    String addrs = startHttpServer();

                    String srcNPL = os2nopl.header(spec)+os2nopl.transform(spec);
                    String osSpec = specToStr(os, DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("os")));
                    
                    //String nplURL = registerNPLBrowserView("/group/",grType,srcNPL);                    
                    //String osURL  = 
                    String oeId = getCreatorId().getWorkspaceId().getName();
                    
                    registerOSBrowserView(oeId, os.getId(), osSpec);
                    
                    //System.out.println("***"+getOpUserBody());
                    //String oeURL  = 
                    registerOEBrowserView(oeId, "/group/",grId,srcNPL,GroupBoard.this,getStyleSheet());
                    
                    // start GUI
                    /*if (guiType.equals("browser") && Desktop.isDesktopSupported() && oeURL != null)
                        Desktop.getDesktop().browse(new URI(oeURL));
                    
                    else */
                    if (hasGUI) {
                        gui = OrgArtNormativeGUI.add(grId, ":: Group Board "+grId+" ("+grType+") ::", nengine);
                        
                        updateGUIThread = new UpdateGuiThread();
                        updateGUIThread.start();

                        updateGuiOE();
                        
                        gui.addNormativeProgram(srcNPL);
                        gui.addSpecification(specToStr(os, DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("ss"))));
                    } else {
                        if (! httpMsg) {
                            logger.info("You can open the Moise GUI using the URL "+addrs);
                            httpMsg = true;
                        }
                    }
                    
                    // create monitoring scheme
                    if (createMonitoring && spec.getMonitoringSch() != null) {
                        String schMonId = grId+"_monitor";
                        monitorSchArt =  makeArtifact(schMonId, SchemeBoard.class.getName(), new ArtifactConfig(schMonId, osFile, spec.getMonitoringSch(), false, hasGUI));
                        orgState.setMonitorSch(schMonId);
                        // TODO: exec produces an error
                        //execLinkedOp(GroupBoard.this.getId(), "addScheme", schMonId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    
    /**
     * The agent executing this operation tries to destroy the instance of the group     
     *    								
     */
    @OPERATION public void destroy() {        
        if (parentGroup != null) {
            try {
                execLinkedOp(parentGroup, "removeSubgroup", getGrpState().getId());
            } catch (OperationException e) {
                e.printStackTrace();
                return; // do not call super destroy
            }                    
        }
        
        super.destroy();
    }
    
    @Override
    public void agKilled(String agName) {
        //logger.info("****** "+agName+" has quit!");
        boolean oldStatus = isWellFormed();
        for (Player p: orgState.getPlayers() ) {
            if (orgState.removePlayer(agName, p.getTarget())) {
                try {
                    logger.info(agName+" has quit, role "+p.getTarget()+" removed by the platform!");
                    leaveRoleWithoutVerify(agName, p.getTarget(), oldStatus);
                } catch (CartagoException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * The agent executing this operation tries to connect the group to a parentGroup     
     *  
     * @param parentGroupId 								the group Id to connect to
     */
    @OPERATION public void setParentGroup(String parentGroupId) throws OperationException {
        parentGroup = lookupArtifact(parentGroupId);
        getGrpState().setParentGroup(parentGroupId);
        execLinkedOp(parentGroup, "addSubgroup", getGrpState().getId(), spec.getId(), parentGroupId);
        execLinkedOp(parentGroup, "updateSubgroupPlayers", orgState.getId(), orgState.getPlayers());
        execLinkedOp(parentGroup, "updateSubgroupFormationStatus", getGrpState().getId(), isWellFormed());        
        execLinkedOp(parentGroup, "updateSubgroupPlayers", orgState.getId(), orgState.getPlayers());
        getObsProperty(obsPropParentGroup).updateValue(new JasonTermWrapper(getGrpState().getParentGroup()));
        updateGuiOE();
    }
    
    /**
     * The agent executing this operation tries to adopt a role in the group
     * 
     * @param role                        the role being adopted
     */
    @OPERATION public void adoptRole(String role)  {
        adoptRole(getOpUserName(), role);
    }
    private void adoptRole(String ag, String role) {
        if (!running) return;
        boolean oldStatus = isWellFormed();
        CollectiveOE bak = orgState.clone();
        orgState.addPlayer(ag, role);
        try {
            nengine.verifyNorms();
            
            boolean status = isWellFormed();
            if (parentGroup != null) {
                execLinkedOp(parentGroup, "updateSubgroupPlayers", orgState.getId(), orgState.getPlayers());
                if (status != oldStatus) {
                    logger.fine(orgState.getId()+": informing parent group that now my formation is "+status);
                    execLinkedOp(parentGroup, "updateSubgroupFormationStatus", orgState.getId(), status);
                }
            }
            notifyObservers();

            defineObsProperty(obsPropPlay, 
                    new JasonTermWrapper(ag), 
                    new JasonTermWrapper(role), 
                    new JasonTermWrapper(this.getId().getName()));            
            if (status != oldStatus) { 
                getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(status ? "ok" : "nok"));
                
                while (!futureSchemes.isEmpty()) {
                    String sch = futureSchemes.remove(0);
                    //logger.info("Since the group "+orgState.getId()+" is now well formed, adding scheme "+sch);
                    addScheme(sch);
                }
            }
            updateGuiOE();            
        } catch (NormativeFailureException e) {
            e.printStackTrace();
            orgState = bak; // takes the backup as the current model since the action failed
            failed("Error adopting role "+role, "reason", new JasonTermWrapper(e.getFail()));
        } catch (Exception e) {
            orgState = bak; // takes the backup as the current model since the action failed
            e.printStackTrace();
            failed(e.toString());
        }   
    }
    
    /**
     * The agent executing this operation tries to give up a role in the group
     * 
     * @param role                        the role being removed/leaved
     */
    @OPERATION public void leaveRole(String role)  {
        if (!running) return;
        CollectiveOE bak = orgState.clone();
        boolean oldStatus = isWellFormed();
        orgState.removePlayer(getOpUserName(), role);
        try {
            nengine.verifyNorms();
            boolean status = leaveRoleWithoutVerify(getOpUserName(), role, oldStatus);
            notifyObservers();
            if (parentGroup != null) {
                execLinkedOp(parentGroup, "updateSubgroupPlayers", orgState.getId(), orgState.getPlayers());
                execLinkedOp(parentGroup, "updateSubgroupFormationStatus", orgState.getId(), status);
            }
        } catch (NormativeFailureException e) {
            orgState = bak; // takes the backup as the current model since the action failed
            failed("Error leaving role "+role, "reason", new JasonTermWrapper(e.getFail()));
        } catch (Exception e) {
            orgState = bak; // takes the backup as the current model since the action failed
            failed(e.toString());
        }   
    }


    private boolean leaveRoleWithoutVerify(String ag, String role, boolean oldStatus)  throws CartagoException, OperationException {
        boolean status = isWellFormed();
        removeObsPropertyByTemplate(obsPropPlay, 
                new JasonTermWrapper(ag), 
                new JasonTermWrapper(role), 
                new JasonTermWrapper(this.getId().getName()));
        if (status != oldStatus)
            getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(status ? "ok" : "nok"));
        updateGuiOE();
        return status;
    }

    /*
    @OPERATION public void startMonitorSch() throws CartagoException {
        if (spec.getMonitoringSch() != null) {
            String schId = model.getId()+"_monitor";
            try {
                String inScope = "group("+spec.getId()+")";
                invokeOp(getFactoryId(), new Op("makeArtifactProc", schId, "ora4masNP.SchemeBoard", new ArtifactConfig(schId, osFile, spec.getMonitoringSch(), inScope, gui != null)));
                model.setMonitorSch(schId);
                addScheme(schId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    */
    
    /**
     * The agent executing this operation tries to add a scheme under the responsibility of a group
     * 
     * @param schId                        the scheme Id being added
     */ 
    @OPERATION public void addScheme(String schId) {
        if (!running) return;
        CollectiveOE bak = orgState.clone();
        try {
            ArtifactId schAr = lookupArtifact(schId);
            getGrpState().addResponsibleForScheme(schId);
            nengine.verifyNorms();
            
            getObsProperty(obsPropSchemes).updateValue(getGrpState().getResponsibleForAsProlog());
            
            schemes.add(schAr);
            notifyObservers();
            updateGuiOE();

            // update in subgroups
            for (Group sg: getGrpState().getSubgroups()) {
                ArtifactId sgid = lookupArtifact(sg.getId());
                execLinkedOp(sgid, "addScheme", schId);                
            }
            
        } catch (NormativeFailureException e) {
            orgState = bak; // takes the backup as the current model since the action failed
            failed("Error adding scheme "+schId, "reason", new JasonTermWrapper(e.getFail()));
        } catch (Exception e) {
            orgState = bak; // takes the backup as the current model since the action failed
            failed(e.toString());
        }   
    }

    /**
     * The group will be responsible for the scheme when its formation is Ok
     * 
     * @param schId                        the scheme Id being added
     */ 
    @OPERATION public void addSchemeWhenFormationOk(String schId) {
        if (!running) return;
        if (isWellFormed()) {
            addScheme(schId);
        } else {
            futureSchemes.add(schId);
        }
    }
    
    /**
     * The agent executing this operation tries to remove a scheme that is under the responsibility of a group
     * 
     * @param schId                        the scheme Id being removed
     */ 
    @OPERATION public void removeScheme(String schId) {
        if (!running) return;
        CollectiveOE bak = orgState.clone();
        try {
            ArtifactId schAid = lookupArtifact(schId); 
            getGrpState().removeResponsibleForScheme(schId);
            nengine.verifyNorms();
            execLinkedOp(schAid, "removeResponsibleGroup", orgState.getId());

            getObsProperty(obsPropSchemes).updateValue(getGrpState().getResponsibleForAsProlog());

            schemes.remove(schAid);
            updateGuiOE();

        } catch (NormativeFailureException e) {
            orgState = bak; // takes the backup as the current model since the action failed
            failed("Error removing scheme "+schId, "reason", new JasonTermWrapper(e.getFail()));
        } catch (Exception e) {
            orgState = bak; // takes the backup as the current model since the action failed
            failed(e.toString());
        }
    }

    @LINK public void addListener(String artId) {
        if (!running) return;
        try {
            listeners.add(lookupArtifact(artId));

            // update in subgroups
            for (Group sg: getGrpState().getSubgroups()) {
                ArtifactId sgid = lookupArtifact(sg.getId());
                execLinkedOp(sgid, "addListener", artId);                
            }
            
        } catch (Exception e) {
            failed(e.toString());
        }   
    }


    private void notifyObservers() throws CartagoException {
        for (ArtifactId a: schemes) {
            execLinkedOp(a, "updateRolePlayers", orgState.getId(), orgState.getPlayers());
        }
        for (ArtifactId a: listeners) {
            execLinkedOp(a, "updateRolePlayers", orgState.getId(), orgState.getPlayers());
        }
    }
    
    @LINK void updateSubgroupPlayers(String grId, Collection<Player> rp) throws NormativeFailureException, CartagoException {
        if (!running) return;
        CollectiveOE bak = orgState.clone();
        boolean oldStatus = isWellFormed();

        try {
            Group g = getGrpState().getSubgroup(grId);
            g.clearPlayers();
            for (Player p: rp) 
                g.addPlayer(p.getAg(), p.getTarget());
            
            nengine.verifyNorms();
    
            boolean status = isWellFormed();
            if (status != oldStatus)
                getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(status ? "ok" : "nok"));
            
            if (parentGroup != null) {
                execLinkedOp(parentGroup, "updateSubgroupFormationStatus", orgState.getId(), status); // my new formation status
                execLinkedOp(parentGroup, "updateSubgroupPlayers", grId, rp);
            }
            updateGuiOE();
        } catch (NormativeFailureException e) {
            e.printStackTrace();
            orgState = bak; // takes the backup as the current model since the action failed
            failed(e.getFail().toString());
        } catch (Exception e) {
            orgState = bak; 
            e.printStackTrace();
            failed(e.toString());
        }
    }

    
    @LINK void updateSubgroupFormationStatus(String grId, boolean isWellFormed) throws NormativeFailureException, CartagoException {
        if (!running) return;
        CollectiveOE bak = orgState.clone();
        boolean oldStatus = isWellFormed();

        try {
            logger.fine("updating status of "+grId+" to "+isWellFormed);
            getGrpState().setSubgroupWellformed(grId, isWellFormed);

            nengine.verifyNorms();
    
            boolean status = isWellFormed();
            if (status != oldStatus) {
                logger.fine("now I, "+orgState.getId()+", am "+status);
                getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(status ? "ok" : "nok"));
            }
            if (parentGroup != null) {
                execLinkedOp(parentGroup, "updateSubgroupFormationStatus", grId, isWellFormed);
                execLinkedOp(parentGroup, "updateSubgroupFormationStatus", orgState.getId(), status);
            }

            updateGuiOE();
        } catch (NormativeFailureException e) {
            e.printStackTrace();
            orgState = bak; // takes the backup as the current model since the action failed
            failed(e.getFail().toString());
        } catch (Exception e) {
            e.printStackTrace();
            orgState = bak; 
            failed(e.toString());
        }
    }
    
    @LINK void addSubgroup(String grId, String grType, String parentGr) throws NormativeFailureException, CartagoException {
        if (!running) return;
        CollectiveOE bak = orgState.clone();
        boolean oldStatus = isWellFormed();
        
        try {
            getGrpState().addSubgroup(grId, grType, parentGr);
            
            nengine.verifyNorms();

            boolean status = isWellFormed();
            if (status != oldStatus)
                getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(status ? "ok" : "nok"));
            getObsProperty(obsPropSubgroups).updateValue(getGrpState().getSubgroupsAsProlog());

            if (parentGroup != null) {
                execLinkedOp(parentGroup, "addSubgroup", grId, grType, parentGr);    
                execLinkedOp(parentGroup, "updateSubgroupFormationStatus", orgState.getId(), status);
            }
            updateGuiOE();
        } catch (NormativeFailureException e) {
            e.printStackTrace();
            orgState = bak; // takes the backup as the current model since the action failed
            failed(e.getFail().toString());
        } catch (Exception e) {
            orgState = bak; // takes the backup as the current model since the action failed
            failed(e.toString());
            e.printStackTrace();
        }
    }

    @LINK void removeSubgroup(String grId) throws NormativeFailureException, CartagoException {
        if (!running) return;
        CollectiveOE bak = orgState.clone();
        boolean oldStatus = isWellFormed();
        
        try {
            getGrpState().removeSubgroup(grId);

            nengine.verifyNorms();

            boolean status = isWellFormed();
            if (status != oldStatus)
                getObsProperty(obsWellFormed).updateValue(new JasonTermWrapper(status ? "ok" : "nok"));
            getObsProperty(obsPropSubgroups).updateValue(getGrpState().getSubgroupsAsProlog());

            if (parentGroup != null) {
                execLinkedOp(parentGroup, "removeSubgroup", grId);    
                execLinkedOp(parentGroup, "updateSubgroupFormationStatus", orgState.getId(), status);  // update my formation status
            }
            updateGuiOE();
        } catch (NormativeFailureException e) {
            e.printStackTrace();
            orgState = bak; // takes the backup as the current model since the action failed
            failed(e.getFail().toString());
        } catch (Exception e) {
            orgState = bak; // takes the backup as the current model since the action failed
            failed(e.toString());
            e.printStackTrace();
        }
    }

    
    @OPERATION @LINK public void admCommand(String cmd) throws CartagoException, jason.asSyntax.parser.ParseException {
        // this operation is available only for the owner of the artifact
    if (getOpUserId() != null && (!getOpUserName().equals(ownerAgent)) && !getOpUserName().equals("workspace-manager")) {
            failed("Error: agent '"+getOpUserName()+"' is not allowed to run "+cmd,"reason",new JasonTermWrapper("not_allowed_to_start(admCommand)"));
        } else {
            Literal lCmd = ASSyntax.parseLiteral(cmd);
            if (lCmd.getFunctor().equals("adoptRole")) {
                adoptRole(fixAgName(lCmd.getTerm(0).toString()), lCmd.getTerm(1).toString());
            } if (lCmd.getFunctor().equals("leaveRole")) {
                System.out.println("adm leave role not implemented yet! come back soon");
            }
        }
    }
    
    protected String getStyleSheetName() {
        return "noplGroupInstance";                
    }

    public boolean isWellFormed() {
        Term aGr = ASSyntax.createAtom(this.getId().getName());
        return nengine.holds(ASSyntax.createLiteral("well_formed", aGr));
    }
    
    public Element getAsDOM(Document document) {
        return getGrAsDOM(getGrpState(), spec.getId(), isWellFormed(), ownerAgent, getGrpState(), document);
    }

    public static Element getGrAsDOM(Group gr, String  spec, boolean isWellFormed, String owner, Group root, Document document) {
        Element grEle = (Element) document.createElement( GroupInstance.getXMLTag());
        grEle.setAttribute("id", gr.getId());
        grEle.setAttribute("specification", spec);
        
        // status
        Element wfEle = (Element) document.createElement("well-formed");
        if (isWellFormed) {
            wfEle.appendChild(document.createTextNode("ok"));      
        } else {
            wfEle.appendChild(document.createTextNode("not ok"));  
        }
        grEle.appendChild(wfEle);
            
        // players
        if (!gr.getPlayers().isEmpty()) {
            Element plEle = (Element) document.createElement("players");
            for (Player p: gr.getPlayers()) {
                Element rpEle = (Element) document.createElement( RolePlayer.getXMLTag());
                rpEle.setAttribute("role", p.getTarget());
                rpEle.setAttribute("agent", p.getAg());
                plEle.appendChild(  rpEle );
            }
            grEle.appendChild(plEle);
        }

        // schemes
        if (!gr.getSchemesResponsibleFor().isEmpty()) {
            Element rfEle = (Element) document.createElement("responsible-for");
            for (String sch: gr.getSchemesResponsibleFor()) {
                Element schEle = (Element) document.createElement( "scheme");
                schEle.setAttribute("id", sch);
                rfEle.appendChild(  schEle );
            }
            grEle.appendChild(rfEle);
        }
        
        // subgroups
        boolean has = false;
        Element sgEle = (Element) document.createElement("subgroups");
        for (Group gi: root.getSubgroups()) {
            if (gi.getParentGroup().equals(gr.getId())) {
                has = true;
                sgEle.appendChild( getGrAsDOM(gi, gi.getGrType(), root.isSubgroupWellformed(gi.getId()), null, root, document) );
            }
        }
        if (has)
            grEle.appendChild(sgEle);
        
        // parent group
        grEle.setAttribute("parent-group", gr.getParentGroup());
        
        if (owner != null)
            grEle.setAttribute("owner", owner);

        return grEle;
    }
    
    public String getAsDot() {
        os2dot t = new os2dot();
        t.showFS = false;
        t.showNS = false;
        t.showLinks = true;
        
        try {
            
            StringWriter so = new StringWriter();
            so.append("digraph "+getGrpState().getId()+" {\n");
            so.append("    rankdir=BT;\n");
            so.append("    compound=true;\n\n");
            
            so.append( t.transformRolesDef(spec.getSS()));
            //so.append( t.transform(spec.getSS().getRootGrSpec(), getGrpState()) );
            so.append( t.transform(spec, getGrpState()) );

            so.append("}\n");
            return so.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
