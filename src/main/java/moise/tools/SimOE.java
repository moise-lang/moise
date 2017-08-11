package moise.tools;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;

import moise.oe.GoalInstance;
import moise.oe.GroupInstance;
import moise.oe.OE;
import moise.oe.OEAgent;
import moise.oe.SchemeInstance;
import moise.os.OS;
import moise.xml.DOMUtils;
import moise.xml.XmlFilter;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;


/**
 * Simple program to show an OE and simulate social events on it
 *
 * @author Jomi Fred Hubner
 */
public class SimOE {

    public static boolean debug = false;

    protected OE currentOE  = null;
    protected OS currentOS  = null;

    protected String OExml  = null;
    protected Node   OE_DOM = null;

    protected String OSxml  = null;
    protected Node   OS_DOM = null;
    protected DocumentBuilder parser = null;


    public    SimOEFrame frame;
    private   String     name = "OrgView";


    //private static String xmlURI = "xml/";
    //private static String binURI = "./";

    public static void main(String[] args) {
        //String binDir = ".";
        String file = null;

        if (args.length == 1) {
            file = args[0];
        } else {
            file = XmlFilter.askOSFile();
            if (file == null)
                return;
            file = file.replaceAll("\\\\", "/");
            System.out.println("Openning "+file);
        }

        try {
            if (file != null) {
                SimOE v = new SimOE(file);
                if (v.getCurrentOE() != null) {
                    v.frame.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            System.exit(0);
                        }
                    });
                    v.frame.centerScreen();
                } else {
                    System.exit(2);
                }
            } else {
                System.exit(1);
            }
        } catch (Exception e) {
            printErr(e.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }


    public SimOE() {
        this(true);
    }
    public SimOE(boolean addSim) {
        frame = new SimOEFrame(this, addSim);
        createUpdateScreenThread();
    }


    public SimOE(String OSxmlURI) throws Exception {
        this();
        currentOE = OE.createOE("print", OSxmlURI);
        if (currentOE != null) {
            setOS(currentOE.getOS());
            frame.uptadeOSComps();
            frame.uptadeOEComps();
            frame.showOE();
        }
    }

    public SimOE(OE oe) throws Exception {
        this(oe, true);
    }
    public SimOE(OE oe, boolean addSim) throws Exception {
        this(addSim);
        currentOE = oe;
        setOS(currentOE.getOS());
        frame.uptadeOSComps();
        frame.uptadeOEComps();
        frame.setVisible(true);
        frame.showOE();
    }

    public void setOE(OE oe) {
        currentOE = oe;
    }

    public OE getCurrentOE() {
        return currentOE;
    }

    public void setName(String s) {
        name = s;
        frame.setTitle("Moise+ ("+name+")");
    }

    public String getName() {
        return name;
    }

    protected String getOExml() {
        if (currentOE == null) {
            return "";
        }
        if (OExml == null) {
            //OExml = OEGenerateXML.generateOE(currentOE);
            OExml = DOMUtils.dom2txt(currentOE);//.getAsXmlString();
        }
        return OExml;
    }

    protected Node getOE_DOM() {
        if (currentOE == null) {
            return null;
        }
        try {
            if (OE_DOM == null) {
                if (parser == null) {
                    parser = DOMUtils.getParser();
                }

                OE_DOM = DOMUtils.getAsXmlDocument(currentOE); //.getAsXmlDocument(); //arser.parse(new InputSource(new StringReader(getOExml())));//parser.getDocument();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return OE_DOM;
    }

    protected OS getCurrentOS() {
        return currentOS;
    }

    protected String getOSxml() {
        if (currentOS == null) {
            return "";
        }
        if (OSxml == null) {
            OSxml = DOMUtils.dom2txt(currentOS); //OSGenerateXML.generateOS(currentOS);
        }

        return OSxml;
    }

    protected Node getOS_DOM() {
        if (currentOS == null) {
            return null;
        }
        try {
            if (OS_DOM == null) {
                if (parser == null) {
                    parser = DOMUtils.getParser();
                }

                InputSource si = new InputSource(new StringReader(getOSxml()));
                //si.setSystemId(xmlURI);
                OS_DOM = parser.parse(si);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return OS_DOM;
    }


    protected void setOS(OS os) {
        try {
            currentOS = os;
            OSxml = null;
            OS_DOM = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean running = true;
    private boolean needsScreenUpdate = true;

    // creates a thread that updates (if necessary) the screen each second
    private void createUpdateScreenThread() {
        new Thread() {
            public void run() {
                while (running) {
                    try {
                        sleep(1000);
                        if (needsScreenUpdate) {
                            needsScreenUpdate = false;
                            OExml  = null;
                            OE_DOM = null;
                            frame.uptadeOEComps();
                            frame.showCurrentObject();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }



    // for simulation
    // ***************************

    public void updateScreen() {
        needsScreenUpdate = true;
        //OExml = null;
        //OE_DOM = null;
        //frame.uptadeOEComps();
        //frame.showCurrentObject();
    }

    public void disposeWindow() {
        frame.dispose();
        running = false;
    }

    void createAg(String name) {
        removeErrorLastFromFrame();
        try {
            currentOE.addAgent(name);
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }

    void removeAg(String name, boolean check) {
        removeErrorLastFromFrame();
        try {
            currentOE.removeAgent(name, check);
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
            e.printStackTrace();
        }
    }


    void createGr(String superGroup, String spec) {
        removeErrorLastFromFrame();
        try {
            if (superGroup.equals("root")) {
                currentOE.addGroup(spec);
            } else {
                currentOE.findGroup(superGroup).addSubGroup(spec);
            }
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }

    void removeGr(String grId) {
        removeErrorLastFromFrame();
        try {
            currentOE.removeGroup(grId);
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }


    SchemeInstance startSCH(String schSpecId) {
        removeErrorLastFromFrame();
        try {
            SchemeInstance sch = currentOE.startScheme(schSpecId);
            updateScreen();
            return sch;
        } catch (Exception e) {
            frame.show("Error="+e);
        }
        return null;
    }

    void finishSCH(SchemeInstance sch) {
        removeErrorLastFromFrame();
        try {
            currentOE.finishScheme( sch);
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }

    void abortSCH(SchemeInstance sch) {
        removeErrorLastFromFrame();
        try {
            currentOE.abortScheme( sch);
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }

    void addResponsibleGroupToSCH(SchemeInstance sch, GroupInstance gr) {
        removeErrorLastFromFrame();
        try {
            sch.addResponsibleGroup(gr);
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }

    void remResponsibleGroupToSCH(SchemeInstance sch, GroupInstance gr) {
        removeErrorLastFromFrame();
        try {
            sch.remResponsibleGroup(gr);
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }

    void agCommitsMission(String agId, String missionId, String schId) {
        removeErrorLastFromFrame();
        try {
            OEAgent a  = currentOE.getAgent(agId);
            a.commitToMission(missionId, schId);
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }

    void agUncommitsMission(String agId, String missionId, String schId) {
        removeErrorLastFromFrame();
        try {
            OEAgent a  = currentOE.getAgent(agId);
            a.removeMission(missionId, schId);
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }

    void agAdoptsRole(String agId, String roleId, String grId) {
        removeErrorLastFromFrame();
        try {
            OEAgent a  = currentOE.getAgent(agId);
            a.adoptRole(roleId, currentOE.findGroup(grId));
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }

    void agGiveUpRole(String agId, String roleId, String grId) {
        removeErrorLastFromFrame();
        try {
            OEAgent a  = currentOE.getAgent(agId);
            a.removeRole(roleId, grId);
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }


    void setGoalArg(GoalInstance gi, String arg, String val) {
        removeErrorLastFromFrame();
        try {
            gi.setArgumentValue(arg, val);
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }


    void setGoalStateSatisfied(GoalInstance gi, OEAgent ag) {
        removeErrorLastFromFrame();
        try {
            gi.setAchieved(ag);
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }

    void setGoalStateImpossible(GoalInstance gi, OEAgent ag) {
        removeErrorLastFromFrame();
        try {
            gi.setImpossible(ag);
            updateScreen();
        } catch (Exception e) {
            frame.show("Error="+e);
        }
    }


    void removeErrorLastFromFrame() {
        // remove last error
        try {
            String e = (String)frame.getCurrentObject();
            if (e.startsWith("Error")) {
                frame.pbackActionPerformed();
            }
        } catch (Exception e) {}
    }

    protected static void print(String s) {
        if (debug) {
            System.err.println("** "+s);
        }
    }

    protected static void printErr(String s) {
        System.err.println("Error: "+s);
    }

}
