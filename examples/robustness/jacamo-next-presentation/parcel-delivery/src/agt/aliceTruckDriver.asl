{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

+!reachDestination : not ignore(_)
	<- .print("Reaching destination...").

+!reachDestination : ignore(I)
	<- .print("Reaching destination IGNORING ",I,"...").

+oblUnfulfilled(obligation(Ag,_,done(_,reachDestination,Ag),_))
	 : not .my_name(Ag)
	<- !investigateDelay.

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


+!reportDelayReason : not accountRequestedByMe
	<- .print("*** REPORTING DELAY REASON... ***");
	   giveAccount(delay,[reason(roadworks),roads([mainStreet,fifthAvenue])]).

+!reportDelayReason : accountRequestedByMe
	<- .print("It's me who requested the account.").


// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
