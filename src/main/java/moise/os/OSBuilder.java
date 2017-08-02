package moise.os;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import moise.common.MoiseConsistencyException;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.fs.Plan;
import moise.os.fs.Plan.PlanOpType;
import moise.os.fs.Scheme;
import moise.os.ss.Group;
import moise.os.ss.Role;
import moise.xml.DOMUtils;

/**
 * API to build an OS and produce its XML file
 * 
 * @author jomi
 *
 */
public class OSBuilder {

    OS os = new OS();
    
    public OS getOS() {
        return os;
    }
    
    public Group addRootGroup(String id) {
    	Group g = new Group(id, os.getSS());
    	os.getSS().setRootGrSpec(g);
    	return g;
    }
    
    public Group addSubGroup(String father, String id) {
    	Group gs = os.getSS().getRootGrSpec().findSubGroup(father);
    	Group g  = new Group(id, os.getSS());
    	gs.addSubGroup(g);
    	return g;
    }
    
    public Role addRole(String grId, String roleId) throws MoiseConsistencyException {
    	Group g = os.getSS().getRootGrSpec().findSubGroup(grId);
    	if (os.getSS().getRoleDef(roleId) == null) {
    		Role r = new Role(roleId, os.getSS());
    		r.addSuperRole("soc");
    		os.getSS().addRoleDef(r);
    	}
    	return g.addRole(roleId);
    }
    
    public Scheme addScheme(String id, String rootGoal) {
        Scheme s = new Scheme(id, os.getFS());
        os.getFS().addScheme(s);
        s.setRoot(new Goal(rootGoal));
        return s;
    }
    
    public Mission addMission(String schemeId, String missionId, String goals) throws MoiseConsistencyException  {
        Scheme s = os.getFS().findScheme(schemeId);
        Mission m = new Mission(missionId, s);
        if (goals != null && goals.length() > 0) {
            for (String g: goals.split(",")) {
                g = g.trim();
                if (s.getGoal(g) == null)
                    s.addGoal(new Goal(g));
                m.addGoal(g);
            }
        }
        s.addMission(m);
        return m;
    }
    
    public Goal addMissionGoal(String missionId, String goalId) throws MoiseConsistencyException {
        return os.getFS().findMission(missionId).addGoal(goalId);       
    }
    
    public Goal addGoal(String schemeId, String goalId, String plan) throws MoiseConsistencyException {
        Scheme sch = os.getFS().findScheme(schemeId); 
        Goal g = sch.getGoal(goalId);
        if (g == null) {
            g = new Goal(goalId);
            sch.addGoal(g);
        }
        
        if (plan != null && plan.length() > 0) {
            PlanOpType op = PlanOpType.sequence;
            String deli = op.toString();
            if (plan.indexOf(",") >= 0) { // is sequence
                op = PlanOpType.sequence;
            } else if (plan.indexOf("||") >= 0) {
                op = PlanOpType.parallel;
                deli = "\\|\\|";
            } else if (plan.indexOf("|") >= 0) {
                op = PlanOpType.choice;
                deli = "\\|";
            }               
            Plan p = new Plan(op, sch, goalId);
            for (String sg: plan.split(deli)) {
                sg = sg.trim();
                if (sch.getGoal(sg) == null)
                    sch.addGoal(new Goal(sg));
                p.addSubGoal(sg);
            }
            g.setPlan(p);
        }
        return g;
    }
    
    public Goal addGoalArg(String schemeId, String goalId, String argId, Object value) {
        Scheme sch = os.getFS().findScheme(schemeId); 
        Goal g = sch.getGoal(goalId);
        g.addArgument(argId, value);
        return g;
    }
    
    public String getXMLSpec() {
        return DOMUtils.dom2txt(os);
    }
    
    public void save(String file) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(getXMLSpec());
        out.close();
    }
}
