+!consult
	<- .print("I'm ill. Requesting a prescription to the doctor...");
	   .send(doctor,tell,request(prescription)).

+!follow_therapy : deliver(drugs)
	<- .print("*** DRUGS RECEIVED! I can start the therapy.").

+!follow_therapy : not deliver(drugs)
	<- .wait({+deliver(drugs)});
	   !follow_therapy.

+!raise_exception_lost_request
	<- .print("Raising exception LOST REQUEST");
	   raiseException(exception_lost_request,[date("23.03.2023")]).

+!raise_exception_no_delivery
	<- .print("Raising exception NO DELIVERY");
	   raiseException(exception_no_delivery,[doctor_name(doctor),date("23.03.2023")]).

+deliver(drugs)
	<- !follow_therapy.

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
