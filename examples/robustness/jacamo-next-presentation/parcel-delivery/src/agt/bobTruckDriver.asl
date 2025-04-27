{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

wait.

+!reachDestination
     : wait
	<- .print("Reaching destination... Some roads are closed! I will be late");
	   -wait;
	   .wait(10000).

+!reachDestination
     : not wait & alternativePath
	<- .print("Reaching destination ON ALTERNATIVE PATH...").

+!reportDelayReason : not accountRequestedByMe
	<- .print("*** REPORTING DELAY REASON... ***");
	   giveAccount(delay,[reason(roadworks),roads([mainStreet,fifthAvenue])]).

-obligation(Ag,_,What,_)
   :  .my_name(Ag) & (satisfied(Scheme,Goal)=What | done(Scheme,Goal,Ag)=What)
   <- .print("I am not obliged to achieve ",Goal," for scheme ",Scheme,", but I will do it anyway!").

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
