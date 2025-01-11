
+!leaveBuildingImmediately
	<- .print("Leaving building!").
	   
+!call911
	<- .print("Calling 911!").
	   
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
