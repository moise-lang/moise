{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

+!movePressDownUp
	<- .print("Can't move the press");
	   .fail.
	   
+!explainSlowdownReason
	<- .print("A human operator is in the dangerous area!");
	   raiseException(exHuman,[slowdownCode(ux57),humanCoords(1,2)]).

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
