/*

Agent alice wants to coordinate some tasks with bob and tom.

The goals for the tasks are: g0, g1, g2, p, q

With the following dependecies:

                       g2 (or)
  g0 ----> g1 --------- ^
           ^            |
           |            |
           |            |
  p ------ ^ --> q ---- ^

g1 depends on g0 and p
q  depends on p
g2 depends on g1 or  q

possible executions:
    (g0 || p) ; (g1 || q ) ; g2
    (g0 || p) ;  q  ; (g1 || g2)

and the allocation:
    p : bob   (because alice asks him to do so)
    g0: alice (by her own initiative)
    g1: alice (because g0 was achieved and she is committed to it)
    q : alice (because p was achived   and she is committed to it)
    g2: tom   (because g1 and q were achieved and he is committed to it)

*/


!start.

+!start
   <- makeArtifact(tml,"ora4mas.simple.SimpleOrgBoard",[],OIa); // NB.: the implementation of the OrgBoard is different and does not require a XML file
      focus(OIa);
      createGroup(grp1,Gid);
      focus(Gid);
      adoptRole(mother); // The role mother will be created before the adoption

      // ask tom to play son
      .send(tom,achieve,adopt_role(son,grp1));

      createScheme(s1,Sid);
      focus(Sid);
      addScheme(s1);

      // create the graph of dependencies
      addGoal(g1,dep(and,[g0,p]));
      addGoal(g2,dep(or, [g1,q]));
      addGoal(q, dep(and,[p]));

      // add a norm to oblige tom (as a son) to commit to g2
      addNorm(obligation,son,g2);

      // Alice committments
      commitGoal(g1); // new operation of moise simple (avoids missions)
      commitGoal(q);
      //debug(inspector_gui(on))[artifact_id(Sid)];

      .wait(2000);
      .print("setting g0 as achieved");
      goalAchieved(g0);

      // ask bob to achieve p
      .send(bob,achieve,p[scheme(s1)]);

      // once bob achieve p, both dependencies of g1 are satisfied and
      // alice becomes obliged to achieve g1 (see next plan)
   .

+!g1 <- .print("doing goal g1"); .wait(1000). // triggered by obligation based on the commitment to g1
+!q  <- .print("doing goal q");  .wait(1000).

/*+goalState(s1,g2,_,_,satisfied)
   <- .print("Finished!");
       destroyScheme(s1);
       destroyGroup(grp1);
   . */

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
