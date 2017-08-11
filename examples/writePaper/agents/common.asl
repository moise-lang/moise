/* Initial goals */

!play.
!join.

/* Plans */

+!play
   <- lookupArtifact("mypaper",GrId);
      ?my_role(R);
      adoptRole(R)[artifact_id(GrId)];
      focus(GrId).
-!play
   <- //.print("waiting to play a role");
      .wait(100);
      !play.

+!join : joined("ora4mas",_).
+!join
   <- joinWorkspace("ora4mas",_).
-!join
   <- .print("waiting to join ora4mas");
      .wait(200);
      !join.

+!quit_mission(M,S)
   <- .print("leaving my mission ",M," on ",S,"....");
      leaveMission(M)[artifact_name(S)].

