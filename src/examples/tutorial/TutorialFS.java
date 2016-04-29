
/**
 *    This program creates an OE with the JojOS (RoboCup) and 
 *    "runs" some social events on it by calling the OE's methods.
 */

import moise.oe.*;
import moise.xml.DOMUtils;

public class TutorialFS {

    public static void createFS(OE currentOE) {
        try {

			// start a scheme from the specification sideAttack
			SchemeInstance sa = currentOE.startScheme("sideAttack");

			// set the argument for the goal g3
			GoalInstance g3 = sa.getGoal("g3");
			g3.setArgumentValue("M2Ag", "Cafu");

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
    }

    public static void main(String[] args) {
        try {

            // OE creation
            OE currentOE = OE.createOE("winGame", "jojFS.xml");
            
			TutorialSS.createSS(currentOE);
			createFS(currentOE);

            System.out.println("final OE in XML format:\n"+DOMUtils.dom2txt(currentOE));
            
            new moise.tools.SimOE(currentOE);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
}
