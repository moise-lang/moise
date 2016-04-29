package ora4mas.nopl.simulator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import moise.os.OS;
import moise.xml.DOMUtils;
import npl.DeonticModality;
import npl.DynamicFactsProvider;
import npl.NPLInterpreter;
import npl.NormativeFailureException;
import npl.NormativeListener;
import npl.NormativeProgram;
import npl.Scope;
import npl.parser.ParseException;
import npl.parser.nplp;
import ora4mas.nopl.oe.Group;
import ora4mas.nopl.oe.OE;
import ora4mas.nopl.oe.Scheme;

/** simple simulator used to demonstrate/test NOPL for MOISE */
public class OIgui implements DynamicFactsProvider {
    
    public static void main(String[] args) throws Exception {
        OIgui oi = new OIgui("examples/writePaper/wp-os.xml", "examples/writePaper/wp-gen.npl");
        oi.initComponents();
        oi.initOE();
    }
    
    NPLInterpreter schInterpreter = new NPLInterpreter();
    Scheme sch;
    OS     os;
    OE     oe = new OE();

    public OIgui(String osFile, String npSrcFile) throws FileNotFoundException, ParseException {
        os = OS.loadOSFromURI(osFile); // parse OS
        
        NormativeProgram p = new NormativeProgram();
        p.setSrc(npSrcFile);
        new nplp(new FileReader(npSrcFile)).program(p, this);

        schInterpreter.setScope(p.getRoot());
        for (Scope s: p.getRoot().getScopes()) {
            if (s.getId().getFunctor().equals("scheme")) {
                schInterpreter.loadNP(s);                
            }
        }
    }
    
    void initOE() {
        Group g = new Group("wp1");
        g.addPlayer("jaime", "editor");
        g.addPlayer("olivier", "writer");
        g.addPlayer("jomi", "writer");
        g.addResponsibleForScheme("sch2");
        oe.addGroup(g);
        
        sch = new Scheme(os.getFS().findScheme("writePaperSch"), "sch2");
        sch.addGroupResponsibleFor(g);
        oe.addScheme(sch);
        
        updateOE();

        //schInterpreter.setDynamicFacts(sch.transform());
        try {
            schInterpreter.verifyNorms();
        } catch (NormativeFailureException e) {
            e.printStackTrace();
        }

        updateNP();

        ScheduledThreadPoolExecutor updater = new ScheduledThreadPoolExecutor(1);
        updater.scheduleAtFixedRate(new Runnable() {
            public void run() {
                updateOE();
                updateNS();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
    
    public void executeAct(Literal action) {
        Scheme schbak = sch.clone();
        if (action.getFunctor().equals("commitMission")) {
            sch.addPlayer(action.getTerm(0).toString(), action.getTerm(1).toString());
        } else if (action.getFunctor().equals("setGoalAchieved")) {
            sch.addGoalAchieved(action.getTerm(0).toString(), action.getTerm(1).toString());
        }
        txtLog.setText("action "+action+ " in oe "+sch.transform()+"\n");
        try {
            //schInterpreter.setDynamicFacts(sch.transform());
            Collection<DeonticModality> result = schInterpreter.verifyNorms();
            txtLog.append("new obligations:"+result);
        } catch (NormativeFailureException e) {
            txtLog.append("** action failed: "+e.getFail());
            sch = schbak; // takes the backup scheme as the current since the action failed
            oe.removeSch(sch.getId());
            oe.addScheme(sch);
        }
        //updateOE();
        //updateNS();
    }
    
    public void executeAct(LogicalFormula expr) {
        try {
            schInterpreter.verifyNorms(); // update norms state
            txtLog.setText("executing query "+expr+", solutions:\n");
            Iterator<Unifier> i = expr.logicalConsequence(schInterpreter.getAg(), new Unifier());
            if (!i.hasNext())
                txtLog.append("-- no solution --");
            while (i.hasNext()) {
                Unifier u = i.next();
                Term t = expr.capply(u);
                txtLog.append("  "+t+"\n");
            }
        } catch (Exception e) {
            txtLog.append("Error: "+e);
        }
    }
    
    private String lastOEStr = "";
    void updateOE() {
        String curOE = oe.toString();
        if (! curOE.equals(lastOEStr))
            txtOE.setText( curOE );
        lastOEStr = curOE;
    }
    
    void updateNP() {
        try {
            File fin = new File(schInterpreter.getScope().getNP().getSrc());
            StringBuilder out = new StringBuilder();
            BufferedReader bin = new BufferedReader(new FileReader(fin));
            String l = bin.readLine();
            while (l != null) {
                out.append(l+"\n");
                l = bin.readLine();
            }
            txtNP.setText(out.toString());    
        } catch (IOException e) {
            txtNP.setText(schInterpreter.getScope().toString()); 
            e.printStackTrace();
        }
    }
    
    private String lastNSStr = "";
    void updateNS() {
        //txtNS.setText(schInterpreter.getStateString());
        //txtNS.setText(DOMUtils.dom2txt(schInterpreter));
        try {
            StringWriter so = new StringWriter();            
            getNSTransformer().transform(new DOMSource(DOMUtils.getAsXmlDocument(schInterpreter)), new StreamResult(so));
            String curStr = so.toString();
            if (! curStr.equals(lastNSStr))
                txtNS.setText(curStr);
            lastNSStr = curStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    JFrame    frame  = new JFrame(":: Scheme Tester ::");
    JTextPane txtOE  = new JTextPane();
    JTextPane txtNP  = new JTextPane();
    JTextPane txtNS  = new JTextPane();
    JTextArea txtLog = new JTextArea(7, 10);
    
    void initComponents() throws Exception {        
        // osp
        JPanel osp = new JPanel(new BorderLayout());
        JTextPane ostext = new JTextPane();
        ostext.setContentType("text/html");
        ostext.setEditable(false);
        ostext.setAutoscrolls(false);
        StringWriter so = new StringWriter();
        InputSource si = new InputSource(new StringReader(DOMUtils.dom2txt(os)));
        getOSTransformer().transform(new DOMSource(getParser().parse(si)), new StreamResult(so)); //DOMUtils.getAsXmlDocument(OS_DOM))
        ostext.setText(so.toString());
        osp.add(BorderLayout.CENTER, new JScrollPane(ostext));
        
        // nsp
        JPanel nsp = new JPanel(new BorderLayout());
        txtNS.setContentType("text/html");
        txtNS.setEditable(false);
        txtNS.setAutoscrolls(false);
        nsp.add(BorderLayout.CENTER, new JScrollPane(txtNS));

        // npp
        JPanel npp = new JPanel(new BorderLayout());
        txtNP.setContentType("text/plain");
        txtNP.setFont(new Font("courier", Font.PLAIN, 14));
        txtNP.setEditable(false);
        npp.add(BorderLayout.CENTER, new JScrollPane(txtNP));

        // oep
        JPanel oep = new JPanel(new BorderLayout());
        txtOE.setContentType("text/plain");
        txtOE.setFont(new Font("courier", Font.PLAIN, 16));
        txtOE.setEditable(false);
        oep.add(BorderLayout.CENTER, new JScrollPane(txtOE));

        // history
        JPanel hp = new JPanel(new BorderLayout());
        final JTextArea txtHist = new JTextArea();
        txtHist.setFont(new Font("courier", Font.PLAIN, 16));
        txtHist.setEditable(false);
        hp.add(BorderLayout.CENTER, new JScrollPane(txtHist));
        
        // center tabled 
        JTabbedPane tpane = new JTabbedPane();
        tpane.add("normative state", nsp);
        tpane.add("history",hp);
        tpane.add("normative program", npp);
        tpane.add("organisation entity", oep);
        tpane.add("specification", osp);
        
        // actions
        JPanel actionsp = new JPanel(new GridLayout(3,1));
        //actionsp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Actions", TitledBorder.LEFT, TitledBorder.TOP));
        JPanel commitp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        commitp.add(new JLabel("commit to missions (agent,mission)"));
        final JTextField cag = new JTextField(10); commitp.add(cag); cag.setText("jaime");
        final JTextField cmi = new JTextField(10); commitp.add(cmi); cmi.setText("mManager");
        JButton    cbt = new JButton("commit");  commitp.add(cbt);
        actionsp.add(commitp);
        cbt.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               executeAct(ASSyntax.createLiteral("commitMission", ASSyntax.createAtom(cag.getText().trim()), ASSyntax.createAtom(cmi.getText().trim())));
           } 
        });

        JPanel achp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        achp.add(new JLabel("achieve goal (agent,goal)"));
        final JTextField aag = new JTextField(10); achp.add(aag); aag.setText("jaime");
        final JTextField ago = new JTextField(10); achp.add(ago); ago.setText("wtitle");
        JButton    abt = new JButton("achieve");  achp.add(abt);
        actionsp.add(achp);
        abt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executeAct(ASSyntax.createLiteral("setGoalAchieved", ASSyntax.createAtom(aag.getText().trim()), ASSyntax.createAtom(ago.getText().trim())));
            } 
        });

        JPanel queryp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        queryp.add(new JLabel("query"));
        final JTextField tquery = new JTextField(20); queryp.add(tquery);
        tquery.setText("well_formed(sch2)");
        JButton    qbt = new JButton("query");  queryp.add(qbt);
        actionsp.add(queryp);
        qbt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    executeAct(ASSyntax.parseFormula(tquery.getText()));
                } catch (jason.asSyntax.parser.ParseException e1) {
                    e1.printStackTrace();
                }
            } 
        });
     
        txtLog.setEditable(false); 
        txtLog.setAutoscrolls(false);
        JPanel sul = new JPanel(new BorderLayout());
        sul.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Actions", TitledBorder.LEFT, TitledBorder.TOP));
        sul.add(BorderLayout.CENTER, actionsp);
        sul.add(BorderLayout.SOUTH, new JScrollPane(txtLog));
     
        frame.getContentPane().setLayout(new BorderLayout());
        frame.add(BorderLayout.CENTER, tpane);
        frame.add(BorderLayout.SOUTH, sul);
        frame.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(0, 0, 800, (int)(screenSize.height * 0.9));
        frame.setLocation(screenSize.width / 2 - frame.getWidth() / 2, screenSize.height / 2 - frame.getHeight() / 2);        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(true);            
            }
        });
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        // add listener for changes
        schInterpreter.addListener(new NormativeListener() {
           public void created(DeonticModality o) {     txtHist.append("created:     "+o+"\n");  }
           public void fulfilled(DeonticModality o) {   txtHist.append("fulfilled:   "+o+"\n");  }
           public void unfulfilled(DeonticModality o) { txtHist.append("unfulfilled: "+o+"\n");  }
           public void inactive(DeonticModality o) {    txtHist.append("inactive:    "+o+"\n");  }
           public void failure(Structure f) {      txtHist.append("failure:     "+f+"\n");  }
        });
    }
    
    private DocumentBuilder parser;
    private DocumentBuilder getParser() throws ParserConfigurationException {
        if (parser == null)
            parser = DOMUtils.getParser();
        return parser;
    }
    private Transformer getOSTransformer() throws TransformerConfigurationException, TransformerFactoryConfigurationError, IOException {
        return DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("os"));
    }
    private Transformer getNSTransformer() throws TransformerConfigurationException, TransformerFactoryConfigurationError, IOException {
        return DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("nstate"));
    }

    
    public boolean isRelevant(PredicateIndicator pi) {
        return sch != null && sch.isRelevant(pi);
    }
    public Iterator<Unifier> consult(Literal l, Unifier u) {
        return sch.consult(l, u);
    }
}
