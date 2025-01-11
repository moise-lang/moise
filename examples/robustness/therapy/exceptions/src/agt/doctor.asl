+!prescribe : request(prescription)
	<- .print("Sending prescription to pharmacist...");
	   //.send(pharmacist,tell,send(prescription)).
	   .

+!handle_exception_lost_request : request(prescription)
	<- .print("Handling exception LOST REQUEST");
	   .send(pharmacist,tell,send(prescription));
	   goalAchieved(prescribe).

+!handle_exception_missing_prescription : request(prescription)
	<- .print("Handling exception MISSING PRESCRIPTION");
	   .send(pharmacist,tell,send(prescription)).
	   
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
