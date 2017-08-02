package moise.oe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import moise.common.MoiseCardinalityException;
import moise.common.MoiseConsistencyException;
import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.os.fs.Goal;
import moise.os.fs.Plan.PlanOpType;
import moise.prolog.ToProlog;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
    Represents an instance goal (in an instance scheme)
    
    @navassoc - specification    - Goal
    @navassoc - plan-to-fulfil   - PlanInstance
    @navassoc - part-of-plan     - PlanInstance
    @navassoc - state            - GoalState
    @navassoc - scheme           - SchemeInstance
    @navassoc - committed-agents * OEAgent
    
    @author Jomi Fred Hubner
 */
public class GoalInstance extends MoiseElement implements ToXML, ToProlog {
    
    private static final long serialVersionUID = 1L;

    public enum GoalState { waiting,  
                            enabled,
                            satisfied,
                            impossible
    };
    
    protected Goal     spec = null;
    
    protected PlanInstance inPlan = null; // the plan this goal belongs to
    protected PlanInstance plan = null;   // the plan to achieve this goal

    protected GoalState            state = GoalState.waiting; 
    protected SchemeInstance       sch;
    protected Map<String,Object>   args = null;
    protected List<OEAgent>        comAgs       = new ArrayList<OEAgent>(); // committed agents
    protected List<OEAgent>        achievedAgs = new ArrayList<OEAgent>(); // committed agents that satisfied this goal

    public GoalInstance(Goal sg, SchemeInstance sch) {
        this.sch  = sch;
        this.spec = sg;
        if (spec.hasArguments()) {
            args = new LinkedHashMap<String,Object>();
            for (String arg: spec.getArguments().keySet()) {
                //Object vl = spec.getArguments().get(arg);
                //if (vl.toString().length() > 0) {
                args.put(arg, spec.getArguments().get(arg) );
                //}
            }
        }
    }
    
    public Goal getSpec() {
        return spec;
    }

    public SchemeInstance getScheme() {
        return sch;
    }
    
    public void setInPlan(PlanInstance pi) {
        inPlan = pi;
    }
    
    
    /** set the plan that achieves this goal */
    public void setPlanToAchieve(PlanInstance pi) throws MoiseConsistencyException {
        if (! spec.hasPlan()) {
            throw new MoiseConsistencyException("the goal "+this+" has no plan in the specification, so it can not get a plan instance.");
        } else if (! spec.getPlan().equals( pi.getSpec())) {
            throw new MoiseConsistencyException("the goal "+this+" is not the target goal of plan ("+pi.getSpec()+"), so it can not get a plan instance.");
        } else {
            plan = pi;
        }
    }
    
    public PlanInstance getPlanToAchieve() {
        return plan;
    }
    
    
    /**
     * set an argument's value for this instance goal
     *
     * <p/>Example: <code>
     *      InstanceGoal gA = sch.getGoal("a");
     *      gA.setArgumentValue("Z", "120");</code>
     *
     * @param arg the argument identification
     * @param value the value for this argument
     * @throws MoiseException there is not such an arg id */
    public void setArgumentValue(String arg, Object value) throws MoiseException {
        // get the arguments
        if (spec.hasArguments() && spec.getArguments().containsKey(arg)) {
            args.put(arg, value);
        } else {
            throw new MoiseException("the goal "+spec+" has not the "+arg+" argument.");
        }
    }
    
    public Object getArgumentValue(String arg) {
        if (args == null)
            return null;
        else
            return args.get(arg);
    }
    
    /** returns all this goal arguments (key=argId, value=Object)  */
    public Map<String,Object> getArgumentValues() {
        return args;
    }
    
    /**
     * sets that this goal is achieved by the agent a.
     * only committed agents can achieve the goal.
     * 
     * If all agents committed to the goal set it as achieved, 
     * the goal is considered as satisfied.
     * 
     * if this goal is achieved and belongs to a plan without committed agents, 
     * check if this super goal was also achieved
     */
    public void setAchieved(OEAgent a) throws MoiseConsistencyException, MoiseCardinalityException {
        //satisfied = true;
        if (comAgs.contains(a)) {
            achievedAgs.add(a);
            int min = spec.getMinAgToSatisfy();
            if (min == -1) {
                min = comAgs.size();
            }
            if (isCommitted() && achievedAgs.size() >= min) {
                state = GoalState.satisfied;
                
                // check super goal achievement
                setSuperAchieved();
            }
        } else {
            throw new MoiseConsistencyException("The agent "+a+" is not committed to "+this+", so it can not set this goal as achieved!");
        }
    }

    private void setSuperAchieved() {
        // check super goal achievement
        if (inPlan != null && !inPlan.getHead().hasComittedAgents()) {
            GoalInstance h = inPlan.getHead();
            if (inPlan.getSpec().getOp() == PlanOpType.parallel) {
                boolean all = true;
                for (GoalInstance gi: inPlan.getGoals()) {
                    if (!gi.isSatisfied()) {
                        all = false;
                        break;
                    }
                }
                if (all) {
                    h.state = GoalState.satisfied;
                    h.setSuperAchieved();
                }
                
            } else if (inPlan.getSpec().getOp() == PlanOpType.choice) {
                // one of the choices was satisfied, set the goal as also satisfied
                h.state = GoalState.satisfied;
                h.setSuperAchieved();
                
            } else if (inPlan.getSpec().getOp() == PlanOpType.sequence) {
                // if this goal is the last in the plan, the super goal is achieved
                if (inPlan.getLastGoal().equals(this)) {
                    h.state = GoalState.satisfied;
                    h.setSuperAchieved();
                }
            }
        }
    }

    
    /**
     * sets this goal as impossible to be achieved.
     * 
     * if this goal belongs to a plan without committed agents, 
     * check if this super goal was also impossible
     */
    public void setImpossible(OEAgent a) throws MoiseConsistencyException {
        if (comAgs.contains(a)) {
            state = GoalState.impossible;

            // check super goal impossibility
            setSuperImpossible();
        } else {
            throw new MoiseConsistencyException("The agent "+a+" is not committed to "+this+", so it can not set this goal as impossible!");
        }
    }

    private void setSuperImpossible() {
        if (inPlan != null && !inPlan.getHead().hasComittedAgents()) {
            GoalInstance h = inPlan.getHead();
            if (inPlan.getSpec().getOp() == PlanOpType.parallel) {
                h.state = GoalState.impossible;
                h.setSuperImpossible();
                
            } else if (inPlan.getSpec().getOp() == PlanOpType.choice) {
                boolean all = true;
                for (GoalInstance gi: inPlan.getGoals()) {
                    if (!gi.isImpossible()) {
                        all = false;
                        break;
                    }
                }
                if (all) {
                    h.state = GoalState.impossible;
                    h.setSuperImpossible();
                }
                
            } else if (inPlan.getSpec().getOp() == PlanOpType.sequence) {
                h.state = GoalState.impossible;
                h.setSuperImpossible();
            }
        }       
    }
    
    /**
     * adds the agent a in the set of agents committed to this goal
     */
    public void committed(OEAgent a) {
        comAgs.add(a);
    }
    
    /**
     * removes the agent a in the set of agents committed to this goal
     */
    public void uncommitted(OEAgent a) {
        comAgs.remove(a);
    }
    
    /** returns true if this goal is
     *     not satisfied yet, 
     *     the scheme is well formed 
     *     super goal is not satisfied,  
     *     super goal is not impossible,
     */
    public boolean isEnabled() {
        if (state == GoalState.enabled)         return true;
        if (state == GoalState.impossible)      return false;
        if (state == GoalState.satisfied)       return false;
        
        // state is waiting, check if becomes possible
        if (checkEnabled() && !hasSuperGoalInState(GoalState.satisfied) && !hasSuperGoalInState(GoalState.impossible) && (sch == null || sch.isWellFormed())) {
            
            // check plan
            PlanInstance pi = getPlanToAchieve();
            if (pi == null) {
                state = GoalState.enabled;
                return true;
            }
            if (pi.getGoals() == null) {
                state = GoalState.enabled;
                return true;
            }

            if (pi.getSpec().getOp() == PlanOpType.choice) {
                for (GoalInstance gi: pi.getGoals()) {
                    if (gi.isSatisfied()) {
                        state = GoalState.enabled;
                        return true;
                    }
                }
                return false;
            } else {
                for (GoalInstance gi: pi.getGoals()) {
                    if (!gi.isSatisfied()) {
                        return false;
                    }
                }
                state = GoalState.enabled;
                return true;
            }
        }
        return false;
    }
    
