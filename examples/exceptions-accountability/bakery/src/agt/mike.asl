{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

bread_flour(baguette,organic).
bread_flour(ciabatta,normal).
bread_flour(pumpernickel,multigrain).

+!knead
	<- .print("Kneading dough...");
	   +todayBread(baguette).

+!obligation(Ag,_,done(_,notifyFlourTypeToBaker,Ag),_) : my_name(Ag)
	<- !notifyFlourTypeToBaker;
	   goalAchieved(notifyFlourTypeToBaker).

+!notifyFlourTypeToBaker
     : todayBread(B) & bread_flour(B,F)
	<- .print("Giving account to baker...");
	   giveAccount(ftKneader,[flourType(F)]).
	   

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
