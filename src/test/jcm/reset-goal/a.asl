count(0).

+!goal2 <- .print(2).
+!goal4 <-
    for (.range(I,1,10)) {
        .print("  loop for goal4 ",I);
        .wait(500);
    }.

+goalState(s,goal3,_,_,satisfied) : count(5)
   <- .print("Enough");
      .wait(15000);
      .stopMAS.

+goalState(s,goal3,_,_,satisfied) : count(N)
   <- .wait(1000);
      -+count(N+1);
      .print("reseting goal 2");
      resetGoal(goal2).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
