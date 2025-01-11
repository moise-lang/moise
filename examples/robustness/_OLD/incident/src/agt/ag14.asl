+obligation(Ag,_,done(sls_sch,insert_into_product_backlog,Ag),_)
	 : .my_name(Ag) &
	   done(sls_sch,pff2ls,Ag)=What &
	   scheme(sls_sch,_,SchId)
	<- .print("Inserting issue into product backlog...");
	   goalAchieved(insert_into_product_backlog)[artifact_id(SchId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }