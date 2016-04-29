// this agent waits for the group creation and then
// adopts the role write and
// commits to the mission mColaborator

{ include("common.asl") }

/* Initial goals */

/* Plans */

// application domain goals
+!wsecs[scheme(S)]
   <- .print("writing sections for scheme ",S,"...").

// when my goal in the scheme is satisfied, leave my mission 
@lqm[atomic]
+goalState(Scheme,wsecs,_,_,satisfied)
    : .my_name(Me) & commitment(Me,mColaborator,Scheme)
   <- !quit_mission(mColaborator, Scheme).


// plans to handle obligations

// only commits to mColaborator!
+obligation(Ag,Norm,committed(Ag,mColaborator,Scheme),DeadLine)
    : .my_name(Ag)
   <- .print("I am obliged to commit to ",mColaborator);
      commitMission(mColaborator)[artifact_name(Scheme)].
      
+obligation(Ag,Norm,achieved(Scheme,Goal,Ag),DeadLine)
    : .my_name(Ag)
   <- .print("I am obliged to achieve goal ",Goal);
      !Goal[scheme(Scheme)];
      goalAchieved(Goal)[artifact_name(Scheme)].
             