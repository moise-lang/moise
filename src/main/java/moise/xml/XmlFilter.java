package moise.xml;

import java.io.File;

import javax.swing.JFileChooser;

public class XmlFilter extends javax.swing.filechooser.FileFilter {
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        if (f.getName().toLowerCase().endsWith("xml")) {
            return true;
        }
        return false;
    }
    public String getDescription() {
        return "XML files";
    }

    public static String askOSFile() {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        fc.setDialogTitle("Choose the OS XML file specification");
        fc.addChoosableFileFilter(new XmlFilter());
        int returnVal = fc.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

}
