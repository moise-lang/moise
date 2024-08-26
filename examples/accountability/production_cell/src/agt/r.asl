// Agent sample_agent in project parseInt

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

!request.

+!request
	<- .wait(500);
	   .print("*** PERFORMING REQUEST STOCK");
	   goalAchieved(requestRemainingStock).
	   
+goalState(_,turnTableMoveUp,_,_,failed)
	<- .print("*** PERFORMING REQUEST FAILURE");
	   goalAchieved(requestStoppedMotorNumber).
	   
+!slowDownProduction
	:	account(stock,[availablePlates(N)])
	<- .wait(1000);
	   .print("Worning, stock plates: ", N);
	   .print("*** TREATING ACCOUNT STOCK: Slowing down production...").

+!stopProduction
	:	account(stock,[availablePlates(N)])
	<- .wait(1000);
	   .print("Error, stock plates: ", N);
	   .print("*** TREATING ACCOUNT STOCK: Stopping production...").
	
+!scheduleTableMotorFix
	 : account(tableFailure,[motorNumber(N)])
	<- .wait(1000);
	   .print("*** TREATING ACCOUNT FAILURE: Scheduling fix for table motor n. ", N);
	   goalReleased(producePlate).

+!extendArm1
	<- .wait(1000);
	   .print("Extending robotic arm 1...").
	
+!grabPlateFromBelt
	<- .wait(1000);
	   .print("Grabbing plate from feed belt...").
	
+!movePlateOnPress
	<- .wait(1000);
	   .print("Moving plate on press...").
	
+!releasePlateOnPress
	<- .wait(1000);
       .print("Releasing plate on press...").
	
+!retractArm1
	<- .wait(1000);
	   .print("Retracting robotic arm 1...").
	
+!turnRobot
	<- .wait(1000);
	   .print("Turning robot...").

+!extendArm2
	<- .wait(1000);
	   .print("Extending robotic arm 2...").
	
+!grabPlateFromPress
	<- .wait(1000);
	   .print("Grabbing plate from press...").
	
+!movePlateOnBelt
	<- .wait(1000);
	   .print("Moving plate on deposit belt...").
	
+!releasePlateOnBelt
	<- .wait(1000);
	   .print("Releasing plate on deposit belt...").
	
+!retractArm2
	<- .wait(1000);
	   .print("Retracting robotic arm 2...").

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
