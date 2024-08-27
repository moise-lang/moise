// Agent sample_agent in project parseInt

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

+!conveyPlateToTable
	<- .wait(1000);
	   .print("Conveying plate to table...").
	
+!notifyRemainingStock
	<- .wait(1000);
	   .print("*** GIVING ACCOUNT STOCK: Available plates ",0);
	   giveAccount(stock,[availablePlates(0)]).

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
