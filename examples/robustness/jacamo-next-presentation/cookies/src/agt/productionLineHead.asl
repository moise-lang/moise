
+!doughMixingAndFeeding
	<- .print("Mixing ingredients...").

+!cutting
	<- .print("Cutting tarts...").
	   
+!jamFilling
	<- .print("Filling tarts with jam...").

+!packaging
	<- .print("Packaging tarts...").

+!dealWithIngredientsShortage
     : raised(ingredientsShortage,Args) &
       .member(missingFillings(I),Args) &
       .member(raspberries,I)
	<- .print("I will use ",strawberries," instead of ",raspberries);
	   goalReleased(ingredientsCollection).

+!notifyProblemWithOven
	<- .print("FIRE! FIRE! FIRE!");
	   raiseException(ovenBroken,[status(fire)]).
	   //raiseException(ovenBroken,[status(noHeat)]).

{ include("inc/worker-common.asl") }
