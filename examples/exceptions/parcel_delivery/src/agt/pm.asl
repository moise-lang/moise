{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

!start.

+!start
     : group(_,parcel_delivery_group,GrArtId)
	<- .print("A parcel must be delivered!");
	   createScheme(psch1, parcel_delivery_scheme, SchArtId);
	   debug(inspector_gui(on))[artifact_id(SchArtId)];
	   addScheme(psch1)[artifact_id(GrArtId)].
	   
+!closeOrder
	 : not second
	<- .print("Closing order...");
	   .print("Another parcel must be delivered!");
	   +second;
	   createScheme(psch2, parcel_delivery_scheme, SchArtId);
	   debug(inspector_gui(on))[artifact_id(SchArtId)];
	   addScheme(psch2)[artifact_id(GrArtId)].
	   
+!closeOrder
	 : second
	<- .print("Closing order...");
	   .print("*** ALL DONE ***").

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
