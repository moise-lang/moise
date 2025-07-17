+!cookingTemperatureSetup
	<- .print("Switching on oven...").
	
+!firstTimeBaking
	<- .print("First time baking...");
	   .print("There is a problem with the oven!");
	   goalFailed(firstTimeBaking);
	   .fail.

+!secondTimeBaking
	<- .print("Second time baking...").



{ include("inc/worker-common.asl") }
