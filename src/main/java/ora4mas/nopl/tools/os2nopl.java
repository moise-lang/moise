package ora4mas.nopl.tools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import moise.common.MoiseElement;
import moise.os.Cardinality;
import moise.os.CardinalitySet;
import moise.os.OS;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.fs.Plan.PlanOpType;
import moise.os.fs.Scheme;
import moise.os.ns.NS;
import moise.os.ns.NS.OpTypes;
import moise.os.ns.Norm;
import moise.os.ss.Compatibility;
import moise.os.ss.Group;
import moise.os.ss.Role;
import moise.os.ss.RoleRel.RoleRelScope;
import moise.os.ss.SS;

/** translate an OS to a NP */
public class os2nopl {
    
    //public static final String NOP_Prefix       = "nop_";
    
    // constants for properties
    public static final String PROP_RoleInGroup           = "role_in_group";
    public static final String PROP_RoleCardinality       = "role_cardinality";
    public static final String PROP_RoleCompatibility     = "role_compatibility";
    public static final String PROP_WellFormedResponsible = "well_formed_responsible";

    public static final String PROP_SubgroupInGroup       = "subgroup_in_group";
    public static final String PROP_SubgroupCardinality   = "subgroup_cardinality";

    public static final String PROP_MissionPermission     = "mission_permission";
    public static final String PROP_LeaveMission          = "mission_left";
    public static final String PROP_MissionCardinality    = "mission_cardinality";
    public static final String PROP_AchNotEnabledGoal     = "ach_not_enabled_goal";
    public static final String PROP_AchNotCommGoal        = "ach_not_committed_goal";

    public static final String PROP_NotCompGoal           = "goal_non_compliance";

    // properties for groups
    public static final String[] NOP_GR_PROPS  = new String[] { PROP_RoleInGroup, PROP_RoleCardinality, PROP_RoleCompatibility, PROP_WellFormedResponsible, PROP_SubgroupInGroup, PROP_SubgroupCardinality};
    // properties for schemes
    public static final String[] NOP_SCH_PROPS = new String[] { PROP_NotCompGoal, PROP_MissionPermission, PROP_LeaveMission, PROP_MissionCardinality, PROP_AchNotEnabledGoal, PROP_AchNotCommGoal };
    
    private static final String NGOA = "ngoal"; // id of the goal obligations
    
