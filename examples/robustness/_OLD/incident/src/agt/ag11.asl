
+obligation(Ag,_,done(sls_sch,handle_2nd_level_issue,Ag),_)
	 : .my_name(Ag) &
	   scheme(sls_sch,_,SchId) &
	   group(GroupName,second_level_support_group,_) &
	   play(Slm,second_level_manager,GroupName) &
	   unsure(yes)
	<- .print("Handling second level issue...");
	   .send(Slm,tell,unsure(yes));
	   goalAchieved(handle_2nd_level_issue)[artifact_id(SchId)].
	   
+obligation(Ag,_,done(sls_sch,handle_2nd_level_issue,Ag),_)
	 : .my_name(Ag) &
	   scheme(sls_sch,_,SchId) &
	   group(GroupName,second_level_support_group,_) &
	   play(Slm,second_level_manager,GroupName) &
	   unsure(no) &
	   result(R)
	<- .print("Handling second level issue...");
	   .send(Slm,tell,unsure(no));
	   .send(Slm,tell,result(R));
	   goalAchieved(handle_2nd_level_issue)[artifact_id(SchId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }