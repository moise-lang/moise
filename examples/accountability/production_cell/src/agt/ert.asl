{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

motorFault(1).

+!turnTableMoveUp
	 : motorFault(N)
	<- .wait(1000);
	   .print("Turning table and moving up...");
	   .print("*** GOAL FAILED");
	   goalFailed(turnTableMoveUp);
	   .fail.

+!turnTableMoveUp
     : not motorFault(N)
	<- .wait(1000);
	   .print("Turning table and moving up...").
	
+!turnTableMoveDown
	<- .wait(1000);
	   .print("Turning table and moving down...").
	
+!notifyStoppedMotorNumber
	 : motorFault(N)
	<- .wait(1000);
	   .print("*** GIVING ACCOUNT FAILURE");
	   giveAccount(tableFailure,[motorNumber(1)]).

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
