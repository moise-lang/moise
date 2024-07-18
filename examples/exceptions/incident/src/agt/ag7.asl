+obligation(Ag,_,done(fls_sch,handle_1st_level_issue,Ag),_)
	 : .my_name(Ag) &
	   scheme(fls_sch,_,SchId) &
	   group(GroupName,first_level_support_group,_) &
	   play(Flm,first_level_manager,GroupName) &
	   result(R)
	<- .print("Handling first level issue...");
	   .send(Flm,tell,result(R));
	   goalAchieved(handle_1st_level_issue)[artifact_id(SchId)].
	   
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }