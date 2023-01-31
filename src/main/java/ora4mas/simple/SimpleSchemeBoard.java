package ora4mas.simple;

import java.util.logging.Logger;

import cartago.CartagoException;
import cartago.OPERATION;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.util.Config;
import moise.common.MoiseConsistencyException;
import moise.common.MoiseException;
import moise.os.OS;
import moise.os.fs.FS;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.fs.Plan;
import moise.os.fs.Plan.PlanOpType;
import moise.os.ns.NS;
import npl.NormativeFailureException;
import npl.parser.ParseException;
import ora4mas.nopl.JasonTermWrapper;
import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.WebInterface;
import ora4mas.nopl.oe.Scheme;

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
 * <li>permission(ag,reason,goal,deadline): current active permission.</br>
 *
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
public class SimpleSchemeBoard extends SchemeBoard {

    protected Logger logger = Logger.getLogger(SimpleSchemeBoard.class.getName());

    protected String ALL_GOALS = "all_goals";
    protected String SCHEME_NAME = "untyped";

    /**
     * Initialises the scheme artifact
     *
     */
    public void init() throws ParseException, MoiseException {
        OS os = new OS();
        FS fs = new FS(os);
        os.setFS(fs);
        NS ns = new NS(os);
        ns.setProperty("default_management", "ignore");
        os.setNS(ns);
        
        spec = new moise.os.fs.Scheme(SCHEME_NAME, fs);
        spec.setRoot(new Goal(ALL_GOALS));
        spec.getRoot().setPlan(new Plan(PlanOpType.parallel, spec, ALL_GOALS));
        spec.getRoot().setMinAgToSatisfy(0);

        fs.addScheme(spec);

        final String schId = getId().getName();
        orgState   = new Scheme(spec, schId);

        oeId = getCreatorId().getWorkspaceId().getName();

        // load normative program
        initNormativeEngine(os, "scheme(untyped)");
        installNormativeSignaler();
        initWspRuleEngine();

        // observable properties
        updateGoalStateObsProp();
        updateGoalArgsObsProp();
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
        schBoards.add(this);
    }
    
    @OPERATION public void addGoal(String goalId, String deps) throws MoiseException, ParseException, NormativeFailureException, jason.asSyntax.parser.ParseException {
    	Goal g = getOrCreateGoal(goalId, true);

        Literal lDeps = ASSyntax.parseLiteral(deps);
        if (lDeps.getFunctor().equals("dep") && lDeps.getArity() == 2) {
            if (lDeps.getTerm(0).toString().equals("and")) {
                for (Term t : (ListTerm)lDeps.getTerm(1)) {
                    g.addDependence(getOrCreateGoal(t.toString(), true));
                }
            } else if (lDeps.getTerm(0).toString().equals("or")) {
                Goal po = getOrCreateGoal(goalId + "_dep", false);
//                spec.removeMission(spec.getMission(po.toString()));
                po.setMinAgToSatisfy(0);
//            po = new Goal(goalId); // no mission for this goal
//            spec.addGoal(g);

                Plan p = new Plan(PlanOpType.choice, spec, po.toString());
                spec.addPlan(p);
                for (Term t : (ListTerm)lDeps.getTerm(1)) {
                    Goal sg = getOrCreateGoal(t.toString(), true);
                    p.addSubGoal(sg);
                    sg.setInPlan(p);
                    spec.getRoot().getPlan().removeSubGoal(sg);
                }

                po.setPlan(p);
                g.addDependence(po);
            } else {
                failed("Wrong second argument for addGoal. Should be 'dep_and(a,b,c)' or 'dep_or(a,b,c)', but was informed '" + deps + "'.");
            }
        }
        updateGoalStateObsProp();
        postReorgUpdates(spec.getFS().getOS(), "scheme(untyped)", "fs");
        getObsProperty(obsPropSpec).updateValue(new JasonTermWrapper(spec.getAsProlog()));
    }

    Goal getOrCreateGoal(String goalId, boolean addMission) throws MoiseConsistencyException {
    	Goal g = spec.getGoal(goalId);
    	if (g == null) {
    		g = new Goal(goalId);      		
    		spec.addGoal(g);
    		spec.getRoot().getPlan().addSubGoal(goalId);
    		g.setInPlan(spec.getRoot().getPlan());
    	}

        if (addMission) {
            // create a corresponding mission
            Mission m = spec.getMission(goalId);
            if (m == null) {
                m = new Mission(goalId, spec);
                spec.addMission(m);
            }
            m.addGoal(goalId);
        }
    	return g;
    }

    @OPERATION public void commitGoal(String goalMission) throws CartagoException {
    	commitMission(getOpUserName(), goalMission);
    }

    @Override
    protected boolean addMissionsInDot() {
    	return false;
    }
    
}
