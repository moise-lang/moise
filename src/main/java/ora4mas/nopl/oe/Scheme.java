package ora4mas.nopl.oe;

import static jason.asSyntax.ASSyntax.createAtom;
import static jason.asSyntax.ASSyntax.createLiteral;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import jaca.ToProlog;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExpr;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import moise.os.fs.Goal;
import moise.os.fs.Plan.PlanOpType;

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
        createLiteral(Group.responsiblePI.getFunctor(), new VarTerm("Gr"), new VarTerm("Sch"))               // from group
    };


    public final static PredicateIndicator schemePI      = dynamicFacts[0].getPredicateIndicator();
    public final static PredicateIndicator committedPI   = dynamicFacts[1].getPredicateIndicator();
    public final static PredicateIndicator exCommittedPI = dynamicFacts[2].getPredicateIndicator();
    public final static PredicateIndicator donePI        = dynamicFacts[3].getPredicateIndicator();
    public final static PredicateIndicator satisfiedPI   = dynamicFacts[4].getPredicateIndicator();

    // specification
    private moise.os.fs.Scheme spec;

    // responsible groups
    private ConcurrentSkipListSet<Group>   groups  = new ConcurrentSkipListSet<Group>();

    // the literal is done(schemeId, goalId, agent name)
    private ConcurrentSkipListSet<Literal> doneGoals = new ConcurrentSkipListSet<Literal>();

    // values for goal arguments (key = goal + arg, value = value)
    private HashMap<Pair<String,String>,Object> goalArgs = new HashMap<Pair<String,String>,Object>();

    // list of satisfied goals
    private Set<String> satisfiedGoals = new HashSet<String>(); // we use "contains" a lot, so remains HashSet

    public Scheme(moise.os.fs.Scheme spec, String id) {
        super(id);
        this.spec = spec;
    }
    
    public moise.os.fs.Scheme getSpec() {
        return spec;
    }

    public void addDoneGoal(String ag, String goal) {
        doneGoals.add(createLiteral(donePI.getFunctor(), termId, createAtom(goal), createAtom(ag)));
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
    
    public Set<Literal> getDoneGoals() {
        return new HashSet<>(doneGoals);
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

        // recompute for all goals which this goal is pre condition
        for (Goal g: spec.getGoals()) {
            if (g.getPreConditionGoals().contains(goal)) {
                changed = resetGoalAndPreConditions(g) || changed;
            }
        }

        return changed;
    }

    public void setGoalArgValue(String goal, String arg, Object value) {
        goalArgs.put(new Pair<String,String>(goal,arg), value);
    }
    public Object getGoalArgValue(String goal, String arg) {
        return goalArgs.get(new Pair<String,String>(goal,arg));
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
        Collection<String> l = new ArrayList<String>();
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
        if (pi.equals(getPlayerPI()) ||  pi.equals(getExPlayerPI()) || pi.equals(monitorSchPI))
            return super.consult(l, u);

        if (pi.equals(Group.playPI) || pi.equals(Group.responsiblePI)) {
            return consultProviders(l, u, groups.iterator());

        } else if (pi.equals(schemePI)) {
            Term lCopy = l.getTerm(0);
            if (u.unifies(lCopy, termId))
                return LogExpr.createUnifIterator(u);
            else
                return LogExpr.EMPTY_UNIF_LIST.iterator();

        } else if (pi.equals(donePI)) {
            return consult(l, u, doneGoals);

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
                List<Unifier> lu = new ArrayList<Unifier>();
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
            for (Goal mg: spec.getMission(p.getTarget()).getGoals()) {
                if (mg.equals(g))
                    tail.append(new Atom(p.getAg()));
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

    public boolean isSatisfied(Goal g) {
        if (satisfiedGoals.contains(g.getId()))
            return true;

        // all pre-conditions
        //    satisfied(S,G) :-     // no agents have to achieve -- automatically satisfied by its pre-conditions
        //           goal(_,G,PCG,_,0,_) & all_satisfied(S,PCG).

        if (g.getMinAgToSatisfy() == 0) { // goal without mission
            if (!g.hasPlan())   // if no plan is defined, it is never satisfied
                return false;
            boolean hasChoicePlan = g.getPlan().getOp() == PlanOpType.choice;
            for (Goal pg: g.getPreConditionGoals()) {
                if (hasChoicePlan) {
                    if (isSatisfied(pg))
                        return true; // if one of the precondition goals is satisfied, g is also satisfied
                } else if (! isSatisfied(pg)) {
                    return false;
                }
            }
            return !hasChoicePlan;
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
        g.monSch = this.monSch;
        g.players.addAll(this.players);
        g.exPlayers.addAll(this.exPlayers);
        g.groups.addAll(this.groups);
        g.doneGoals.addAll(this.doneGoals);
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
        out.append("  goal arguments:\n");
        for (Pair<String,String> k: goalArgs.keySet())
            out.append("    "+k.l+" "+k.r+"="+ goalArgs.get(k)+"\n");

        //out.append("\n  accomplished missions:\n");
        //for (String m: accomplisedMissions)
        //    out.append("    "+m+"\n");

        return out.toString();
    }
}
