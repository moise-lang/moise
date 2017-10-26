+!goal2 <- .print(2).
+!goal4 <- .print(4).

+goalState(s,goal1,_,_,satisfied)
   <- .wait(1000);
      .print("reseting goal 2");
      resetGoal(goal2).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
