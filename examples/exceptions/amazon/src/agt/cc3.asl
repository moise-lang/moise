
+!sendResult[scheme(Sch)]
     : result(R)
	<- .print("Sending result to customer...");
	   getCreditCardOwner(Owner);
	   .send(Owner,tell,result(R,Sch)).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
