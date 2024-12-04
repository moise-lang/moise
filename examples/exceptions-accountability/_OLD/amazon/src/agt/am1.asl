
+!receiveOrder
	 : play(Ag,amazonWorker5,gam)
	<- .print("Receiving new order...");
	   getOrderRecipient(Recipient);
	   .send(Ag,tell,recipient(Recipient)).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
