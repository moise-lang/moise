+!ingredientsCollection
	<- .print("Providing ingredients...");
	   .print("Some ingredients are missing!");
	   goalFailed(ingredientsCollection);
	   .fail.
	   
+!notifyIngredientsShortage
	<- .print("Notifying that raspberries are missing... Strawberries are available instead");
	   raiseException(ingredientsShortage,[availableFillings([strawberries])]).

{ include("inc/worker-common.asl") }