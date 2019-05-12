!start.

+!start
   <- makeArtifact(tml,"ora4mas.light.LightOrgBoard",[],OIa);
      focus(OIa);
      createGroup(g1,Gid);
      focus(Gid);
      adoptRole(father);

      createScheme(s1,Sid);
      focus(Sid);
      addGoal(g1,m1,[g0,p]);
      commitMission(m1);
      debug(inspector_gui(on))[artifact_id(Sid)];

      .wait(2000);
      goalAchieved(g0);

      // ask bob to achieve p
      .send(bob,achieve,p[scheme(s1)]);
   .

+!g1 <- .print(done_g1). // triggered by obligation based on the commitment with m1

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
