{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

+!locateAddress
	<- .print("Locating address...").

+!planPath
     : not ignore(I)
	<- .print("Planning path...").
	
+!planPath
     : ignore(I) &
       group(GroupName,delivery_group,_) &
	   .findall(X,play(X,truckDriver,GroupName),L) &
	   play(T,truckDriver,GroupName)
	<- .print("Planning path, IGNORING ROADS ",I);
	   for ( .member(T,L) ) {
        .print("Notifying ",T,"...");
		.send(T,tell,alternativePath);
	}.

// TREATING THE ACCOUNT
// +!updateGlobalMap
//      : account(delay,Args) & .member(roads(I),Args)
// 	<- .print("*** Adding closed roads to ignore list...");
// 	   +ignore(I).

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
