{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

!start.
!check.

+!start
  <- +start;
     .print("START").

+!check
  <- .wait(1000);
     ?start;
     .print("CHECK").

+!handleDelay
     : raised(delay,[eta(N)]) &
       N < 200
	<- .print("I wait a little bit more...").

+!handleDelay
     : raised(delay,[eta(N)]) &
       N >= 200
	<- .print("I give up.");
	   goalReleased(task).

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
