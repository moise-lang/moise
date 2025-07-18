+!ingredientsCollection
	<- .print("Providing ingredients...");
	   .print("Some ingredients are missing!");
	   goalFailed(ingredientsCollection);
	   .fail.
	   
+!notifyIngredientsShortage
	<- .print("Notifying that raspberries are missing");
	   raiseException(ingredientsShortage,[missingFillings([raspberries])]).

{ include("inc/worker-common.asl") }