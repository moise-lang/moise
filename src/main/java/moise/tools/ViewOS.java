package moise.tools;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Simple program to show an OS
 *
 * @author Jomi Fred Hubner
 */
public class ViewOS extends SimOE {
    
    public ViewOS(String OSxmlURI) throws Exception {
        super(OSxmlURI);
        frame.tabPanel.remove(0);
        //setOS(OS.loadOSFromURI(OSxmlURI));
        frame.uptadeOSComps();
        frame.showOS();
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("pass as argument the uri for the OS file (in xml)");
            System.exit(1);
        }
        try {
            ViewOS v = new  ViewOS(args[0]);
            v.frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
        } catch (Exception e) {
            printErr(e.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
}
