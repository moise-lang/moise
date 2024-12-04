{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

+!heatOven
	<- .print("Heating oven...").

+!bake
	<- .print("Baking bread...").

+!obligation(Ag,_,done(_,notifyFlourTypeToSeller,Ag),_) : my_name(Ag)
	<- !notifyFlourTypeToSeller;
	   goalAchieved(notifyFlourTypeToSeller).

+!notifyFlourTypeToSeller : account(ftKneader,[flourType(FT)])
	<- .print("Giving account to seller...");
	   giveAccount(ftBaker,[flourType(FT)]).

+!notifyFlourTypeToSeller : not account(ftKneader,[flourType(FT)])
	<- .print("Requesting account to kneader...");
	   goalAchieved(requestFlourTypeToKneader);
	   .wait({+account(ftKneader,_)});
	   !notifyFlourTypeToSeller.

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
