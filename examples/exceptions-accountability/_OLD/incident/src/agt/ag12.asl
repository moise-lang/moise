+obligation(Ag,_,done(sls_sch,ask_developer,Ag),_)
	 : .my_name(Ag) &
	   group(_,developer_group,GrArtId)
	<- .print("Asking support to developer...");
	   createScheme(dev_sch, scheme_developer, SchArtId);
	   //debug(inspector_gui(on))[artifact_id(SchArtId)];
	   addScheme(dev_sch)[artifact_id(GrArtId)].
	   
+result(R)
	 : group(GroupName,second_level_support_group,_) &
	   play(SLM,second_level_manager,GroupName) &
	   scheme(sls_sch,_,SchId)
	<- .print("Feedback received from developer!");
	   .send(SLM,tell,result(R));
	   goalAchieved(ask_developer)[artifact_id(SchId)].
	   
	   
+obligation(Ag,_,done(dev_sch,root_developer,Ag),_)
	 : .my_name(Ag) &
	   scheme(dev_sch,_,SchId)
	<- .print("*** SCHEME DEVELOPER COMPLETED ***");
	   goalAchieved(root_developer)[artifact_id(SchId)].

+obligation(Ag,_,done(dev_sch,handle_developer_exception,Ag),_)
	 : .my_name(Ag) &
	   scheme(dev_sch,_,DevSchId) &
	   scheme(sls_sch,_,SlsSchId)
	<- .print("Handling developer exception... Cannot achieve ask_developer");
	   //goalAchieved(examine_problem)[artifact_id(SchId)].
	   goalFailed(ask_developer)[artifact_id(SlsSchId)];
	   goalReleased(root_developer)[artifact_id(DevSchId)];
	   goalAchieved(handle_developer_exception)[artifact_id(DevSchId)].
	   
+obligation(Ag,_,done(dev_sch,handle_developer_feedback_delay,Ag),_)
	 : .my_name(Ag) &
	   scheme(dev_sch,_,DevSchId) &
	   scheme(sls_sch,_,SlsSchId)
	<- .print("Handling developer feedback delay... Cannot achieve ask_developer");
	   goalFailed(ask_developer)[artifact_id(SlsSchId)];
	   goalReleased(root_developer)[artifact_id(DevSchId)];
	   goalAchieved(handle_developer_feedback_delay)[artifact_id(DevSchId)].
	   
+obligation(Ag,_,done(sls_sch,raise_second_level_exception,Ag),_)
	 : .my_name(Ag) &
	   scheme(sls_sch,_,SlsSchId) &
	   raised(developer_exception,[warrantyStatus(no)])
	<- .print("Raising second level exception! Product out of warranty");
	   raiseException(second_level_exception,[warrantyStatus(no)])[artifact_id(SlsSchId)];
	   goalAchieved(raise_second_level_exception)[artifact_id(SlsSchId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }