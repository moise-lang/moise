
+!receiveItems
	<- .print("Waiting for items...");
	   .wait({+order});
	   .print("Order received!").

+problemInOrderDelivery
	<- goalFailed(receiveItems).

+obligation(Ag,_,done(_,raiseItemsNotReceived,Ag),_)[artifact_id(ArtId)]
     : .my_name(Ag)
	<- .print("Raising exception for receive items... Items not received!");
	   raiseException(itemsNotReceived,[])[artifact_id(ArtId)];
	   goalAchieved(raiseItemsNotReceived).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
