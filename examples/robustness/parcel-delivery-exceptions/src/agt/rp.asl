{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

+!locateAddress
	<- .print("Locating address...").

+!planPath
     : not ignore(I)
	<- .print("Planning path...").
	
+!planPath
     : ignore(I) &
       group(GroupName,parcel_delivery_group,_) &
	   play(T,truck,GroupName)
	<- .print("Planning path, IGNORING ROADS ",I);
	   .send(T,tell,alternativePath);.
	
+!updateMap
     : raised(exParcel,Args) & .member(closedRoads(I),Args)
	<- .print("*** Adding closed roads to ignore list...");
	   +ignore(I).

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
