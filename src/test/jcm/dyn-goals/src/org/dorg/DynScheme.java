package dorg;

import cartago.OPERATION;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.fs.Plan;
import moise.os.fs.Plan.PlanOpType;
import npl.NormativeFailureException;
import ora4mas.nopl.Operation;
import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.oe.Scheme;

public class DynScheme extends SchemeBoard {

    @OPERATION void addSubGoal(final String superGoalId, final String newGoalId) {
        if (spec.getGoal(newGoalId) != null) {
            failed("goal "+newGoalId+" already exists!");                   
        }
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                // consider the same plan, parallel by default
                Goal sg = spec.getGoal(superGoalId);
                if (sg == null)
                    failed("the goal "+superGoalId+" wasn't found!");
        
                Plan sgp = sg.getPlan();
                if (sgp == null) {
                    sgp = new Plan(spec);
                    sgp.setOp(PlanOpType.parallel);
                }
        
                Goal g = new Goal(newGoalId);
                spec.addGoal(g);
                g.setInPlan(sgp);
                sgp.addSubGoal(newGoalId);

                // reset the super goal
                getSchState().removeDoneGoal(sg);
                getSchState().removeSatisfied(sg.getId());
                getSchState().computeSatisfiedGoals();
                
                updateGoalStateObsProp();
                
                reorganise();
            }
        }, "Error adding goal "+newGoalId);
    }

    @OPERATION void addMissionGoal(final String missionId, final String goalId) {
        ora4masOperationTemplate(new Operation() {
            public void exec() throws NormativeFailureException, Exception {
                Mission m = spec.getMission(missionId);
                if (m == null) {
                    m = new Mission(missionId, spec);
                    spec.addMission(m);
                }
                m.addGoal(goalId);
                
                reorganise();
            }
        }, "Error adding mission for goal "+goalId);
    }    
    
    @Override
    public void mergeState(Object s) {
        Scheme otherSch = (Scheme)s;
        
        // import goals from otherSch
        otherSch.getSpec().getGoals().removeAll(spec.getGoals());
        for (Goal g: otherSch.getSpec().getGoals()) {
            if (g.getInPlan() != null) {
                addSubGoal(g.getInPlan().getTargetGoal().getId(), g.getId());
                for (String m: g.getScheme().getGoalMissionsId(g))
                    addMissionGoal(m, g.getId());
            }
        }
        
        super.mergeState(s);
    }
}
