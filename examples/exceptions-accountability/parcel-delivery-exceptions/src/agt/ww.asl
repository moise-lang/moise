{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

+!takeGoodsFromWarehouse
	<- .print("Taking goods from warehouse...").
	
+!packUpGoods
	<- .print("Packing up goods...").
	
+!loadOnTruck
	<- .print("Loading parcel on truck...").

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
