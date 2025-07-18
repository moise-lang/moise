{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

+!scheduleTableMotorFix
	:	raised(exMotor, Args) & .member(motorNumber(N), Args)
	<- .print("Scheduling table motor fix for motor n. ", N);
	   .wait(5000);
	   //.wait({+motorFixed});
	   goalReleased(turnTableMoveUp);
	   .print("Problem solved in motor ", N, "! Production resumed").

+!replaceERT
	<- .print("Replacing elevating rotary table... ");
	   .wait(5000);
	   	.print("ERT replaced! Production resumed");
	   resetGoal(turnTableMoveUp).

+oblUnfulfilled(obligation(_,_,done(_,conveyPlateToTable,_),_))
	<- .print("Requesting remaining stock...");
	   goalAchieved(requestRemainingStock).

+!pauseProduction
	:	raised(exMotor,[motorNumber(N)])
	<- .print("Pausing production due to a problem on motor ", N, "...");
	   .wait(5000);
	   goalReleased(turnTableMoveUp);
	   .print("Problem solved in motor ", N, "! Production resumed").

+!slowDownProduction
     : account(stock,[availablePlates(N)]) &
       N >= 5
	<- .print("Setting production speed to 70%...");
	   .broadcast(tell, slowdown).
	
+!slowDownProduction
     : account(stock,[availablePlates(N)]) &
       N >= 2
	<- .print("Setting production speed to 30%...");
	   .broadcast(tell, slowdown).
	
+!stopProduction
     : account(stock,[availablePlates(0)])
	<- .print("Temporarily stopping production...");
	   .broadcast(tell, stop).

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
