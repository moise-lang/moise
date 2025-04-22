+!fill_prescription : send(prescription)
	<- .print("Sending drugs to patient...");
	   .send(patient,tell,deliver(drugs)).

+!handle_exception_no_delivery_no_fill : send(prescription)
	<- .print("Handling exception NO DELIVERY, NO FILL");
	   .send(patient,tell,deliver(drugs));
	   goalAchieved(fill_prescription).

+!handle_exception_no_delivery_no_fill : not send(prescription)
	<- .print("Handling exception NO DELIVERY, NO FILL");
	   goalFailed(fill_prescription).

+!handle_exception_no_delivery : send(prescription)
	<- .print("Handling exception NO DELIVERY");
	   .send(patient,tell,deliver(drugs)).

+!raise_exception_missing_prescription
     : raised(exception_no_delivery,Args)[raiser(Patient)] & 
	   .member(date(Date),Args) & .member(doctor_name(Doctor),Args)
	<- .print("Raising exception MISSING PRESCRIPTION");
	   raiseException(exception_missing_prescription,[date(Date),patient_name(Patient)]).

+send(prescription) : goalState(_,fill_prescription,_,_,failed)
	<- .print("Sending drugs to patient...");
	   .send(patient,tell,deliver(drugs));
	   goalReleased(fill_prescription).
	   
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
