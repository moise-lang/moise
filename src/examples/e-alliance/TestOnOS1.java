
/**
 *    This program creates an OE with the OS1 (E-Alliance) and 
 *    "runs" some social events on it by calling the OE's methods.
 */

import moise.oe.GoalInstance;
import moise.oe.GroupInstance;
import moise.oe.OE;
import moise.oe.OEAgent;
import moise.oe.SchemeInstance;
import moise.xml.DOMUtils;

class TestOnOS1 {
    
    public static void main(String[] args) {
        try {

            // OE creation
            OE currentOE = OE.createOE("print", "e-allianceOS1.xml");
            if (currentOE == null) return;
            
            // Group creation
            GroupInstance   all    = currentOE.addGroup("alliance");
            System.out.println(all+" well formed status:"+all.wellFormedStatus());
            GroupInstance   reorg  = all.addSubGroup("reorgGr");
            
            // Agent entrance
            OEAgent a    = currentOE.addAgent("A");
            OEAgent b    = currentOE.addAgent("B");
            OEAgent c    = currentOE.addAgent("C");
            OEAgent org  = currentOE.addAgent("Org");
            
            // Role Adoption
            a.adoptRole("Printshop", all);
            b.adoptRole("Printshop", all);
            c.adoptRole("Printshop", all);
            c.adoptRole("OrgParticipant", reorg);
            a.adoptRole("Selector", reorg);
            
            System.out.println(reorg+" well formed status: "+reorg.wellFormedStatus());
            org.adoptRole("OrgManager", reorg);
            org.adoptRole("Historian", reorg);
            

            // scheme starting
            SchemeInstance sch = currentOE.startScheme("test");
            sch.addResponsibleGroup(all);
            
            // setting goal's arguments
			GoalInstance gA = sch.getGoal("a");
            gA.setArgumentValue("Z", "120");

            // Mission commitment
            c.commitToMission("test.m1", sch);

			// mission m1 is satisfied my C
			sch.getGoal("d").setAchieved(c);
			sch.getGoal("test").setAchieved(c);
			
            //c.removeMission("test.m1", sch);
            
            System.out.println("final OE in XML format:\n"+DOMUtils.dom2txt(currentOE));
            
            System.out.println("OK!");
            new moise.tools.SimOE(currentOE);
            
        } catch (Exception e) {
            System.err.println("Error="+e);
            e.printStackTrace();
            System.exit(1);
        }
    }
    
}
