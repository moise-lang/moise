package ora4mas.nopl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import npl.*;
import org.xml.sax.InputSource;

import cartago.AbstractWSPRuleEngine;
import cartago.AgentQuitRequestInfo;
import cartago.Artifact;
import cartago.ArtifactId;
import cartago.CartagoEnvironment;
import cartago.CartagoException;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OperationException;
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
    public final static String sglSanction       = "sanction";
    public final static String sglDestroyed      = "destroyed";

    protected NPLInterpreter nengine;
    protected NormativeListener myNPLListener;

    protected CollectiveOE       orgState;
    //protected ArtifactId         monitorSchArt = null;
    protected GUIInterface gui = null;

    protected boolean running = true;
    protected UpdateGuiThread updateGUIThread = null;

    protected String oeId = null; // the id of the org

    protected String ownerAgent = null; // the name of the agent that created this artifact

    protected List<ArtifactId> dfpListeners = new CopyOnWriteArrayList<>();
    protected List<ArtifactId> normativeEvtListeners = new CopyOnWriteArrayList<>();

    protected String orgBoardName = null;
    
    protected Logger logger = Logger.getLogger(OrgArt.class.getName());
    protected Logger getLogger() {  return logger;   }

    protected boolean runningDestroy = false;

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
        var p = new NormativeProgram();
        try {
            new nplp(new StringReader(os2nopl.transform(os))).program(p, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        var root = p.getRoot();
        var scope = root.findScope(type);
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
        runningDestroy = true;
        
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
            @Override public void created(NormInstance o) {
                beginExtSession();
                try {
                    defineObsProperty(o.getFunctor(), getTermsAsProlog(o)).addAnnot( getNormIdTerm(o) );
                    notifyNormativeEventListeners(sglOblCreated+"("+o+")");
                } finally {
                    endExtSession();                   
                }
                //signalsQueue.offer(new Pair<String, Structure>(sglOblCreated, o));
            }
            @Override public void fulfilled(NormInstance o) {
                try {
                    beginExtSession();
                    removeObsPropertyByTemplate(o.getFunctor(), getTermsAsProlog(o)); // cause concurrent modification on cartago
                    signal(sglOblFulfilled, new JasonTermWrapper(o));
                    notifyNormativeEventListeners(sglOblFulfilled+"("+o+")");
                } catch (java.lang.IllegalArgumentException e) {
                    // ignore, the obligations was not there anymore
                } finally {
                    endExtSession();                   
                }
                //execInternalOp("NPISignals", sglOblFulfilled, o);
            }
            @Override public void unfulfilled(NormInstance o) {
                try {
                    beginExtSession();
                    removeObsPropertyByTemplate(o.getFunctor(), getTermsAsProlog(o));
                    signal(sglOblUnfulfilled, new JasonTermWrapper(o));
                    notifyNormativeEventListeners(sglOblUnfulfilled+"("+o+")");
                } catch (Exception e) {
                    logger.log(Level.FINE, "error removing obs prop for "+o, e);
                } finally {
                    endExtSession();                   
                }
                //execInternalOp("NPISignals", sglOblUnfulfilled, o);
            }
            @Override public void inactive(NormInstance o) {
                try {
                    beginExtSession();
                    removeObsPropertyByTemplate(o.getFunctor(), getTermsAsProlog(o));
                } catch (java.lang.IllegalArgumentException e) {
                    // ignore, the obligations was not there anymore
                } finally {
                    endExtSession();                   
                }
            }

            @Override public void failure(Structure f) {
                try {
                    beginExtSession();
                    //execInternalOp("NPISignals", sglNormFailure, f);
                    signal(sglNormFailure, new JasonTermWrapper(f));
                    notifyNormativeEventListeners(sglNormFailure+"("+f+")");
                } finally {
                    endExtSession();                   
                }
            }

            @Override
            public void sanction(String normId, NPLInterpreter.EventType event, Structure s) {
                try {
                    beginExtSession();
                    signal(sglSanction,
                            new JasonTermWrapper(normId),
                            new JasonTermWrapper(event.name()),
                            s);
                    notifyNormativeEventListeners(sglSanction+"("+normId+","+event+","+s+")");
                } finally {
                    endExtSession();
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

    // subscribe / listners protocol

    @LINK void subscribeDFP(ArtifactId subscriber) throws OperationException {
        dfpListeners.add(subscriber);
        notifyDFPListeners();
    }

    void notifyDFPListeners() {
        for (ArtifactId aid: dfpListeners) {
            try {
                execLinkedOp(aid, "updateDFP", getId().getName(), (DynamicFactsProvider)orgState);
            } catch (Exception e) {
                System.out.println("error with listener "+aid.getName()+", we failed to contact it."+e);
                e.printStackTrace();
            }
        }
    }

    @LINK void subscribeNormativeEvents(ArtifactId subscriber) throws OperationException {
        normativeEvtListeners.add(subscriber);
    }
    void notifyNormativeEventListeners(String evt) {
        for (ArtifactId aid: normativeEvtListeners) {
            try {
                execLinkedOp(aid, "normativeEvent", evt);
            } catch (Exception e) {
                // TODO: I do not know why the error occurs, commented the msg for now !!!
                //  System.out.println("error with listener "+aid.getName()+", we failed to contact it."+e);
                //e.printStackTrace();
            }
        }
    }

    protected void ora4masOperationTemplate(Operation op, String errorMsg) {
        if (runningDestroy) return; 
        if (!running) return;
        CollectiveOE bak = orgState.clone();
        try {
            op.exec();
            updateGuiOE();
            notifyDFPListeners();
        } catch (NormativeFailureException e) {
            orgState = bak; // takes the backup as the current model since the action failed
            getLogger().info("error. "+e);
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

    static Object[] getTermsAsProlog(NormInstance o) {
        Object[] terms = new Object[o.getArity()];
        int i = 0;
        for (Term t: o.getTerms())
            terms[i++] = new JasonTermWrapper(t);
        return terms;
    }

    static Object getNormIdTerm(NormInstance o) {
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
        if (spec == null)
            return "";
        StringWriter so = new StringWriter();
        String specStr = DOMUtils.dom2txt(spec);
        if (!specStr.isEmpty()) {
            InputSource si = new InputSource(new StringReader(specStr));
            transformer.transform(new DOMSource(getParser().parse(si)), new StreamResult(so));
        }
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
                } catch (InterruptedException e) {
                    // ignore, system going down
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static Ora4masWSPRuleEngine wspEng = null;

    public synchronized void initWspRuleEngine() {
        if (wspEng == null) {
            wspEng = new Ora4masWSPRuleEngine();
            String wks = getId().getWorkspaceId().getFullName();
            new Thread() {
                public void run() {
                    try {
                        // TODO: use new cartago API
                        //CartagoBasicContext cartagoCtx = new CartagoBasicContext("OrgArt setup", CartagoEnvironment.ROOT_WSP_DEFAULT_NAME);
                        //cartagoCtx.doAction(new Op("setWSPRuleEngine", wspEng), -1);
                         
                        CartagoEnvironment.getInstance().resolveWSP(wks).getWorkspace().setWSPRuleEngine(wspEng);
                    } catch (CartagoException e) {
                        logger.warning("Error setting up setWSPRuleEngine: "+e.getMessage());
                        e.printStackTrace();
                    }
                };
            }.start();
        }
        wspEng.addListener(this);
    }

    public void agKilled(String agName) throws Exception {

    }


    protected void debug(String kind, String title, boolean hasOE) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
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
            } catch (InterruptedException e) {
                //System.out.println("*** exiting GUI ***");
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
        for (var l: nengine.getFacts())
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
