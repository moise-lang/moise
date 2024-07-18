+obligation(Ag,_,What,_)
	 : .my_name(Ag) &
	   done(dev_sch,pff2ls,Ag)=What &
	   scheme(dev_sch,_,SchId) &
	   result(solved)
	<- .print("Providing feedback to second level support...");
	   goalAchieved(pff2ls)[artifact_id(SchId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }