+!consult
	<- .print("I'm ill. Requesting a prescription to the doctor...");
	   .send(doctor,tell,request(prescription)).

+!follow_therapy : deliver(drugs)
	<- .print("*** DRUGS RECEIVED! I can start the therapy.").

+!follow_therapy : not deliver(drugs)
	<- .wait({+deliver(drugs)});
	   !follow_therapy.

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
