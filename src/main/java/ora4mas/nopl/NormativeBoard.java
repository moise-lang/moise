package ora4mas.nopl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cartago.ArtifactId;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OperationException;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.util.Config;
import moise.common.MoiseException;
import npl.DynamicFactsProvider;
import npl.NPLInterpreter;
import npl.NormativeFailureException;
import npl.NormativeProgram;
import npl.parser.ParseException;
import npl.parser.nplp;
import ora4mas.nopl.oe.CollectiveOE;

/**
 * Artifact to manage a normative program (NPL)
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
 * @author Jomi
 */
public class NormativeBoard extends OrgArt {

    protected Map<String, DynamicFactsProvider> dynProviders = new HashMap<String, DynamicFactsProvider>();
    
    protected Logger logger = Logger.getLogger(NormativeBoard.class.getName());

    /**
     * Initialises the normative artifact
     */
    public void init() {
        oeId = getCreatorId().getWorkspaceId().getName();
        String nbId = getId().getName();

        nengine = new NPLInterpreter();
        nengine.init();
        installNormativeSignaler();

        if (! "false".equals(Config.get().getProperty(Config.START_WEB_OI))) {
            WebInterface w = WebInterface.get();
            try {
                w.registerOEBrowserView(oeId, "/norm/", nbId, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }         
    }
    
    /**
     * Loads a normative program
     * 
     * @param nplProgram       a string with the NPL program (or a file name)
     *
     * @throws ParseException  if the OS file is not correct
     * @throws MoiseException  if schType was not specified
     */
    @OPERATION @LINK public void load(String nplProgram) throws ParseException, MoiseException, FileNotFoundException {
        NormativeProgram p = new NormativeProgram();

        File f = new File(nplProgram);
        if (f.exists()) {
            new nplp(new FileReader(nplProgram)).program(p, this);
        } else {
            new nplp(new StringReader(nplProgram)).program(p, this);
        }
        nengine.loadNP(p.getRoot());
        
        if (gui != null) {
            gui.setNormativeProgram(getNPLSrc());
        }
    }

    @OPERATION public void debug(String kind) throws Exception {
        final String schId = getId().getName();
        if (kind.equals("inspector_gui(on)")) {
            gui = GUIInterface.add(schId, "... Norm Board "+schId+" ...", nengine, false);
            
            updateGUIThread = new UpdateGuiThread();
            updateGUIThread.start();
         
            updateGuiOE();
            
            gui.setNormativeProgram(getNPLSrc());
        }
        if (kind.equals("inspector_gui(off)")) {
            System.out.println("not implemented yet, ask the developers to do so.");
        }    
    }
    
    @OPERATION void addFact(String f) throws jason.asSyntax.parser.ParseException, NormativeFailureException {
        nengine.addFact(ASSyntax.parseLiteral(f));
        nengine.verifyNorms();
        updateGuiOE();
    }
    
    @OPERATION void removeFact(String f) throws jason.asSyntax.parser.ParseException, NormativeFailureException {
        nengine.removeFact(ASSyntax.parseLiteral(f));
        nengine.verifyNorms();
        updateGuiOE();
    }

    @LINK void updateDFP(String id, DynamicFactsProvider p) throws NormativeFailureException {
        dynProviders.put(id, p);
        nengine.verifyNorms();
        updateGuiOE();
    }
    

    @OPERATION @LINK void doSubscribeDFP(String artName) throws OperationException {
        ArtifactId aid = lookupArtifact(artName);
        execLinkedOp(aid, "subscribeDFP", getId());
    }
    
    
    @Override
    public String getDebugText() {
        boolean first = true;
        StringBuilder out = new StringBuilder(super.getDebugText());
        for (DynamicFactsProvider p: dynProviders.values()) {
            System.out.println(p);
            if (p instanceof CollectiveOE) {
                for (Literal l: ((CollectiveOE)p).transform()) {
                    if (first) {
                        out.append("\n\n** dynamic facts:\n");
                        first = false;
                    }
                    out.append("     "+l+"\n");
                }
            }
        }
        return out.toString();
    }
    
    @Override
    public String getNPLSrc() {
        return nengine.getNormsString();
    }
    
    protected String getStyleSheetName() {
        return null;                 
    }
    
    public Element getAsDOM(Document document) {
        return nengine.getAsDOM(document);
    }
    
    // DFP methods
    
    public boolean isRelevant(PredicateIndicator pi) {
        for (DynamicFactsProvider p: dynProviders.values())
            if (p.isRelevant(pi))
                return true;
        return false;
    }
    
    public Iterator<Unifier> consult(Literal l, Unifier u) {
        for (DynamicFactsProvider p: dynProviders.values())
            if (p.isRelevant(l.getPredicateIndicator())) 
                return p.consult(l, u);
        return null;
    }

}
