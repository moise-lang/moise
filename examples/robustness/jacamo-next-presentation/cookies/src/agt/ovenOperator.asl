+!cookingTemperatureSetup
	<- .print("Switching on oven...").
	
+!firstTimeBaking
	<- .print("First time baking...");
	   .print("There is a problem with the oven!");
	   +fire;
	   goalFailed(firstTimeBaking);
	   .fail.

+!secondTimeBaking
	<- .print("Second time baking...").

+!notifyProblemWithOven : fire
	<- .print("FIRE! FIRE! FIRE!");
	   raiseException(ovenBroken,[status(fire)]).

{ include("inc/worker-common.asl") }
