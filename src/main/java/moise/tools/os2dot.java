package moise.tools;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import moise.os.Cardinality;
import moise.os.OS;
import moise.os.fs.FS;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.fs.Plan.PlanOpType;
import moise.os.fs.Scheme;
import moise.os.ns.NS;
import moise.os.ns.NS.OpTypes;
import moise.os.ns.Norm;
import moise.os.ss.Compatibility;
import moise.os.ss.Group;
import moise.os.ss.Link;
import moise.os.ss.Role;
import moise.os.ss.RoleRel.RoleRelScope;
import moise.os.ss.SS;
import moise.xml.XmlFilter;
import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.oe.Player;



/**
 * Convert OS into DOT code (to plot a graph)
 *
 * @author Jomi
 */
public class os2dot {

    public boolean showSS = true, showFS = true, showNS = true;
    public boolean showLinks      = true;
    public boolean showMissions   = true;
    public boolean showConditions = false;
    File    osFile;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            new os2dot(XmlFilter.askOSFile());
            //System.err.println("The OS file must be informed");
            //System.exit(1);
        } else {
            new os2dot(args[0]);
        }
    }

    public os2dot() {
    }

    public os2dot(String file) throws Exception {
        osFile = new File(file);
        new GUI(this);
    }

    public String transform(OS os)  throws Exception {
        StringWriter so = new StringWriter();

        so.append("digraph "+os.getId()+" {\n");
        if (!showSS && !showFS && showNS)
            so.append("    rankdir=LR;\n");
        else
            so.append("    rankdir=BT;\n");
        so.append("    compound=true;\n\n");

        if (showSS) so.append(transform(os.getSS(), null));
        if (showFS) so.append(transform(os.getFS()));
        if (showNS) so.append(transform(os.getNS()));

        so.append("}\n");

        //System.out.println(so);

        return so.toString();
    }

    public String transform(SS ss, ora4mas.nopl.oe.Group grInstance) {
        StringWriter so = new StringWriter();

        so.append("\n    subgraph cluster_SS { \n"); // label=\"Structure\" labelloc=t labeljust=r fontname=\"Italic\" \n");
        // Roles
        so.append(transformRolesDef(ss));

        // groups
        so.append( transform(ss.getRootGrSpec(), grInstance) );
        so.append("    }\n");

        return so.toString();
    }

    public String transformRolesDef(SS ss) {
        StringWriter so = new StringWriter();
        so.append("        // role hierarchy\n");
        for (Role r: ss.getRolesDef()) {
            so.append(transform(r));
            for (Role e: r.getSuperRoles()) {
                if (!e.getId().equals("soc"))
                    so.append("        "+r.getId()+" -> "+e.getId()+" [arrowhead=onormal,arrowsize=1.5];\n");
            }
        }
        return so.toString();
    }

    String transform(Role r) {
        String font = ",fontname=\"Helvetic\"";
        if (r.isAbstract()) {
            font=",fontname=\"Italic\""; //,style=filled,fillcolor=wheat
        }
        return "        "+r.getId()+" [shape=box,style=rounded"+font+"];\n";
    }

    public String transform(Group g, ora4mas.nopl.oe.Group gInstance) {
        StringWriter so = new StringWriter();

        so.append("\n        // group "+g.getId()+"\n");
        String id    = g.getId();
        String label = g.getId();
        if (gInstance != null) {
            id    = gInstance.getId();
            label = gInstance.getId() + ": " + label;
        }
        so.append("        "+id+" [label=\""+label+"\",shape=tab, fontname=\"Courier-Bold\",style=filled];\n");
        //so.append("     "+g.getId()+" [shape=box, fontname=\"Courier-Bold\",style=filled,fillcolor=lightyellow];\n");
        for (Role r: g.getRoles().getAll()) {
            String card = g.getRoleCardinality(r).toStringFormat2();
            so.append("        "+id+" -> "+r.getId()+"  [arrowtail=odiamond, arrowhead=none, dir=both, label=\""+card+"\",fontname=\"Times\",arrowsize=1.5];\n");
        }
        for (Group sg: g.getSubGroups()) {
            String card = g.getSubGroupCardinality(sg).toStringFormat2();
            if (gInstance == null) {
                so.append("        "+id+" -> "+sg.getId()+"  [arrowtail=odiamond, arrowhead=none, dir=both, label=\""+card+"\",fontname=\"Times\",arrowsize=1.5];\n");
                so.append(transform(sg, null));
            } else {
                for (ora4mas.nopl.oe.Group sgi: gInstance.getSubgroups()) {
                    if (sgi.getGrType().equals(sg.getId())) {
                        so.append("        "+id+" -> "+sgi.getId()+"  [arrowtail=odiamond, arrowhead=none, dir=both, label=\""+card+"\",fontname=\"Times\",arrowsize=1.5];\n");
                        so.append(transform(sg, sgi));
                    }
                }
            }
        }

        if (showLinks) {
            for (Link l: g.getLinks()) {
                String type = "normal";
                if (l.getTypeStr().equals("communication"))
                    type = "dot";
                else if (l.getTypeStr().equals("acquaintance"))
                    type = "vee";

                String dir = "";
                if (l.isBiDir())
                    dir += ",arrowtail="+type;
                String shape = "";
                if (l.getScope() == RoleRelScope.IntraGroup)
                    shape = ",style=dotted";
                so.append("        "+l.getSource()+" -> "+l.getTarget()+" [arrowhead="+type+dir+shape+"];\n");
            }
            for (Compatibility c: g.getCompatibilities()) {
                String dir = "arrowhead=diamond";
                if (c.isBiDir())
                    dir += ",arrowtail=diamond";
                String shape = "";
                if (c.getScope() == RoleRelScope.IntraGroup)
                    shape = ",style=dotted";
                so.append("        "+c.getSource()+" -> "+c.getTarget()+"  ["+dir+shape+"];\n");
            }
        }

        if (gInstance != null) {
            for (Player p: gInstance.getPlayers()) {
                so.append("        "+p.getAg()+ ";\n"); // [shape=plaintext]
                so.append("        "+p.getAg()+" -> "+p.getTarget()+" [arrowsize=0.5];\n");
                //so.append("        "+p.getAg()+" -> "+p.getTarget()+" [label=\""+id+"\",arrowsize=0.5];\n");
            }

            for (String s: gInstance.getSchemesResponsibleFor()) {
                so.append("        "+s+ "[shape=hexagon, style=filled, fontname=\"Courier\", URL=\"/scheme/"+s+"\"];\n");
                so.append("        "+id+" -> "+s+" [label=\"responsible\nfor\",labelfontsize=8,fontname=\"Italic\",arrowhead=open];\n");

            }
        }

        return so.toString();
    }

    public String transform(FS fs) {
        StringWriter so = new StringWriter();
        // schemes
        for (Scheme s: fs.getSchemes()) {
            //so.append("\n    subgraph cluster_"+s.getId()+" {label=\""+s.getId()+"\" labelloc=t labeljust=r fontname=\"Italic\" \n");
            so.append("        // goals\n");
            so.append(transform(s.getRoot(), 0, null));

            if (showMissions) {
                so.append("\n        // missions\n");
                for (Mission m: s.getMissions()) {
                    so.append(transform(m,s));
                    for (Goal g: m.getGoals()) {
                        so.append("        "+m.getId()+" -> "+g.getId()+" [arrowsize=0.5];\n");
                    }
                }
            }
            //so.append("    }\n");
        }

        return so.toString();
    }

    String transform(Mission m) {
        return "        "+m.getId()+" [fontname=\"Helvetic\", shape=diamond, style=rounded];\n";
    }

    public static String transform(Mission m, Scheme spec) {
        String card = "";
        if (! card.equals(Cardinality.defaultValue)) {
            card = "\n("+spec.getMissionCardinality(m).toStringFormat2()+")";
        }
        return "        "+m.getId()+" [label=\""+m.getId()+card+"\", fontname=\"Helvetic\", shape=plaintext,fontsize=10];\n";
    }


    public static String transform(Goal g, int pos, SchemeBoard sch) {
        StringBuilder so = new StringBuilder();
        String color = "black";
        if (sch != null) {
            if (sch.getSchState().isSatisfied(g)) {
                color = "blue";
            } else {
                Term tSch = ASSyntax.createString(sch.getSchState().getId());
                Atom aGoal  = new Atom(g.getId());
                if (sch.getNormativeEngine().holds(ASSyntax.createLiteral("well_formed", tSch)) && sch.getNormativeEngine().holds(ASSyntax.createLiteral("enabled", tSch, aGoal))) {
                    color = "green4";
                }
            }
        }
        String label = g.getId();
        if (pos > 0)
            label = pos+":"+label;
        String shape = "plaintext";
        String peri = "0";
        if (g.hasPlan()) {
            if (g.getPlan().getOp() == PlanOpType.choice) {
                shape = "underline";
                peri  = "1";
            } else if (g.getPlan().getOp() == PlanOpType.parallel) {
                shape = "underline";
                peri = "2";
            }
        }
        so.append("        "+g.getId()+" [label=\""+label+"\", shape="+shape+",peripheries="+peri+",fontname=\"Helvetic\",fontcolor="+color+"]; \n");
        if (g.hasPlan()) {
            String type=",arrowhead=none";

            Goal previous = null;
            int ppos = 0;
            if (g.getPlan().getOp() == PlanOpType.sequence)
                ppos = 1;
            for (Goal sg: g.getPlan().getSubGoals()) {
                so.append(transform(sg,ppos,sch));
                so.append("        "+sg.getId()+" -> "+g.getId()+" [samehead=true"+type+"];\n");
                if (ppos > 0) {
                    ppos++;
                    if (previous != null)
                        so.append("        "+previous.getId()+" -> "+sg.getId()+" [style=dotted, constraint=false, arrowhead=empty,arrowsize=0.5,color=lightgrey];\n");
                    previous = sg;
                }
            }
        }

        return so.toString();
    }

    /*
    String transform(Goal g) {
        StringBuilder so = new StringBuilder();
        so.append("        "+g.getId()+" [shape=plaintext,fontname=\"Helvetic\"]; \n");
        if (g.hasPlan()) {
            String type=",arrowhead=none";
            if (g.getPlan().getOp() == PlanOpType.parallel)
                type=",arrowhead=tee";
            else if (g.getPlan().getOp() == PlanOpType.choice)
                type=",arrowhead=vee";

            for (Goal sg: g.getPlan().getSubGoals()) {
                so.append("        "+sg.getId()+" -> "+g.getId()+" [samehead=true"+type+"];\n");
                so.append(transform(sg));
            }
        }

        return so.toString();
    }
    */

    public String transform(NS ns) {
        StringWriter so = new StringWriter();
        so.append("\n\n    // NS\n");
        Set<String> done = new HashSet<String>();
        for (Norm n: ns.getNorms()) {
            String e = n.getRole().toString()+n.getMission();
            if (!done.contains(e) || showConditions) {
                done.add(e);

                String s = "bold";
                if (n.getType() == OpTypes.permission)
                    s = "filled";
                String cond = "";
                if (showConditions) {
                    cond = "plays role";
                    if (!n.getCondition().equals("true"))
                        cond = n.getCondition();
                    if (n.getTimeConstraint() != null)
                        cond += "@"+n.getTimeConstraint();
                }

                so.append( transform(n.getRole()));
                so.append( transform(n.getMission()));

                so.append("        "+n.getRole()+" -> "+n.getMission().getId()+" [arrowhead=inv,style="+s+",label=\""+cond+"\"];\n"); // decorate=true,
            }
        }

        return so.toString();
    }

}

class GUI extends JFrame {

    os2dot transformer;
    JTextField jtDot    = new JTextField("/opt/local/bin/dot");
    @SuppressWarnings({ "rawtypes", "unchecked" })
    JComboBox cbFormat = new JComboBox(new String [] {"pdf", "png", "fig", "svg"} );
    JCheckBox  links  = new JCheckBox("links");
    JCheckBox  missions  = new JCheckBox("missions");
    JCheckBox  cond  = new JCheckBox("norm's condition");
    JCheckBox  ss  = new JCheckBox("SS");
    JCheckBox  fs  = new JCheckBox("FS");
    JCheckBox  ns  = new JCheckBox("NS");

    JPanel mainPane;
    JTextArea  console = new JTextArea(3, 50);
    ImageIcon icon;

    GUI(os2dot s) {
        super("Translation of MOISE to Graphic (via dot) for "+s.osFile);
        transformer = s;
        setSize(1200, 600);
        initComponents();
        updateGraphic();
        //pack();
        setVisible(true);
    }

    void updateGraphic() {
        try {
            OS os = OS.loadOSFromURI(transformer.osFile.getAbsolutePath());
            File fin = File.createTempFile("moise-ss", ".dot");
            File fimg = File.createTempFile("moise-ss", ".png");
            generateImg(os, fin, fimg, "png");
            Image i = new ImageIcon(fimg.getAbsolutePath()).getImage();
            int w = this.getWidth()-20;
            if (i.getWidth(null) > w) {
                double red = (double)w / i.getWidth(null);
                i = i.getScaledInstance(w, (int)(i.getHeight(null)*red), Image.SCALE_SMOOTH);
            }
            icon.setImage(i);
            mainPane.updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateImg(OS os, File fin, File fimg, String format) {
        try {
            transformer.showLinks = links.isSelected();
            transformer.showMissions = missions.isSelected();
            transformer.showConditions = cond.isSelected();
            transformer.showSS    = ss.isSelected();
            transformer.showFS    = fs.isSelected();
            transformer.showNS    = ns.isSelected();

            String dotProgram = transformer.transform(os);
            //System.out.println(dotProgram);

            FileWriter out = new FileWriter(fin);
            out.append(dotProgram);
            out.close();
            Process p = Runtime.getRuntime().exec(jtDot.getText().trim()+" -T"+format+" "+fin.getAbsolutePath()+" -o "+fimg.getAbsolutePath());
            p.waitFor();

        } catch (Exception e1) {
            console.setText(e1.toString());
            e1.printStackTrace();
        }
    }

    private void initComponents() {

        JPanel top = new JPanel();
        top.add(new JLabel("Path to dot program: "));  top.add(jtDot);
        top.add(new JLabel("Output format: "));  top.add(cbFormat);
        top.add(ss); ss.setSelected(true);
        top.add(links); links.setSelected(true);
        top.add(fs); fs.setSelected(true);
        top.add(missions); missions.setSelected(true);
        top.add(ns); ns.setSelected(true);
        top.add(cond); cond.setSelected(false);

        links.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { updateGraphic();  }  });
        missions.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { updateGraphic();  }  });
        cond.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { updateGraphic();  }  });
        ss.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { updateGraphic();  }  });
        fs.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { updateGraphic();  }  });
        ns.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { updateGraphic();  }  });

        JButton btStore = new JButton("Store");
        btStore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OS os = OS.loadOSFromURI(transformer.osFile.getAbsolutePath());
                String path = transformer.osFile.getAbsoluteFile().getParent();
                String format = cbFormat.getSelectedItem().toString();
                File fin  = new File(path+"/"+os.getId()+".dot");
                File fimg = new File(path+"/"+os.getId()+"."+format);
                generateImg(os, fin, fimg, format);
                console.setText("dot file stored at "+fin+"\n");
                console.append("Image generated at "+fimg);
            }
        });
        top.add(btStore);

        icon = new ImageIcon();
        JPanel imgP = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imgP.add(new JLabel(icon));


        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(BorderLayout.CENTER, new JScrollPane(console));

        mainPane = new JPanel(new BorderLayout());
        mainPane.add(BorderLayout.NORTH, top);
        mainPane.add(BorderLayout.CENTER, imgP);
        mainPane.add(BorderLayout.SOUTH, bottom);
        getContentPane().add(mainPane);
    }
}
