
+!sendToCarrier
	 : group(gca,carrier_group,GrArtId) &
	   recipient(Recipient)
	<- .print("Sending items to carrier...");
	   createScheme(casch, carrier_sch, SchArtId);
	   setArgumentValue(shipOrder,recipient,Recipient)[artifact_id(SchArtId)];
	   addScheme(casch)[artifact_id(GrArtId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