    /** the goal is enabled in the scheme state, i.e., its pre-condition goals are satisfied. */
    private boolean checkEnabled() {
        // compute the possibility
        if (sch == null) { // there is no scheme to check permission
            return true;
        }
        
        if (inPlan == null) { // there is no plan where this goal is in
            return true;
        }
        
        if (sch.getSpec().getRoot().getId().equals(this.spec.getId())) { // this goal is the SCH root
            return true;
        }
        
        // there is plan (sequence) that defines if the goal is possible
        // (choice or parallel does not defines permission)
        if (inPlan.getSpec().getOp() == PlanOpType.sequence) {
            // get the goal just before this
            Goal previous = inPlan.getSpec().getPreviousSubGoals(this.spec.getId());
            if (previous == null) {
                // this goal is the plan first goal
                // its permission depends on its plan head goal
                return inPlan.getHead().checkEnabled();
            } else {
                // the previous goal state defines this goal permission
                return sch.getGoal(previous.getId()).isSatisfied();
            }
        } else {
            // it is the inPlan's head that defines the sequence
            return inPlan.getHead().checkEnabled();
        }
    }

    /** a goal is achieved if enough committed agents have set it as satisfied */
    public boolean isSatisfied()   {  
        return state == GoalState.satisfied;  
    }
    
    public GoalState getState() {
        if (state == GoalState.waiting) isEnabled(); // check if the goal becomes possible
        return state; 
    }
    
    public boolean isImpossible()  { return state == GoalState.impossible;  }
    
    public boolean isCommitted()   { return !comAgs.isEmpty();  }
    
    public boolean hasComittedAgents()              { return !comAgs.isEmpty(); }
    public Collection<OEAgent> getCommittedAgents() { return comAgs;  }
    public Collection<OEAgent> getAchievedAgents()  { return achievedAgs; }

    public boolean hasSuperGoalInState(GoalState s) {
        if (inPlan != null) {
            GoalInstance head = inPlan.getHead();
            if (head != null) {
                if (head.getState() == s) {
                    return true;
                } else {
                    return head.hasSuperGoalInState(s);
                }
            }
        }
        return false;
    }
    
    public static String getXMLTag() {
        return "goal";
    }

    
    public Element getAsDOM(Document document) {
        Element giEle = (Element) document.createElement(getXMLTag());
        giEle.setAttribute("specification", getSpec().getId());
        giEle.setAttribute("state", getState()+"");
        giEle.setAttribute("root", (inPlan == null)+"");
        giEle.setAttribute("committed-ags", getCommittedAgents().toString());
        giEle.setAttribute("achieved-by", getAchievedAgents().toString());
    
        // arguments
        Map<String,Object> args = getArgumentValues();
        if (args != null) {
            for (String key: args.keySet()) {
                Element argEle = (Element) document.createElement("argument");
                argEle.setAttribute("id",key);
                Object value = args.get(key);
                if (value != null)
                    argEle.setAttribute("value", value.toString());
                giEle.appendChild(argEle);
            }
        }
    
        // plan
        if (getSpec().getPlan() != null) {
            giEle.appendChild(getSpec().getPlan().getAsDOM(document));
        }
        
        // explicit dependencies
        if (getSpec().hasDependence()) {
            for (Goal dg: getSpec().getDependencies()) {
                Element ea = (Element) document.createElement("depends-on");
                ea.setAttribute("goal", dg.getId());
                giEle.appendChild(ea);                
            }
        }        
        
        return giEle;
    }    

    @Override
    public int hashCode() {
        return sch.hashCode()+spec.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;

        if (o instanceof GoalInstance) {
            GoalInstance gi = (GoalInstance)o;
            return spec.equals(gi.spec) && sch.equals(gi.sch);
        }
        return false;
    }
    
    public String getAsProlog() {
        StringBuilder s = new StringBuilder(spec.getId());
        if (spec.getArguments() != null && !spec.getArguments().isEmpty()) {
            s.append("(");
            String v = "";
            for (String arg: spec.getArguments().keySet())  {
                Object vl = args.get(arg);
                s.append(v); v = ",";
                if (vl == null || vl.toString().length() == 0) {
                    s.append(arg);
                } else {
                    s.append(vl);
                }
            }
            s.append(")");
        }
        return s.toString();
    }

    public String toString() {
        return spec.toString();
    }
    
}
