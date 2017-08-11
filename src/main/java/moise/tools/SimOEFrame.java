package moise.tools;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import moise.oe.GoalInstance;
import moise.oe.GroupInstance;
import moise.oe.MissionPlayer;
import moise.oe.OE;
import moise.oe.OEAgent;
import moise.oe.RolePlayer;
import moise.oe.SchemeInstance;
import moise.os.OS;
import moise.os.fs.FS;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.fs.Scheme;
import moise.os.ns.NS;
import moise.os.ss.Group;
import moise.os.ss.Role;
import moise.os.ss.SS;
import moise.xml.DOMUtils;

/**
 *
 * @author  jomi
 */
@SuppressWarnings("unchecked")
public class SimOEFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    SimOE         ag            = null;
    Object        currentObject = null; // which object is selected

    boolean       hasSim        = true;

    Stack<Object> history       = new Stack<Object>();

    TransformerFactory tFactory   = TransformerFactory.newInstance();
    Transformer osTransformer     = null;
    Transformer oeTransformer     = null;
    Transformer grSpecTransformer = null;
    Transformer schSpecTransformer = null;
    Transformer roleTransformer   = null;
    Transformer missionTransformer= null;
    Transformer goalTransformer   = null;
    Transformer ssTransformer     = null;
    Transformer fsTransformer     = null;
    Transformer dsTransformer     = null;
    Transformer agTransformer     = null;
    Transformer grTransformer     = null;
    Transformer schTransformer    = null;
    Transformer briefOE           = null;

    public SimOEFrame(SimOE ag) {
        this(ag, true);
    }

    public SimOEFrame(SimOE ag, boolean addSim) {
        super("Moise ("+ag.getName()+")");
        this.ag = ag;
        this.hasSim = addSim;

        // to load .xsl from the moise.jar
        tFactory.setURIResolver(new URIResolver() {
            public Source resolve(String href, String base) throws TransformerException {
                return getXSL(href);
            }
        });

        initComponents(addSim);
    }

    public void centerScreen() {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setBounds(0, 0, 800, (int)(screenSize.height * 0.7)); //(int)(screenSize.width * 0.8)
            setLocation(screenSize.width / 2 - this.getWidth() / 2, screenSize.height / 2 - this.getHeight() / 2);

            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    setVisible(true);
                }
            });
        } catch (Exception e) {
            System.out.println("problem in setVisible!");
            e.printStackTrace();
        }
    }

    public void showOS() {
        try {
            currentObject = ag.getCurrentOS();
            pushHistory(currentObject);
            showCurrentObject();
        } catch (Exception e) {        }
    }

    protected void uptadeOSComps() {
        try {
            OSTreeModel osTreeModel = new OSTreeModel(osTree);
            osTreeModel.setOS( ag.getCurrentOS());
            osTree.setModel( osTreeModel.getModel() );

            Group grs = ag.getCurrentOS().getSS().getRootGrSpec();
            if (grs != null) {
                mySetModelPreserPosSelectedIndex(grSpecsInGrCreation, grs.getAllSubGroupsTree());
            }
            mySetModelPreserPosSelectedIndex(schSpecInSchStart, ag.getCurrentOS().getFS().getSchemes());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showOE() {
        try {
            currentObject = ag.getCurrentOE();
            if (currentObject != null) {
                pushHistory(currentObject);
                showCurrentObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void uptadeOEComps() {
        OE oe = ag.getCurrentOE();
        if (oe == null) return;

        try {
            OETreeModel oeTreeModel = new OETreeModel(oeTree);
            oeTreeModel.setOE( oe );
            oeTree.setModel( oeTreeModel.getModel() );
            if (hasSim) {
                // rebuild the list of current groups
                List allWithRoot = new ArrayList(oe.getAllSubGroupsTree());
                allWithRoot.add("root");

                mySetModelPreserPosSelectedIndex(grCreatedGroups, allWithRoot);

                mySetModelPreserPosSelectedIndex(grInstancesInGroup, oe.getAllSubGroupsTree());

                updateAgentRoleComponents();

                // rebuild the list of current SCHs
                mySetModelPreserPosSelectedIndex(schInstancesInMission, oe.getSchemes());

                mySetModelPreserPosSelectedIndex(schInstancesInGoal, oe.getSchemes());
                updateSchemeGoalsCB();

                mySetModelPreserPosSelectedIndex(schInstanceInSchFinish, oe.getSchemes());

                mySetModelPreserPosSelectedIndex(schInstanceInAbort, oe.getSchemes());

                mySetModelPreserPosSelectedIndex(schInstanceInRespGr, oe.getSchemes());
                updateSchemeRespGoals();


                // missions
                updateAgentMissionComponents();

                // agNames
                mySetModelPreserPosSelectedIndex(agNamesInRole, oe.getAgents());

                mySetModelPreserPosSelectedIndex(agNamesInMission, oe.getAgents());

                mySetModelPreserPosSelectedIndex(agNamesInRemoveAg, oe.getAgents());

                mySetModelPreserPosSelectedIndex(agNamesInGoalInstance, oe.getAgents());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void mySetModelPreserPosSelectedIndex(JComboBox cb, Collection c) {
        mySetModelPreserPosSelectedIndex(cb, new Vector(c));
    }

    private void mySetModelPreserPosSelectedIndex(JComboBox cb, Vector v) {
        int oldSize = cb.getModel().getSize();
        int oldPos = cb.getSelectedIndex();
        Collections.sort(v);
        cb.setModel(new DefaultComboBoxModel( v ));
        if (v.size() == oldSize) {
            cb.setSelectedIndex(oldPos);
        }
    }

    /**
     * set the combo box Goals for the selected scheme
     */
    protected void updateSchemeGoalsCB() {
        SchemeInstance sch = (SchemeInstance)schInstancesInGoal.getSelectedItem();
        if (sch != null) {
            mySetModelPreserPosSelectedIndex(goalInstanceInGoal, sch.getGoals());
            updateGoalsComponents();
        } else {
            setArgValue.setEnabled(false);
            setGoalState.setEnabled(false);
        }
    }

    /**
     * update the goal interface components
     */
    protected void updateGoalsComponents() {
        setArgValue.setEnabled(false);
        setGoalState.setEnabled(false);
        GoalInstance gi = (GoalInstance)goalInstanceInGoal.getSelectedItem();
        if (gi != null) {
            setGoalState.setEnabled(true);

            if (gi.getSpec().hasArguments()) {
                Collection<String> args = gi.getSpec().getArguments().keySet();
                if (args != null) {
                    mySetModelPreserPosSelectedIndex(goalArgListInGoal, args);
                    setArgValue.setEnabled(true);
                }
                updateGoalArgValue();
            }

            goalStateInGoal.setSelectedIndex(0);
            if (gi.isSatisfied()) {
                goalStateInGoal.setSelectedIndex(1);
            }
            if (!gi.isEnabled()) {
                goalStateInGoal.setSelectedIndex(2);
            }
        }
    }

    /**
     * update the goal argument value
     */
    protected void updateGoalArgValue() {
        GoalInstance gi = (GoalInstance)goalInstanceInGoal.getSelectedItem();
        String   selArg = (String)goalArgListInGoal.getSelectedItem();
        Object   argVal = gi.getArgumentValue(selArg);
        if (argVal == null) {
            argValueInGoal.setText("");
        } else {
            argValueInGoal.setText(argVal.toString());
        }
    }

    /**
     * set the combo box responsible groups for the selected scheme
     */
    protected void updateSchemeRespGoals() {
        addRespGr.setEnabled(false);
        remRespGr.setEnabled(false);
        SchemeInstance sch = (SchemeInstance)schInstanceInRespGr.getSelectedItem();
        if (sch != null) {
            Vector grs = new Vector(ag.getCurrentOE().getAllSubGroupsTree());
            grs.removeAll(sch.getResponsibleGroups());
            mySetModelPreserPosSelectedIndex( grInRespGr, grs);
            mySetModelPreserPosSelectedIndex( schGrsInRespGr, sch.getResponsibleGroups());
            if (grs.size() > 0) {
                addRespGr.setEnabled(true);
            }
            if (sch.getResponsibleGroups().size() > 0) {
                remRespGr.setEnabled(true);
            }
        }
    }

    /**
     * update the agent role interface components
     */
    protected void updateAgentRoleComponents() {
        Collection agRoles;
        Collection agGrs;
        if (adoptORgiveUpRole.getSelectedIndex() == 1) { // give up a role
            agRoles = new HashSet();
            agGrs   = new HashSet();
            OEAgent selAg = (OEAgent)agNamesInRole.getSelectedItem();
            if (selAg != null) {
                Iterator i = selAg.getRoles().iterator();
                while (i.hasNext()) {
                    RolePlayer rp = (RolePlayer)i.next();
                    agRoles.add( rp.getRole() );
                    agGrs.add( rp.getGroup() );
                }
            }
        } else { // adopt role
            agRoles = ag.getCurrentOS().getSS().getRolesDef();
            agGrs   = ag.getCurrentOE().getAllSubGroupsTree();
        }
        mySetModelPreserPosSelectedIndex(agRolesInRole, agRoles);
        mySetModelPreserPosSelectedIndex(grInRole, agGrs);
        if (agRoles.size() == 0 || agGrs.size() == 0) {
            agAdoptRole.setEnabled(false);
        } else {
            agAdoptRole.setEnabled(true);
        }
    }

    /**
     * update the agent mission interface components
     */
    protected void updateAgentMissionComponents() {
        Collection agMissions;
        Collection agSchs;
        if (commitORuncommit.getSelectedIndex() == 1) { // give up a mission
            agMissions = new HashSet();
            agSchs     = new HashSet();
            OEAgent selAg = (OEAgent)agNamesInMission.getSelectedItem();
            if (selAg != null) {
                Iterator i = selAg.getMissions().iterator();
                while (i.hasNext()) {
                    MissionPlayer mp = (MissionPlayer)i.next();
                    agMissions.add( mp.getMission() );
                    agSchs.add( mp.getScheme() );
                }
            }
        } else { // adopt mission
            agMissions = ag.getCurrentOS().getFS().getAllMissions();
            agSchs     = ag.getCurrentOE().getSchemes();
        }
        mySetModelPreserPosSelectedIndex(missionInMission, agMissions);
        mySetModelPreserPosSelectedIndex(schInstancesInMission, agSchs);
        if (agMissions.size() == 0 || agSchs.size() == 0) {
            okMission.setEnabled(false);
        } else {
            okMission.setEnabled(true);
        }
    }


    public void show(String s) {
        currentObject = s;
        pushHistory(currentObject);
        showCurrentObject();
    }



    protected Object getCurrentObject() {
        return currentObject;
    }
    protected void setCurrentObject(Object o ) {
        currentObject = o;
    }

    @SuppressWarnings("unused")
    protected void showCurrentObject() {
        if (currentObject == null) {
            textArea.setText("");
            return;
        }
        String type = showAs.getSelectedItem().toString();
        if (type.startsWith("brief"))
            type = "html";

        textArea.setContentType("text/"+type);

        try {
            // try String
            String s = (String) currentObject;
            if (s.equals("SS")) {
                currentObject = ag.getCurrentOS().getSS();
                showCurrentObject();
            } else if (s.equals("FS")) {
                currentObject = ag.getCurrentOS().getFS();
                showCurrentObject();
            } else if (s.equals("NS")) {
                currentObject = ag.getCurrentOS().getNS();
                showCurrentObject();
            } else if (s.equals("Roles") || s.equals("Groups") || s.equals("Missions") || s.equals("Goals & plans") || s.equals("Agents") || s.equals("Schemes")) {
                return;
            } else {
                textArea.setText(s);
            }
            return;
        } catch (Exception e) {}

        StringWriter so = new StringWriter();

        boolean isHtml = showAs.getSelectedItem().equals("html") || showAs.getSelectedItem().equals("brief-html");

        try {
            // try OE
            OE oe = (OE) currentObject;
            if (showAs.getSelectedItem().equals("xml")) {
                textArea.setText( ag.getOExml() );
            } else if (showAs.getSelectedItem().equals("html")) {
                try {
                    getOETransformer().transform(new DOMSource( ag.getOE_DOM()), new StreamResult(so));
                } catch (Exception e) {
                    textArea.setText("Error="+e);
                    e.printStackTrace();
                    return;
                }
            } else if (showAs.getSelectedItem().equals("brief-html")) {
                try {
                    getBriefOE().transform(new DOMSource( ag.getOE_DOM()), new StreamResult(so));
                } catch (Exception e) {
                    textArea.setText("Error="+e);
                    e.printStackTrace();
                    return;
                }
            }
        } catch (Exception e) {}

        try {
            // try agent
            OEAgent oeAg = (OEAgent) currentObject;
            if (showAs.getSelectedItem().equals("xml")) {
                textArea.setText( DOMUtils.dom2txt(oeAg) );
            } else if (isHtml) {
                try {
                    getAgTransformer().setParameter("agentId", oeAg.getId());
                    getAgTransformer().transform(new DOMSource( ag.getOE_DOM()), new StreamResult(so));
                } catch (Exception e) {
                    textArea.setText("Error="+e);
                    e.printStackTrace();
                    return;
                }
            }
        } catch (Exception e) {}

        try {
            // try scheme instance
            SchemeInstance sch = (SchemeInstance)currentObject;
            if (showAs.getSelectedItem().equals("xml")) {
                textArea.setText( DOMUtils.dom2txt(sch));
            } else if (isHtml) {
                try {
                    getSCHTransformer().setParameter("schId", sch.getId());
                    getSCHTransformer().transform(new DOMSource( ag.getOE_DOM()), new StreamResult(so));
                } catch (Exception e) {
                    textArea.setText("Error="+e);
                    e.printStackTrace();
                    return;
                }
            }
        } catch (Exception e) {}

        try {
            // try group instance
            GroupInstance gr = (GroupInstance) currentObject;
            if (showAs.getSelectedItem().equals("xml")) {
                textArea.setText( DOMUtils.dom2txt(gr));
            } else if (isHtml) {
                try {
                    getGrTransformer().setParameter("groupId", gr.getId());
                    getGrTransformer().transform(new DOMSource( ag.getOE_DOM()), new StreamResult(so));
                } catch (Exception e) {
                    textArea.setText("Error="+e);
                    e.printStackTrace();
                    return;
                }
            }
        } catch (Exception e) {}

        try {
            // try OS
            OS os = (OS) currentObject;
            if (showAs.getSelectedItem().equals("xml")) {
                textArea.setText( ag.getOSxml() );
            } else if (isHtml) {
                try {
                    getOSTransformer().transform(new DOMSource( ag.getOS_DOM()), new StreamResult(so));//new StreamResult(so)); //new FileOutputStream("xxx.html")));
                } catch (Exception e) {
                    textArea.setText("Error="+e);
                    e.printStackTrace();
                    return;
                }
            }
        } catch (Exception e) {}

        try {
            //
            // try Role
            //
            Role role = (Role) currentObject;
            try {
                String Rolexml = DOMUtils.roleDetails(role); //SSGenerateXML.generateFullRoleDescription(role, "");
                if (showAs.getSelectedItem().equals("xml")) {
                    textArea.setText(Rolexml);
                } else {
                    getRoleTransformer().setParameter("roleId", role.getId());
                    getRoleTransformer().transform(new StreamSource(new StringReader(Rolexml)), new StreamResult(so));
                }
            } catch (Exception e) {
                textArea.setText("Error="+e);
                e.printStackTrace();
                return;
            }
        } catch (Exception e) {}

        try {
            //
            // try Mission
            //
            Mission mission = (Mission) currentObject;
            try {
                if (showAs.getSelectedItem().equals("xml")) {
                    textArea.setText( DOMUtils.dom2txt(mission));
                } else {
                    try {
                        getMissionTransformer().setParameter("missionId", mission.getId());
                        getMissionTransformer().transform(new DOMSource( ag.getOS_DOM()), new StreamResult(so));
                    } catch (Exception e) {
                        textArea.setText("Error="+e);
                        e.printStackTrace();
                        return;
                    }
                }
            } catch (Exception e) {
                textArea.setText("Error="+e);
                e.printStackTrace();
                return;
            }
        } catch (Exception e) {}

        try {
            //
            // try Goal
            //
            Goal goal = (Goal) currentObject;
            try {
                if (showAs.getSelectedItem().equals("xml")) {
                    textArea.setText( DOMUtils.dom2txt(goal));
                } else {
                    try {
                        getGoalTransformer().setParameter("goalId", goal.getId());
                        getGoalTransformer().transform(new DOMSource( ag.getOS_DOM()), new StreamResult(so));
                    } catch (Exception e) {
                        textArea.setText("Error="+e);
                        e.printStackTrace();
                        return;
                    }
                }
            } catch (Exception e) {
                textArea.setText("Error="+e);
                e.printStackTrace();
                return;
            }
        } catch (Exception e) {}

        try {
            // try Group spec
            Group gr = (Group) currentObject;
            if (showAs.getSelectedItem().equals("xml")) {
                textArea.setText( DOMUtils.dom2txt(gr));

            } else {
                try {
                    getGrSpecTransformer().setParameter("grSpecId", gr.getId());
                    getGrSpecTransformer().transform(new DOMSource( ag.getOS_DOM()), new StreamResult(so));
                } catch (Exception e) {
                    textArea.setText("Error="+e);
                    e.printStackTrace();
                    return;
                }
            }
        } catch (Exception e) {}

        try {
            // try scheme spec
            Scheme sch = (Scheme) currentObject;
            if (showAs.getSelectedItem().equals("xml")) {
                textArea.setText( DOMUtils.dom2txt(sch));

            } else {
                try {
                    getSchSpecTransformer().setParameter("schemeSpecId", sch.getId());
                    getSchSpecTransformer().transform(new DOMSource( ag.getOS_DOM()), new StreamResult(so));
                } catch (Exception e) {
                    textArea.setText("Error="+e);
                    e.printStackTrace();
                    return;
                }
            }
        } catch (Exception e) {}

        try {
            // try SS
            SS ss = (SS) currentObject;
            if (showAs.getSelectedItem().equals("xml")) {
                textArea.setText( DOMUtils.dom2txt(ss));

            } else {
                try {
                    getSSTransformer().transform(new DOMSource( ag.getOS_DOM()), new StreamResult(so));
                } catch (Exception e) {
                    textArea.setText("Error="+e);
                    e.printStackTrace();
                    return;
                }
            }
        } catch (Exception e) {}

        try {
            // try FS
            FS fs = (FS) currentObject;
            if (showAs.getSelectedItem().equals("xml")) {
                textArea.setText( DOMUtils.dom2txt(fs));

            } else {
                try {
                    getFSTransformer().transform(new DOMSource( ag.getOS_DOM()), new StreamResult(so));
                } catch (Exception e) {
                    textArea.setText("Error="+e);
                    e.printStackTrace();
                    return;
                }
            }
        } catch (Exception e) {}

        try {
            // try DS
            NS ds = (NS) currentObject;
            if (showAs.getSelectedItem().equals("xml")) {
                textArea.setText( DOMUtils.dom2txt(ds));

            } else {
                try {
                    getDSTransformer().transform(new DOMSource( ag.getOS_DOM()), new StreamResult(so));
                } catch (Exception e) {
                    textArea.setText("Error="+e);
                    e.printStackTrace();
                    return;
                }
            }
        } catch (Exception e) {}


        try {
            if (showAs.getSelectedItem().equals("html") || showAs.getSelectedItem().equals("brief-html")) {
                textArea.setText(so.toString());
            }
            textArea.setCaretPosition(1);
        } catch (Exception e) {}
    }


    private StreamSource getXSL(String href) {
        try {
            if (!href.endsWith(".xsl")) {
                href += ".xsl";
            }
            return new StreamSource(
                SimOEFrame.class.getResource("/xml/"+href).openStream());
        } catch (Exception e) {
            System.err.println("Could not open "+href+" in XML resources!");
            return null;
        }
    }

    private Transformer getOSTransformer() throws TransformerConfigurationException {
        if (osTransformer == null) {
            osTransformer = tFactory.newTransformer(getXSL("os"));
        }
        return osTransformer;
    }

    private Transformer getGrSpecTransformer() throws TransformerConfigurationException {
        if (grSpecTransformer == null) {
            grSpecTransformer = tFactory.newTransformer(getXSL("groupSpec"));
        }
        return grSpecTransformer;
    }

    private Transformer getSchSpecTransformer() throws TransformerConfigurationException {
        if (schSpecTransformer == null) {
            schSpecTransformer = tFactory.newTransformer(getXSL("schemeSpec"));
        }
        return schSpecTransformer;
    }

    private Transformer getRoleTransformer() throws TransformerConfigurationException {
        if (roleTransformer == null) {
            roleTransformer   = tFactory.newTransformer(getXSL("role"));
        }
        return roleTransformer;
    }

    private Transformer getMissionTransformer() throws TransformerConfigurationException {
        if (missionTransformer == null) {
            missionTransformer   = tFactory.newTransformer(getXSL("mission"));
        }
        return missionTransformer;
    }

    private Transformer getGoalTransformer() throws TransformerConfigurationException {
        if (goalTransformer == null) {
            goalTransformer   = tFactory.newTransformer(getXSL("goal"));
        }
        return goalTransformer;
    }

    private Transformer getSSTransformer() throws TransformerConfigurationException {
        if (ssTransformer == null) {
            ssTransformer   = tFactory.newTransformer(getXSL("ss"));
        }
        return ssTransformer;
    }
    private Transformer getFSTransformer() throws TransformerConfigurationException {
        if (fsTransformer == null) {
            fsTransformer   = tFactory.newTransformer(getXSL("fs"));
        }
        return fsTransformer;
    }
    private Transformer getDSTransformer() throws TransformerConfigurationException {
        if (dsTransformer == null) {
            dsTransformer   = tFactory.newTransformer(getXSL("ns"));
        }
        return dsTransformer;
    }

    private Transformer getOETransformer() throws TransformerConfigurationException {
        if (oeTransformer == null) {
            oeTransformer   = tFactory.newTransformer(getXSL("oe"));
        }
        return oeTransformer;
    }
    private Transformer getBriefOE() throws TransformerConfigurationException {
        if (briefOE == null) {
            briefOE   = tFactory.newTransformer(getXSL("brief-oe"));
        }
        return briefOE;
    }
    private Transformer getAgTransformer() throws TransformerConfigurationException {
        if (agTransformer == null) {
            agTransformer   = tFactory.newTransformer(getXSL("agent"));
        }
        return agTransformer;
    }
    private Transformer getGrTransformer() throws TransformerConfigurationException {
        if (grTransformer == null) {
            grTransformer   = tFactory.newTransformer(getXSL("groupInstance"));
        }
        return grTransformer;
    }
    private Transformer getSCHTransformer() throws TransformerConfigurationException {
        if (schTransformer == null) {
            schTransformer   = tFactory.newTransformer(getXSL("schemeInstance"));
        }
        return schTransformer;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents(boolean addSim) {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        showAs = new javax.swing.JComboBox();
        jPanel16 = new javax.swing.JPanel();
        back = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextPane();
        tabPanel = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        oeTree = new javax.swing.JTree();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        osTree = new javax.swing.JTree();
        OESimTabPanel = new javax.swing.JTabbedPane();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel15 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        grCreatedGroups = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        grSpecsInGrCreation = new javax.swing.JComboBox();
        okGroupCreation = new javax.swing.JButton();
        jPanel21 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        grInstancesInGroup = new javax.swing.JComboBox();
        okFinishGr = new javax.swing.JButton();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel19 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        schSpecInSchStart = new javax.swing.JComboBox();
        okSCHStart = new javax.swing.JButton();
        jPanel30 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel29 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        schInstanceInRespGr = new javax.swing.JComboBox();
        jPanel18 = new javax.swing.JPanel();
        grInRespGr = new javax.swing.JComboBox();
        addRespGr = new javax.swing.JButton();
        jPanel28 = new javax.swing.JPanel();
        schGrsInRespGr = new javax.swing.JComboBox();
        remRespGr = new javax.swing.JButton();
        jPanel22 = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        schInstancesInGoal = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        goalInstanceInGoal = new javax.swing.JComboBox();
        jPanel26 = new javax.swing.JPanel();
        goalStateInGoal = new javax.swing.JComboBox();
        setGoalState = new javax.swing.JButton();
        jPanel25 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        goalArgListInGoal = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        argValueInGoal = new javax.swing.JTextField();
        setArgValue = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        schInstanceInSchFinish = new javax.swing.JComboBox();
        finishScheme = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        schInstanceInAbort = new javax.swing.JComboBox();
        abortMission = new javax.swing.JButton();
        jTabbedPane4 = new javax.swing.JTabbedPane();
        jPanel13 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        agName = new javax.swing.JTextField();
        agCreationOk = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        agNamesInRole = new javax.swing.JComboBox();
        agNamesInGoalInstance = new javax.swing.JComboBox();
        adoptORgiveUpRole = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        agRolesInRole = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        grInRole = new javax.swing.JComboBox();
        agAdoptRole = new javax.swing.JButton();
        jPanel20 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        agNamesInMission = new javax.swing.JComboBox();
        commitORuncommit = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        missionInMission = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        schInstancesInMission = new javax.swing.JComboBox();
        okMission = new javax.swing.JButton();
        jPanel27 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        agNamesInRemoveAg = new javax.swing.JComboBox();
        removeAg = new javax.swing.JButton();
        checkRemoveAg = new javax.swing.JCheckBox();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOneTouchExpandable(true);
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.BorderLayout());

        jLabel2.setText("show as");
        jPanel5.add(jLabel2);

        showAs.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "html", "brief-html", "xml" }));
        showAs.setToolTipText("how to show the selected component");
        showAs.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                changeShowAs(evt);
            }
        });

        jPanel5.add(showAs);

        jPanel4.add(jPanel5, java.awt.BorderLayout.WEST);

        back.setText("back");
        back.setEnabled(false);
        back.setIcon(new ImageIcon(SimOEFrame.class.getResource("/images/back.gif")));
        back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backActionPerformed(evt);
            }
        });

        jPanel16.add(back);

        jPanel4.add(jPanel16, java.awt.BorderLayout.CENTER);

        jPanel3.add(jPanel4, java.awt.BorderLayout.SOUTH);

        jScrollPane1.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "component description"));
        textArea.setEditable(false);
        textArea.setAutoscrolls(false);
        textArea.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                hyperLink(evt);
            }
        });

        jScrollPane1.setViewportView(textArea);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(jPanel3);

        tabPanel.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel6.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "main OE components"));
        oeTree.setModel((new OETreeModel()).getModel());
        oeTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                oeTreeValueChanged(evt);
            }
        });

        jScrollPane2.setViewportView(oeTree);

        jPanel6.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel6, java.awt.BorderLayout.CENTER);

        tabPanel.addTab("OE", null, jPanel1, "");

        jPanel2.setLayout(new java.awt.BorderLayout());

        jScrollPane3.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "main OS components"));
        osTree.setToolTipText("click on the element to see details");
        osTree.setMinimumSize(new java.awt.Dimension(200, 100));
        osTree.setModel((new OSTreeModel(osTree)).getModel());
        osTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                osTreeValueChanged(evt);
            }
        });

        jScrollPane3.setViewportView(osTree);

        jPanel2.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        tabPanel.addTab("OS", null, jPanel2, "");

        jSplitPane1.setLeftComponent(tabPanel);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        if (hasSim) {
            OESimTabPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "OE dynamics simulation", javax.swing.border.TitledBorder.TRAILING, javax.swing.border.TitledBorder.DEFAULT_POSITION));
            jTabbedPane3.setTabPlacement(javax.swing.JTabbedPane.LEFT);
            jPanel15.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

            jLabel6.setText("subgroup of");
            jPanel15.add(jLabel6);

            grCreatedGroups.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "root" }));
            jPanel15.add(grCreatedGroups);

            jLabel7.setText("group specification");
            jPanel15.add(jLabel7);

            jPanel15.add(grSpecsInGrCreation);

            okGroupCreation.setText("ok");
            okGroupCreation.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    okGroupCreationActionPerformed(evt);
                }
            });

            jPanel15.add(okGroupCreation);

            jTabbedPane3.addTab("create", null, jPanel15, "");

            jPanel21.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

            jLabel20.setText("group instance");
            jPanel21.add(jLabel20);

            jPanel21.add(grInstancesInGroup);

            okFinishGr.setText("finish");
            okFinishGr.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    okFinishGrActionPerformed(evt);
                }
            });

            jPanel21.add(okFinishGr);

            jTabbedPane3.addTab("remove", null, jPanel21, "");

            OESimTabPanel.addTab("groups", null, jTabbedPane3, "");

            jTabbedPane2.setTabPlacement(javax.swing.JTabbedPane.LEFT);
            jPanel19.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

            jLabel9.setText("scheme specification");
            jPanel19.add(jLabel9);

            jPanel19.add(schSpecInSchStart);

            okSCHStart.setText("start");
            okSCHStart.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    okSCHStartActionPerformed(evt);
                }
            });

            jPanel19.add(okSCHStart);

            jTabbedPane2.addTab("start", null, jPanel19, "");

            jPanel30.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

            jPanel9.setLayout(new java.awt.GridBagLayout());

            jPanel29.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 1));

            jPanel29.setBorder(new javax.swing.border.TitledBorder(null, "scheme selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 2, 11)));
            jLabel12.setText("scheme");
            jPanel29.add(jLabel12);

            schInstanceInRespGr.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    schInstanceInRespGrActionPerformed(evt);
                }
            });

            jPanel29.add(schInstanceInRespGr);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            jPanel9.add(jPanel29, gridBagConstraints);

            jPanel18.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 1));

            jPanel18.setBorder(new javax.swing.border.TitledBorder(null, "add group", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 2, 11)));
            jPanel18.add(grInRespGr);

            addRespGr.setText("add");
            addRespGr.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    addRespGrActionPerformed(evt);
                }
            });

            jPanel18.add(addRespGr);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            jPanel9.add(jPanel18, gridBagConstraints);

            jPanel28.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 1));

            jPanel28.setBorder(new javax.swing.border.TitledBorder(null, "remove group", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 2, 11)));
            jPanel28.add(schGrsInRespGr);

            remRespGr.setText("remove");
            remRespGr.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    remRespGrActionPerformed(evt);
                }
            });

            jPanel28.add(remRespGr);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            jPanel9.add(jPanel28, gridBagConstraints);

            jPanel30.add(jPanel9);

            jTabbedPane2.addTab("responsible groups", null, jPanel30, "");

            jPanel22.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

            jPanel23.setLayout(new java.awt.GridBagLayout());

            jPanel24.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 1));

            jPanel24.setBorder(new javax.swing.border.TitledBorder(null, "goal selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 2, 11)));
            jLabel13.setText("scheme");
            jPanel24.add(jLabel13);

            schInstancesInGoal.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    schInstancesInGoalItemStateChanged(evt);
                }
            });

            jPanel24.add(schInstancesInGoal);

            jLabel14.setText("goal");
            jPanel24.add(jLabel14);

            goalInstanceInGoal.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    goalInstanceInGoalItemStateChanged(evt);
                }
            });

            jPanel24.add(goalInstanceInGoal);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            jPanel23.add(jPanel24, gridBagConstraints);

            jPanel26.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 1));

            jPanel26.setBorder(new javax.swing.border.TitledBorder(null, "state", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 2, 11)));
            goalStateInGoal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "waiting", "satisfied", "impossible" }));
            jPanel26.add(goalStateInGoal);
            jPanel26.add(new JLabel("by"));
            jPanel26.add(agNamesInGoalInstance);

            setGoalState.setText("set");
            setGoalState.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setGoalStateActionPerformed(evt);
                }
            });

            jPanel26.add(setGoalState);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            jPanel23.add(jPanel26, gridBagConstraints);

            jPanel25.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 1));

            jPanel25.setBorder(new javax.swing.border.TitledBorder(null, "arguments", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 2, 11)));
            jLabel15.setText("argument");
            jPanel25.add(jLabel15);

            goalArgListInGoal.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    goalArgListInGoalItemStateChanged(evt);
                }
            });

            jPanel25.add(goalArgListInGoal);

            jLabel16.setText("value");
            jPanel25.add(jLabel16);

            argValueInGoal.setColumns(10);
            argValueInGoal.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setArgValueActionPerformed(evt);
                }
            });

            jPanel25.add(argValueInGoal);

            setArgValue.setText("set");
            setArgValue.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setArgValueActionPerformed(evt);
                }
            });

            jPanel25.add(setArgValue);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            jPanel23.add(jPanel25, gridBagConstraints);

            jPanel22.add(jPanel23);

            jTabbedPane2.addTab("goals state", null, jPanel22, "");

            jPanel10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

            jLabel18.setText("scheme instance");
            jPanel10.add(jLabel18);

            jPanel10.add(schInstanceInSchFinish);

            finishScheme.setText("finish");
            finishScheme.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    finishSchemeActionPerformed(evt);
                }
            });

            jPanel10.add(finishScheme);

            jTabbedPane2.addTab("finish", null, jPanel10, "");

            jPanel12.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

            jLabel17.setText("scheme instance");
            jPanel12.add(jLabel17);

            jPanel12.add(schInstanceInAbort);

            abortMission.setText("abort");
            abortMission.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    abortMissionActionPerformed(evt);
                }
            });

            jPanel12.add(abortMission);

            jTabbedPane2.addTab("abort", null, jPanel12, "");

            OESimTabPanel.addTab("schemes", null, jTabbedPane2, "");

            jTabbedPane4.setTabPlacement(javax.swing.JTabbedPane.LEFT);
            jPanel13.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

            jLabel1.setText("agent's name");
            jPanel13.add(jLabel1);

            agName.setColumns(10);
            agName.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    agCreationOkPerformed(evt);
                }
            });

            jPanel13.add(agName);

            agCreationOk.setText("create");
            agCreationOk.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    agCreationOkPerformed(evt);
                }
            });

            jPanel13.add(agCreationOk);

            jTabbedPane4.addTab("create", null, jPanel13, "");

            jPanel14.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

            jLabel3.setText("agent");
            jPanel14.add(jLabel3);

            agNamesInRole.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    agNamesInRoleActionPerformed(evt);
                }
            });

            jPanel14.add(agNamesInRole);

            adoptORgiveUpRole.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "adopts", "gives up" }));
            adoptORgiveUpRole.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    adoptORgiveUpRoleItemStateChanged(evt);
                }
            });

            jPanel14.add(adoptORgiveUpRole);

            jLabel4.setText("the role");
            jPanel14.add(jLabel4);

            jPanel14.add(agRolesInRole);

            jLabel5.setText("in the group");
            jPanel14.add(jLabel5);

            jPanel14.add(grInRole);

            agAdoptRole.setText("ok");
            agAdoptRole.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    agAdoptRoleActionPerformed(evt);
                }
            });

            jPanel14.add(agAdoptRole);

            jTabbedPane4.addTab("roles", null, jPanel14, "");

            jPanel20.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

            jLabel8.setText("agent");
            jPanel20.add(jLabel8);

            agNamesInMission.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    agNamesInMissionActionPerformed(evt);
                }
            });

            jPanel20.add(agNamesInMission);

            commitORuncommit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "commits", "uncommits" }));
            commitORuncommit.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    commitORuncommitItemStateChanged(evt);
                }
            });

            jPanel20.add(commitORuncommit);

            jLabel10.setText("to the mission");
            jPanel20.add(jLabel10);

            jPanel20.add(missionInMission);

            jLabel11.setText("in the scheme");
            jPanel20.add(jLabel11);

            jPanel20.add(schInstancesInMission);

            okMission.setText("ok");
            okMission.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    okMissionActionPerformed(evt);
                }
            });

            jPanel20.add(okMission);

            jTabbedPane4.addTab("missions", null, jPanel20, "");

            jPanel27.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

            jLabel22.setText("agent");
            jPanel27.add(jLabel22);

            jPanel27.add(agNamesInRemoveAg);

            removeAg.setText("remove");
            removeAg.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    removeAgActionPerformed(evt);
                }
            });

            jPanel27.add(removeAg);

            checkRemoveAg.setSelected(true);
            checkRemoveAg.setText("check consistencies");
            checkRemoveAg.setToolTipText("will (or not) check if the agent is playing a role");
            jPanel27.add(checkRemoveAg);

            jTabbedPane4.addTab("remove", null, jPanel27, "");

            OESimTabPanel.addTab("agents", null, jTabbedPane4, "");

            getContentPane().add(OESimTabPanel, java.awt.BorderLayout.SOUTH);
        }

        pack();
    }//GEN-END:initComponents

    private void schInstanceInRespGrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schInstanceInRespGrActionPerformed
        // Add your handling code here:
        updateSchemeRespGoals();
    }//GEN-LAST:event_schInstanceInRespGrActionPerformed

    private void remRespGrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remRespGrActionPerformed
        // Add your handling code here:
        ag.remResponsibleGroupToSCH( (SchemeInstance)schInstanceInRespGr.getSelectedItem(), (GroupInstance)schGrsInRespGr.getSelectedItem());
    }//GEN-LAST:event_remRespGrActionPerformed

    private void addRespGrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRespGrActionPerformed
        // Add your handling code here:
        ag.addResponsibleGroupToSCH( (SchemeInstance)schInstanceInRespGr.getSelectedItem(), (GroupInstance)grInRespGr.getSelectedItem());
    }//GEN-LAST:event_addRespGrActionPerformed

    private void commitORuncommitItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_commitORuncommitItemStateChanged
        // Add your handling code here:
        updateAgentMissionComponents();
    }//GEN-LAST:event_commitORuncommitItemStateChanged

    private void agNamesInMissionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agNamesInMissionActionPerformed
        // Add your handling code here:
        updateAgentMissionComponents();
    }//GEN-LAST:event_agNamesInMissionActionPerformed

    private void agNamesInRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agNamesInRoleActionPerformed
        // Add your handling code here:
        updateAgentRoleComponents();
    }//GEN-LAST:event_agNamesInRoleActionPerformed

    private void adoptORgiveUpRoleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_adoptORgiveUpRoleItemStateChanged
        // Add your handling code here:
        updateAgentRoleComponents();
    }//GEN-LAST:event_adoptORgiveUpRoleItemStateChanged

    private void removeAgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAgActionPerformed
        // Add your handling code here:
        ag.removeAg(agNamesInRemoveAg.getSelectedItem().toString(), checkRemoveAg.isSelected());
    }//GEN-LAST:event_removeAgActionPerformed

    private void abortMissionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abortMissionActionPerformed
        // Add your handling code here:
        ag.abortSCH((SchemeInstance)schInstanceInAbort.getSelectedItem());
    }//GEN-LAST:event_abortMissionActionPerformed

    private void okFinishGrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okFinishGrActionPerformed
        // Add your handling code here:
        ag.removeGr( grInstancesInGroup.getSelectedItem().toString() );
    }//GEN-LAST:event_okFinishGrActionPerformed

    private void finishSchemeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finishSchemeActionPerformed
        // Add your handling code here:
        ag.finishSCH((SchemeInstance)schInstanceInSchFinish.getSelectedItem());
    }//GEN-LAST:event_finishSchemeActionPerformed

    private void setGoalStateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setGoalStateActionPerformed
        // Add your handling code here:
        GoalInstance gi = (GoalInstance)goalInstanceInGoal.getSelectedItem();
        if (gi != null) {
            if (goalStateInGoal.getSelectedIndex() == 1) {
                ag.setGoalStateSatisfied(gi, (OEAgent)agNamesInGoalInstance.getSelectedItem());
            }
            if (goalStateInGoal.getSelectedIndex() == 2) {
                ag.setGoalStateImpossible(gi, (OEAgent)agNamesInGoalInstance.getSelectedItem());
            }
        }
    }//GEN-LAST:event_setGoalStateActionPerformed

    private void setArgValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setArgValueActionPerformed
        // Add your handling code here:
        GoalInstance gi = (GoalInstance)goalInstanceInGoal.getSelectedItem();
        if (gi != null) {
            String selArg = (String)goalArgListInGoal.getSelectedItem();
            if (selArg != null) {
                ag.setGoalArg( gi, selArg, argValueInGoal.getText().trim());
            }
        }
    }//GEN-LAST:event_setArgValueActionPerformed

    private void goalArgListInGoalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_goalArgListInGoalItemStateChanged
        // Add your handling code here:
        updateGoalArgValue();
    }//GEN-LAST:event_goalArgListInGoalItemStateChanged

    private void goalInstanceInGoalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_goalInstanceInGoalItemStateChanged
        // Add your handling code here:
        updateGoalsComponents();
    }//GEN-LAST:event_goalInstanceInGoalItemStateChanged

    private void schInstancesInGoalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_schInstancesInGoalItemStateChanged
        // Add your handling code here:
        updateSchemeGoalsCB();
    }//GEN-LAST:event_schInstancesInGoalItemStateChanged

    private void okMissionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okMissionActionPerformed
        // Add your handling code here:
        if (commitORuncommit.getSelectedIndex() == 0) {
            ag.agCommitsMission(agNamesInMission.getSelectedItem().toString(),
            missionInMission.getSelectedItem().toString(),
            schInstancesInMission.getSelectedItem().toString());
        } else {
            ag.agUncommitsMission(agNamesInMission.getSelectedItem().toString(),
            missionInMission.getSelectedItem().toString(),
            schInstancesInMission.getSelectedItem().toString());
        }
    }//GEN-LAST:event_okMissionActionPerformed

    private void okSCHStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okSCHStartActionPerformed
        // Add your handling code here:
        ag.startSCH(schSpecInSchStart.getSelectedItem().toString());
    }//GEN-LAST:event_okSCHStartActionPerformed

    private void pushHistory(Object o) {
        if (o == null) return;

        if (history.isEmpty()) {
            history.push(o);
        } else if (history.peek() != o) {
            back.setToolTipText(history.peek().toString());
            history.push(o);
        }

        if (history.size() <= 1) {
            back.setEnabled(false);
            back.setToolTipText("no back");
        } else {
            back.setEnabled(true);
        }
        //System.out.println("pushing "+o.toString());
    }

    public void pbackActionPerformed() {
        backActionPerformed(null);
    }
    private void backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backActionPerformed
        // Add your handling code here:
        if (! history.isEmpty()) {
            history.pop();
            if (! history.isEmpty()) {
                currentObject = history.peek();

                // set tool tiop
                history.pop();
                if (! history.isEmpty()) {
                    back.setToolTipText(history.peek().toString());
                }
                history.push(currentObject);
            }
            showCurrentObject();
        }
        if (history.size() <= 1) {
            back.setEnabled(false);
            back.setToolTipText("no back");
        } else {
            back.setEnabled(true);
        }
    }//GEN-LAST:event_backActionPerformed

    private void agAdoptRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agAdoptRoleActionPerformed
        // Add your handling code here:
        if (adoptORgiveUpRole.getSelectedIndex() == 0) {
            ag.agAdoptsRole(agNamesInRole.getSelectedItem().toString(),
            agRolesInRole.getSelectedItem().toString(),
            grInRole.getSelectedItem().toString());
        } else {
            ag.agGiveUpRole(agNamesInRole.getSelectedItem().toString(),
            agRolesInRole.getSelectedItem().toString(),
            grInRole.getSelectedItem().toString());
        }
    }//GEN-LAST:event_agAdoptRoleActionPerformed

    private void okGroupCreationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okGroupCreationActionPerformed
        // Add your handling code here:
        ag.createGr(grCreatedGroups.getSelectedItem().toString(), grSpecsInGrCreation.getSelectedItem().toString());
    }//GEN-LAST:event_okGroupCreationActionPerformed

    private void agCreationOkPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agCreationOkPerformed
        // Add your handling code here:
        ag.createAg( agName.getText().trim());
    }//GEN-LAST:event_agCreationOkPerformed

    private void oeTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_oeTreeValueChanged
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)evt.getPath().getLastPathComponent();
        currentObject = node.getUserObject();
        try {
            if (evt.getPath().getPath().length == 1) {
                currentObject = ag.getCurrentOE();
            }
            pushHistory(currentObject);
            showCurrentObject();
        } catch (Exception e) {}
    }//GEN-LAST:event_oeTreeValueChanged

    private void hyperLink(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_hyperLink
        // Add your handling code here:
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            //System.out.println("evt="+evt.getDescription());
            String uri = "role.xsl?roleId=";
            int pos = evt.getDescription().indexOf(uri);
            if (pos >= 0) {
                String id = evt.getDescription().substring(uri.length());
                currentObject = ag.getCurrentOS().getSS().getRoleDef(id);
            } else {
                uri = "groupSpec.xsl?grSpecId=";
                pos = evt.getDescription().indexOf(uri);
                if (pos >= 0) {
                    String id = evt.getDescription().substring(uri.length());
                    currentObject = ag.getCurrentOS().getSS().getRootGrSpec().findSubGroup(id);
                } else {
                    uri = "schemeSpec.xsl?schemeSpecId=";
                    pos = evt.getDescription().indexOf(uri);
                    if (pos >= 0) {
                        String id = evt.getDescription().substring(uri.length());
                        currentObject = ag.getCurrentOS().getFS().findScheme(id);
                    } else {
                        uri = "mission.xsl?missionId=";
                        pos = evt.getDescription().indexOf(uri);
                        if (pos >= 0) {
                            String id = evt.getDescription().substring(uri.length());
                            currentObject = ag.getCurrentOS().getFS().findMission(id);
                        } else {
                            uri = "goal.xsl?goalId=";
                            pos = evt.getDescription().indexOf(uri);
                            if (pos >= 0) {
                                String id = evt.getDescription().substring(uri.length());
                                currentObject = ag.getCurrentOS().getFS().findGoal(id);
                            } else {
                                uri = "agent.xsl?agentId=";
                                pos = evt.getDescription().indexOf(uri);
                                if (pos >= 0) {
                                    String id = evt.getDescription().substring(uri.length());
                                    currentObject = ag.getCurrentOE().getAgent(id);
                                } else {
                                    uri = "groupInstance.xsl?groupId=";
                                    pos = evt.getDescription().indexOf(uri);
                                    if (pos >= 0) {
                                        String id = evt.getDescription().substring(uri.length());
                                        currentObject = ag.getCurrentOE().findGroup(id);
                                    } else {
                                        uri = "schemeInstance.xsl?schId=";
                                        pos = evt.getDescription().indexOf(uri);
                                        if (pos >= 0) {
                                            String id = evt.getDescription().substring(uri.length());
                                            currentObject = ag.getCurrentOE().findScheme(id);
                                        } else {
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            pushHistory(currentObject);
            showCurrentObject();
        }
    }//GEN-LAST:event_hyperLink

    private void changeShowAs(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_changeShowAs
        // Add your handling code here:
        showCurrentObject();
    }//GEN-LAST:event_changeShowAs


    private void osTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_osTreeValueChanged
        // Add your handling code here:
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)evt.getPath().getLastPathComponent();
        currentObject = node.getUserObject();
        try {
            if (evt.getPath().getPath().length == 1) {
                currentObject = ag.getCurrentOS();
            }
            pushHistory(currentObject);
            showCurrentObject();
        } catch (Exception e) {}
    }//GEN-LAST:event_osTreeValueChanged


    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        //System.exit(0);
        //ag.stopAg();
    }//GEN-LAST:event_exitForm


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JButton setGoalState;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JComboBox missionInMission;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JTextField argValueInGoal;
    private javax.swing.JCheckBox checkRemoveAg;
    private javax.swing.JComboBox agNamesInRemoveAg;
    public javax.swing.JTabbedPane tabPanel;
    private javax.swing.JComboBox grInRespGr;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JButton addRespGr;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JButton okFinishGr;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JComboBox schInstanceInRespGr;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JButton removeAg;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox schSpecInSchStart;
    private javax.swing.JComboBox goalInstanceInGoal;
    private javax.swing.JButton agAdoptRole;
    private javax.swing.JComboBox schInstancesInGoal;
    private javax.swing.JButton setArgValue;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JComboBox goalStateInGoal;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane OESimTabPanel;
    private javax.swing.JTree osTree;
    private javax.swing.JComboBox schInstanceInSchFinish;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JButton remRespGr;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTree oeTree;
    private javax.swing.JComboBox showAs;
    private javax.swing.JComboBox grInstancesInGroup;
    private javax.swing.JComboBox agNamesInMission;
    private javax.swing.JComboBox schInstancesInMission;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JComboBox schInstanceInAbort;
    private javax.swing.JComboBox grSpecsInGrCreation;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JComboBox adoptORgiveUpRole;
    private javax.swing.JComboBox commitORuncommit;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JButton back;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JButton agCreationOk;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JTextPane textArea;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JComboBox agRolesInRole;
    private javax.swing.JTextField agName;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JButton abortMission;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JComboBox goalArgListInGoal;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox grCreatedGroups;
    private javax.swing.JComboBox schGrsInRespGr;
    private javax.swing.JButton okSCHStart;
    private javax.swing.JComboBox agNamesInRole;
    private javax.swing.JComboBox agNamesInGoalInstance;
    private javax.swing.JButton finishScheme;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JButton okMission;
    private javax.swing.JComboBox grInRole;
    private javax.swing.JButton okGroupCreation;
    private javax.swing.JPanel jPanel29;
    // End of variables declaration//GEN-END:variables

}
