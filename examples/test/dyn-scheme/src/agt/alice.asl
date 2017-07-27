+!commit(Org,Sch,Mis)
   <- joinWorkspace(Org,O4MWsp);
      lookupArtifact(Sch,SchArtId)[wid(O4MWsp)];
      tmporg::focus(SchArtId)[wid(O4MWsp)];
      tmporg::commitMission(Mis);
   .

{ begin namespace(tmporg) }

+!buy_items <- .print("buy_items").
+!assist_assemble <- .print("assist_assemble").

{ end }

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl", tmporg) }
