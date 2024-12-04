
+!cookingTemperatureSetup
	<- .print("Switching on oven...").
	
+!firstTimeBaking
	<- .print("First time baking...").

+!secondTimeBaking
	<- .print("Second time baking...").
	

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
