+!parseAmount
	<- .print("Parsing amount...");
	   .wait(2000);
	   parseAmount.

-!parseAmount[env_failure_reason(firstNaNIndex(Value))]
	<- .print("The inserted string is not a number!");
	   +firstNaNIndex(Value);
	   goalFailed(parseAmount);
	   .fail.

+!raiseNan
     : firstNaNIndex(I)
	<- .wait(2000);
	   .print("Raising exception NAN: ", index(I));
	   raiseException(nan,[index(I)]);
	   -firstNaNIndex(I).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
