
/**
 *    This program creates an OE with the JojOS (RoboCup)
 */

import moise.oe.*;
import moise.xml.DOMUtils;

public class HelloMoise {
    public static void main(String[] args) {
        try {
            // OE creation using the OS from the file os1.xml
            OE currentOE = OE.createOE("winGame", "jojOS.xml");

            System.out.println("Final OE in XML format:\n"+DOMUtils.dom2txt(currentOE));
            
            new moise.tools.SimOE(currentOE);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
}
