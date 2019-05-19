!start.

+!start
   <- makeArtifact(tml,"ora4mas.light.LightOrgBoard",[],OIa);
      focus(OIa);
      createGroup(g1,Gid);
      focus(Gid);
      adoptRole(father);

      // ask tom to play son
      .send(tom,achieve,adopt_role(son,g1));

      createScheme(s1,Sid);
      focus(Sid);
      addScheme(s1);

      // create a new goal g1, that depends on goals g0 and p
      addGoal(g1,[g0,p]);

      addGoal(g2,[g1]);

      // commit to goal g1)
      commitGoal(g1);
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
