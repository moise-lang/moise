// common plans for participants

/* Initial goals */

!start.
!join.

/* Plans */

+!start 
   <- lookupArtifact("auction",GrArtId); 
      adoptRole(participant);
	  focus(GrArtId).
-!start
   <- .wait(100);
      !start.
     
+!join 
   <- .wait(500);
      joinWorkspace("ora4mas",_).
-!join
   <- .wait(100);
      !join.
