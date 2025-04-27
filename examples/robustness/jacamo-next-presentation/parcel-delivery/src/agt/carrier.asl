{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

+!unloadFromTruck
	<- .print("Unloading parcel from truck...").
	
+!bringParcelToCustomerDoor
	<- .print("Bringing parcel to customer door...").
	
+!collectProofOfDelivery
	<- .print("Collecting proof of delivery...").

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
