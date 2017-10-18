
!start.

+!start
   <- createWorkspace(wbob);
      joinWorkspace(wbob,O4MWsp);

      makeArtifact(myorg, "ora4mas.nopl.OrgBoard", ["src/org/org.xml"], OrgArtId)[wid(O4MWsp)];
      tmporg::focus(OrgArtId);
      tmporg::createScheme(s1, scheme1, SchArtId)[wid(O4MWsp)];
      s1::focus(SchArtId)[wid(O4MWsp)];
      //tmporg::debug(inspector_gui(on));
      s1::commitMission(missionB);

      .wait(500);
      .print("Sending state to Alice");

      !s1::send_update(alice);
   .

+!s1::goal2 <- .print("doing g2").
+!s1::goal4 <- .print("doing g4").

+s1::goalState(_,goal1,_,_,satisfied)
   <- .print("*** all done! ***").

{ include("sync-sch.asl", s1) }

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl", s1) }