    // condition for each property
    private static final Map<String, String> condCode = new HashMap<String, String>();
    static {
        condCode.put(PROP_RoleInGroup,           "play(Agt,R,Gr) & group_id(Gr) & not role_cardinality(R,_,_)");
        condCode.put(PROP_RoleCardinality,       "group_id(Gr) & role_cardinality(R,_,RMax) & rplayers(R,Gr,RP) & RP > RMax");
        //condCode.put(PROP_RoleCompatibility,     "play(Agt,R1,Gr) & play(Agt,R2,Gr) & R1 < R2 & not compatible(R1,R2,gr_inst)"); // TODO: revise and use fcompatible
        condCode.put(PROP_RoleCompatibility,     "play(Agt,R1,Gr) & play(Agt,R2,Gr) & group_id(Gr) & R1 < R2 & not fcompatible(R1,R2,gr_inst)"); // note that is have to be play and not fplay

        condCode.put(PROP_SubgroupInGroup,       "group_id(Gr) & subgroup(G,GT,Gr) & not subgroup_cardinality(GT,_,_)");
        condCode.put(PROP_SubgroupCardinality,   "group_id(Gr) & subgroup_cardinality(SG,_,SGMax) & .count(subgroup(_,SG,Gr),SGP) & SGP > SGMax");
        
        condCode.put(PROP_WellFormedResponsible, "responsible(Gr,S) & not monitor_scheme(S) & not well_formed(Gr)");
        condCode.put(PROP_MissionPermission,     "committed(Agt,M,S) & not (mission_role(M,R) & responsible(Gr,S) & fplay(Agt,R,Gr))");
        condCode.put(PROP_LeaveMission,          "leaved_mission(Agt,M,S) & not mission_accomplished(S,M)");
        condCode.put(PROP_MissionCardinality,    "scheme_id(S) & mission_cardinality(M,_,MMax) & mplayers(M,S,MP) & MP > MMax");
        condCode.put(PROP_AchNotEnabledGoal,     "achieved(S,G,Agt) & mission_goal(M,G) & not mission_accomplished(S,M) & not enabled(S,G)");
        //condCode.put(PROP_AchNotCommGoal,        "achieved(S,G,Agt) & mission_goal(M,G) & not mission_accomplished(S,M) & not committed(Agt,M,S)");
        condCode.put(PROP_AchNotCommGoal,        "achieved(S,G,Agt) & .findall(M, mission_goal(M,G) & (committed(Agt,M,S) | mission_accomplished(S,M)), [])");
        condCode.put(PROP_NotCompGoal,           "obligation(Agt,"+NGOA+"(S,M,G),Obj,TTF) & not Obj & `now` > TTF");
    }
    // arguments that 'explains' the property
    private static final Map<String, String> argsCode = new HashMap<String, String>();
    static {
        argsCode.put(PROP_RoleInGroup,           "Agt,R,Gr");
        argsCode.put(PROP_RoleCardinality,       "R,Gr,RP,RMax");
        argsCode.put(PROP_RoleCompatibility,     "R1,R2,Gr");

        argsCode.put(PROP_SubgroupInGroup,       "G,GT,Gr");
        argsCode.put(PROP_SubgroupCardinality,   "SG,Gr,SGP,SGMax");

        argsCode.put(PROP_WellFormedResponsible, "Gr");
        argsCode.put(PROP_MissionPermission,     "Agt,M,S");
        argsCode.put(PROP_LeaveMission,          "Agt,M,S");
        argsCode.put(PROP_MissionCardinality,    "M,S,MP,MMax");
        argsCode.put(PROP_AchNotEnabledGoal,     "S,G,Agt");
        argsCode.put(PROP_AchNotCommGoal,        "S,G,Agt");
        argsCode.put(PROP_NotCompGoal   ,        "obligation(Agt,"+NGOA+"(S,M,G),Obj,TTF)");
    }
    
    /** transforms an OS into NPL code */
    public static String transform(OS os) {
        StringBuilder np = new StringBuilder();
        
        np.append(header(os));

        // main scope
        np.append("scope organisation("+os.getId()+") {\n\n");
        
        np.append( roleHierarchy( os.getSS()) + "\n\n");
        
        // groups
        np.append( transform( os.getSS().getRootGrSpec() )+ "\n");

        // schemes
        for (Scheme sch: os.getFS().getSchemes())
            np.append( transform( sch) + "\n");
        
        np.append("} // end of organisation "+os.getId()+"\n");
        
        return np.toString();
    }
    
    /** transforms a Group Spec into NPL code */
    public static String transform(Group gr) {
        StringBuilder np = new StringBuilder();
        np.append("scope group("+gr.getId()+") {\n\n");
        
        np.append("   // ** Facts from OS\n");
        
        //np.append( roleHirarchy(gr.getSS()) +"\n");
        
        CardinalitySet<Role> roles = gr.getRoles();
        for (Role r: roles) {
            Cardinality c = roles.getCardinality(r);
            np.append("   role_cardinality("+r.getId()+","+c.getMin()+","+c.getMax()+").\n");
        }
        for (Group sg: gr.getSubGroups()) {
            Cardinality c = gr.getSubGroupCardinality(sg);
            np.append("   subgroup_cardinality("+sg.getId()+","+c.getMin()+","+c.getMax()+").\n");
        }
        
        np.append("\n");
        for (Compatibility c: gr.getUpCompatibilities()) {
            String scope = "gr_inst";
            if (c.getScope() == RoleRelScope.InterGroup)
                scope = "org";
            np.append("   compatible("+c.getSource()+","+c.getTarget()+","+scope+").\n"); 
            if (c.isBiDir())
                np.append("   compatible("+c.getTarget()+","+c.getSource()+","+scope+").\n");
        }
        np.append("\n   // ** Rules\n");
        np.append("   rplayers(R,G,V)    :- .count(play(_,R,G),V).\n");

        np.append("   well_formed(G)");
        String sep = " :-\n";
        for (Role r: roles) {
            Cardinality c = roles.getCardinality(r);
            String var = "V"+r.getId();
            np.append(sep+"      rplayers("+r.getId()+",G,"+var+") & "+var+" >= "+c.getMin()+" & "+var+" <= "+c.getMax());
            sep = " &\n";
        }
        for (Group sg: gr.getSubGroups()) {
            Cardinality c = gr.getSubGroupCardinality(sg);
            String var = "S"+sg.getId();
            np.append(sep+"      .count(subgroup(_,"+sg.getId()+",G),"+var+") & "+var+" >= "+c.getMin()+" & "+var+" <= "+c.getMax()); //+" & subgroup_well_formed("+sg.getId()+")");            
            sep = " &\n";
        }
        np.append(sep+"      .findall(GInst, subgroup(GInst,_,G), ListSubgroups) & all_subgroups_well_formed(ListSubgroups).\n");            

        
        np.append("   all_subgroups_well_formed([]).\n");
        np.append("   all_subgroups_well_formed([H|T]) :- subgroup_well_formed(H) & all_subgroups_well_formed(T).\n");

        np.append("\n   // ** Properties check \n");
        
        boolean hasMonSch = gr.getMonitoringSch() != null;
        generateProperties(NOP_GR_PROPS, gr.getSS(), np, gr.getSS().getOS().getNS(), "group_id(Gr)", hasMonSch);

        np.append("} // end of group "+gr.getId()+"\n");

        for (Group sgr: gr.getSubGroups()) {
            np.append("\n\n// ** Group "+sgr.getId()+", subgroup of "+gr.getId()+"\n");
            np.append( transform(sgr) + "\n");
        }
        
          
        /*
        if (hasMonSch) {
            Scheme monSch = gr.getSS().getOS().getFS().findScheme( gr.getMonitoringSch() );
            if (monSch != null) {
                np.append("\n// monitoring scheme for the group "+gr.getId()+"\n");
                np.append( transform(monSch) );
            }
        }
        */
        
        return np.toString();
    }

    public static String transform(Role r) {
        StringBuilder np = new StringBuilder();
        for (Role sr: r.getSubRoles()) {
            np.append("   subrole("+sr.getId()+","+r.getId()+").\n");
            np.append( transform(sr) );
        }
        return np.toString();        
    }
    
    private static String roleHierarchy(SS ss) {
        StringBuilder np = new StringBuilder("\n   // Role hierarchy\n");
        np.append(  transform( ss.getRoleDef("soc")) + "\n");
        np.append(  "   // f* rules implement the role hierarchy transitivity\n");
        np.append(  "   // t* rules implement the transitivity of some relations\n\n");
        np.append(  "   // fplay(A,R,G) is true if A play R in G or if A play a subrole of R in G\n");
        np.append(  "   fplay(A,R,G) :- play(A,R,G).\n");
        np.append(  "   fplay(A,R,G) :- subrole(R1,R) & fplay(A,R1,G).\n\n");
        np.append(  "   // fcompatible(R1,R2,S) is true if R1 or its sub-roles are compatible with R2 in scope S\n");
        /*
        //np.append(  "   fcompatible(R1,R2,S) :- tsubrole(R1,R2).\n");  // produces an error all is compatiple with all due to all being compatible with soc
        np.append(  "   fcompatible(R1,R2,S) :- tcompatible(R1,R2,S).\n");
        np.append(  "   fcompatible(R1,R2,S) :- subrole(R1,R2).\n");
        //np.append(  "   fcompatible(R1,R2,S) :- subrole(R1,R) & fcompatible(R,R2,S).\n"); // Didn' work see house example
        np.append(  "   fcompatible(R1,R2,S) :- subrole(R2,R) & fcompatible(R1,R,S).\n\n");
        np.append(  "   tcompatible(R1,R2,S) :- compatible(R1,R2,S).\n");
        np.append(  "   tcompatible(R1,R2,S) :- compatible(R1,R3,S) & tcompatible(R3,R2,S).\n");
        */
        np.append(  "   fcompatible(R1,R2,S) :- tsubrole(R1,R2).\n");
        np.append(  "   fcompatible(R1,R2,S) :- tsubrole(R1,R1a) & tsubrole(R2,R2a) & compatible(R1a,R2a,S).\n");
        np.append(  "   fcompatible(R1,R2,S) :- tcompatible(R1,R2,S,[R1,R2]).\n");

        //np.append(  "   tcompatible(R1,R2,S) :- compatible(R1,R2,S).\n");
        // member is there to avoid infinity loops
        np.append(  "   tcompatible(R1,R2,S,Path) :- compatible(R1,R3,S) & not .member(R3,Path) & tcompatible(R3,R2,S,[R3|Path]).\n");

        np.append(  "   tsubrole(R,R).\n");
        np.append(  "   tsubrole(R1,R2)    :- subrole(R1,R2).\n");
        np.append(  "   tsubrole(R1,R2)    :- subrole(R1,R3) & tsubrole(R3,R2).\n");
        return np.toString();        
    }
    
    /** transforms a Scheme Spec into NPL code */
    public static String transform(Scheme sch) {
        StringBuilder np = new StringBuilder();
        np.append("scope scheme("+sch.getId()+") {\n\n");
        np.append("   // ** Facts from OS\n\n");
        np.append("   // mission_cardinality(mission id, min, max)\n");
        for (Mission m: sch.getMissions()) {
            Cardinality c = sch.getMissionCardinality(m);
            np.append("   mission_cardinality("+m.getId()+","+c.getMin()+","+c.getMax()+").\n");
        }
        
        np.append("\n   // mission_role(mission id, role id)\n");
        Set<String> generated = new HashSet<String>();
        for (Norm dr: sch.getFS().getOS().getNS().getNorms()) {
            if (sch.getMissions().contains(dr.getMission())) {
                String rel = "   mission_role("+dr.getMission().getId()+","+dr.getRole().getId()+").\n";
                if (!generated.contains(rel)) {
                    generated.add(rel);
                    np.append(rel);            
                }
            }
        }
        //np.append("   fmission_role(M,R) :- mission_role(M,R).\n");
        //np.append("   fmission_role(M,R) :- subrole(R,R1) & fmission_role(M,R1).\n\n");
        np.append("\n   // mission_goal(mission id, goal id)\n");
        for (Mission m: sch.getMissions()) {
            for (Goal g: m.getGoals()) {
                np.append("   mission_goal("+m.getId()+","+g.getId()+").\n");                
            }
        }

        np.append("\n   // goal(missions, goal id, dependence (on goal statisfaction), type, #ags to satisfy, ttf)\n");

        StringBuilder superGoal = new StringBuilder();
        for (Goal g: sch.getGoals()) {
            try {
                superGoal.append("   super_goal("+g.getInPlan().getTargetGoal().getId()+", "+g.getId()+").\n");
            } catch (Exception e) {}
            
            StringBuilder smis = new StringBuilder("[");
            String com = "";
            for (String m: sch.getGoalMissionsId(g)) { 
                smis.append(com+m);
                com = ",";
            }
            smis.append("]");
            
            //String smis  = "nomission";
            //if (mis != null) smis = mis.getId(); 
            String ttf = g.getTTF();
            if (ttf.length() == 0) ttf = "1 year";
            List<Goal> precond = g.getPreConditionGoals();
            /*
            permitted.append("   permitted(S,"+g.getId()+") :- ");
            String sep = "";
            for (Goal dg: precond) {
                permitted.append(sep+"satisfied(S,"+dg.getId()+")");
                sep = " & ";
            }
            if (precond.isEmpty()) 
                permitted.append("true");
            permitted.append(".\n");                                
            */
            String prec = precond.toString();
            if (g.hasPlan() && g.getPlan().getOp() == PlanOpType.choice)
                prec = "dep(or,"+prec+")";
            else
                prec = "dep(and,"+prec+")";
            String nag  = g.getMinAgToSatisfy() == -1 ? "all" : ""+g.getMinAgToSatisfy();
            np.append("   goal("+smis+","+g.getId()+","+prec+","+g.getType()+","+nag+",`"+ttf+"`).\n");
            
        }
        np.append(superGoal.toString());
        
        np.append("\n   // ** Rules\n");
        
        np.append("   mplayers(M,S,V) :- .count(committed(_,M,S),V).\n");
        np.append("   well_formed(S)");
        String sep = " :- \n";
        for (Mission m: sch.getMissions()) {
            Cardinality c = sch.getMissionCardinality(m);
            String var = "V"+m.getId();
            np.append(sep+"      (mission_accomplished(S,"+m.getId()+") | mplayers("+m.getId()+",S,"+var+") & "+var+" >= "+c.getMin()+" & "+var+" <= "+c.getMax()+")");
            sep = " &\n";
        }
        np.append(".\n");

        np.append("   is_finished(S) :- satisfied(S,"+sch.getRoot().getId()+").\n");
        np.append("   mission_accomplished(S,M) :- .findall(Goal, mission_goal(M,Goal), MissionGoals) & all_satisfied(S,MissionGoals).\n");
        
        //np.append( roleHirarchy(sch.getFS().getOS().getSS()) );
        /*
        // satisfied is a dynamic facts (it cannot be computed form the state: once satisfied, satisfied forever, independently of goal states -- see leaveMission issue)
        np.append("\n   // conditions for satisfiability\n");
        np.append("   satisfied(S,G) :-     // no agents have to achieve -- automatically satisfied by its pre-conditions\n");
        np.append("      goal(_,G,PCG,_,0,_) & all_satisfied(S,PCG).\n");  
        np.append("   satisfied(S,G) :-     // all committed agents have to achieve\n");
        //np.append("      goal(M,G,_,_,all,_) & mission_cardinality(M,Min,_) & .count(achieved(S,G,A),AA) & AA >= Min.\n");  
        np.append("      goal(M,G,_,_,all,_) & well_formed(S) & mplayers(M,S,V)  & .count(achieved(S,G,A),AA) & AA >= V.\n");  
        np.append("   satisfied(S,G) :-     // some agents have to achieve\n");
        np.append("      goal(_,G,_,_,X,_)  & X > 0 & .count( achieved(S,G,A), X ).\n\n");  
        */
        
        np.append("   all_satisfied(_,[]).\n");
        np.append("   all_satisfied(S,[G|T]) :- satisfied(S,G) & all_satisfied(S,T).\n");
        np.append("   any_satisfied(S,[G|_]) :- satisfied(S,G).\n");
        np.append("   any_satisfied(S,[_|T]) :- any_satisfied(S,T).\n\n");
        
        np.append("   // enabled goals (i.e. dependence between goals)\n");
        np.append("   enabled(S,G) :- goal(_, G,  dep(or,PCG), _, NP, _) & NP \\== 0 & any_satisfied(S,PCG).\n");
        np.append("   enabled(S,G) :- goal(_, G, dep(and,PCG), _, NP, _) & NP \\== 0 & all_satisfied(S,PCG).\n");

        //np.append("   super_satisfied(S,G) :- not super_goal(_,G).\n");
        np.append("   super_satisfied(S,G) :- super_goal(SG,G) & satisfied(S,SG).\n");
        
        if (!sch.isMonitorSch()) {
            np.append("\n   // ** Norms\n\n");
            //np.append("   // --- missions ---\n");
            for (Norm nrm: sch.getFS().getOS().getNS().getNorms()) {            
                if (sch.getMissions().contains(nrm.getMission())) {
                    generateNormEntry(
                            nrm, sch.getMissionCardinality(nrm.getMission()), 
                            np
                    );
                }
            }
        }

        np.append("\n   // --- Goals ---\n");
        
        np.append("   // agents are obliged to fulfill their enabled goals\n");
        np.append("   norm "+NGOA+": \n");
        np.append("           committed(A,M,S) & mission_goal(M,G) & goal(_,G,_,achievement,_,D) &\n");
        np.append("           well_formed(S) & not satisfied(S,G) & enabled(S,G) & \n");
        np.append("           not super_satisfied(S,G)\n");
        np.append("        -> obligation(A,"+NGOA+"(S,M,G),achieved(S,G,A),`now` + D).\n"); 
        // TODO: maintenance goals
        //np.append("   // maintenance goals\n");
        
        np.append("\n   // --- Properties check ---\n");
        boolean hasMonSch = sch.getMonitoringSch() != null;

        generateProperties(NOP_SCH_PROPS, sch, np, sch.getFS().getOS().getNS(), "scheme_id(S) & responsible(Gr,S)", hasMonSch);

        /*
        if (hasMonSch) {
            Scheme monSch = sch.getFS().findScheme( sch.getMonitoringSch() );
            if (monSch != null) {
                np.append("\n// monitoring scheme for the scheme "+sch.getId()+"\n");
                np.append( transform(monSch) );
            }
        }
        */       

        np.append("} // end of scheme "+sch.getId()+"\n");
        return np.toString();
    }
    
