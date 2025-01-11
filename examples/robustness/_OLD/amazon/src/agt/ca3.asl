
problem.

+obligation(Ag,_,done(_,deliverItems,Ag),_)[artifact_id(ArtId)]
	 : not problem &
	   goalArgument(_,shipOrder,"recipient",Recipient)
	<- .print("Delivering items...");
	   .send(Recipient,tell,order);
	   goalAchieved(deliverItems)[artifact_id(ArtId)].

+obligation(Ag,_,done(_,deliverItems,Ag),_)[artifact_id(ArtId)]
	 : problem
	<- .print("Problem in delivering items!");
	   goalFailed(deliverItems)[artifact_id(ArtId)].

+obligation(Ag,_,done(_,raiseItemsLost,Ag),_)[artifact_id(ArtId)]
     : .my_name(Ag) &
       goalArgument(_,shipOrder,"recipient",Recipient)
	<- .print("Raising exception for deliver items... Items lost!");
	   raiseException(itemsLost,[recipient(Recipient)])[artifact_id(ArtId)];
	   goalAchieved(raiseItemsLost).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
