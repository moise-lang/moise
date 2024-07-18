+obligation(Ag,_,done(am_sch,explain_solution,Ag),_)
	 : .my_name(Ag) &
	   scheme(am_sch,_,SchId) &
	   group(GroupName,key_account_management_group,_) &
	   play(C,customer,GroupName) &
	   feedback(F)
	<- .print("Explaining solution...");
	   .send(C,tell,solution(F));
	   goalAchieved(explain_solution)[artifact_id(SchId)].
	   
+obligation(Ag,_,done(am_sch,explain_solution,Ag),_)
	 : .my_name(Ag) &
	   scheme(am_sch,_,SchId) &
	   group(GroupName,key_account_management_group,_) &
	   play(C,customer,GroupName) &
	   not feedback(F)
	<- .print("Explaining solution...");
	   .send(C,tell,solution(reboot));
	   goalAchieved(explain_solution)[artifact_id(SchId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }