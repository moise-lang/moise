// this agent waits for the group creation and then
// adopts the role writer and
// commits to the mission mColaborator

my_role(writer).

{ include("common.asl") }

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
+obligation(Ag,Norm,committed(Ag,mColaborator,Scheme),Deadline)[artifact_id(ArtId),workspace(_,_,W)]
    : .my_name(Ag)
   <- .print("I am obliged to commit to mColaborator on ",Scheme,"... doing so");
      commitMission(mColaborator)[artifact_name(Scheme), wid(W)].
+obligation(Ag,Norm,committed(Ag,Mission,Scheme),Deadline)
    : .my_name(Ag)
   <- .print("Ignoring obligation to commit to ",Mission," on ",Scheme).

{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
