package ora4mas.nopl.simulator;

import jason.infra.centralised.RunCentralisedMAS;
import jason.runtime.RuntimeServicesInfraTier;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.SchemeBoard;
import cartago.util.agent.CartagoBasicContext;

public class SimulatorGUI {
    List<AgentGUI> ags = new ArrayList<AgentGUI>();
    
    // singleton pattern
    private static SimulatorGUI simulatorGUISingleton = null;
    public static SimulatorGUI getInstance() {
        if (simulatorGUISingleton == null)
            simulatorGUISingleton = new SimulatorGUI();
        return simulatorGUISingleton;
    }

    JFrame frame;

    private SimulatorGUI() {
        try {
            final CartagoBasicContext ctxt = new CartagoBasicContext("simulator");
            
            JPanel posfile = new JPanel(new FlowLayout(FlowLayout.LEFT));
            posfile.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Specification file", TitledBorder.LEFT, TitledBorder.TOP));
            final JTextField fileTF = new JTextField(30); fileTF.setText("org-spec.xml");
            posfile.add(fileTF);
            
            // Group creation panel
            JPanel grp = new JPanel(new FlowLayout(FlowLayout.LEFT));
            grp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Group Board Creation", TitledBorder.LEFT, TitledBorder.TOP));
            final JTextField gIdTF = new JTextField(7); gIdTF.setText("g1");
            grp.add(new JLabel("group instance id: "));
            grp.add(gIdTF);
            final JTextField gTypeTF = new JTextField(10); gTypeTF.setText("wpgroup");
            grp.add(new JLabel("group type id: "));
            grp.add(gTypeTF);
            JButton crGrBT = new JButton("create");
            grp.add(crGrBT);
            crGrBT.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        ctxt.makeArtifact(gIdTF.getText().trim(),  GroupBoard.class.getName(),  new Object[] { fileTF.getText().trim(), gTypeTF.getText().trim(), false, true });
                        for (AgentGUI a: ags) {
                            a.initArtsCBmodel();
                            a.initOpsCBmodel();
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
    
            
            // Scheme creation panel
            JPanel schp = new JPanel(new FlowLayout(FlowLayout.LEFT));
            schp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Scheme Board Creation", TitledBorder.LEFT, TitledBorder.TOP));
            final JTextField sIdTF = new JTextField(7); sIdTF.setText("sch1");
            schp.add(new JLabel("scheme id: "));
            schp.add(sIdTF);
            final JTextField sTypeTF = new JTextField(10); sTypeTF.setText("writePaperSch");
            schp.add(new JLabel("scheme type: "));
            schp.add(sTypeTF);
            JButton crSchBT = new JButton("create");
            schp.add(crSchBT);
            crSchBT.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        ctxt.makeArtifact(sIdTF.getText().trim(),  SchemeBoard.class.getName(),  new Object[] { fileTF.getText().trim(), sTypeTF.getText().trim(), false, true });
                        for (AgentGUI a: ags) {
                            a.initArtsCBmodel();
                            a.initOpsCBmodel();
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
    
            // Agent creation panel
            JPanel agp = new JPanel(new FlowLayout(FlowLayout.LEFT));
            agp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Agent Creation", TitledBorder.LEFT, TitledBorder.TOP));
            final JTextField agNameTF = new JTextField(10); 
            agp.add(new JLabel("agent name: "));
            agp.add(agNameTF);
            JButton crAgBT = new JButton("create");
            agp.add(crAgBT);
            crAgBT.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String name = agNameTF.getText().trim();
                    if (name.length() > 0) {
                        try {
                            RuntimeServicesInfraTier rs = RunCentralisedMAS.getRunner().getRuntimeServices();
                            rs.createAgent(name, "orgagent.asl", null, Arrays.asList(SimOrgAgent.class.getName()), null, null);
                            rs.startAgent(name);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
    
            frame = new JFrame("Moise Simulador - ORA4MAS");
            frame.getContentPane().setLayout(new GridLayout(0,1));
            frame.add(posfile);
            frame.add(grp);
            frame.add(schp);
            frame.add(agp);
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e ) {
            System.out.println("Error creating GUI "+e);
            e.printStackTrace();
        }
    }

    public void addAg(AgentGUI a) {
        ags.add(a);
    }
}
