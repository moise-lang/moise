package ora4mas.nopl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import cartago.AbstractWSPRuleEngine;
import cartago.AgentQuitRequestInfo;
import cartago.Artifact;
import cartago.ArtifactId;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.LINK;
import cartago.OPERATION;
import cartago.Op;
import cartago.OperationException;
import cartago.util.agent.CartagoBasicContext;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.runtime.SourcePath;
import moise.common.MoiseException;
import moise.os.OS;
import moise.xml.DOMUtils;
import moise.xml.ToXML;
import npl.DeonticModality;
import npl.DynamicFactsProvider;
import npl.NPLInterpreter;
import npl.NormativeFailureException;
import npl.NormativeListener;
import npl.NormativeProgram;
import npl.Scope;
import npl.parser.ParseException;
import npl.parser.nplp;
import ora4mas.nopl.oe.CollectiveOE;
import ora4mas.nopl.tools.os2nopl;

/** Common class for all organisational artifacts */
public abstract class OrgArt extends Artifact implements ToXML, DynamicFactsProvider {

    // signals
    public final static String sglOblCreated     = "oblCreated";
    public final static String sglOblFulfilled   = "oblFulfilled";
    public final static String sglOblUnfulfilled = "oblUnfulfilled";
    public final static String sglOblInactive    = "oblInactive";
    public final static String sglNormFailure    = "normFailure";

    public final static String sglDestroyed      = "destroyed";

    protected NPLInterpreter     nengine;
    protected NormativeListener  myNPLListener;

    protected CollectiveOE       orgState;
    //protected ArtifactId         monitorSchArt = null;
    protected GUIInterface gui = null;

    protected boolean running = true;
    protected UpdateGuiThread updateGUIThread = null;

    protected String oeId = null; // the id of the org

    protected String ownerAgent = null; // the name of the agent that created this artifact

    protected List<ArtifactId> listeners = new ArrayList<>();

    protected String orgBoardName = null;
    
    protected Logger logger = Logger.getLogger(OrgArt.class.getName());

    public static String fixOSFile(String osFile) {
        try {
            if (!new File(osFile).exists()) {
                // try from jar
                OS.class.getResource("/"+osFile).openStream();
                return SourcePath.CRPrefix + "/" + osFile;
            }
        } catch (Exception e) { }
        return osFile;
    }
    
    public String getOEId() {
        return oeId;
    }
    public String getArtId() {
        return getId().getName();
    }

    protected void initNormativeEngine(OS os, String type) throws MoiseException, ParseException {
        if (nengine != null)
            nengine.stop();

        nengine = new NPLInterpreter();

        //System.out.println(os2nopl.transform(os));
        NormativeProgram p = new NormativeProgram();
        new nplp(new StringReader(os2nopl.transform(os))).program(p, this);
        Scope root = p.getRoot();
        Scope scope = root.findScope(type);
        if (scope == null)
            throw new MoiseException("scope for "+type+" does not exist!");
        nengine.loadNP(scope);
    }

    public NPLInterpreter getNormativeEngine() {
        return nengine;
    }

    @OPERATION public void setOwner(String artOwner) {
        if (ownerAgent == null)
            ownerAgent = artOwner;
        else if (ownerAgent.equals(getOpUserName()))
            ownerAgent = artOwner;
        else
            failed("you can not change the owner");
    }
    
    @LINK public void setOrgBoardName(String n) {
        this.orgBoardName = n;
    }
    
    protected boolean isUserAllowed() {
        return  ownerAgent == null ||
                getOpUserName().equals(ownerAgent) || 
                getOpUserName().equals("workspace-manager");
    }

    protected void destroy() {
        if (ownerAgent != null && getCurrentOpAgentId() != null && (!getOpUserName().equals(ownerAgent)) ) {
            failed("you can not destroy the artifact, only the owner can!");
            return;
        }

        nengine.removeListener(myNPLListener);
        nengine.stop();
        if (gui != null) {
            gui.remove();
        }
        /*if (monitorSchArt != null) {
            try {
                execLinkedOp(monitorSchArt, "destroy");
            } catch (OperationException e) {
                e.printStackTrace();
            }
        }*/
        signal(sglDestroyed, new Atom(getId().getName()));

        running = false;
        if (updateGUIThread != null)
            updateGUIThread.interrupt();
        if (WebInterface.isRunning())
            WebInterface.get().removeOE(oeId, getId().getName());

        try {
            dispose(getId());
        } catch (OperationException e) {
            e.printStackTrace();
        }
    }

    protected void installNormativeSignaler() {
        // version that works (using internal op)
        myNPLListener = new NormativeListener() {
            public void created(DeonticModality o) {
                beginExternalSession();
                try {
                    defineObsProperty(o.getFunctor(), getTermsAsProlog(o)).addAnnot( getNormIdTerm(o) );
                } finally {
                    endExternalSession(true);                   
                }
                //signalsQueue.offer(new Pair<String, Structure>(sglOblCreated, o));
            }
            public void fulfilled(DeonticModality o) {
                try {
                    beginExternalSession();
                    removeObsPropertyByTemplate(o.getFunctor(), getTermsAsProlog(o)); // cause concurrent modification on cartago
                    signal(sglOblFulfilled, new JasonTermWrapper(o));
                } catch (java.lang.IllegalArgumentException e) {
                    // ignore, the obligations was not there anymore
                } finally {
                    endExternalSession(true);                   
                }
                //execInternalOp("NPISignals", sglOblFulfilled, o);
            }
            public void unfulfilled(DeonticModality o) {
                try {
                    beginExternalSession();
                    removeObsPropertyByTemplate(o.getFunctor(), getTermsAsProlog(o));
                    signal(sglOblUnfulfilled, new JasonTermWrapper(o));
                } catch (Exception e) {
                    logger.log(Level.FINE, "error removing obs prop for "+o, e);
                } finally {
                    endExternalSession(true);                   
                }
                //execInternalOp("NPISignals", sglOblUnfulfilled, o);
            }
            public void inactive(DeonticModality o) {
                try {
                    beginExternalSession();
                    removeObsPropertyByTemplate(o.getFunctor(), getTermsAsProlog(o));
                } catch (java.lang.IllegalArgumentException e) {
                    // ignore, the obligations was not there anymore
                } finally {
                    endExternalSession(true);                   
                }
            }

            public void failure(Structure f) {
                try {
                    beginExternalSession();
                    //execInternalOp("NPISignals", sglNormFailure, f);
                    signal(sglNormFailure, new JasonTermWrapper(f));                    
                } finally {
                    endExternalSession(true);                   
                }
            }
        };

        // version that does not work
        /*
        myNPLListener = new NormativeListener() {
            public void created(DeonticModality o) {
                defineObsProperty(o.getFunctor(), getTermsAsProlog(o));
            }
            public void fulfilled(DeonticModality o) {
                try {
                    beginExternalSession();
                    removeObsPropertyByTemplate(o.getFunctor(), getTermsAsProlog(o)); // cause concurrent modification on cartago
                    signal(sglOblFulfilled, new JasonTermWrapper(o));
                    endExternalSession(true);
                } catch (java.lang.IllegalArgumentException e) {
                    // ignore, the obligations was not there anymore
                }
            }
            public void unfulfilled(DeonticModality o) {
                beginExternalSession();
                removeObsPropertyByTemplate(o.getFunctor(), getTermsAsProlog(o));
                signal(sglOblUnfulfilled, new JasonTermWrapper(o));
                endExternalSession(true);
            }
            public void inactive(DeonticModality o) {
                beginExternalSession();
                removeObsPropertyByTemplate(o.getFunctor(), getTermsAsProlog(o));
                //signal(sglOblInactive, new JasonTermWrapper(o));
                endExternalSession(true);
            }

            public void failure(Structure f) {
                beginExternalSession();
                signal(sglNormFailure, new JasonTermWrapper(f));
                endExternalSession(true);
            }
        };
        */
        nengine.addListener(myNPLListener);
    }

    // Manage the signal related to changes in NPI ===> too slow!
    // TODO: wait for a better solution from cartago
    // solved now by exec int op

    //Queue<Pair<String, Structure>> signalsQueue = new ConcurrentLinkedQueue<Pair<String,Structure>>();

