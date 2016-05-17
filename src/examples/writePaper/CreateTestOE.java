
/**
 *    This program creates an OE with the Write a Paper purpose and 
 *    "runs" some social events on it by calling the OE's methods.
 */

import moise.oe.*;

class CreateTestOE {

    public static void createSS(OE currentOE) {
        try {
            // Group creation
            GroupInstance   wpgroup = currentOE.addGroup("wpgroup");

            // Agent entrance
            OEAgent jaime    = currentOE.addAgent("Jaime");
            OEAgent jomi     = currentOE.addAgent("Jomi");
            OEAgent gustavo  = currentOE.addAgent("Gustavo");

            // Role Adoption
            jaime.adoptRole("editor", wpgroup);
            jomi.adoptRole("writer", wpgroup);
            gustavo.adoptRole("writer", wpgroup);
            
            // start a scheme from the specification writePaperSch
            SchemeInstance sa = currentOE.startScheme("writePaperSch");
            sa.addResponsibleGroup(wpgroup);
            
            // Commit to missions
            jaime.commitToMission("mManager", sa);
            jomi.commitToMission("mColaborator", sa);
            jomi.commitToMission("mBib", sa);
            gustavo.commitToMission("mColaborator", sa);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        try {
            // OE creation
            OE currentOE = OE.createOE("writePaperSoc", "wp-os.xml");
            if (currentOE != null) {
                createSS(currentOE);
                
                //System.out.println("final OE in XML format:\n"+OEGenerateXML.generateOE(currentOE));
                
                new moise.tools.SimOE(currentOE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