    private static void generateProperties(String[] props, MoiseElement ele, StringBuilder np, NS ns, String getReferee, boolean hasMonitoringSch) {
        for (String prop: props) {            
            // check if some norm exist for the propriety
            boolean hasNorm = false;
            /*if (hasMonitoringSch) { //!ele.getId().startsWith("monitoring")) {
                for (Norm dr: ns.getNorms()) {
                    String condition = dr.getCondition();
                    if (dr.getType() == OpTypes.obligation && condition.substring(1).equals(prop)) {
                        hasNorm = true;
                        generateNormEntry(dr, null, getReferee+" & monitor_scheme(MonSch)", "MonSch", np, null);
                        break; // skip this property
                    }
                }
                if (hasNorm)
                    continue;
            }*/
            np.append("   norm "+prop+": \n"); 
            String sep = "";
            for (String t: condCode.get(prop).split("&")) {
                np.append(sep+"           "+t.trim());
                sep = " &\n";
            }
            
            // the case of fail
            String args    = argsCode.get(prop);
            //String propValue = ele.getStrProperty(NOP_Prefix+prop, "fail");
            //if (propValue.equals("fail"))
            np.append("\n        -> fail("+prop+"("+args+")).\n");            
        }
    }
    
    private static void generateNormEntry(Norm nrm, Cardinality card, StringBuilder np) {
        String id = nrm.getId();
        String m  = nrm.getMission().getId();
        String tc = nrm.getTimeConstraint() == null ? "+`1 year`" : "+`"+nrm.getTimeConstraint().getTC()+"`";
        String comment = "";
        String args    = "";
        String condition = nrm.getCondition();                
        // macro condition
        if (condition.startsWith("#")) {
            condition = condition.substring(1);
            comment   = " // "+condition;
            args      = "("+argsCode.get(condition)+")";
            condition = condCode.get(condition) + " &\n           ";
        } else if (condition.equals("true")) {
            condition = "";
        } else {
            condition = condition + " &\n           ";
        }
        
        String extraCond = "not mission_accomplished(S,"+m+") // if all mission's goals are satisfied, the agent is not obliged to commit to the mission";
        condition        = condition + "scheme_id(S) & responsible(Gr,S)";
        String mplayers  = "mplayers("+m+",S,V) & mission_cardinality("+m+",MMinCard,MMaxCard) & ";
        String fplay     = "fplay(A,"+nrm.getRole().getId()+",Gr)";
        String cons      = args+",committed(A,"+m+",S), `now`"+tc+").\n";
        if (card.getMin() > 0 && nrm.getType() == OpTypes.obligation) { // the obligation
            np.append("   norm "+id+": "+comment+"\n");
            np.append("           "+condition);
            np.append(" &\n           "+mplayers+"V < MMinCard");
            np.append(" &\n           "+fplay);
            np.append(" &\n           "+extraCond);
            np.append("\n        -> obligation("+"A,"+id+cons);
            id = "p"+id;
        }
        if (card.getMin() < card.getMax() || nrm.getType() == OpTypes.permission) { // the permission
            np.append("   norm "+id+": "+comment+"\n");
            np.append("           "+condition);
            np.append(" &\n           "+mplayers+"V < MMaxCard");
            if (nrm.getType() == OpTypes.obligation) // an obl was created
                np.append(" & V >= MMinCard");
            np.append(" &\n           fplay(A,"+nrm.getRole().getId()+",Gr)");
            np.append(" &\n           "+extraCond);
            np.append("\n        -> permission("+"A,"+id+cons);
        }
    }
    
    public static String header(MoiseElement ele) {
        StringBuilder np = new StringBuilder();
        np.append("/*\n");
        np.append("    This program was automatically generated from\n");
        np.append("    the organisation specification '"+ele.getId()+"'\n    on "+new SimpleDateFormat("MMMM dd, yyyy - HH:mm:ss").format(new Date())+"\n\n");
        np.append("    This is a MOISE tool, see more at http://moise.sourceforge.net\n\n");
        np.append("*/\n\n");
        return np.toString();
    }
}
