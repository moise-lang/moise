// common plans for participants

/* Initial goals */

!start.
!join.

/* Plans */

+!start
   <- lookupArtifact("auction",GrArtId);
      adoptRole(participant)[artifact_id(GrArtId)];
	  focus(GrArtId).
-!start
   <- .wait(100);
      !start.

+!join : joined("ora4mas",_).
+!join
   <- .wait(500);
      joinWorkspace("ora4mas",_).
-!join
   <- .wait(100);
      !join.
