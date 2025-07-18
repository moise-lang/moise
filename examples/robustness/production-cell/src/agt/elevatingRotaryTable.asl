{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

+!turnTableMoveUp : not stoppedMotor(_)
	<- .print("Turning table and moving up...");
	   .print("Unable to move up!");
	   goalFailed(turnTableMoveUp);
	   +stoppedMotor(2);
	   .fail.

+!turnTableMoveUp : stoppedMotor(_)
	<- .print("Turning table and moving up...").
	
+!turnTableMoveDown
	<- .print("Turning table and moving down...").
	
+!notifyStoppedMotorNumber : stoppedMotor(N)
	<- .print("The broken motor is the number ", N);
	   raiseException(exMotor,[motorNumber(N)]).

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
