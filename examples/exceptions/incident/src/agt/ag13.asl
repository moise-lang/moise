+obligation(Ag,_,done(sls_sch,provide_feedback_for_1st_level_support,Ag),_)
	 : .my_name(Ag) &
	   scheme(sls_sch,_,SchId) &
	   group(GroupName,second_level_support_group,_) &
	   play(SLM,second_level_manager,GroupName)
	<- .print("Providing feedback to first level support...");
	   .send(SLM,tell,feedback(reinstall_driver));
	   goalAchieved(provide_feedback_for_1st_level_support)[artifact_id(SchId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }