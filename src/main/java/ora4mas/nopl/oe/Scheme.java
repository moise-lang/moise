package ora4mas.nopl.oe;

import static jason.asSyntax.ASSyntax.createAtom;
import static jason.asSyntax.ASSyntax.createLiteral;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import jaca.ToProlog;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExpr;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import moise.common.MoiseException;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.fs.Plan.PlanOpType;
import moise.os.fs.robustness.AccountingGoal;
import moise.os.fs.robustness.HandlingGoal;
import moise.os.fs.robustness.RaisingGoal;
import moise.os.fs.robustness.Report;
import moise.os.fs.robustness.RequestingGoal;
import moise.os.fs.robustness.TreatmentGoal;
import npl.NPLInterpreter;
import npl.parser.nplp;

/**
 Represents an instance of scheme

 @navassoc - specification       - moise.os.fs.Scheme
 @navassoc - responsible_groups  * Group

 @author Jomi Fred Hubner
*/
public class Scheme extends CollectiveOE {

    // dynamic facts for which this class can answer "consult" (from DynamicFactsProvider interface and used by NPL interpreter)
    public final static Literal[] dynamicFacts = {
        createLiteral("scheme_id", new VarTerm("SID")),
        createLiteral("committed", new VarTerm("Ag"), new VarTerm("Mis"), new VarTerm("SID")),
        createLiteral("leaved_mission", new VarTerm("Ag"), new VarTerm("Mis"), new VarTerm("SID")),
        createLiteral("done", new VarTerm("SID"), new VarTerm("Goal"), new VarTerm("Ag") ),
        createLiteral("satisfied", new VarTerm("SID"), new VarTerm("Goal")),
        createLiteral(Group.playPI.getFunctor(), new VarTerm("Ag"), new VarTerm("Role"), new VarTerm("Gr")), // from group
        createLiteral(Group.responsiblePI.getFunctor(), new VarTerm("Gr"), new VarTerm("Sch")),               // from group
        createLiteral("failed",new VarTerm("SID"), new VarTerm("Goal")),
        createLiteral("released", new VarTerm("SID"), new VarTerm("Goal")),
        createLiteral("raised", new VarTerm("Report"), new VarTerm("Ag"), new VarTerm("Args")),
        createLiteral("account", new VarTerm("Report"), new VarTerm("Ag"), new VarTerm("Args"))
    };


    public final static PredicateIndicator schemePI      = dynamicFacts[0].getPredicateIndicator();
    public final static PredicateIndicator committedPI   = dynamicFacts[1].getPredicateIndicator();
    public final static PredicateIndicator exCommittedPI = dynamicFacts[2].getPredicateIndicator();
    public final static PredicateIndicator donePI        = dynamicFacts[3].getPredicateIndicator();
    public final static PredicateIndicator satisfiedPI   = dynamicFacts[4].getPredicateIndicator();
    public final static PredicateIndicator failedPI      = dynamicFacts[7].getPredicateIndicator();
    public final static PredicateIndicator releasedPI    = dynamicFacts[8].getPredicateIndicator();
    public final static PredicateIndicator raisedPI      = dynamicFacts[9].getPredicateIndicator();
    public final static PredicateIndicator accountPI     = dynamicFacts[10].getPredicateIndicator();

    // specification
    private moise.os.fs.Scheme spec;

    // responsible groups
    private ConcurrentSkipListSet<Group>   groups  = new ConcurrentSkipListSet<>();

    // the literal is done(schemeId, goalId, agent name)
    private ConcurrentSkipListSet<Literal> doneGoals = new ConcurrentSkipListSet<>();


    // the literal is failed(schemeId, goalId)
    private List<Literal> failedGoals = new CopyOnWriteArrayList<>();

    // the literal is released(schemeId, goalId)
    private ConcurrentSkipListSet<Literal> releasedGoals = new ConcurrentSkipListSet<>();

    // values for goal arguments (key = goal + arg, value = value)
    private HashMap<Pair<String,String>,Object> goalArgs = new HashMap<>();

    // the literal is raised(report id, agent name, arguments)
    private List<Literal> raiseds = new CopyOnWriteArrayList<>();

    // the literal is account(report id, agent name, arguments)
    private List<Literal> accounts = new CopyOnWriteArrayList<>();

    // list of satisfied goals
    private Set<String> satisfiedGoals = new HashSet<>(); // we use "contains" a lot, so remains HashSet

    public Scheme(moise.os.fs.Scheme spec, String id) {
        super(id);
        this.spec = spec;

        // copy initial values of goal args
        for (Goal g: spec.getGoals()) {
            if (g.getArguments() != null) {
                for (String arg: g.getArguments().keySet()) {
                    Object vl = g.getArguments().get(arg);
                    if (vl != null && vl.toString().length()>0)
                        setGoalArgValue(g.getId(), arg, vl);
                }
            }
        }
    }

    public moise.os.fs.Scheme getSpec() {
        return spec;
    }

    public void addDoneGoal(String ag, String goal) {
        doneGoals.add(createLiteral(donePI.getFunctor(), termId, createAtom(goal), createAtom(ag)));
    }

    public void addFailedGoal(String goal) {
        failedGoals.add(createLiteral(failedPI.getFunctor(), termId, createAtom(goal)));
    }

    public void addReleasedGoal(String goal) {
        releasedGoals.add(createLiteral(releasedPI.getFunctor(), termId, createAtom(goal)));
    }

    public void addRaised(String ag, String exception, Object[] arguments) throws ParseException {
        String argS = "[";
        int i = 0;
        while(i < arguments.length -1) {
            argS += arguments[i] + ",";
            i++;
        }
        if(arguments.length > 0) {
            argS += arguments[arguments.length-1];
        }
        argS += "]";
        raiseds.add(createLiteral(raisedPI.getFunctor(), createAtom(exception), createAtom(ag), ASSyntax.parseList(argS)));
    }

    public void addAccount(String ag, String account, Object[] arguments) throws ParseException, npl.parser.ParseException {
        String argS = "[";
        int i = 0;
        while(i < arguments.length -1) {
            argS += arguments[i] + ",";
            i++;
        }
        if(arguments.length > 0) {
            argS += arguments[arguments.length-1];
        }
        argS += "]";
        accounts.add(createLiteral(accountPI.getFunctor(), createAtom(account), createAtom(ag), ASSyntax.parseList(argS)));
    }

    public Term getTermId() {
        return termId;
    }

    public boolean removeDoneGoal(Goal goal) {
        boolean r = false;
        Atom gAtom = createAtom(goal.getId());
        Iterator<Literal> iDoneGoals = doneGoals.iterator();
        while (iDoneGoals.hasNext()) {
            Literal l = iDoneGoals.next();
            if (l.getTerm(1).equals(gAtom)) {
                iDoneGoals.remove();
                r = true;
            }
        }
        return r;
    }

    public boolean removeFailedGoal(Goal goal) {
        boolean r = false;
        Atom gAtom = createAtom(goal.getId());
        for(Literal l :failedGoals) {
            if (l.getTerm(1).equals(gAtom)) {
                failedGoals.remove(l);
                r = true;
            }
        }
        return r;
    }

    public boolean removeReleasedGoal(Goal goal) {
        
        boolean r = false;
        Atom gAtom = createAtom(goal.getId());
        Iterator<Literal> iReleasedGoals = releasedGoals.iterator();
        while (iReleasedGoals.hasNext()) {
            Literal l = iReleasedGoals.next();
            if (l.getTerm(1).equals(gAtom)) {
                iReleasedGoals.remove();
                r = true;
            }
        }
        return r;
    }

    public boolean removeRaised(Report report) {
        boolean r = false;
        Atom eAtom = createAtom(report.getId());
        for(Literal l : raiseds) {
            if (l.getTerm(1).equals(eAtom)) {
                raiseds.remove(l);
                r = true;
            }
        }
        return r;
    }
    
    public boolean removeAccount(Report report) {
        boolean r = false;
        Atom eAtom = createAtom(report.getId());
        for(Literal l : accounts) {
            if (l.getTerm(1).equals(eAtom)) {
                accounts.remove(l);
                r = true;
            }
        }
        return r;
    }

    public Set<Literal> getDoneGoals() {
        return new HashSet<>(doneGoals);
    }
    
    public Set<Literal> getReleasedGoals() {
        return releasedGoals;
    }

    public boolean resetGoal(Goal goal) {
        boolean changed = resetGoalAndPreConditions(goal);

        if (goal.hasPlan()) {
            // also reset subgoals
            for (Goal g: goal.getPlan().getSubGoals()) {
                changed = resetGoal(g) || changed;
            }
        }

        if (changed) {
            // reset also satisfied goals
            satisfiedGoals.clear();
        }
        return changed;
    }
    
    protected boolean resetGoalAndPreConditions(Goal goal) {
        boolean changed = removeDoneGoal(goal);

        if(!changed) {
            changed = removeFailedGoal(goal);
        }
        if(!changed) {
            changed = removeReleasedGoal(goal);
        }

        // recompute for all goals which this goal is pre condition
        for (Goal g: spec.getGoals()) {
            if (g.getPreConditionGoals().contains(goal)) {
                changed = resetGoalAndPreConditions(g) || changed;
            }
        }

        return changed;
    }

    public boolean resetExceptions(NPLInterpreter nengine) {
        boolean changed = false;
        for(Literal l : raiseds) {
            try {
                Report ex = spec.getReport(l.getTerm(0).toString());
                boolean anyConditionHolding = false;
                for(RaisingGoal tg : ex.getRaisingGoals()) {
                
                    LogicalFormula whenCondition = tg.getWhenCondition();

                    nplp parser = new nplp(new StringReader(whenCondition.toString()));
                    parser.setDFP(this);
                    LogicalFormula formula = (LogicalFormula)parser.log_expr();
                    if(nengine.holds(formula)) {
                        anyConditionHolding = true;
                    }
                }
                if(!anyConditionHolding) {
                    raiseds.remove(l);
                    for(RaisingGoal tg : ex.getRaisingGoals()) {
                        resetGoal(tg);
                    }
                    for(HandlingGoal cg : ex.getHandlingGoals()) {
                        resetGoal(cg);
                    }
                    resetExceptions(nengine);
                    changed = true;
                }
            } catch (MoiseException | npl.parser.ParseException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        return changed;
    }
    
    public boolean resetAccounts(NPLInterpreter nengine) {
        boolean changed = false;
        for(Literal l : accounts) {
            try {
                Report r = spec.getReport(l.getTerm(0).toString());
                boolean anyConditionHolding = false;
                for(AccountingGoal ag : r.getAccountingGoals()) {
                
                    LogicalFormula whenCondition = ag.getWhenCondition();

                    nplp parser = new nplp(new StringReader(whenCondition.toString()));
                    parser.setDFP(this);
                    LogicalFormula formula = (LogicalFormula)parser.log_expr();
                    if(nengine.holds(formula)) {
                        anyConditionHolding = true;
                    }
                }
                if(!anyConditionHolding) {
                    accounts.remove(l);
                    for(RequestingGoal rg : r.getRequestingGoals()) {
                        resetGoal(rg);
                    }
                    for(AccountingGoal ag : r.getAccountingGoals()) {
                        resetGoal(ag);
                    }
                    for(TreatmentGoal tg : r.getTreatmentGoals()) {
                        resetGoal(tg);
                    }
                    resetAccounts(nengine);
                    changed = true;
                }
            } catch (MoiseException | npl.parser.ParseException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        return changed;
    }

    public void setGoalArgValue(String goal, String arg, Object value) {
        goalArgs.put(new Pair<>(goal,arg), value);
    }
    public Object getGoalArgValue(String goal, String arg) {
        return goalArgs.get(new Pair<>(goal,arg));
    }
    public Map<Pair<String,String>,Object> getGoalsArgs() {
        return goalArgs;
    }

    public void addGroupResponsibleFor(Group g) {
        groups.remove(g);
        groups.add(g);
    }
    public void removeGroupResponsibleFor(Group g) {
        groups.remove(g);
    }

    public Collection<Group> getGroupsResponsibleFor() {
        return new HashSet<>(groups);
    }
    public Collection<String> getIdsGroupsResponsibleFor() {
        Collection<String> l = new ArrayList<>();
        for (Group g: getGroupsResponsibleFor()) {
            l.add(g.getId());
        }
        return l;
    }


    @Override
    PredicateIndicator getPlayerPI() {
        return committedPI;
    }
    @Override
    PredicateIndicator getExPlayerPI() {
        return exCommittedPI;
    }

    // DFP methods

    public Literal[] getDynamicFacts() {
        return dynamicFacts;
    }

    @Override
    public Iterator<Unifier> consult(Literal l, Unifier u) {
        PredicateIndicator pi = l.getPredicateIndicator();
        if (pi.equals(getPlayerPI()) ||  pi.equals(getExPlayerPI())) // || pi.equals(monitorSchPI))
            return super.consult(l, u);

        if (pi.equals(Group.playPI) || pi.equals(Group.responsiblePI)) {
            return consultFromProviders(l, u, groups.iterator());

        } else if (pi.equals(schemePI)) {
            Term lCopy = l.getTerm(0);
            if (u.unifies(lCopy, termId))
                return LogExpr.createUnifIterator(u);
            else
                return LogExpr.EMPTY_UNIF_LIST.iterator();

        } else if (pi.equals(raisedPI)) {
            return consultFromCollection(l, u, raiseds);

        } else if (pi.equals(accountPI)) {
            return consultFromCollection(l, u, accounts);
            
        } else if (pi.equals(donePI)) {
            return consultFromCollection(l, u, doneGoals);

        } else if (pi.equals(releasedPI)) {
            return consultFromCollection(l, u, releasedGoals);

        } else if (pi.equals(failedPI)) {
            return consultFromCollection(l, u, failedGoals);

        } else if (pi.equals(satisfiedPI)) {
            Term lCopy = l.getTerm(1).capply(u);
            if (lCopy.isGround()) {
                if (satisfiedGoals.contains(lCopy.toString())) {
                    return LogExpr.createUnifIterator(u);
                } else {
                    return LogExpr.EMPTY_UNIF_LIST.iterator();
                }
            } else {
                // usually this alternative is not used (the term is group in the NP), so we do not improve its performance.
                List<Unifier> lu = new ArrayList<>();
                for (String g: satisfiedGoals) {
                    Literal sg = createLiteral("satisfied", termId, createAtom(g));
                    Unifier c = u.clone();
                    if (c.unifiesNoUndo(sg, l))
                        lu.add(c);
                }
                return lu.iterator();
            }
        }
        return LogExpr.EMPTY_UNIF_LIST.iterator();
    }


    public ToProlog getResponsibleGroupsAsProlog() {
        return getCollectionAsProlog(getIdsGroupsResponsibleFor());
    }


    /** returns a list of agents committed to a particular goal */
    public ListTerm getCommittedAgents(Goal g) {
        ListTerm lCommittedBy = new ListTermImpl();
        ListTerm tail = lCommittedBy;
        for (Player p: getPlayers()) {
            Mission m = spec.getMission(p.getTarget());
            if (m != null) {
                for (Goal mg: m.getGoals()) {
                    if (mg.equals(g))
                        tail.append(new Atom(p.getAg()));
                }
            }
        }
        return lCommittedBy;
    }


    /** discover goals that are now satisfied, returns true if some new goal was satisfied */
    public boolean computeSatisfiedGoals() {
        boolean changed = false;
        for (Goal g: spec.getGoals()) {
            if ( !satisfiedGoals.contains(g.getId()) && isSatisfied(g) ) {
                satisfiedGoals.add(g.getId());
                changed = true;
                //System.out.println("added sat "+g);
            }
        }
        return changed;
    }

    public void setAsSatisfied(String g) {
        satisfiedGoals.add(g);
    }
    public void removeSatisfied(String g) {
        satisfiedGoals.remove(g);
    }

    public boolean isReleased(Goal g) {
        for(Literal l : releasedGoals) {
            if (l.getTerm(1).toString().equals(g.getId())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isSatisfied(Goal g) {
        if (satisfiedGoals.contains(g.getId()))
            return true;

        //no agents have to achieve -- automatically satisfied if preconditions are satisfied or released
        if (g.getMinAgToSatisfy() == 0) { // goal without mission
            if (!g.hasPlan())   // if no plan is defined, it is never satisfied
                return false;
            boolean hasChoicePlan = g.getPlan().getOp() == PlanOpType.choice;
            int nPreconditionsSatisfied = 0;
            int nPreconditionsReleased = 0;
            for (Goal pg: g.getPreConditionGoals()) {
                if(isSatisfied(pg))
                    nPreconditionsSatisfied++;
                else if(isReleased(pg)) {
                    nPreconditionsReleased++;
                }
                //if (hasChoicePlan) {
                //    if (isSatisfied(pg))
                //        return true; // if one of the precondition goals is satisfied, g is also satisfied
                    
                //} else if (!isSatisfied(pg)) {
                //    return false;
                //}
            }
            if(hasChoicePlan) {
                return (nPreconditionsSatisfied != 0) || (nPreconditionsReleased == g.getPreConditionGoals().size());
            }
            else {
                return nPreconditionsReleased + nPreconditionsSatisfied == g.getPreConditionGoals().size();
            }
            //return !hasChoicePlan;
        }

        int a = 0; // qty of achieved
        for (Literal p: doneGoals)
            if (p.getTerm(1).toString().equals(g.getId()))
                a++;

        // all committed agents
        //    satisfied(S,G) :-     // all committed agents have to achieve
        //           goal(M,G,_,_,all,_) & well_formed(S) & mplayers(M,S,V)  & .count( achieved(S,G,A), AA ) & AA >= V.
        if (g.getMinAgToSatisfy() == -1) { // -1 means all committed agents
            if (a == 0) // at least one agent have to do it
                return false;
            Set<String> missions = spec.getGoalMissionsId(g);
            int v = 0;
            for (Player p: getPlayers())
                if (missions.contains( p.getTarget() )) //.equals(mission))
                    v++;

            return a >= v;
        } else {
            return a == g.getMinAgToSatisfy();
        }
    }


    public Scheme clone() {
        Scheme g = new Scheme(spec,id);
        //g.monSch = this.monSch;
        g.players.addAll(this.players);
        g.exPlayers.addAll(this.exPlayers);
        g.groups.addAll(this.groups);
        g.doneGoals.addAll(this.doneGoals);
        g.failedGoals.addAll(this.failedGoals);
        g.raiseds.addAll(this.raiseds);
        g.accounts.addAll(this.accounts);
        g.releasedGoals.addAll(this.releasedGoals);
        //g.accomplisedMissions.addAll(this.accomplisedMissions);
        g.satisfiedGoals.addAll(this.satisfiedGoals);
        g.goalArgs.putAll(this.goalArgs);
        g.playersAsLiteralList.addAll(this.playersAsLiteralList);
        g.exPlayersAsLiteralList.addAll(this.exPlayersAsLiteralList);
        return g;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("Scheme: "+id+"\n  players:\n");
        for (Player p: players)
            out.append("    "+p+"\n");
        out.append("  responsible groups: ");
        for (Group s: groups)
            out.append(s.getId()+" ");
        out.append("\n  done goals:\n");
        for (Literal g: doneGoals)
            out.append("    "+g+"\n");
        out.append("  satisfied goals:\n");
        for (String g: satisfiedGoals)
            out.append("    "+g+"\n");
        out.append("  failed goals:\n");
        for (Literal g: failedGoals)
            out.append("    "+g+"\n");
        out.append("  released goals:\n");
        for (Literal g: releasedGoals)
            out.append("    "+g+"\n");
        out.append("  goal arguments:\n");
        for (Pair<String,String> k: goalArgs.keySet())
            out.append("    "+k.l+" "+k.r+"="+ goalArgs.get(k)+"\n");

        //out.append("\n  accomplished missions:\n");
        //for (String m: accomplisedMissions)
        //    out.append("    "+m+"\n");

        return out.toString();
    }
}
