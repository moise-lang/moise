
/**
 *    This program creates the OE of the tutorial
 *    with the SS/FS/DS events already done.
 */

import moise.oe.*;

public class TutorialDS {

    public static void main(String[] args) {
        try {

            // OE creation
            OE currentOE = OE.createOE("winGame", "jojOS.xml");

            TutorialSS.createSS(currentOE);
            TutorialFS.createFS(currentOE);

            new moise.tools.SimOE(currentOE);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
