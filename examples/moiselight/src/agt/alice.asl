!start.

+!start
   <- makeArtifact(tml,"ora4mas.light.LightOrgBoard",[],OIa);
      focus(OIa);
      createGroup(g1,Gid);
      focus(Gid);
      adoptRole(father);

      createScheme(s1,Sid);
      focus(Sid);
      
      // create a new goal g1, in mission m1 that depends on goals g0 and p
      addGoal(g1,m1,[g0,p]);

      // commit to mission m1 (goal g1)
      commitMission(m1);
      debug(inspector_gui(on))[artifact_id(Sid)];

      .wait(2000);
      goalAchieved(g0);

      // ask bob to achieve p
      .send(bob,achieve,p[scheme(s1)]);

      // once bob achieve p, both dependencies of g1 are satisfied and
      // alice becomes obliged to achieve g1 (see next plan)
   .

+!g1 <- .print(done_g1). // triggered by obligation based on the commitment with m1

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
