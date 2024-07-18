{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

wait.

+!reachDestination
     : wait
	<- .print("Reaching destination... Some roads are closed! I will be late");
	   -wait;
	   .wait(10000).

+!reachDestination
     : not wait
	<- .print("Reaching destination...").

+oblUnfulfilled(obligation(Ag,_,done(_,reachDestination,Ag),_))
	 : not .my_name(Ag)
	<- !investigateDelay.

+!investigateDelay
	 : not account(delay,_)
	<- .print("*** REQUESTING DELAY REASON ***");
	   +accountRequestedByMe;
	   goalAchieved(requestDelayReason);
	   .wait({+account(_)});
	   !investigateDelay.

+!investigateDelay
	 : account(delay,Args) & .member(reason(roadworks),Args)
	<- .print("*** ADDING CLOSED ROADS TO IGNORE LIST... ***");
	   +ignore(I).


+!reportDelayReason : not accountRequestedByMe
	<- .print("*** REPORTING DELAY REASON... ***");
	   giveAccount(delay,[reason(roadworks),roads([mainStreet,fifthAvenue])]).


// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
