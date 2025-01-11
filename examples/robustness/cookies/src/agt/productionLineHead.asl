+!dealWithIngredientsShortage
     : raised(ingredientsShortage,Args) &
       .member(availableFillings(I),Args) &
       .member(strawberries,I)
	<- .print("I will use ",strawberries," instead of ",raspberries);
	   goalReleased(ingredientsCollection).
	   
+!doughMixingAndFeeding
	<- .print("Mixing ingredients...").

+!cutting
	<- .print("Cutting tarts...").
	   
+!jamFilling
	<- .print("Filling tarts with jam...").

+!packaging
	<- .print("Packaging tarts...").

{ include("inc/worker-common.asl") }
