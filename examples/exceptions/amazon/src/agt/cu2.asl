
+obligation(Ag,_,done(_,retry,Ag),_)[artifact_id(ArtId)]
     : not alreadyRetried
	<- .print("Retrying once...");
	   +alreadyRetried;
	   resetGoal(checkout)[artifact_id(ArtId)].
	   
+obligation(Ag,_,done(_,retry,Ag),_)[artifact_id(ArtId)]
     : alreadyRetried
	<- .print("Failed again!");
	   goalFailed(retry)[artifact_id(ArtId)].
	   
+obligation(Ag,_,done(_,raiseCheckoutFailed,Ag),_)[artifact_id(ArtId)]
	<- .print("Raising exception checkout failed...");
	   raiseException(checkoutFailed,[])[artifact_id(ArtId)];
	   goalAchieved(raiseCheckoutFailed).
	   
+!checkout
	<- .print("Checkout completed!").

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
