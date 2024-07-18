+obligation(Ag,_,What,_)
	 : .my_name(Ag) &
	   done(fls_sch,provide_feedback_for_account_manager,Ag)=What &
	   scheme(fls_sch,_,SchId) &
	   group(GroupName,first_level_support_group,_) &
	   play(FLM,first_level_manager,GroupName) &
	   feedback(F)
	<- .print("Providing feedback to key account manager...");
	   .send(FLM,tell,feedback(F));
	   goalAchieved(provide_feedback_for_account_manager)[artifact_id(SchId)].

+obligation(Ag,_,What,_)
	 : .my_name(Ag) &
	   done(fls_sch,provide_feedback_for_account_manager,Ag)=What &
	   scheme(fls_sch,_,SchId) &
	   group(GroupName,first_level_support_group,_) &
	   play(FLM,first_level_manager,GroupName) &
	   not feedback(F)
	<- .print("Providing feedback to key account manager...");
	   .send(FLM,tell,feedback(reinstall_drivers));
	   goalAchieved(provide_feedback_for_account_manager)[artifact_id(SchId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }