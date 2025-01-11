
+!takePayment
	 : balance(B) &
	   B - 500 >= 0 &
	   play(Ag,creditCardEmployee3,gcc)
	<- -balance(B);
	   .print("Taking payment...");
	   takePayment(700);
	   .send(Ag,tell,result(ok)).

+!takePayment
	 : balance(B) &
	   B - 700 < 0 &
	   play(Ag,creditCardEmployee3,gcc)
	<- -balance(B);
	   .print("Balance insufficient!");
	   .send(Ag,tell,result(ko)).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
