+obligation(Ag,_,done(am_sch,get_description,Ag),_)
	 : .my_name(Ag) &
	   scheme(am_sch,_,SchId) &
	   group(GroupName,key_account_management_group,_) &
	   play(C,customer,GroupName)
	<- .print("Getting description...");
	   .send(C,tell,ask_description).

+description(D)
	 : scheme(am_sch,_,SchId) &
	   group(GroupName,key_account_management_group,_) &
	   play(Am,key_account_manager,GroupName)
	<- .print("Description received!");
	   .send(Am,tell,description(D));
	   goalAchieved(get_description)[artifact_id(SchId)].
	   
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }