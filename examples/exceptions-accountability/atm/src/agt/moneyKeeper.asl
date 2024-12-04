// Agent sample_agent in project parseInt

+!provideMoney
	<- .print("Delivering money...");
	   giveMoney;
	   .wait(10000).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
