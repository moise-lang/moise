+obligation(Ag,_,done(dev_sch,provide_feedback_for_2nd_level_support,Ag),_)
	 : .my_name(Ag) &
	   scheme(dev_sch,_,DevSchId) &
	   result(R) &
	   (R=solved | R=next_release) & 
	   group(GroupName,developer_group,_) &
	   play(DM,developer_manager,GroupName)
	<- .print("Providing feedback to second level support...");
	   .send(DM,tell,result(R));
	   goalAchieved(provide_feedback_for_2nd_level_support)[artifact_id(DevSchId)].
	   
+obligation(Ag,_,done(dev_sch,raise_developer_feedback_delay,Ag),_)
	 : .my_name(Ag) &
	   scheme(dev_sch,_,DevSchId)
	<- .print("I'm late in providing feedback to second level support!");
	   raiseException(developer_feedback_delay,[reason(too_much_work)])[artifact_id(SchId)];
	   goalAchieved(raise_developer_feedback_delay)[artifact_id(DevSchId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }