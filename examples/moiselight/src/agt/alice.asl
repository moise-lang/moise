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

      // add a norm to oblige tom (as a son) to commit to g2
      addNorm(obligation,son,g2);

      // plan, goals and allocation
      //
      // the dependecies in the scheme are:
      //                      g2
      //     g0 --> g1 -------^
      //     p  --- ^ --> q --^
      //
      // possible execution:
      //     (g0 || p) ; (g1 || q) ; g2
      //
      // and the allocation:
      //     p : bob (because alice ask him to do so)
      //     g0: alice (by her own initiative)
      //     g1: alice (because g0 was achieved and she is committed to it)
      //     q : alice (because p was achived   ans she is committed to it)
      //     g2: tom   (because g1 and q were achieved and he is committed to it)

      // create a new goal g1, that depends on goals g0 and p
      addGoal(g1,[g0,p]);
      addGoal(g2,[g1,q]);
      addGoal(q,[p]);

      // Alice commentments
      commitGoal(g1);
      commitGoal(q);
      debug(inspector_gui(on))[artifact_id(Sid)];

      .wait(2000);
      .print("setting g0 as achieved");
      goalAchieved(g0);

      // ask bob to achieve p
      .send(bob,achieve,p[scheme(s1)]);

      // once bob achieve p, both dependencies of g1 are satisfied and
      // alice becomes obliged to achieve g1 (see next plan)
   .

+!g1 <- .print("doing goal g1"). // triggered by obligation based on the commitment to g1
+!q  <- .print("doing goal q").

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
