package ora4mas.nopl.simulator;

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

import cartago.AgentIdCredential;
import cartago.ArtifactId;
import cartago.CartagoContext;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.Op;
import cartago.WorkspaceId;
import jason.infra.centralised.RunCentralisedMAS;
import jason.runtime.RuntimeServices;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.ORA4MASConstants;
import ora4mas.nopl.SchemeBoard;

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
            CartagoService.createWorkspace(ORA4MASConstants.ORA4MAS_WSNAME);
            CartagoService.enableDebug(ORA4MASConstants.ORA4MAS_WSNAME);
            final CartagoContext ctx = CartagoService.startSession(ORA4MASConstants.ORA4MAS_WSNAME, new AgentIdCredential("simulator"));
            final WorkspaceId wid = ctx.getJoinedWspId(ORA4MASConstants.ORA4MAS_WSNAME);

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
                        ArtifactId aid = ctx.makeArtifact(wid, gIdTF.getText().trim(),  GroupBoard.class.getName(), new Object[] { fileTF.getText().trim(), gTypeTF.getText().trim() });
                        if (aid == null) {
                            System.out.println("Error creating group board! ");
                            return;
                        }
                        ctx.doAction(aid, new Op("debug", new Object[] { "inspector_gui(on)" } ));
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
                        ArtifactId aid = ctx.makeArtifact(wid, sIdTF.getText().trim(),  SchemeBoard.class.getName(),  new Object[] { fileTF.getText().trim(), sTypeTF.getText().trim()});
                        if (aid == null) {
                            System.out.println("Error creating group board! ");
                            return;
                        }
                        ctx.doAction(aid, new Op("debug", new Object[] { "inspector_gui(on)" } ));

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
                            RuntimeServices rs = RunCentralisedMAS.getRunner().getRuntimeServices();
                            rs.createAgent(name, "orgagent.asl", null, Arrays.asList(SimOrgAgent.class.getName()), null, null, null);
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

    public static void main(String[] args) throws CartagoException {
        CartagoService.startNode();
        SimulatorGUI.getInstance();
    }
}
