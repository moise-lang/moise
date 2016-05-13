package ora4mas.nopl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import cartago.AbstractWSPRuleEngine;
import cartago.AgentQuitRequestInfo;
import cartago.Artifact;
import cartago.ArtifactId;
import cartago.CartagoException;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import cartago.Op;
import cartago.OperationException;
import cartago.util.agent.CartagoBasicContext;
import jason.architecture.MindInspectorWeb;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import moise.common.MoiseException;
import moise.os.OS;
import moise.xml.DOMUtils;
import moise.xml.ToXML;
import npl.DeonticModality;
import npl.DynamicFactsProvider;
import npl.NPLInterpreter;
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
    protected ArtifactId         monitorSchArt = null;
    protected OrgArtNormativeGUI gui = null;
    
    protected boolean running = true;
    protected UpdateGuiThread updateGUIThread = null;
    
    protected String ownerAgent = null; // the name of the agent that created this artifact
    
    public NPLInterpreter getNPLInterpreter() {
        return nengine;
    }
    
    protected void initNormativeEngine(OS os, String type) throws MoiseException, ParseException {
        nengine = new NPLInterpreter();

        //System.out.println(os2nopl.transform(os));
        NormativeProgram p = new NormativeProgram();
        new nplp(new StringReader(os2nopl.transform(os))).program(p, this);
        Scope root = p.getRoot();
        //if (inScope != null)
        //    root = p.getRoot().findScope(inScope);
        Scope scope = root.findScope(type);
        if (scope == null)
            throw new MoiseException("scope for "+type+" does not exist!");            
        nengine.setScope(scope);
        //execInternalOp("NPISignals");                
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
    protected void destroy() {
        if (ownerAgent != null && getOpUserId() != null && (!getOpUserName().equals(ownerAgent)) ) {
            failed("you can not destroy the artifact, only the owner can!");
            return;
        }

        nengine.removeListener(myNPLListener);
        nengine.stop();
        if (gui != null) {
            gui.remove();
        }
        if (monitorSchArt != null) {
            try {
                execLinkedOp(monitorSchArt, "destroy");
            } catch (OperationException e) {
                e.printStackTrace();
            }
        }
        signal(sglDestroyed, getId().getName());
        running = false;
        if (updateGUIThread != null)
            updateGUIThread.interrupt();
                
    }
    
    protected void installNormativeSignaler() {
        myNPLListener = new NormativeListener() {
            public void created(DeonticModality o) {  
                defineObsProperty(o.getFunctor(), getTermsAsProlog(o));
                //signalsQueue.offer(new Pair<String, Structure>(sglOblCreated, o));
            }
            public void fulfilled(DeonticModality o) {
                try {
                    removeObsPropertyByTemplate(o.getFunctor(), getTermsAsProlog(o)); // cause concurrent modification on cartago
                    // signal does not work if not initiated by an OPERATION
                    // signalQueue is toooo slow!
                    
                    //signal(sglOblFulfilled, new JasonTermWrapper(o));
                    //signalsQueue.offer(new Pair<String, Structure>(sglOblFulfilled, o));
                    execInternalOp("NPISignals", sglOblFulfilled, o);
                } catch (java.lang.IllegalArgumentException e) {
                    // ignore, the obligations was not there anymore
                }
            }
            public void unfulfilled(DeonticModality o) { 
                removeObsPropertyByTemplate(o.getFunctor(), getTermsAsProlog(o));
                //signal(sglOblUnfulfilled, new JasonTermWrapper(o));
                //signalsQueue.offer(new Pair<String, Structure>(sglOblUnfulfilled, o));
                execInternalOp("NPISignals", sglOblUnfulfilled, o);
            }
            public void inactive(DeonticModality o) {    
                removeObsPropertyByTemplate(o.getFunctor(), getTermsAsProlog(o));
                //signal(sglOblInactive, new JasonTermWrapper(o));
                //signalsQueue.offer(new Pair<String, Structure>(sglOblInactive, o));
            }
            
            public void failure(Structure f) {     
                //signal(sglNormFailure, new JasonTermWrapper(f));                
                execInternalOp("NPISignals", sglNormFailure, f);
            }
        };
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

    @INTERNAL_OPERATION void NPISignals(String signal, Term arg) {
        signal(signal, new JasonTermWrapper(arg));
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
    
    private Transformer guiStyleSheet = null;
    protected Transformer getStyleSheet() throws TransformerConfigurationException, IOException {
        if (guiStyleSheet == null)
            guiStyleSheet = DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL( getStyleSheetName() ));
        return guiStyleSheet;                    
    }
    
    protected void updateGuiOE() {
        if (gui != null && updateGUIThread != null)
            updateGUIThread.update();                
    }

    /** manages listener to be notified about agents that quit the system */

    class Ora4masWSPRuleEngine extends AbstractWSPRuleEngine {
        List<OrgArt> l = new ArrayList<OrgArt>();
        
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
                        CartagoBasicContext cartagoCtx = new CartagoBasicContext("OrgArt setup","default");
                        cartagoCtx.doAction(new Op("setWSPRuleEngine", wspEng), -1);
                        wspEng.addListener(OrgArt.this);    
                    } catch (CartagoException e) {
                        e.printStackTrace();
                    } 
                };
            }.start();
        }
    }
    
    abstract public void agKilled(String agName);
    
    /** Http Server for GUI */
    
    static HttpServer httpServer = null;
    static int        httpServerPort = 3271;
    static String     httpServerURL = "http://localhost:"+httpServerPort;
    //static Map<String,String> pages = new TreeMap<String,String>();
    static int refreshInterval = 5;
    //protected static String osID = "";
    protected static Map<String,Map<String,String>> oePages = new HashMap<String, Map<String,String>>();
    
    public static synchronized String startHttpServer()  {
        if (httpServer == null) {
            try {
                httpServer = HttpServer.create(new InetSocketAddress(httpServerPort), 0);
                httpServer.setExecutor(Executors.newCachedThreadPool());
                registerRootBrowserView();
                registerOEListBrowserView();
                httpServer.start();
                httpServerURL = "http://"+InetAddress.getLocalHost().getHostAddress()+":"+httpServerPort;
                System.out.println("Moise Http Server running on "+httpServerURL);
            } catch (BindException e) {
                httpServerPort++;
                return startHttpServer();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return httpServerURL;
    }

    static void registerRootBrowserView() {        
        if (httpServer == null)
            return;
        try {
            httpServer.createContext("/", new HttpHandler() {                                
                public void handle(HttpExchange exchange) throws IOException {
                    String requestMethod = exchange.getRequestMethod();
                    Headers responseHeaders = exchange.getResponseHeaders();
                    responseHeaders.set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, 0);
                    OutputStream responseBody = exchange.getResponseBody();

                    if (requestMethod.equalsIgnoreCase("GET")) {
                        String path = exchange.getRequestURI().getPath();
                        StringWriter so = new StringWriter();

                        if (path.length() < 2) { // it is the root
                            so.append("<html><head><title>Moise Web View</title></head><body>");
                            so.append("<iframe width=\"20%\" height=\"100%\" align=left src=\"/oe\" border=5 frameborder=0 ></iframe>");
                            so.append("<iframe width=\"78%\" height=\"100%\" align=left src=\"/groupsandschemes\" name=\"oe-frame\" border=5 frameborder=0></iframe>");
                            so.append("</body></html>");
                        } else {
                            if (path.indexOf("agent.xsl") > 0) {
                                try {
                                    if (MindInspectorWeb.isRunning()) {
                                        String query = exchange.getRequestURI().getRawQuery(); // what follows ?
                                        String addr = MindInspectorWeb.getURL()+"/agent-mind/"+getAgNameFromPath(query);
                                        so.append("<meta http-equiv=\"refresh\" content=\"0; url="+addr+"\" />");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }                            
                        }
                        responseBody.write(so.toString().getBytes());
                    }                                
                    responseBody.close();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String getAgNameFromPath(String query) {
        int nameStart = query.indexOf("agentId=");
        if (nameStart < 0) return null;
        nameStart += "agentId=".length();
        int nameEnd   = query.indexOf("&",nameStart+1);
        if (nameEnd >= 0)
            return query.substring(nameStart,nameEnd).trim();
        else
            return query.substring(nameStart).trim();        
    }
    
    private static void registerOEListBrowserView() {        
        if (httpServer == null)
            return;
        try {
            httpServer.createContext("/oe", new HttpHandler() {                                
                public void handle(HttpExchange exchange) throws IOException {
                    String requestMethod = exchange.getRequestMethod();
                    Headers responseHeaders = exchange.getResponseHeaders();
                    responseHeaders.set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, 0);
                    OutputStream responseBody = exchange.getResponseBody();

                    if (requestMethod.equalsIgnoreCase("GET")) {
                        responseBody.write(("<html><head><title>Moise (list of organisational entities)</title><meta http-equiv=\"refresh\" content=\""+refreshInterval+"\" ></head><body>").getBytes());
                        for (String oeId: oePages.keySet()) {
                            responseBody.write(("<font size=\"+2\"><p style='color: red; font-family: arial;'>organisation <b>"+oeId+"</b></p></font>").getBytes());
                            
                            Map<String,String> pages = oePages.get(oeId);
                            StringWriter os  = new StringWriter();
                            StringWriter gr  = new StringWriter();  gr.append("<br/><scan style='color: red; font-family: arial;'>groups</scan> <br/>");
                            StringWriter sch = new StringWriter(); sch.append("<br/><scan style='color: red; font-family: arial;'>schemes</scan> <br/>");
                            // show os
                            // show groups
                            // show schemes
                            for (String id: pages.keySet()) {
                                String addr = pages.get(id);
                                String html = "<a href=\""+addr+"\" target=\"oe-frame\" style=\"font-family: arial; text-decoration: none\">"+id+"</a><br/>";
                                if (addr.endsWith("os"))
                                    os.append(html);
                                else if (addr.indexOf("group") > 0)
                                    gr.append("- "+html);
                                else
                                    sch.append("- "+html);
                            }
                            responseBody.write( os.toString().getBytes());
                            responseBody.write( gr.toString().getBytes());
                            responseBody.write( sch.toString().getBytes());
                        }
                    }                                
                    responseBody.write("<hr/>by <a href=\"http://moise.sf.net\" target=\"_blank\">Moise</a>".getBytes());
                    responseBody.write("</body></html>".getBytes());
                    responseBody.close();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // used for cache below
    private String lastDot = "";
    private File lastImgFile = null;
    private byte[] lastData = null;
    
    String registerOEBrowserView(final String oeId, final String pathSpec, final String id, final String srcNPL, final ToXML oe, final Transformer transformer) {
        if (httpServer == null)
            return null;
        try {
            String addr = "/" + oeId + pathSpec + id;
            httpServer.createContext(addr, new HttpHandler() {                                
                public void handle(HttpExchange exchange) throws IOException {
                    String requestMethod = exchange.getRequestMethod();
                    Headers responseHeaders = exchange.getResponseHeaders();
                    OutputStream responseBody = exchange.getResponseBody();
                    
                    if (requestMethod.equalsIgnoreCase("GET")) {
                        try {
                            if (exchange.getRequestURI().getPath().endsWith("svg")) {
                                responseHeaders.set("Content-Type", "image/svg+xml");
                                String program = null;
                                try {
                                    program = getDotPath();
                                } catch (java.lang.NoClassDefFoundError e) {}
                                //for (String s: exchange.getRequestHeaders().keySet())
                                //    System.out.println("* "+s+" = "+exchange.getRequestHeaders().getFirst(s));
                                if (program != null) {
                                    String dot = getAsDot();
                                    if (dot != null) {
                                        if (!lastDot.endsWith(dot)) { // new dot
                                            lastDot = dot;
                                            File fin    = File.createTempFile("jacamo-", ".dot");
                                            lastImgFile = File.createTempFile("jacamo-", ".svg");

                                            FileWriter out = new FileWriter(fin);
                                            out.append(dot);
                                            out.close();
                                            Process p = Runtime.getRuntime().exec(program+" -Tsvg "+fin.getAbsolutePath()+" -o "+lastImgFile.getAbsolutePath());
                                            p.waitFor();
                                            
                                            lastData = new byte[(int)lastImgFile.length()];
                                            FileInputStream in = new FileInputStream(lastImgFile);
                                            in.read(lastData);
                                            in.close();
                                        }
                                        //responseHeaders.set("Last-Modified", new Date( lastImgFile.lastModified()).toGMTString() );
                                        //responseHeaders.set("Cache-control", "max-age=2" );
                                        //responseHeaders.set("ETag", "x"+lastImgFile.hashCode());
                                        exchange.sendResponseHeaders(200, 0);
                                        //exchange.sendResponseHeaders(304, -1);
                                        responseBody.write(lastData);
                                    }
                                }
                            } else {
                                exchange.sendResponseHeaders(200, 0);
                                responseHeaders.set("Content-Type", "text/html");
                                if (exchange.getRequestURI().getPath().endsWith("debug")) {
                                    responseBody.write(("<html><head><title>"+id+" debug</title></head><body><pre>").getBytes());
                                    responseBody.write(getDebugText().getBytes());
                                    responseBody.write("</pre>".getBytes());
                                } else if (exchange.getRequestURI().getPath().endsWith(".npl")) {
                                    responseBody.write(("<html><head><title>"+id+".npl</title></head><body><pre>").getBytes());
                                    responseBody.write(srcNPL.getBytes());                                            
                                    responseBody.write("</pre>".getBytes());
                                } else {                            
                                    //String path = exchange.getRequestURI().getPath();
                                    responseBody.write(("<html><head><title>"+id+"</title><meta http-equiv=\"refresh\" content=\""+refreshInterval+"\"></head><body>").getBytes());
                                    StringWriter so = new StringWriter();
                                    try {
                                        if (getDotPath() != null)
                                            transformer.setParameter("show-oe-img", "true");
                                    } catch (java.lang.NoClassDefFoundError e) {}
                                    transformer.transform(new DOMSource(DOMUtils.getAsXmlDocument(oe)),new StreamResult(so)); //OrgArtNormativeGUI.getParser().parse(si)), new StreamResult(so));
                                    responseBody.write(so.toString().getBytes());                                            
                                    so = new StringWriter();
                                    getNSTransformer().transform(new DOMSource(DOMUtils.getAsXmlDocument(nengine)), new StreamResult(so));
                                    responseBody.write(so.toString().getBytes());
                                    responseBody.write(("<hr/><a href="+id+".npl>NPL program</a>").getBytes());                                
                                    responseBody.write((" / <a href="+id+"/debug>debug page</a>").getBytes());                                
                                }
                                responseBody.write("</body></html>".getBytes());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }                                
                    responseBody.close();
                }
            });
            Map<String,String> pages = oePages.get(oeId);
            if (pages == null) {
                pages = new HashMap<String, String>();
                oePages.put(oeId, pages);
            }
            pages.put(id, addr);
            return httpServerURL+addr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    String dpath = null;
    String getDotPath() throws Exception {
        if (dpath == null) {
            Properties p = new Properties();
            p.load(new FileInputStream( new File(System.getProperties().get("user.home") + File.separator + ".jacamo/user.properties") ));
            String r = p.getProperty("dotPath");
            if (r == null)
                r = "/opt/local/bin/dot";
            if (new File(r).exists()) {
                dpath = r;
            } else { 
                r = "/usr/bin/dot";
                if (new File(r).exists()) {
                    dpath = r;
                }            
            }            
        }
        return dpath;
    }

    String registerOSBrowserView(String oeId, String osId, final String osSpec) {        
        if (httpServer == null)
            return null;
        try {
            String addr = "/" + oeId + "/os";
            httpServer.createContext(addr, new HttpHandler() {                                
                public void handle(HttpExchange exchange) throws IOException {
                    String requestMethod = exchange.getRequestMethod();
                    Headers responseHeaders = exchange.getResponseHeaders();
                    responseHeaders.set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, 0);
                    OutputStream responseBody = exchange.getResponseBody();

                    if (requestMethod.equalsIgnoreCase("GET")) {
                        responseBody.write(osSpec.getBytes());
                    }                                
                    responseBody.close();
                }
            });
            Map<String,String> pages = oePages.get(oeId);
            if (pages == null) {
                pages = new HashMap<String, String>();
                oePages.put(oeId, pages);
            }
            pages.put("specification", addr);
            return httpServerURL+addr;                          
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String specToStr(ToXML spec, Transformer transformer) throws Exception {
        StringWriter so = new StringWriter();
        InputSource si = new InputSource(new StringReader(DOMUtils.dom2txt(spec)));
        transformer.transform(new DOMSource(getParser().parse(si)), new StreamResult(so));
        return so.toString();        
    }
    
    private DocumentBuilder parser;
    public DocumentBuilder getParser() throws ParserConfigurationException {
        if (parser == null)
            parser = DOMUtils.getParser();
        return parser;
    }
    
    public Transformer getNSTransformer() throws TransformerConfigurationException, TransformerFactoryConfigurationError, IOException {
        return DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("nstate"));
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
                                gui.updateOE(getDebugText(), OrgArt.this, getStyleSheet());
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
    
    String getDebugText() {
        StringBuilder out = new StringBuilder();
        out.append(orgState.toString());
        out.append("\n\n\n** as a list of dynamic facts:\n");
        for (Literal l: orgState.transform()) 
            out.append("     "+l+"\n");
        out.append("\n\n\n** as a dump memory:\n");
        for (Literal l: nengine.getAg().getBB())
            out.append("     "+l+"\n");
        return out.toString();
    }

    protected static String fixAgName(String ag) {
        if (ag.startsWith("\""))
            return ag.substring(1, ag.length()-1);
        else
            return ag;
    }

    // DFP methods
    
    public boolean isRelevant(PredicateIndicator pi) {
        return orgState.isRelevant(pi);
    }
    
    public Iterator<Unifier> consult(Literal l, Unifier u) {
        return orgState.consult(l, u);
    }
}
