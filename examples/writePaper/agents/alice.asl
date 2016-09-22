// this agent waits for the group creation and then
// adopts the role writer and
// commits to the missions mColaborator and mBib

my_role(writer).

{ include("common.asl") }

/* Plans */

// application domain goals
+!wsecs <- .print("writing sections...").
+!wrefs <- .print("organising bibliography...").

// conditions to leave missions
@lgss1[atomic]
+goalState(Scheme,wsecs,_,_,satisfied)
    : .my_name(Me) & commitment(Me,mColaborator,Scheme)
   <- !quit_mission(mColaborator,Scheme).
      
@lgss2[atomic]
+goalState(Scheme,wrefs,_,_,satisfied)
    : .my_name(Me) & commitment(Me,mBib,Scheme)
   <- !quit_mission(mBib,Scheme).

{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
   