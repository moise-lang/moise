package moise.os.fs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import moise.common.MoiseConsistencyException;
import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.prolog.ToProlog;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 Represents a Plan (one operator, and success rate, and a set of goals).

 @navassoc - head-goal - Goal
 @composed - goals - Goal
 @navassoc - operation - PlanOpType
 @navassoc - scheme - Scheme


 @author Jomi Fred Hubner
*/
public class Plan extends MoiseElement implements ToXML, ToProlog {

    private static final long serialVersionUID = 1L;

    public enum PlanOpType {
        sequence { public String toString() { return ","; }
                   public String opName()   { return "sequence";}},
        choice   { public String toString() { return "|"; }
                   public String opName()   { return "choice";}},
        parallel { public String toString() { return "||"; }
                   public String opName()   { return "parallel";}};
        abstract String opName();
    }

    protected List<Goal> subGoals = new ArrayList<Goal>();
    protected PlanOpType op = null;
    protected double     successRate = 1;
    protected Goal       target = null;
    protected Scheme     sch = null;

    public Plan(Scheme sch)  {
        this.sch = sch;
    }

    public Plan(PlanOpType op, Scheme sch, String targetGoalId) throws MoiseConsistencyException {
        super();
        this.sch = sch;
        setOp(op);
        setTarget(targetGoalId);
        setId(target.getFullId());
    }


    //
    // Op methods
    //
    public void setOp(PlanOpType op) throws MoiseConsistencyException {
        this.op = op;
    }

    public PlanOpType getOp() {
        return op;
    }

    //
    // Head methods
    //

    // for constructor use only (the headGoalId can only be set in the plan
    // construction
    private void setTarget(String targetGoalId) throws MoiseConsistencyException {
        if (targetGoalId != null) {
            Goal g = sch.getGoal(targetGoalId);
            if (g == null) {
                throw new MoiseConsistencyException("the goal '" + targetGoalId
                        + "' is not defined in the scheme " + sch.getId());
            }
            setTarget(g);
        }
    }
    private void setTarget(Goal goal) throws MoiseConsistencyException {
        if (goal.getPlan() != null) {
            if (!goal.getPlan().equals(this)) {
                throw new MoiseConsistencyException("the goal '"
                        + goal + "' already has a plan in the scheme "
                        + sch.getId());
            }
        }
        this.target = goal;
        goal.setPlan(this);
    }

    public Goal getTargetGoal() {
        return target;
    }

    //
    // Subgoals methods
    //
    public void addSubGoal(String goalId) throws MoiseConsistencyException {
        Goal g = sch.getGoal(goalId);
        if (g == null) {
            throw new MoiseConsistencyException("the goal "+goalId+" is not defined in the scheme "+sch.getId());
        }
        addSubGoal(g);
    }
    public void addSubGoal(Goal g) {
        if (!subGoals.contains(g))
            subGoals.add(g);
    }

    public boolean removeSubGoal(Goal g) {
        return subGoals.remove(g);
    }

    public List<Goal> getSubGoals() {
        return subGoals;
    }

    /**
     * Looks into the subgoals of the plan to find out a Goal like goalId
     */
    public Goal containsSubGoals(String goalId) {
        for (Goal sg: subGoals) {
            if (goalId.equals(sg.getId())) {
                return sg;
            }
        }
        return null;
    }

    /**
     * Looks into the subgoals of the plan to find out the previous Goal of goalId.
     * E.g: for the plan "p = g1, g2, g3", the g2's  previous goal is g1.
     *
     * Returns null either if the goal does no exist or has no previous goal.
     */
    public Goal getPreviousSubGoals(String goalId) {
        Goal previous = null;
        for (Goal sg: subGoals) {
            if (goalId.equals(sg.getId())) {
                return previous;
            }
            previous = sg;
        }
        return null;
    }

    //
    // Success methods
    //
    public void setSuccessRate(double d) {
        successRate = d;
    }
    public double getSuccessRate() {
        return successRate;
    }


    public String toString() {
        StringBuffer s = new StringBuffer();
        Iterator<Goal> ip = subGoals.iterator();
        while (ip.hasNext()) {
            s.append( ip.next() );
            if (ip.hasNext()) {
                s.append(op.toString());
            }
        }

        return target + "= (" + successRate +") " + s;
    }

    /** returns a string representing the plan in Prolog syntax, format:
     *     plan(operator,list of goals)
     */
    public String getAsProlog() {
        StringBuilder s = new StringBuilder("plan("+op.opName()+",[");
        String v="";
        for (Goal sg: getSubGoals()) {
            s.append( v+sg.getAsProlog() );
            v=",";
        }
        s.append("])");
        return s.toString();
    }

    public static String getXMLTag() {
        return "plan";
    }

    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("operator", op.opName());
        if (getSuccessRate() > 0 && getSuccessRate() < 1) {
            ele.setAttribute("success-rate", getSuccessRate()+"");
        }
        if (getProperties().size() > 0) {
            ele.appendChild(getPropertiesAsDOM(document));
        }

        // goals
        for (Goal sg: getSubGoals()) {
            ele.appendChild(sg.getAsDOM(document));
        }
        return ele;
    }


    public void setFromDOM(Element ele, Goal targetGoal) throws MoiseException {
        setPropertiesFromDOM(ele);
        setTarget(targetGoal);

        String operator = ele.getAttribute("operator");
        for (PlanOpType opt: PlanOpType.values()) {
            if (opt.opName().equals(operator)) {
                setOp(opt);
                break;
            }
        }

        String sr = ele.getAttribute("success-rate");
        if (sr != null && sr.length()>0) {
            setSuccessRate(Double.parseDouble(sr));
        }

        // goals
        for (Element eg: DOMUtils.getDOMDirectChilds(ele, Goal.getXMLTag())) {
            String goalId = eg.getAttribute("id");
            Goal gs = Optional.ofNullable(sch.getGoal(goalId)).orElseGet(() -> new Goal(goalId));
            gs.setInPlan(this);
            sch.addGoal(gs);
            gs.setFromDOM(eg, sch);
            subGoals.add(gs);
        }
    }
}
