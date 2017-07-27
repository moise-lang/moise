!start.

+!start
   <- makeArtifact(b,"tools.CreateOS", [], _);
      create("x.xml");
      .print("OS created!");

      createWorkspace(temp_org);
      joinWorkspace(temp_org,O4MWsp);

      makeArtifact(myorg, "ora4mas.nopl.OrgBoard", ["x.xml"], OrgArtId)[wid(O4MWsp)];
      tmporg::focus(OrgArtId);
      tmporg::createScheme(s1, st, SchArtId)[wid(O4MWsp)];
      tmporg::focus(SchArtId)[wid(O4MWsp)];
      //debug(inspector_gui(on))[artifact_id(SchArtId)];
      tmporg::commitMission(mag1);
      .send(alice,achieve,commit(temp_org,s1,mag2));
   .

{ begin namespace(tmporg) }

+goalState(_,job_delivered,_,_,satisfied)[artifact_id(SchArtId)]
   <- .print("*** all done! ***");
      .print("scheme will be destroyed in 10 seconds")
      .wait(10000);
      destroy[artifact_id(SchArtId)];
   .

+!go_to_buy <- .print("go_to_buy").
+!go_to_workshop <- .print("go_to_workshop").
+!do_assemble <- .print("do_assemble").
+!stop <- .print("stop").
+!deliver <- .print("deliver").
{ end }

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl", tmporg) }
