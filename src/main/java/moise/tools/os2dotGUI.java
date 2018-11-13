package moise.tools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import moise.os.OS;
import moise.xml.XmlFilter;



/**
 * Convert OS into DOT code (to plot a graph)
 *
 * @author Jomi
 */
public class os2dotGUI extends os2dot {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            new os2dotGUI(XmlFilter.askOSFile());
            //System.err.println("The OS file must be informed");
            //System.exit(1);
        } else {
            new os2dotGUI(args[0]);
        }
    }

    public os2dotGUI(String file) throws Exception {
        osFile = new File(file);
        new GUI(this);
    }

}

class GUI extends JFrame {

    os2dotGUI transformer;
    //JTextField jtDot    = new JTextField("/opt/local/bin/dot");
    @SuppressWarnings({ "rawtypes", "unchecked" })
    //JComboBox cbFormat = new JComboBox(new String [] {"pdf", "png", "fig", "svg"} );
    JComboBox cbFormat = new JComboBox(new String [] {"svg", "png", "ps"} );
    JCheckBox  links  = new JCheckBox("links");
    JCheckBox  missions  = new JCheckBox("missions");
    JCheckBox  cond  = new JCheckBox("norm's condition");
    JCheckBox  ss  = new JCheckBox("SS");
    JCheckBox  fs  = new JCheckBox("FS");
    JCheckBox  ns  = new JCheckBox("NS");

    JPanel mainPane;
    JTextArea  console = new JTextArea(3, 50);
    ImageIcon icon;

    GUI(os2dotGUI s) {
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
            Image i = new ImageIcon(generateImg(os, "png")).getImage();
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

    private byte[] generateImg(OS os, String format) {
        try {
            transformer.showLinks = links.isSelected();
            transformer.showMissions = missions.isSelected();
            transformer.showConditions = cond.isSelected();
            transformer.showSS    = ss.isSelected();
            transformer.showFS    = fs.isSelected();
            transformer.showNS    = ns.isSelected();

            String dotProgram = transformer.transform(os);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MutableGraph g = Parser.read(dotProgram);
            Format f = Format.SVG;
            if (format.equals("png"))
                f = Format.PNG;
            if (format.equals("ps"))
                f = Format.PS2;
            Graphviz.fromGraph(g).render(f).toOutputStream(out);
            return out.toByteArray();
        } catch (Exception e1) {
            console.setText(e1.toString());
            e1.printStackTrace();
        }
        return null;
    }

    private void initComponents() {

        JPanel top = new JPanel();
        //top.add(new JLabel("Path to dot program: "));  top.add(jtDot);
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
                File fimg = new File(path+"/"+os.getId()+"."+format);
                //FileWriter out = new FileWriter(fimg);
                try (FileOutputStream fos = new FileOutputStream(fimg)) {
                       fos.write(generateImg(os, format));
                       //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                console.setText("Image generated at "+fimg);
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
