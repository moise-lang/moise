+problem
	 : group(_,key_account_management_group,GrArtId)
	<- .print("Customer has a problem! Starting Key Account Management process...");
	   createScheme(am_sch, scheme_account_manager, SchArtId);
	   //debug(inspector_gui(on))[artifact_id(SchArtId)];
	   addScheme(am_sch)[artifact_id(GrArtId)].

+obligation(Ag,_,done(am_sch,can_handle,Ag),_)
	 : .my_name(Ag) &
	   description(easy_problem) &
	   scheme(am_sch,_,SchId)
	<- .print("The problem can be handled at this level!");
	   goalAchieved(can_handle)[artifact_id(SchId)].
	   
+obligation(Ag,_,done(am_sch,cannot_handle,Ag),_)
	 : .my_name(Ag) &
	   description(hard_problem) &
	   scheme(am_sch,_,SchId)
	<- .print("The problem cannot be handled at this level! First level support needed");
	   goalAchieved(cannot_handle)[artifact_id(SchId)].
	   
+obligation(Ag,_,done(am_sch,root_account_manager,Ag),_)
	 : .my_name(Ag) &
	   scheme(am_sch,_,SchId)
	<- .print("*** SCHEME KEY ACCOUNT MANAGER COMPLETED ***");
	   goalAchieved(root_account_manager)[artifact_id(SchId)].
	   
+obligation(Ag,_,done(am_sch,cancel_problem_request,Ag),_)
	 : .my_name(Ag) &
	   scheme(am_sch,_,AmSchId) &
	   raised(account_manager_exception,[warrantyStatus(no)]) &
	   group(GroupName,key_account_management_group,_) &
	   play(C,customer,GroupName)
	<- .print("Handling account manager exception... The product is out of warranty, canceling problem request");
	   .send(C,tell,problem_request_canceled);
	   goalReleased(root_account_manager)[artifact_id(AmSchId)];
	   goalAchieved(cancel_problem_request)[artifact_id(AmSchId)].
	   
+obligation(Ag,_,done(am_sch,invite_to_recall,Ag),_)
	 : .my_name(Ag) &
	   scheme(am_sch,_,AmSchId) &
	   group(GroupName,key_account_management_group,_) &
	   play(C,customer,GroupName)
	<- .print("Handling account manager timeout... Asking customer to recall");
	   .send(C,tell,please_recall);
	   goalReleased(root_account_manager)[artifact_id(AmSchId)];
	   goalAchieved(invite_to_recall)[artifact_id(AmSchId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }