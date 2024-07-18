+obligation(Ag,_,done(fls_sch,ask_2nd_level_support,Ag),_)
	 : .my_name(Ag) &
	   group(_,second_level_support_group,GrArtId)
	<- .print("Asking support to second level...");
	   createScheme(sls_sch, scheme_2nd_level_support, SchArtId);
	   //debug(inspector_gui(on))[artifact_id(SchArtId)];
	   addScheme(sls_sch)[artifact_id(GrArtId)].
	   
+obligation(Ag,_,done(sls_sch,unsure_no,Ag),_)
	 : .my_name(Ag) &
	   unsure(no) &
	   scheme(sls_sch,_,SchId)
	<- .print("I'm sure about the solution!");
	   goalAchieved(unsure_no)[artifact_id(SchId)].
	   
+obligation(Ag,_,done(sls_sch,unsure_yes,Ag),_)
	 : .my_name(Ag) &
	   unsure(yes) &
	   scheme(sls_sch,_,SchId)
	<- .print("I'm unsure! Better ask developer");
	   goalAchieved(unsure_yes)[artifact_id(SchId)].
	   
+obligation(Ag,_,done(sls_sch,result_solved,Ag),_)
	 : .my_name(Ag) &
	   result(solved) &
	   scheme(sls_sch,_,SchId)
	<- .print("Issue resolved");
	   goalAchieved(result_solved)[artifact_id(SchId)].
	   
+obligation(Ag,_,done(sls_sch,result_fix_in_next_release,Ag),_)
	 : .my_name(Ag) &
	   result(next_release) &
	   scheme(sls_sch,_,SchId)
	<- .print("Issue must be fixed in next release");
	   goalAchieved(result_fix_in_next_release)[artifact_id(SchId)].
	   
+feedback(F)
	 : group(GroupName,first_level_support_group,_) &
	   play(FLW,first_level_worker_3,GroupName) &
	   scheme(fls_sch,_,SchId)
	<- .print("Feedback received from second level support!");
	   .send(FLW,tell,feedback(F));
	   goalAchieved(ask_2nd_level_support)[artifact_id(SchId)].
	   
+obligation(Ag,_,done(sls_sch,root_2nd_level_support,Ag),_)
	 : .my_name(Ag) &
	   scheme(sls_sch,_,SchId)
	<- .print("*** SCHEME SECOND LEVEL SUPPORT COMPLETED ***");
	   goalAchieved(root_2nd_level_support)[artifact_id(SchId)].
	   
+obligation(Ag,_,done(sls_sch,handle_second_level_exception,Ag),_)
	 : .my_name(Ag) &
	   scheme(sls_sch,_,SlsSchId) &
	   scheme(fls_sch,_,FlsSchId)
	<- .print("Handling second level exception... Cannot achieve ask_2nd_level_support");
	   goalFailed(ask_2nd_level_support)[artifact_id(FlsSchId)];
	   goalReleased(root_2nd_level_support)[artifact_id(SlsSchId)];
	   goalAchieved(handle_second_level_exception)[artifact_id(SlsSchId)].
	   
+obligation(Ag,_,done(fls_sch,raise_first_level_exception,Ag),_)
	 : .my_name(Ag) &
	   scheme(fls_sch,_,FlsSchId) &
	   raised(second_level_exception,[warrantyStatus(no)])
	<- .print("Raising first level exception! Product out of warranty");
	   raiseException(first_level_exception,[warrantyStatus(no)])[artifact_id(FlsSchId)];
	   goalAchieved(raise_first_level_exception)[artifact_id(FlsSchId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }