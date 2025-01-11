{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

satisfied(G) :- goalState(_,G,_,_,satisfied).

+!sell : not account(flourType(T)) & not satisfied(getAuthorization)
	<- .print("Getting authorization...");
	   goalAchieved(getAuthorization);
	   !sell.

+!sell : not account(ftBaker,[flourType(T)])
	<- .print("Requesting account to baker...");
	   goalAchieved(requestFlourTypeToBaker);
	   .wait({+account(ftBaker,_)});
	   !sell.

+!sell : account(ftBaker,[flourType(organic)])
	<- .print("Selling at a HIGHER price...").

+!sell : account(ftBaker,[flourType(normal)])
	<- .print("Selling at a LOWER price...").

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
