+obligation(Ag,_,done(am_sch,ask_1st_level_support,Ag),_)
	 : .my_name(Ag) &
	   scheme(am_sch,_,SchId) &
	   group(_,first_level_support_group,GrArtId)
	<- .print("Asking support to first level...");
	   createScheme(fls_sch, scheme_1st_level_support, SchArtId);
	   //debug(inspector_gui(on))[artifact_id(SchArtId)];
	   addScheme(fls_sch)[artifact_id(GrArtId)].
	   
+feedback(F)
	 : group(GroupName,key_account_management_group,_) &
	   play(AMW,key_account_worker_3,GroupName) &
	   scheme(am_sch,_,SchId)
	<- .print("Feedback received from first level support!");
	   .send(AMW,tell,feedback(F));
	   goalAchieved(ask_1st_level_support)[artifact_id(SchId)].	   

+obligation(Ag,_,done(fls_sch,result_solved,Ag),_)
	 : .my_name(Ag) &
	   result(solved) &
	   scheme(fls_sch,_,SchId)
	<- .print("Problem solved!");
	   goalAchieved(result_solved)[artifact_id(SchId)].
	   
+obligation(Ag,_,done(fls_sch,result_2nd_level_issue,Ag),_)
	 : .my_name(Ag) &
	   result(unsolved) &
	   scheme(fls_sch,_,SchId)
	<- .print("The problem has not been solved! Second level support needed");
	   goalAchieved(result_2nd_level_issue)[artifact_id(SchId)].

+obligation(Ag,_,done(fls_sch,root_1st_level_support,Ag),_)
	 : .my_name(Ag) &
	   scheme(fls_sch,_,SchId)
	<- .print("*** SCHEME FIRST LEVEL SUPPORT COMPLETED ***");
	   goalAchieved(root_1st_level_support)[artifact_id(SchId)].
	   
+obligation(Ag,_,done(fls_sch,handle_first_level_exception,Ag),_)
	 : .my_name(Ag) &
	   scheme(fls_sch,_,FlsSchId) &
	   scheme(am_sch,_,AmSchId)
	<- .print("Handling first level exception... Cannot achieve ask_1st_level_support");
	   goalFailed(ask_1st_level_support)[artifact_id(AmSchId)];
	   goalReleased(root_1st_level_support)[artifact_id(FlsSchId)];
	   goalAchieved(handle_first_level_exception)[artifact_id(FlsSchId)].
	   
+obligation(Ag,_,done(am_sch,raise_account_manager_exception,Ag),_)
	 : .my_name(Ag) &
	   scheme(am_sch,_,AmSchId) &
	   raised(first_level_exception,[warrantyStatus(no)])
	<- .print("Raising account manager exception! Product out of warranty");
	   raiseException(account_manager_exception,[warrantyStatus(no)])[artifact_id(AmSchId)];
	   goalAchieved(raise_account_manager_exception)[artifact_id(AmSchId)].
	   
+obligation(Ag,_,done(am_sch,raise_account_manager_timeout,Ag),_)
	 : .my_name(Ag) &
	   scheme(am_sch,_,AmSchId)
	<- .print("Raising account manager timeout!");
	   raiseException(account_manager_timeout,[])[artifact_id(AmSchId)];
	   goalAchieved(raise_account_manager_timeout)[artifact_id(AmSchId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }