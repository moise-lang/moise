
+!ingredientsCollection
	<- .print("Providing ingredients...");
	   .print("Some ingredients are missing!");
	   goalFailed(ingredientsCollection);
	   .fail.
	   
+!notifyIngredientsShortage
	<- .print("Notifying that strawberries are missing...");
	   raiseException(ingredientsShortage,[availableFillings([strawberries])]).
	   
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
