
+!browseProducts
	<- .print("Browsing products...").

+!addItems
	<- .print("Adding items to basket...").

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
