
available(raspberries).
available(chocolate).

+!dealWithIngredientsShortage
     : raised(ingredientsShortage,Args) &
       .member(availableFillings(I),Args) &
       .member(strawberries,I) &
       available(raspberries)
	<- .print("I will use ",raspberries," instead of ",strawberries);
	   goalReleased(ingredientsCollection).
	   
+!doughMixingAndFeeding
	<- .print("Mixing ingredients...").

+!cutting
	<- .print("Cutting tarts...").
	   
+!jamFilling
	<- .print("Filling tarts with jam...").

+!packaging
	<- .print("Packaging tarts...").

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
