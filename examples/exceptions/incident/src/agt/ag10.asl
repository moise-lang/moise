
+obligation(Ag,_,What,_)
	 : .my_name(Ag) &
	   done(am_sch,rec,Ag)=What &
	   scheme(am_sch,_,SchId) &
	   group(GroupName,key_account_management_group,_) &
	   play(C,customer,GroupName)
	<- .print("Please recall...");
	   .send(C,tell,please_recall);
	   goalAchieved(rec)[artifact_id(SchId)]. 
	   
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }