package ora4mas.nopl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jason.asSyntax.Structure;
import moise.xml.DOMUtils;
import moise.xml.ToXML;
import npl.DeonticModality;
import npl.NPLInterpreter;
import npl.NormativeListener;

/** General GUI for OrgArts */
public class GUIInterface {

    NPLInterpreter nengine;

    private static JFrame  frame;
    private static JTabbedPane allArtsPane;
    private static ScheduledThreadPoolExecutor updater = new ScheduledThreadPoolExecutor(1);
    private static int guiCount = 0;

    private JTabbedPane tpane;
    private JTextPane txtOE  = new JTextPane();
    private JTextPane txtNF  = new JTextPane();
    private JTextPane txtNS  = new JTextPane();
    private JTextArea txtNP  = null;
    private JTextPane ostext = null;
    private JTextArea txtLog = new JTextArea(9, 10);
    private JPanel    artPanel;

    private GUIInterface() {
    }

    private static void initFrame() {
        frame = new JFrame("..:: Organisation Inspector ::..");
        allArtsPane = new JTabbedPane(JTabbedPane.LEFT);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.add(BorderLayout.CENTER, allArtsPane);
        frame.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        guiCount = guiCount+30;
        frame.setBounds(0, 0, 800, (int)(screenSize.height * 0.8));
        frame.setLocation((screenSize.width / 2)-guiCount - frame.getWidth() / 2, (screenSize.height / 2)+guiCount - frame.getHeight() / 2);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(true);
            }
        });
    }

    public static GUIInterface add(String id, String title, NPLInterpreter nengine, boolean hasOE) throws Exception {
        if (frame == null)
            initFrame();

        final GUIInterface gui = new GUIInterface();
        gui.nengine = nengine;

        // normative state
        JPanel nsp = new JPanel(new BorderLayout());
        gui.txtNS.setContentType("text/html");
        gui.txtNS.setEditable(false);
        gui.txtNS.setAutoscrolls(false);
        nsp.add(BorderLayout.CENTER, new JScrollPane(gui.txtNS));

        // organisational entity
        JPanel oep = new JPanel(new BorderLayout());
        gui.txtOE.setContentType("text/html");
        gui.txtOE.setEditable(false);
        gui.txtOE.setAutoscrolls(false);
        oep.add(BorderLayout.CENTER, new JScrollPane(gui.txtOE));

        // normative facts
        JPanel nFacts = new JPanel(new BorderLayout());
        gui.txtNF.setContentType("text/plain");
        gui.txtNF.setFont(new Font("courier", Font.PLAIN, 16));
        gui.txtNF.setEditable(false);
        gui.txtNF.setAutoscrolls(false);
        nFacts.add(BorderLayout.CENTER, new JScrollPane(gui.txtNF));

        // center tabled
        gui.tpane = new JTabbedPane();
        if (hasOE)
            gui.tpane.add("organisation entity", oep);
        gui.tpane.add("normative state", nsp);
        gui.tpane.add("normative facts", nFacts);

        gui.txtLog.setEditable(false);
        gui.txtLog.setAutoscrolls(false);
        gui.txtLog.setFont(new Font("courier", Font.PLAIN, 14));

        JPanel sul = new JPanel(new BorderLayout());
        sul.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "History", TitledBorder.LEFT, TitledBorder.TOP));
        sul.add(BorderLayout.SOUTH, new JScrollPane(gui.txtLog));

        gui.artPanel = new JPanel(new BorderLayout());
        gui.artPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), title, TitledBorder.CENTER, TitledBorder.TOP));

        gui.artPanel.add(BorderLayout.CENTER, gui.tpane);
        gui.artPanel.add(BorderLayout.SOUTH, sul);
        allArtsPane.add(id, gui.artPanel);

        updater.scheduleAtFixedRate(new Runnable() {
            public void run() {
                gui.updateNS();
            }
        }, 0, 1, TimeUnit.SECONDS);

        // add listener for changes
        nengine.addListener(new NormativeListener() {
           public void created(DeonticModality o) {     gui.txtLog.append("created:     "+o+"\n");  }
           public void fulfilled(DeonticModality o) {   gui.txtLog.append("fulfilled:   "+o+"\n");  }
           public void unfulfilled(DeonticModality o) { gui.txtLog.append("unfulfilled: "+o+"\n");  }
           public void inactive(DeonticModality o) {    gui.txtLog.append("inactive:    "+o+"\n");  }
           public void failure(Structure f) {      gui.txtLog.append("failure:     "+f+"\n");  }
        });

        return gui;
    }

    public void remove() {
        allArtsPane.remove(artPanel);
    }

    public void setNormativeProgram(String source) {
        // normative program
        JPanel npp = new JPanel(new BorderLayout());
        if (txtNP == null) {
            txtNP = new JTextArea();
            txtNP.setFont(new Font("courier", Font.PLAIN, 14));
            txtNP.setEditable(false);
            npp.add(BorderLayout.CENTER, new JScrollPane(txtNP));
            tpane.add("normative program", npp);
        }
        txtNP.setText(source);
    }

    public void setSpecification(String sSpec) throws Exception {
        if (ostext == null) {
            JPanel osp = new JPanel(new BorderLayout());
            ostext = new JTextPane();
            ostext.setContentType("text/html");
            ostext.setEditable(false);
            ostext.setAutoscrolls(false);
            osp.add(BorderLayout.CENTER, new JScrollPane(ostext));
            tpane.add("specification", osp);
        }
        ostext.setText(sSpec);
    }

    private String lastOEStr = "";
    public void updateOE(ToXML oe, Transformer transformer) throws Exception {
        if (transformer == null) return;

        StringWriter so = new StringWriter();
        transformer.transform(new DOMSource(DOMUtils.getAsXmlDocument(oe)), new StreamResult(so));
        String sOE = so.toString();
        if (! sOE.equals(lastOEStr)) {
            txtOE.setText( sOE );
        }
        lastOEStr = sOE;
    }

    private String lastNFacts = "";
    public void updateNFacts(String nFacts)  {
        if (! nFacts.equals(lastNFacts)) {
            txtNF.setText( nFacts);
        }
        lastOEStr = nFacts;
    }


    private String lastNSStr = "";
    public void updateNS() {
        //txtNS.setText(schInterpreter.getStateString());
        //txtNS.setText(DOMUtils.dom2txt(schInterpreter));
        try {
            StringWriter so = new StringWriter();
            getNSTransformer().transform(new DOMSource(DOMUtils.getAsXmlDocument(nengine)), new StreamResult(so));
            String curStr = so.toString();
            if (! curStr.equals(lastNSStr))
                txtNS.setText(curStr);
            lastNSStr = curStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DocumentBuilder parser;
    public DocumentBuilder getParser() throws ParserConfigurationException {
        if (parser == null)
            parser = DOMUtils.getParser();
        return parser;
    }

    private Transformer nsTransformer = null;
    public Transformer getNSTransformer() throws TransformerConfigurationException, TransformerFactoryConfigurationError, IOException {
        if (nsTransformer == null)
            nsTransformer = DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("nstate"));
        return nsTransformer;
    }
}

