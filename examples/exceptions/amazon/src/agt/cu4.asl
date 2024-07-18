
+!sendOrder
	 : group(gam,amazon_group,GrArtId) &
	   play(Ag,customerOrderReceiver,gcu)
	<- .print("Sending order to Amazon...");
	   addOrder(Ag);
	   createScheme(amsch, amazon_sch, SchArtId);
	   addScheme(amsch)[artifact_id(GrArtId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
