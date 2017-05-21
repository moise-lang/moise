package ora4mas.nopl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import jason.architecture.MindInspectorWeb;
import moise.xml.DOMUtils;

/** Web Interface for ORA4MAS */
public class WebInterface  {
    
    static WebInterface singleton = null;
    
    HttpServer httpServer = null;
    int        httpServerPort = 3271;
    String     httpServerURL = "http://localhost:"+httpServerPort;
    //static Map<String,String> pages = new TreeMap<String,String>();
    int refreshInterval = 5;
    //protected static String osID = "";
    protected Map<String,Map<String,String>> oePages = new HashMap<String, Map<String,String>>();

    
    private WebInterface() {
    }
    
    public synchronized static WebInterface get() {
        if (singleton == null) {
            singleton = new WebInterface();
            singleton.startHttpServer();
        }
        return singleton;
    }
    
    public static boolean isRunning() {
        return singleton != null;
    }
    
    public String getURL() {
        return httpServerURL;
    }
    
    private void startHttpServer()  {
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
                startHttpServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void registerRootBrowserView() {        
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
    
    private void registerOEListBrowserView() {        
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
                            StringWriter nor = new StringWriter(); nor.append("<br/><scan style='color: red; font-family: arial;'>norms</scan> <br/>");
                            // show os
                            // show groups
                            // show schemes
                            for (String id: pages.keySet()) {
                                String addr = pages.get(id);
                                String html = "<a href=\""+addr+"\" target=\"oe-frame\" style=\"font-family: arial; text-decoration: none\">"+id+"</a><br/>";
                                if (addr.endsWith("os"))
                                    os.append(html);
                                else if (addr.indexOf("/group") > 0)
                                    gr.append("- "+html);
                                else if (addr.indexOf("/scheme") > 0)
                                    sch.append("- "+html);
                                else
                                    nor.append("- "+html);
                            }
                            responseBody.write( os.toString().getBytes());
                            responseBody.write( gr.toString().getBytes());
                            responseBody.write( sch.toString().getBytes());
                            responseBody.write( nor.toString().getBytes());
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
    
    public String registerOEBrowserView(final String oeId, final String pathSpec, final String id, final OrgArt orgArt) {
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
                                    String dot = orgArt.getAsDot();
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
                                    responseBody.write(orgArt.getDebugText().getBytes());
                                    responseBody.write("</pre>".getBytes());
                                } else if (exchange.getRequestURI().getPath().endsWith(".npl")) {
                                    responseBody.write(("<html><head><title>"+id+".npl</title></head><body><pre>").getBytes());
                                    responseBody.write(orgArt.getNPLSrc().getBytes());                                            
                                    responseBody.write("</pre>".getBytes());
                                } else {                            
                                    //String path = exchange.getRequestURI().getPath();
                                    responseBody.write(("<html><head><title>"+id+"</title><meta http-equiv=\"refresh\" content=\""+refreshInterval+"\"></head><body>").getBytes());
                                    StringWriter so = new StringWriter();
                                    if (orgArt.getStyleSheet() != null) {
                                        try {
                                            if (getDotPath() != null)
                                                orgArt.getStyleSheet().setParameter("show-oe-img", "true");
                                        } catch (java.lang.NoClassDefFoundError e) {}
                                        orgArt.getStyleSheet().transform(new DOMSource(DOMUtils.getAsXmlDocument(orgArt)),new StreamResult(so));
                                        responseBody.write(so.toString().getBytes());
                                    }
                                    so = new StringWriter();
                                    orgArt.getNSTransformer().transform(new DOMSource(DOMUtils.getAsXmlDocument(orgArt.getNormativeEngine())), new StreamResult(so));
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
    
    public void removeOE(String oeId, String id) {
        if (oeId == null) return;
        String addr = oePages.get(oeId).remove(id);
        if (addr != null)
            httpServer.removeContext(addr);
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

    HttpContext osContext = null;
    
    String registerOSBrowserView(String oeId, String osId, final String osSpec) {        
        if (httpServer == null)
            return null;
        try {
            String addr = "/" + oeId + "/os";
            if (osContext != null)
                httpServer.removeContext(osContext);
            osContext = httpServer.createContext(addr, new HttpHandler() {                                
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

    protected static String fixAgName(String ag) {
        if (ag.startsWith("\""))
            return ag.substring(1, ag.length()-1);
        else
            return ag;
    }

}
