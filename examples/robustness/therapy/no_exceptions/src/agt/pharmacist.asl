+!fill_prescription : send(prescription)
	<- .print("Sending drugs to patient...");
	   .send(patient,tell,deliver(drugs)).
	   
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