    /*
    @INTERNAL_OPERATION void NPISignals() {
        Pair<String,Structure> s = null;
        while (running) {
            s = signalsQueue.poll();
            while (s != null) {
                //System.out.println("******"+s);
                signal(s.getFirst(), new JasonTermWrapper(s.getSecond()));
                s = signalsQueue.poll();
            }
            await_time(500);
        }
    }
    */

    /*@INTERNAL_OPERATION void NPISignals(String signal, Term arg) {
        signal(signal, new JasonTermWrapper(arg));
    }*/

    // subscribe protocol

    @LINK void subscribeDFP(ArtifactId subscriber) throws OperationException {
        listeners.add(subscriber);
        notifyListeners();
    }

    void notifyListeners() {
        for (ArtifactId aid: listeners) {
            try {
                execLinkedOp(aid, "updateDFP", getId().getName(), (DynamicFactsProvider)orgState);
            } catch (Exception e) {
                System.out.println("error with listener "+aid.getName()+", we failed to contact it."+e);
                e.printStackTrace();
            }
        }
    }


    protected void ora4masOperationTemplate(Operation op, String errorMsg) {
        if (!running) return;
        CollectiveOE bak = orgState.clone();
        try {
            op.exec();
            updateGuiOE();
            notifyListeners();
        } catch (NormativeFailureException e) {
            orgState = bak; // takes the backup as the current model since the action failed
            logger.info("error. "+e);
            if (errorMsg == null)
                failed(e.getFail().toString());
            else
                failed(errorMsg, "reason", new JasonTermWrapper(e.getFail()));
        } catch (Exception e) {
            orgState = bak;
            e.printStackTrace();
            failed(errorMsg+". "+e.toString());
        }
    }

    public void postReorgUpdates(OS os, String nplId, String ss) throws MoiseException, ParseException {
        // change normative program
        initNormativeEngine(os, nplId);
        installNormativeSignaler();

        updateGuiOE();

        try {
            if (gui != null) {
                gui.setNormativeProgram(getNPLSrc());
                gui.setSpecification(specToStr(os, DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL(ss))));
            }

            /*if (WebInterface.isRunning()) {
                String osSpec = specToStr(os, DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("os")));
                String oeId = getCreatorId().getWorkspaceId().getName();

                WebInterface.get().registerOSBrowserView(oeId, os.getId(), osSpec);
            }
            */
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static Object[] getTermsAsProlog(DeonticModality o) {
        Object[] terms = new Object[o.getArity()];
        int i = 0;
        for (Term t: o.getTerms())
            terms[i++] = new JasonTermWrapper(t);
        return terms;
    }

    static Object getNormIdTerm(DeonticModality o) {
        return new JasonTermWrapper(
                ASSyntax.createStructure(
                        "norm",
                        new Atom(o.getNorm().getId()),
                        o.getUnifierAsTerm()));
    }

    static Object[] getTermsAsProlog(Literal o) {
        Object[] terms = new Object[o.getArity()];
        int i = 0;
        for (Term t: o.getTerms())
            terms[i++] = new JasonTermWrapper(t);
        return terms;
    }


    abstract protected String getStyleSheetName();

    protected String getAsDot() {
        return null;
    }

    private DocumentBuilder parser;
    public DocumentBuilder getParser() throws ParserConfigurationException {
        if (parser == null)
            parser = DOMUtils.getParser();
        return parser;
    }


    public String specToStr(ToXML spec, Transformer transformer) throws Exception {
        StringWriter so = new StringWriter();
        InputSource si = new InputSource(new StringReader(DOMUtils.dom2txt(spec)));
        transformer.transform(new DOMSource(getParser().parse(si)), new StreamResult(so));
        return so.toString();
    }


    private Transformer guiStyleSheet = null;
    public Transformer getStyleSheet() throws TransformerConfigurationException, IOException {
        if (getStyleSheetName() == null)
            return null;
        if (guiStyleSheet == null)
            guiStyleSheet = DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL( getStyleSheetName() ));
        return guiStyleSheet;
    }

    private Transformer nsTransformer = null;
    public Transformer getNSTransformer() throws TransformerConfigurationException, TransformerFactoryConfigurationError, IOException {
        if (nsTransformer == null)
            nsTransformer = DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("nstate"));
        return nsTransformer;
    }

    protected void updateGuiOE() {
        if (gui != null && updateGUIThread != null)
            updateGUIThread.update();
    }

    /** manages listener to be notified about agents that quit the system */

    class Ora4masWSPRuleEngine extends AbstractWSPRuleEngine {
        List<OrgArt> l = new ArrayList<>();

        void addListener(OrgArt o) {
            l.add(o);
        }

        @Override
        protected void processAgentQuitRequest(AgentQuitRequestInfo req) {
            for (OrgArt o: l) {
                try {
                    o.agKilled(req.getAgentId().getAgentName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static Ora4masWSPRuleEngine wspEng = null;

    public synchronized void initWspRuleEngine() {
        /*ICartagoController ctrl = CartagoService.getController("default");
        for (ArtifactId aid: ctrl.getCurrentArtifacts()) {
            System.out.println("*** "+aid);
        }*/

        if (wspEng == null) {
            wspEng = new Ora4masWSPRuleEngine();
            new Thread() {
                public void run() {
                    try {
                        // TODO: use new cartago API
                        CartagoBasicContext cartagoCtx = new CartagoBasicContext("OrgArt setup", CartagoService.MAIN_WSP_NAME);
                        cartagoCtx.doAction(new Op("setWSPRuleEngine", wspEng), -1);
                    } catch (CartagoException e) {
                        e.printStackTrace();
                    }
                };
            }.start();
        }
        wspEng.addListener(this);
    }

    public void agKilled(String agName) {

    }


    protected void debug(String kind, String title, boolean hasOE) throws Exception {
        final String id = getId().getName();
        if (kind.equals("inspector_gui(on)")) {
            if (gui != null)
                gui.remove();
            gui = GUIInterface.add(id, "... "+title+" "+id+" ...", nengine, hasOE);

            updateGUIThread = new UpdateGuiThread();
            updateGUIThread.start();

            updateGuiOE();

            gui.setNormativeProgram(getNPLSrc());
        }
        if (kind.equals("inspector_gui(off)")) {
            if (gui != null)
                gui.remove();
            try {
                updateGUIThread.interrupt();
            } catch (Exception e) {}
            gui = null;
        }
    }

    class UpdateGuiThread extends Thread {
        boolean ok = false;

        void update() {
            ok = false;
            //notifyAll();
        }

        @Override
        public void run() {
            try {
                while (running) {
                    if (ok)
                        sleep(1000);
                    else
                        sleep(100); // always sleep a bit
                    if (!ok) {
                        ok = true;
                        try {
                            if (gui != null) {
                                gui.updateOE(OrgArt.this, getStyleSheet());
                                gui.updateNFacts(getDebugText());
                            }
                        } catch (ConcurrentModificationException e) {
                            ok = false;
                            // ignore try later
                        } catch (Exception e) {
                            ok = false;
                            e.printStackTrace();
                        }
                    }
                }
            } catch (InterruptedException e) {
                // no problem, just quit
            }
        }
    }

    public String getDebugText() {
        StringBuilder out = new StringBuilder();
        String space = "";
        if (orgState != null) {
            out.append(orgState.toString());
            out.append("\n\n\n** dynamic facts:\n");
            for (Literal l: orgState.transform())
                out.append("     "+l+"\n");
            out.append("\n\n\n** facts:\n");
            space = "     ";
        }
        for (Literal l: nengine.getAg().getBB())
            out.append(space+l+"\n");
        return out.toString();
    }

    public String getNPLSrc() {
        return "";
    }

    protected static String fixAgName(String ag) {
        if (ag.startsWith("\""))
            return ag.substring(1, ag.length()-1);
        else
            return ag;
    }

    public String getOpUserName() {
    	if (getCurrentOpAgentId() == null) 
    		return "none";
    	else
    		return getCurrentOpAgentId().getAgentName();
    }

    // DFP methods

    public boolean isRelevant(PredicateIndicator pi) {
        return orgState.isRelevant(pi);
    }

    public Iterator<Unifier> consult(Literal l, Unifier u) {
        return orgState.consult(l, u);
    }
}
