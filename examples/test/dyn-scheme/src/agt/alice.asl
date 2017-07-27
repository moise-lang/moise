+!commit(Org,Sch,Mis)
   <- joinWorkspace(Org,O4MWsp);
      lookupArtifact(Sch,SchArtId)[wid(O4MWsp)];
      focus(SchArtId)[wid(O4MWsp)];
      commitMission(Mis)[wid(O4MWsp)];
   .

+!buy_items <- .print("buy_items").
+!assist_assemble <- .print("assist_assemble").

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
