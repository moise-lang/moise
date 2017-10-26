count(0).

+!goal2 <- .print(2).
+!goal4 <- .print(4).

+goalState(s,goal1,_,_,satisfied) : count(5)
   <- .print("Enough");
      .stopMAS.

+goalState(s,goal1,_,_,satisfied) : count(N)
   <- .wait(1000);
      -+count(N+1);
      .print("reseting goal 2");
      resetGoal(goal2).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
