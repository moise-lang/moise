package ora4mas.nopl.tools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jason.asSyntax.Literal;
import moise.common.MoiseElement;
import moise.os.Cardinality;
import moise.os.CardinalitySet;
import moise.os.OS;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.fs.Plan.PlanOpType;
import moise.os.fs.Scheme;
import moise.os.fs.robustness.AccountingGoal;
import moise.os.fs.robustness.ContextGoal;
import moise.os.fs.robustness.HandlingGoal;
import moise.os.fs.robustness.NotificationPolicy;
import moise.os.fs.robustness.RaisingGoal;
import moise.os.fs.robustness.Report;
import moise.os.fs.robustness.RequestingGoal;
import moise.os.fs.robustness.TreatmentGoal;
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

    //public static final String PROP_NotCompGoal           = "goal_non_compliance";

    public static final String PROP_FailNotEnabledGoal = "fail_not_enabled_goal";
    
    public static final String PROP_ExcUnknown = "exc_unknown";
    public static final String PROP_ExcAgNotAllowed = "exc_agent_not_allowed";
    public static final String PROP_ExcCondNotHolding = "exc_condition_not_holding";
    public static final String PROP_ExcArgNotGround = "exc_arg_not_ground";
    public static final String PROP_ExcArgMissing = "exc_arg_missing";
    public static final String PROP_ExcArgUnknown = "exc_arg_unknown";
    public static final String PROP_AchThrGoalExcNotRaised = "ach_thr_goal_exc_not_raised";

    // properties for groups
    public static final String[] NOP_GR_PROPS  = new String[] { PROP_RoleInGroup, PROP_RoleCardinality, PROP_RoleCompatibility, PROP_WellFormedResponsible, PROP_SubgroupInGroup, PROP_SubgroupCardinality};
    // properties for schemes
    public static final String[] NOP_SCH_PROPS = new String[] { //PROP_NotCompGoal,
            PROP_LeaveMission, PROP_AchNotEnabledGoal, PROP_AchNotCommGoal, PROP_MissionPermission, PROP_MissionCardinality,
            PROP_FailNotEnabledGoal, PROP_ExcUnknown, PROP_ExcAgNotAllowed, PROP_ExcCondNotHolding, PROP_AchThrGoalExcNotRaised, PROP_ExcArgNotGround, PROP_ExcArgMissing, PROP_ExcArgUnknown };
    // properties for norms
    public static final String[] NOP_NS_PROPS = new String[] {  };

    private static final String NGOAL = "ngoal"; // id of the goal obligations

    // condition for each property
    private static final Map<String, String> condCode = new HashMap<String, String>();
    static {
        condCode.put(PROP_RoleInGroup,           "play(Agt,R,Gr) & group_id(Gr) & not role_cardinality(R,_,_)");
        condCode.put(PROP_RoleCardinality,       "group_id(Gr) & role_cardinality(R,_,RMax) & rplayers(R,Gr,RP) & RP > RMax");
        condCode.put(PROP_RoleCompatibility,     "play(Agt,R1,Gr) & play(Agt,R2,Gr) & group_id(Gr) & R1 < R2 & not fcompatible(R1,R2,gr_inst)"); // note that is have to be play and not fplay

        condCode.put(PROP_SubgroupInGroup,       "group_id(Gr) & subgroup(G,GT,Gr) & not subgroup_cardinality(GT,_,_)");
        condCode.put(PROP_SubgroupCardinality,   "group_id(Gr) & subgroup_cardinality(SG,_,SGMax) & .count(subgroup(_,SG,Gr),SGP) & SGP > SGMax");

        condCode.put(PROP_WellFormedResponsible, "responsible(Gr,S) & not well_formed(Gr)"); // not monitor_scheme(S) &
        condCode.put(PROP_MissionPermission,     "committed(Agt,M,S) & not (mission_role(M,R) & responsible(Gr,S) & fplay(Agt,R,Gr))");
        condCode.put(PROP_LeaveMission,          "leaved_mission(Agt,M,S) & not mission_accomplished(S,M)");
        condCode.put(PROP_MissionCardinality,    "scheme_id(S) & mission_cardinality(M,_,MMax) & mplayers(M,S,MP) & MP > MMax");
        condCode.put(PROP_AchNotEnabledGoal,     "done(S,G,Agt) & mission_goal(M,G) & not mission_accomplished(S,M) & not (enabled(S,G) | satisfied(S,G))");
        condCode.put(PROP_AchNotCommGoal,        "done(S,G,Agt) & .findall(M, mission_goal(M,G) & (committed(Agt,M,S) | mission_accomplished(S,M)), [])");
        //condCode.put(PROP_NotCompGoal,           "obligation(Agt,"+NGOA+"(S,M,G),Obj,TTF) & not Obj & `now` > TTF");

        condCode.put(PROP_FailNotEnabledGoal, 
                "failed(S,G) & mission_goal(M,G) & not mission_accomplished(S,M) & not enabled(S,G)");
        condCode.put(PROP_ExcUnknown,
                "raised(E,Ag,Args) & not report(E,_)");
        condCode.put(PROP_ExcAgNotAllowed,
                "raised(E,Ag,Args) & report(E,_) & mission_goal(M,TG) & raisingGoal(TG,E,_) & not committed(Ag,M,S)");
        condCode.put(PROP_ExcCondNotHolding,
                "raised(E,Ag,Args) & report(E,NP) & notificationPolicy(NP,_,Condition) & not (raisingGoal(TG,E,_) & (Condition | done(S,TG,Ag)))");
        condCode.put(PROP_AchThrGoalExcNotRaised,
                "done(S,TG,Ag) & raisingGoal(TG,E,_) & not super_goal(_,TG) & not raised(E,Ag,Args)");
        condCode.put(PROP_ExcArgNotGround,
                "raised(E,Ag,Args) & report(E,_) & .member(Arg,Args) & not .ground(Arg)");
        condCode.put(PROP_ExcArgMissing,
                "raised(E,Ag,Args) & report(E,_) & argument(E,ArgFunctor,ArgArity) & not (.member(Arg,Args) & Arg=..[ArgFunctor,T,A] & .length(T,ArgArity))");
        condCode.put(PROP_ExcArgUnknown,
                "raised(E,Ag,Args) & report(E,_) & .member(Arg,Args) & Arg=..[ArgFunctor,T,A] & .length(T,ArgArity) & not argument(E,ArgFunctor,ArgArity)");
        
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
        //argsCode.put(PROP_NotCompGoal   ,        "obligation(Agt,"+NGOA+"(S,M,G),Obj,TTF)");
        
        argsCode.put(PROP_FailNotEnabledGoal, "S,G");
        
        argsCode.put(PROP_ExcUnknown, "S,E,Ag");
        argsCode.put(PROP_ExcAgNotAllowed, "S,E,Ag");
        argsCode.put(PROP_ExcCondNotHolding, "S,E,Ag,Condition");
        argsCode.put(PROP_ExcArgNotGround, "S,E,Arg");
        argsCode.put(PROP_AchThrGoalExcNotRaised, "S,G,E,Ag");
        argsCode.put(PROP_ExcArgMissing, "S,E,ArgFunctor,ArgArity");
        argsCode.put(PROP_ExcArgUnknown, "S,E,Arg");
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
            np.append( transform( sch, true) + "\n");

        np.append("} // end of organisation "+os.getId()+"\n");

        return np.toString();
    }

    /** transforms a Group Spec into NPL code */
    public static String transform(Group gr) {
        if (gr == null)
            return "";

        StringBuilder np = new StringBuilder();
        np.append("scope group("+gr.getId()+") {\n\n");

        np.append("   // ** Facts from OS\n");

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

        generateProperties(NOP_GR_PROPS, gr.getSS().getOS().getNS(), np);

        np.append("} // end of group "+gr.getId()+"\n");

        for (Group sgr: gr.getSubGroups()) {
            np.append("\n\n// ** Group "+sgr.getId()+", subgroup of "+gr.getId()+"\n");
            np.append( transform(sgr) + "\n");
        }

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
        np.append(  "   fcompatible(R1,R2,S) :- tsubrole(R1,R2).\n");
        np.append(  "   fcompatible(R1,R2,S) :- tsubrole(R1,R1a) & tsubrole(R2,R2a) & compatible(R1a,R2a,S).\n");
        np.append(  "   fcompatible(R1,R2,S) :- tcompatible(R1,R2,S,[R1,R2]).\n");

        // member is there to avoid infinity loops
        np.append(  "   tcompatible(R1,R2,S,Path) :- compatible(R1,R3,S) & not .member(R3,Path) & tcompatible(R3,R2,S,[R3|Path]).\n");

        np.append(  "   tsubrole(R,R).\n");
        np.append(  "   tsubrole(R1,R2)    :- subrole(R1,R2).\n");
        np.append(  "   tsubrole(R1,R2)    :- subrole(R1,R3) & tsubrole(R3,R2).\n");
        return np.toString();
    }

    /** transforms a Scheme Spec into NPL code */
    public static String transform(Scheme sch, boolean isSB) {
        StringBuilder np = new StringBuilder();
        np.append("scope scheme("+sch.getId()+") {\n\n");
        np.append("   // ** Facts from OS\n\n");

        if (!isSB) {
            np.append( roleHierarchy(sch.getFS().getOS().getSS()));
        }

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

            String ttf = g.getTTF();
            if (ttf.length() == 0) ttf = "1 year";
            List<Goal> precond = g.getPreConditionGoals();
            String prec = precond.toString();
            if (g.hasPlan() && g.getPlan().getOp() == PlanOpType.choice)
                prec = "dep(or,"+prec+")";
            else
                prec = "dep(and,"+prec+")";
            String nag  = g.getMinAgToSatisfy() == -1 ? "all" : ""+g.getMinAgToSatisfy();
            np.append("   goal("+smis+","+g.getId()+","+prec+","+g.getType()+","+nag+",`"+ttf+"`)");
            if (g.getLocation() != null && g.getLocation().length() > 0) {
                np.append("[location(\""+g.getLocation()+"\")]");
            //} else {
            //    np.append("[location(\"anywhere\")]");
            }

            np.append(".\n");
        }
        np.append(superGoal.toString());

        String notificationPolicy = "\n   // notificationPolicy(policy id, target id, condition formula)\n";
        
        String report =      "\n   // report(report id, policy id)\n";
        String argument =  "\n   // argument(report id, functor, arity)\n";
        
        String raisingGoal =        "\n   // raisingGoal(goal id, report id, when condition)\n";
        String handlingGoal =       "\n   // handlingGoal(goal id, report id, when condition)\n";

        String contextGoal = "\n   // contextGoal(goal id, report id)\n";
        String requestingGoal = "\n   // requestingGoal(goal id, report id, when condition)\n";
        String accountingGoal = "\n   // accountingGoal(goal id, report id, when condition)\n";
        String treatmentGoal = "\n   // treatmentGoal(goal id, report id, when condition)\n";
        
        for (NotificationPolicy npol : sch.getNotificationPolicies()) {
            notificationPolicy += "   notificationPolicy(" + npol.getId() + "," + npol.getTarget() + "," + npol.getCondition().toString() + ").\n";
            for(Report r : npol.getReports()) {
                report += "   report(" + r.getId() + "," + npol.getId() + ").\n";
                for(Literal l : r.getArguments()) {
                    argument += "   argument("+r.getId()+","+l.getFunctor().toString()+","+l.getArity()+").\n";
                }
                for(RaisingGoal raisg : r.getRaisingGoals()) {
                    raisingGoal += "   raisingGoal("+raisg.getId()+","+r.getId()+","+raisg.getWhenCondition().toString()+").\n";
                    for(Goal sg : raisg.getSubGoals()) {
                        raisingGoal += "   raisingGoal("+sg.getId()+","+r.getId()+","+raisg.getWhenCondition().toString()+").\n";
                    }
                }
                for(HandlingGoal hg : r.getHandlingGoals()) {
                    handlingGoal += "   handlingGoal("+hg.getId()+","+r.getId()+","+hg.getWhenCondition().toString()+").\n";
                    for(Goal sg : hg.getSubGoals()) {
                        handlingGoal += "   handlingGoal("+sg.getId()+","+r.getId()+","+hg.getWhenCondition().toString()+").\n";
                    }
                }
                for(ContextGoal cg : r.getContextGoals()) {
                    contextGoal += "   contextGoal("+ cg.getId() + "," + r.getId() + ").\n";
                    for(Goal sg : cg.getSubGoals()) {
                        contextGoal += "   contextGoal(" + sg.getId() + "," + r.getId() + ").\n";
                    }
                }
                for(RequestingGoal reqg : r.getRequestingGoals()) {
                    requestingGoal += "   requestingGoal("+reqg.getId()+","+r.getId()+","+reqg.getWhenCondition().toString()+").\n";
                    for(Goal sg : reqg.getSubGoals()) {
                        requestingGoal += "   requestingGoal("+sg.getId()+","+r.getId()+","+reqg.getWhenCondition().toString()+").\n";
                    }
                }
                for(AccountingGoal ag : r.getAccountingGoals()) {
                    accountingGoal += "   accountingGoal("+ag.getId()+","+r.getId()+","+ag.getWhenCondition().toString()+").\n";
                    for(Goal sg : ag.getSubGoals()) {
                        accountingGoal += "   accountingGoal("+sg.getId()+","+r.getId()+","+ag.getWhenCondition().toString()+").\n";
                    }
                }
                for(TreatmentGoal tg : r.getTreatmentGoals()) {
                    treatmentGoal += "   treatmentGoal("+tg.getId()+","+r.getId()+","+tg.getWhenCondition().toString()+").\n";
                    for(Goal sg : tg.getSubGoals()) {
                        treatmentGoal += "   treatmentGoal("+sg.getId()+","+r.getId()+","+tg.getWhenCondition().toString()+").\n";
                    }
                }
            }
        }
        
        np.append(notificationPolicy);
        np.append(report);
        np.append(argument);
        np.append(raisingGoal);
        np.append(handlingGoal);
        np.append(contextGoal);
        np.append(requestingGoal);
        np.append(accountingGoal);
        np.append(treatmentGoal);

        np.append("\n   // ** Rules\n");

        np.append("   mplayers(M,S,V) :- .count(committed(_,M,S),V).\n");
        np.append("   well_formed(S)");
        String sep = " :- \n";
        for (Mission m: sch.getMissions()) {
            Cardinality c = sch.getMissionCardinality(m);
            String var = "V"+m.getId();
            np.append(sep+"      (mission_accomplished(S,"+m.getId()+") | not mission_accomplished(S,"+m.getId()+") & mplayers("+m.getId()+",S,"+var+") & "+var+" >= "+c.getMin()+" & "+var+" <= "+c.getMax()+")");
            sep = " &\n";
        }
        np.append(".\n");

        np.append("   is_finished(S) :- satisfied(S,"+sch.getRoot().getId()+").\n");
        np.append("   mission_accomplished(S,M) :- .findall(Goal, mission_goal(M,Goal), MissionGoals) & all_satisfied(S,MissionGoals).\n");

        np.append("   all_satisfied(_,[]).\n");
        np.append("   all_satisfied(S,[G|T]) :- satisfied(S,G) & all_satisfied(S,T).\n");
        np.append("   any_satisfied(S,[G|_]) :- satisfied(S,G).\n");
        np.append("   any_satisfied(S,[G|T]) :- not satisfied(S,G) & any_satisfied(S,T).\n\n");

        np.append("   all_released(_,[]).\n");
        np.append("   all_released(S,[G|T]) :- released(S,G) & all_released(S,T).\n");
        np.append("   all_satisfied_released(_,[]).\n");
        np.append("   all_satisfied_released(S,[G|T]) :- (satisfied(S,G) | released(S,G)) & all_satisfied_released(S,T).\n\n");

        np.append("   // enabled goals (i.e. dependence between goals)\n");
        //np.append("   enabled(S,G) :- goal(_, G,  dep(or,PCG), _, NP, _) & NP \\== 0 & any_satisfied(S,PCG).\n");
        //np.append("   enabled(S,G) :- goal(_, G, dep(and,PCG), _, NP, _) & NP \\== 0 & all_satisfied(S,PCG).\n");
        np.append("   enabled(S,G) :- goal(_, G,  dep(or,PCG), _, NP, _) & not (requestingGoal(G,_,_) | accountingGoal(G,_,_) | treatmentGoal(G,_,_) | raisingGoal(G,_,_) | handlingGoal(G,_,_)) & NP \\== 0 & (any_satisfied(S,PCG) | all_released(S,PCG)).\n");
        np.append("   enabled(S,G) :- goal(_, G, dep(and,PCG), _, NP, _) & not (requestingGoal(G,_,_) | accountingGoal(G,_,_) | treatmentGoal(G,_,_) | raisingGoal(G,_,_) | handlingGoal(G,_,_)) & NP \\== 0 & all_satisfied_released(S,PCG).\n\n");

        np.append("   enabled(S,RG) :- raisingGoal(RG,E,When) &\r\n"
                + "                    When &\r\n"
                + "                    notificationPolicy(NPol,_,Condition) &\r\n"
                + "                    report(E,NPol) &\r\n"
                + "                    Condition &\r\n"   
                + "                    goal(_, RG,  Dep, _, NP, _) & NP \\== 0 & \r\n"
                + "                    ((Dep = dep(or,PCG)  & (any_satisfied(S,PCG) | all_released(S,PCG))) |\r\n"
                + "                     (Dep = dep(and,PCG) & all_satisfied_released(S,PCG))\r\n"
                + "                    ).\r\n");
        
        np.append("   enabled(S,HG) :- handlingGoal(HG,E,When) &\r\n" 
                + "                    When &\r\n"
                + "                    raised(E,_,_) &\r\n"
                + "                    raisingGoal(RG,E,_) &\r\n" 
                + "                    satisfied(S,RG) &\r\n"
                + "                    goal(_, HG,  Dep, _, NP, _) & NP \\== 0 &\r\n"
                + "                    ((Dep = dep(or,PCG)  & (any_satisfied(S,PCG) | all_released(S,PCG))) |\r\n"
                + "                     (Dep = dep(and,PCG) & all_satisfied_released(S,PCG))\r\n"
                + "                    ).\r\n");
        
        np.append("   enabled(S,RG) :- requestingGoal(RG,A,When) &\r\n"
                + "                    When &\r\n"
                + "                    notificationPolicy(NPol,_,Condition) &\r\n"
                + "                    report(A,NPol) &\r\n"
                + "                    Condition &\r\n"
                + "                    (not contextGoal(CG,A) | (contextGoal(CG,A) & satisfied(S,CG))) &\r\n"
                + "                    goal(_, RG,  Dep, _, NP, _) & NP \\== 0 & \r\n"
                + "                    ((Dep = dep(or,PCG)  & (any_satisfied(S,PCG) | all_released(S,PCG))) |\r\n"
                + "                     (Dep = dep(and,PCG) & all_satisfied_released(S,PCG))\r\n"
                + "                    ).\r\n");
        
        np.append("   enabled(S,AG) :- accountingGoal(AG,A,When) &\r\n"
                + "                    When &\r\n"
                + "                    requestingGoal(RG,A,_) &\r\n"
                + "                    satisfied(S,RG) &\r\n"
                + "                    goal(_, AG,  Dep, _, NP, _) & NP \\== 0 & \r\n"
                + "                    ((Dep = dep(or,PCG)  & (any_satisfied(S,PCG) | all_released(S,PCG))) |\r\n"
                + "                     (Dep = dep(and,PCG) & all_satisfied_released(S,PCG))\r\n"
                + "                    ).\r\n");
                
        np.append("   enabled(S,TG) :- treatmentGoal(TG,A,When) &\r\n"
                + "                    When &\r\n"
                + "                    account(A,_,_) &\r\n"
                + "                    accountingGoal(AG,A,_) &\r\n"
                + "                    satisfied(S,AG) &\r\n"
                + "                    goal(_, TG,  Dep, _, NP, _) & NP \\== 0 &\r\n"
                + "                    ((Dep = dep(or,PCG)  & (any_satisfied(S,PCG) | all_released(S,PCG))) |\r\n"
                + "                     (Dep = dep(and,PCG) & all_satisfied_released(S,PCG))\r\n"
                + "                    ).\r\n\n");
        
        np.append("   super_satisfied(S,G) :- super_goal(SG,G) & satisfied(S,SG).\n");

        np.append("\n   // ** Norms\n");


        np.append("\n   // --- Properties check ---\n");
        if (isSB) {
            generateProperties(NOP_SCH_PROPS, sch.getFS().getOS().getNS(), np);
        } else {
            generateProperties(NOP_NS_PROPS, sch.getFS().getOS().getNS(), np);
        }

        if (!isSB) {
            np.append("\n   // --- commitments ---\n");
            for (Norm nrm: sch.getFS().getOS().getNS().getNorms()) {
                if (sch.getMissions().contains(nrm.getMission())) {
                    np.append(generateNormEntry(
                            nrm, sch.getMissionCardinality(nrm.getMission()))
                    );
                }
            }
        }

        if (isSB) {
            np.append("\n   // agents are obliged to fulfill their enabled goals\n");
            np.append("   norm "+NGOAL+": \n");
            np.append("           committed(A,M,S) & mission_goal(M,G) & \n");
            //np.append("           enabled(S,G) & \n"); // tested by the maint. cond. of the norm
            np.append("           ((goal(_,G,_,achievement,_,D) & What = satisfied(S,G)) | \n");
            np.append("            (goal(_,G,_,performance,_,D) & What = done(S,G,A))) &\n");
            // TODO: implement location as annot for What. create an internal action to add this annot?
            //np.append("           ((goal(_,G,_,_,_,_)[location(L)] & WhatL = What[location(L)]) | (not goal(_,G,_,_,_,_)[location(L)] & WhatL = What)) &\n");
            np.append("           well_formed(S) & \n");
            np.append("           not satisfied(S,G) & \n");
            np.append("           not failed(_,G) & \n");
            np.append("           not released(_,G) & \n");
            np.append("           not (requestingGoal(G,_,_) | contextGoal(G,_)) & \n");
            np.append("           not super_satisfied(S,G)\n");
            np.append("        -> obligation(A,(enabled(S,G) & not failed(S,G)),What,`now` + D).\n");
            // TODO: maintenance goals
            //np.append("   // maintenance goals\n");
        }

        np.append("} // end of scheme "+sch.getId()+"\n");
        return np.toString();
    }

    private static void generateProperties(String[] props, NS ns, StringBuilder np) {
        String defaultM = ns.getStrProperty("default_management", "fail");
        for (String prop: props) {
            // check if some norm exist for the propriety
            String conf = ns.getStrProperty(prop, defaultM);
            if (conf.equals("ignore"))
                continue;

            np.append("   norm "+prop+": ");

            String space = "           ";
            if (conf.equals("fail")) {
                np.append(" \n");
            } else {
                np.append("true\n");
                np.append("        -> prohibition(Agt,true,\n");
                space += "    ";
            }

            String sep = "";
            for (String t: condCode.get(prop).split("&")) {
                np.append(sep+space+t.trim());
                sep = " &\n";
            }

            if (conf.equals("fail")) {
                np.append("\n        -> fail("+prop+"("+argsCode.get(prop)+")).\n");
            } else {
                np.append(",\n"+space+"`never`).\n");
            }
        }
    }

    public static String generateNormEntry(Norm nrm, Cardinality card) {
        StringBuilder np = new StringBuilder();
        
        String id = nrm.getId();
        String m  = nrm.getMission().getId();
        String tc = nrm.getTimeConstraint() == null ? "`1 year`" : "`"+nrm.getTimeConstraint().getTC()+"`";
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
        String cons      = args+",committed(A,"+m+",S),"+tc+").\n";
        if (card.getMin() > 0 && nrm.getType() == OpTypes.obligation) { // the obligation
            np.append("   norm "+id+": "+comment+"\n");
            np.append("           "+condition);
            np.append(" &\n           "+mplayers+"V < MMinCard");
            np.append(" &\n           "+fplay);
            np.append(" &\n           "+extraCond);
            np.append("\n        -> obligation("+"A,not well_formed(S)"+cons);
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
            np.append("\n        -> permission("+"A,responsible(Gr,S)"+cons);
        }
        return np.toString();
    }

    public static String header(MoiseElement ele) {
        StringBuilder np = new StringBuilder();
        np.append("/*\n");
        np.append("    This program was automatically generated from\n");
        np.append("    the organisation specification '"+ele.getId()+"'\n    on "+new SimpleDateFormat("MMMM dd, yyyy - HH:mm:ss").format(new Date())+"\n\n");
        np.append("    This is a MOISE tool, see more at https://moise-lang.github.io \n\n");
        np.append("*/\n\n");
        return np.toString();
    }
}
