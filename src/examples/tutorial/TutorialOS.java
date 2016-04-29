
/**
 *    This program creates the OE of the tutorial
 *    with the SS/FS/DS events already done.
 */

import moise.oe.*;

public class TutorialOS {
    
    public static void createDS(OE currentOE) {
        try {
            
            SchemeInstance sch = currentOE.findScheme("sch_sideAttack0");
            if (sch == null) return;
            
            // add the responsible groups
            sch.addResponsibleGroup("gr_attack1");
            sch.addResponsibleGroup("gr_defense2");
            
            // commit the agents to the schs missions
            currentOE.getAgent("Roberto Carlos").commitToMission("m2", sch);
            currentOE.getAgent("Ronaldo").commitToMission("m3", sch);
            currentOE.getAgent("Lucio").commitToMission("m1", sch);

			// Satisfy the goal g1
			GoalInstance g1 = sch.getGoal("g1");
			g1.setAchieved(currentOE.getAgent("Lucio"));
			
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        try {
            
            // OE creation
            OE currentOE = OE.createOE("winGame", "jojOS.xml");
            
            TutorialSS.createSS(currentOE);
            TutorialFS.createFS(currentOE);
            TutorialOS.createDS(currentOE);
            
            new moise.tools.SimOE(currentOE);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
}
