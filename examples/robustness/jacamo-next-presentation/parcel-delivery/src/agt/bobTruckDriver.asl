{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

wait.

+!reachDestination
     : wait
	<- .print("Reaching destination... Some roads are closed! I will be late");
	   -wait;
	   +alternativePath;
	   .wait(5000).

+!reachDestination
     : not wait & alternativePath
	<- .print("Reaching destination ON ALTERNATIVE PATH...").


// GIVING THE ACCOUNT
+!reportDelayReason : not accountRequestedByMe
	<- .print("*** REPORTING DELAY REASON... ***");
	   giveAccount(delay,[reason(roadworks),roads([mainStreet,fifthAvenue])]).

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
