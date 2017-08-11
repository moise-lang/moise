/*
 * PlanInstace.java
 *
 * Created on 29 de Novembro de 2002, 14:40
 */

package moise.oe;

import java.util.ArrayList;
import java.util.List;

import moise.common.MoiseConsistencyException;
import moise.common.MoiseElement;
import moise.os.fs.Goal;
import moise.os.fs.Plan;


/**
 Represents a plan instance inside a scheme

 @navassoc - specification  - Plan
 @navassoc - scheme  - SchemeInstance
 @navassoc - head-goal  - GoalInstance
 @has      - goals  * GoalInstance

 @author  jomi
*/
public class PlanInstance extends MoiseElement {

    private static final long serialVersionUID = 1L;

    protected Plan           spec = null;
    protected SchemeInstance sch;
    protected GoalInstance   head;

    protected List<GoalInstance> goals = new ArrayList<GoalInstance>(); // sub-goals

    public PlanInstance(Plan p) {
        this.spec = p;
    }

    public Plan getSpec() {
        return spec;
    }

    public SchemeInstance getScheme() {
        return sch;
    }

    public GoalInstance getHead() {
        return head;
    }

    public void setGoalInstances(SchemeInstance sch) throws MoiseConsistencyException {
        this.sch = sch;

        for (Goal sg: spec.getSubGoals()) {
            GoalInstance gi = sch.getGoal(sg);
            goals.add( gi );
            gi.setInPlan( this );
        }

        head = sch.getGoal(spec.getTargetGoal());
        head.setPlanToAchieve( this );
    }

    public List<GoalInstance> getGoals() {
        return goals;
    }

    public GoalInstance getLastGoal() {
        return goals.get(goals.size()-1);
    }

    public String toString() {
        return "*"+spec.toString();
    }

}
