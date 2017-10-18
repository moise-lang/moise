
!start.

+!start
   <- createWorkspace(walice);
      joinWorkspace(walice,O4MWsp);

      makeArtifact(myorg, "ora4mas.nopl.OrgBoard", ["src/org/org.xml"], OrgArtId)[wid(O4MWsp)];
      tmporg::focus(OrgArtId);
      tmporg::createScheme(s2, scheme1, SchArtId)[wid(O4MWsp)];
      s2ns::focus(SchArtId)[wid(O4MWsp)];
      //tmporg::debug(inspector_gui(on));
      s2ns::commitMission(missionA);

      .wait(1000);
      !s2ns::send_update(bob);
   .

+!s2ns::goal3 <- .print("doing g3").

+s2ns::goalState(_,goal1,_,_,satisfied)
   <- .print("*** all done! ***").

{ include("sync-sch.asl", s2ns) }

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl", s2ns) }
