package ora4mas.nopl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.CartagoException;
import cartago.LINK;
import cartago.OPERATION;
import cartago.ObsProperty;
import cartago.OperationException;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Term;
import jason.util.Config;
import moise.common.MoiseException;
import moise.oe.GoalInstance;
import moise.oe.MissionPlayer;
import moise.oe.SchemeInstance;
import moise.os.OS;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.tools.os2dot;
import moise.xml.DOMUtils;
import npl.NPLLiteral;
import npl.NormativeFailureException;
import npl.parser.ParseException;
import ora4mas.nopl.oe.Group;
import ora4mas.nopl.oe.Player;
import ora4mas.nopl.oe.Scheme;
import ora4mas.nopl.tools.os2nopl;

/**
 * Artifact to manage a scheme instance.
 * <br/><br/>
 * 
 * <b>Operations</b> (see details in the methods list below):
 * <ul>
 * <li>commitMission
 * <li>leaveMission
 * <li>goalAchieved
 * <li>setArgumentValue
 * <li>resetGoal
 * <li>destroy
 * </ul>
 * 
 * <b>Observable properties</b>:
 * <ul>
 * <li>commitment(ag,mission,sch): agent ag is committed to the mission in the scheme (we have as many obs prop as commitments).</br>
 *     e.g. <code>commitment(bob,mission1,s1)</code>
 * <li>groups: a list of groups responsible for the scheme.</br>
 *     e.g. <code>groups([g1])</code>
 * <li>goalState(schId, goal, list of committed agents, list of agents that performed the goal, state); where states are: waiting, enabled, satisfied).</br>
 *     e.g. <code>goalState(s1,g5,[alice,bob],[alice],satisfied)</code>
 * <li>specification: the specification of the scheme in the OS (a prolog like representation).
 * <li>obligation(ag,reason,goal,deadline): current active obligations.</br>
 *     e.g. <code>obligation(bob,ngoal(s1,mission1,g5),done(s1,bid,bob),1475417322254)</code>
 * <li>goalArgument(schemeId, goalId, argId, value): value of goals' arguments, defined by the operation setArgumentValue</br>
 *     e.g. <code>goalArgument(sch1, winner, "W", "Bob")</code>
 * </ul>
 * 
 * <b>Signals</b> (obligation o has the form: obligation(to whom, maintenance condition, what, deadline)):
 * <ul>
 * <li>oblCreated(o): the obligation <i>o</i> is created.
 * <li>oblFulfilled(o): the obligation <i>o</i> is fulfilled
 * <li>oblUnfulfilled(o): the obligation <i>o</i> is unfulfilled (e.g. by timeout).
 * <li>oblInactive(o): the obligation <i>o</i> is inactive (e.g. its maintenance condition does not hold anymore).</br>
 *    e.g. <code>o = obligation(Ag,_,done(Sch,bid,Ag), TTF)</code> if the <code>bid</code> is a performance goal and
 *         <code>o = obligation(Ag,_,satisfied(Sch,bid), TTF)</code> if the <code>bid</code> is an achievement goal.
 * <li>normFailure(f): the failure <i>f</i> has happened (e.g. due some regimentation).</br>
 *    e.g. <code>f = fail(mission_permission(Ag,M,Sch))</code>. The f comes from the normative program.
 * </ul>
 * 
 * @navassoc - specification - moise.os.fs.Scheme
 * @see moise.os.fs.Scheme
 * @author Jomi
 */
public class SchemeBoard extends OrgArt {

    private moise.os.fs.Scheme spec;
    
    public static final String obsPropSpec       = "specification";
    public static final String obsPropGroups     = "groups";
    public static final String obsPropCommitment = "commitment";
    
    public static final PredicateIndicator piGoalState = new PredicateIndicator("goalState", 5);

    protected Logger logger = Logger.getLogger(SchemeBoard.class.getName());

    public Scheme getSchState() {
        return (Scheme)orgState;
    }
    
    
    /**
     * Initialises the scheme artifact
     * 
     * @param osFile           the organisation specification file (path and file name)
     * @param schType          the type of the scheme (as defined in the OS)
     * @throws ParseException  if the OS file is not correct
     * @throws MoiseException  if schType was not specified
     */
    public void init(final String osFile, final String schType) throws ParseException, MoiseException {
        final OS os = OS.loadOSFromURI(osFile);
        spec = os.getFS().findScheme(schType);
        
        final String schId = getId().getName();
        orgState   = new Scheme(spec, schId);
        
        if (spec == null)
            throw new MoiseException("scheme "+schType+" does not exist!");
        
        oeId = getCreatorId().getWorkspaceId().getName();

        // load normative program
        initNormativeEngine(os, "scheme("+schType+")");
        installNormativeSignaler();
        initWspRuleEngine();

        // observable properties
        updateGoalStateObsProp();
        defineObsProperty(obsPropGroups,  getSchState().getResponsibleGroupsAsProlog());
        defineObsProperty(obsPropSpec, new JasonTermWrapper(spec.getAsProlog()));
        
        if (! "false".equals(Config.get().getProperty(Config.START_WEB_OI))) {
            WebInterface w = WebInterface.get();
            try {
                w.registerOEBrowserView(oeId, "/scheme/", schId, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }                    
    }
    
    @OPERATION public void debug(String kind) throws Exception {
        final String schId = getId().getName();
        if (kind.equals("inspector_gui(on)")) {
            gui = GUIInterface.add(schId, "... Scheme Board "+schId+" ("+spec.getId()+") ...", nengine, true);
            
            updateGUIThread = new UpdateGuiThread();
            updateGUIThread.start();
         
            updateGuiOE();
            
            gui.setNormativeProgram(getNPLSrc());
            gui.addSpecification(specToStr(spec.getFS().getOS(), DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("fsns"))));
        }
        if (kind.equals("inspector_gui(off)")) {
            System.out.println("not implemented yet, ask the developers to do so.");
        }    
    }
    
    /**
     * The agent executing this operation tries to delete the scheme board artifact 
     */
    @OPERATION public void destroy() {
        try {
            super.destroy();
            
            for (Group g: getSchState().getGroupsResponsibleFor()) {
                ArtifactId aid;
                try {
                    aid = lookupArtifact(g.getId());
                    if (aid != null)
                        execLinkedOp(aid, "removeScheme", getId().getName());
                } catch (OperationException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void agKilled(String agName) {
        //logger.info("****** "+agName+" has quit! Removing its missions.");
        for (Player p: orgState.getPlayers() ) {
            if (orgState.removePlayer(agName, p.getTarget())) {
                try {
                    logger.info(agName+" has quit, mission "+p.getTarget()+" removed by the platform!");
                    removeObsPropertyByTemplate(obsPropCommitment, 
                            new JasonTermWrapper(agName), 
                            new JasonTermWrapper(p.getTarget()), 
                            this.getId().getName());
                    //updateMonitorScheme();
                    updateGuiOE();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    /**
     * The agent executing this operation tries to commit to a mission in the scheme.
     * 
     * <p>Verifications:<ul> 
     *     <li>mission max cardinality</li>
     *     <li>mission permission (if the agent plays a role that permits it to commit to the mission)</li>
     * </ul>    
     * 
     * @param mission                     the mission being committed to
     * @throws NormativeFailureException  the failure produced if the adoption breaks some regimentation
     * @throws CartagoException           some cartago problem
     */
    @OPERATION public void commitMission(String mission) throws CartagoException {
        commitMission(getOpUserName(), mission);
    }
    private void commitMission(final String ag, final String mission) throws CartagoException {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                orgState.addPlayer(ag, mission);
                nengine.verifyNorms();
                
                defineObsProperty(obsPropCommitment, 
                        new JasonTermWrapper(ag), 
                        new JasonTermWrapper(mission), 
                        new JasonTermWrapper(SchemeBoard.this.getId().getName()));
                updateGoalStateObsProp();
                
                //updateMonitorScheme();
            }
        }, "Error committing to mission "+mission);
    }
    
    /**
     * The agent executing this operation tries to leave/remove its mission in the scheme
     * 
     * <p>Verifications:<ul> 
     *     <li>the agent must be committed to the mission</li>
     *     <li>the mission's goals have to be satisfied (otherwise the agent is obliged to commit again to the mission)</li>
     * </ul>
     * 
     * @param mission                     the mission being removed
     * @throws NormativeFailureException  the failure produced if the remove breaks some regimentation
     * @throws CartagoException           some cartago problem
     * @throws MoiseException             some moise inconsistency (the agent is not committed to the mission)
     */
    @OPERATION public void leaveMission(final String mission) throws CartagoException, MoiseException {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                if (orgState.removePlayer(getOpUserName(), mission)) {
                    nengine.verifyNorms();
                    
                    removeObsPropertyByTemplate(obsPropCommitment, 
                            new JasonTermWrapper(getOpUserName()), 
                            new JasonTermWrapper(mission), 
                            new JasonTermWrapper(SchemeBoard.this.getId().getName()));
                    
                    //updateMonitorScheme();
                }                
            }
        },"Error leaving mission "+mission);
    }
    
    /** The agent executing this operation set the goal as performed by it.
     *  
     * <p>Verifications:<ul> 
     *     <li>the agent must be committed to the goal</li>
     *     <li>the goal has to be enabled</li>
     * </ul>
     */
    @OPERATION public void goalAchieved(String goal) throws CartagoException {
        goalDone(getOpUserName(), goal);
    }
    
    private void goalDone(final String agent, final String goal) throws CartagoException {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                getSchState().addDoneGoal(agent, goal);
                nengine.verifyNorms();
                if (getSchState().computeSatisfiedGoals()) { // add satisfied goals
                    //nengine.setDynamicFacts(orgState.transform());        
                    nengine.verifyNorms();
                }
                //updateMonitorScheme();
    
                updateGoalStateObsProp();
            }
        },"Error achieving goal "+goal);
    }
    
    /** The agent executing this operation sets a value for a goal argument.
     *  
     *  @param goal                     The goal to which the value should be added
     *  @param var                      name of the variable to which the value is modified
     *  @param value                    value set to the variable of the goal
     */
    @OPERATION public void setArgumentValue(final String goal, final String var, final Object value) throws CartagoException {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                getSchState().setGoalArgValue(goal, var, value.toString());
                nengine.verifyNorms();
                //updateMonitorScheme();
    
                updateGoalStateObsProp();
                defineObsProperty("goalArgument", ASSyntax.createAtom(SchemeBoard.this.getId().getName()), 
                        new Atom(goal), 
                        ASSyntax.createString(var), 
                        value);
            }
        },"Error setting value of argument "+var+" of "+goal+" as "+value);
    }
    
    /** The agent executing this operation reset some goal.
     * It becomes not achieved, also goals that depends on it or sub-goals are set as unachieved    
     * @param goal                      The goal to be reset
     */
    @OPERATION public void resetGoal(final String goal) throws CartagoException {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                if (getSchState().resetGoal(spec.getGoal(goal))) {
                    getSchState().computeSatisfiedGoals();
                }
                nengine.verifyNorms();
                //updateMonitorScheme();
    
                updateGoalStateObsProp();
            }
        }, "Error reseting goal "+goal);
    }

    /**
     * Commands that the owner of the scheme can perform.
     * 
     * @param cmd, possible values (as strings):
     *     commitMission(<agent>,<mission>),
     *     goalDone(<agent>,<goal>) -- for performance goals --,
     *     goalSatisfied(<goal>) -- for achievement goals --
     *     
     * @throws CartagoException
     * @throws jason.asSyntax.parser.ParseException
     */
    @OPERATION @LINK public void admCommand(String cmd) throws CartagoException, jason.asSyntax.parser.ParseException {
        if (!running) return;
        // this operation is available only for the owner of the artifact
        if ((!getOpUserName().equals(ownerAgent)) && !getOpUserName().equals("workspace-manager")) {   
            failed("Error: agent '"+getOpUserName()+"' is not allowed to run "+cmd,"reason",new JasonTermWrapper("not_allowed_to_start(admCommand)"));
        } else {
            Literal lCmd = ASSyntax.parseLiteral(cmd);
            if (lCmd.getFunctor().equals("goalDone")) {
                goalDone(fixAgName(lCmd.getTerm(0).toString()), lCmd.getTerm(1).toString());
            } else if (lCmd.getFunctor().equals("commitMission")) {
                commitMission(fixAgName(lCmd.getTerm(0).toString()), lCmd.getTerm(1).toString());
            } else if (lCmd.getFunctor().equals("goalSatisfied")) {
                enableSatisfied(lCmd.getTerm(0).toString());
            }
        }
    }
    
    protected void enableSatisfied(final String goal) {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                getSchState().setAsSatisfied(goal);
                getSchState().computeSatisfiedGoals();
                nengine.verifyNorms();
                //updateMonitorScheme();
    
                updateGoalStateObsProp();
            }
        }, "Error setting goal "+goal+" as satisfied");
    }
    
    // used by Maicon in the interaction implementation
    @LINK public void interactionCommand(String cmd) throws CartagoException, jason.asSyntax.parser.ParseException {
        Literal lCmd = ASSyntax.parseLiteral(cmd);
        if (lCmd.getFunctor().equals("goalAchieved")) {
            goalDone(fixAgName(lCmd.getTerm(0).toString()), lCmd.getTerm(1).toString());
        }
    }
    
    @LINK void updateRolePlayers(final String grId, final Collection<Player> rp) throws NormativeFailureException, CartagoException {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                Group g = new Group(grId);
                for (Player p: rp)
                    g.addPlayer(p.getAg(), p.getTarget());
                g.addResponsibleForScheme(orgState.getId());
                
                boolean newLink = !getSchState().getGroupsResponsibleFor().contains(g);
                getSchState().addGroupResponsibleFor(g);
        
                nengine.verifyNorms();
        
                getObsProperty(obsPropGroups).updateValue(getSchState().getResponsibleGroupsAsProlog());
                if (newLink) {
                    // first time the group is linked to this scheme, create normative board
                    // create normative board
                    String nbId = grId+"."+orgState.getId();
                    ArtifactId aid = makeArtifact(nbId, NormativeBoard.class.getName(), new ArtifactConfig() );                    
                    execLinkedOp(aid, "load", os2nopl.transform(spec, false));
                    execLinkedOp(aid, "doSubscribeDFP", orgState.getId());
                }
            }
        }, null);
    }
    
    @LINK void removeResponsibleGroup(final String grId) throws CartagoException {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                getSchState().removeGroupResponsibleFor( new Group(grId) );
        
                nengine.verifyNorms();
                //updateMonitorScheme();
    
                getObsProperty(obsPropGroups).updateValue(getSchState().getResponsibleGroupsAsProlog());
            }
        }, null);
    }
    
    // list of obs props for goal states
    private List<ObsProperty> goalStObsProps = new ArrayList<ObsProperty>();
    
    protected void updateGoalStateObsProp() {
        List<Literal> goals = getGoalStates();

        // remove goals in obs prop that is no more in goal states
        Iterator<ObsProperty> iop = goalStObsProps.iterator();
        while (iop.hasNext()) {
            ObsProperty op = iop.next();
            
            // search in goals
            boolean found = false;
            Iterator<Literal> i = goals.iterator();
            while (i.hasNext()) {
                Literal g = i.next();
                
                if (isObsPropEqualsGoal(g,op)) {
                    found = true;
                    i.remove(); // this goal does not be added                    
                    break;
                }
            }
            
            if (!found) { // remove
                iop.remove();
                removeObsPropertyByTemplate(op.getName(), op.getValues());
            }
        }
        
        // add the remaining as new obs prop
        for (Literal goal: goals) {
            Object[] terms = getTermsAsProlog(goal);
            defineObsProperty(goal.getFunctor(), terms);
            goalStObsProps.add( getObsPropertyByTemplate(goal.getFunctor(), terms));
        }
    }

    private boolean isObsPropEqualsGoal(Literal g, ObsProperty op) {
        if (!g.getFunctor().equals(op.getName()))
            return false;
        for (int i=0; i<g.getArity(); i++) 
            //if (! ((JasonTermWrapper)op.getValue(i)).getTerm().equals(g.getTerm(i)) )
            if (! ((JasonTermWrapper)op.getValue(i)).toString().equals(g.getTerm(i).toString()) )
                return false;
        return true;
    }


    /*
    private void updateMonitorScheme() throws CartagoException {
        if (monitorSchArt != null) {
            execLinkedOp(monitorSchArt, "updateMonitoredScheme", orgState);
        }
    }
    */
    
    /*
    public static List<String> computeAccomplisedMissions(String schId, Collection<Mission> missions, NPLInterpreter nengine) {
        Atom aSch = new Atom(schId);
        List<String> as = new ArrayList<String>();
        for (Mission m: missions) {
            boolean all = true;
            for (Goal g: m.getGoals()) {
                //System.out.println(m+" "+g);
                Atom aGoal  = new Atom(g.getId());
                if (!nengine.holds(ASSyntax.createLiteral("satisfied", aSch, aGoal))) {
                    all = false;
                    //System.out.println("not ok for "+aSch+" "+aGoal);
                    break;
                }
            }
            if (all)
                as.add(m.getId());
        }
        return as;
    }
    */

    private static final Atom aWaiting   = new Atom("waiting");
    private static final Atom aEnabled   = new Atom("enabled");
    private static final Atom aSatisfied = new Atom("satisfied");
        
    List<Literal> getGoalStates() {
        List<Literal> all = new ArrayList<Literal>();
        Term tSch = ASSyntax.createAtom(this.getId().getName());
        for (Goal g: spec.getGoals()) {
            Atom aGoal  = new Atom(g.getId());
            Literal lGoal = ASSyntax.createLiteral(g.getId());
            
            // add arguments
            // removed to keep it compatible with obligation event
            // goalArgValue obs property was added for that
            /*if (g.hasArguments()) {
                for (String arg: g.getArguments().keySet()) {
                    String value = getSchState().getGoalArgValue(g.getId(), arg);
                    if (value == null) {
                        lGoal.addTerm(new VarTerm(arg));
                    } else {
                        try {
                            lGoal.addTerm(ASSyntax.parseTerm(value));
                        } catch (jason.asSyntax.parser.ParseException e) {
                            lGoal.addTerm(new StringTermImpl(value));
                        }
                    }
                }
            }*/
                
            // state
            Atom aState = aWaiting;
            if (nengine.holds(new NPLLiteral(ASSyntax.createLiteral("satisfied", tSch, aGoal), orgState))) { 
                aState = aSatisfied;
            } else if (nengine.holds(ASSyntax.createLiteral("well_formed", tSch)) && 
                nengine.holds(ASSyntax.createLiteral("enabled", tSch, aGoal))) {
                aState = aEnabled;
            }              

            // performed by
            ListTerm lAchievedBy = new ListTermImpl();
            ListTerm tail = lAchievedBy;
            for (Literal p: getSchState().getDoneGoals()) {
                if (p.getTerm(1).equals(aGoal))
                    tail = tail.append(p.getTerm(2));
            }
            
            // create the literal
            Literal lGoalSt = ASSyntax.createLiteral(
                    piGoalState.getFunctor(),
                    tSch,
                    lGoal,
                    getSchState().getCommittedAgents(g), // lCommittedBy
                    lAchievedBy,
                    aState);
            all.add(lGoalSt);
        }
        return all;
    }

    @Override
    public String getNPLSrc() {
        if (spec != null)
            return os2nopl.header(spec)+os2nopl.transform(spec, true);
        else
            return super.getNPLSrc();
    }
    
    protected String getStyleSheetName() {
        return "noplSchemeInstance";                
    }

    
    public Element getAsDOM(Document document) {
        Element schEle = (Element) document.createElement( SchemeInstance.getXMLTag());
        schEle.setAttribute("id", getSchState().getId());
        schEle.setAttribute("specification", spec.getId());
        schEle.setAttribute("root-goal", spec.getRoot().getId());
        schEle.setAttribute("owner", ownerAgent);

        Term aSch = ASSyntax.createAtom(this.getId().getName());

        // status
        Element wfEle = (Element) document.createElement("well-formed");
        if (nengine.holds(ASSyntax.createLiteral("well_formed", aSch))) {
            wfEle.appendChild(document.createTextNode("ok"));            
        } else {
            wfEle.appendChild(document.createTextNode("not ok"));  
        }
        schEle.appendChild(wfEle);
        
        // players
        if (!getSchState().getPlayers().isEmpty()) {
            Element plEle = (Element) document.createElement("players");
            for (Player p: getSchState().getPlayers()) {
                Element mpEle = (Element) document.createElement( MissionPlayer.getXMLTag());
                mpEle.setAttribute("mission", p.getTarget());
                mpEle.setAttribute("agent", p.getAg());
                plEle.appendChild(  mpEle );
            }
            schEle.appendChild(plEle);
        }

        // responsible groups
        Element rgEle = (Element) document.createElement("responsible-groups");
        for (Group g: getSchState().getGroupsResponsibleFor()) {
            Element gEle = (Element) document.createElement("group");
            gEle.setAttribute("id", g.getId());
            rgEle.appendChild(gEle);
        }
        schEle.appendChild(rgEle);

        // goals (with variable values)
        List<Literal> goals = getGoalStates();
        if (!goals.isEmpty()) {
            Element gsEle = (Element) document.createElement("goals");
            for (Literal lg: goals) {
                String gId = ((Literal)lg.getTerm(1)).getFunctor(); 
                Goal   gSpec = spec.getGoal(gId);
                Element giEle = (Element) document.createElement(GoalInstance.getXMLTag());
                giEle.setAttribute("specification", gId);
                giEle.setAttribute("state", lg.getTerm(4).toString());
                giEle.setAttribute("root", gSpec.isRoot()+"");
                giEle.setAttribute("committed-ags", lg.getTerm(2).toString());
                giEle.setAttribute("achieved-by", lg.getTerm(3).toString());
                StringBuilder spaces = new StringBuilder();
                for (int i=0; i<gSpec.getDepth(); i++)
                    spaces.append("  ");
                giEle.setAttribute("depth", spaces.toString());
                
                // arguments
                if (gSpec.hasArguments()) {
                    for (String arg: gSpec.getArguments().keySet()) {
                        String value = getSchState().getGoalArgValue(gId, arg);
                        Element argEle = (Element) document.createElement("argument");
                        argEle.setAttribute("id",arg);
                        if (value != null) {
                            argEle.setAttribute("value", value);
                        } else {
                            argEle.setAttribute("value", "undefined");                            
                        }
                        giEle.appendChild(argEle);                        
                    }
                }
                
                // plan
                if (gSpec.getPlan() != null) {
                    giEle.appendChild(gSpec.getPlan().getAsDOM(document));
                }
                // explicit dependencies
                
                for (Goal dg: gSpec.getPreConditionGoals()) {
                    Element ea = (Element) document.createElement("depends-on");
                    if (gSpec.hasDependence() && gSpec.getDependencies().contains(dg)) {
                        ea.setAttribute("explicit", "true");
                    }
                    ea.setAttribute("goal", dg.getId());
                    giEle.appendChild(ea);                
                }
                                
                gsEle.appendChild(giEle);
            }
            schEle.appendChild(gsEle);
        }

        return schEle;
    }
    
    public String getAsDot() {
        StringWriter so = new StringWriter();
        
        so.append("digraph "+getId()+" {ordering=out label=\""+getId()+": "+spec.getId()+"\" labelloc=t labeljust=r fontname=\"Italic\" \n");
        so.append("    rankdir=BT; \n");
        
        // goals
        so.append( os2dot.transform( spec.getRoot(), 0, this));

        // missions 
        for (Mission m: spec.getMissions()) {
            so.append( os2dot.transform(m, spec));
            for (Goal g: m.getGoals()) {
                so.append("        "+m.getId()+" -> "+g.getId()+" [arrowsize=0.5];\n");
            }                
        }
        for (Player p: getSchState().getPlayers()) {
            so.append("        "+p.getAg()+ "[shape=plaintext];\n");
            so.append("        "+p.getAg()+" -> "+p.getTarget()+" [arrowsize=0.5];\n");
        }

        so.append("}\n");
        //System.out.println(so);
        return so.toString();
    }

    /*
    @LINK void updateMonitoredScheme(Scheme monitoredSch) throws NormativeFailureException, CartagoException {
        // TODO
        //model.setMonitoredSch(monitoredSch);
        //nengine.setDynamicFacts(oe2np.transform(model));        
        //nengine.verifyNorms();
        //updateGuiOE();
    }
    */
}
