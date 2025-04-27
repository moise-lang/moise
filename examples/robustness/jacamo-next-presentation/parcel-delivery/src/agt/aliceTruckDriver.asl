{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

+!reachDestination : not ignore(_)
	<- .print("Reaching destination...").

+goalState(_,reachDestination,_,[Ag],enabled)
	 : .my_name(Ag)
	<- .print("I arrived before Bob! Investigating the delay...");
	   !investigateDelay.

+!investigateDelay
	 : not account(delay,_)
	<- .print("*** REQUESTING DELAY REASON ***");
	   +accountRequestedByMe;
	   goalAchieved(requestDelayReason);
	   .wait({+account(delay,_)});
	   !investigateDelay.

+!investigateDelay
	 : account(delay,Args) & .member(reason(roadworks),Args) & .member(roads(I),Args)
	<- .print("*** ADDING CLOSED ROADS TO IGNORE LIST... ***");
	   +ignore(I).

+!reachDestination : ignore(I)
	<- .print("Reaching destination IGNORING ",I,"...").

+!reportDelayReason : accountRequestedByMe
	<- .print("It's me who requested the account.").


// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
