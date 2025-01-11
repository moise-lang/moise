!start.

+!start
	<- .wait(1000);
	   !solve_a_problem.

+!solve_a_problem
     : group(GroupName,key_account_management_group,_) &
	   play(Am,key_account_manager,GroupName)
	<- .send(Am, tell, problem).

+ask_description[source(Sender)]
	 : description(D)
	<- .print("Sending description...");
	   .send(Sender,tell,description(D)).
	   
+solution(S)
	<- .print("Problem solved through ", S, ". Thank you!").
	
+problem_request_canceled
	<- .print("Too bad =(").
		
+please_recall
	<- .print("I'll recall later.'").

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }