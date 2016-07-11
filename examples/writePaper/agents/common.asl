/* Common code for Alice and Carol */

/* useful rules */

/* Initial goals */

!start.
!join.

/* Plans */

+!start 
   <- lookupArtifact("mypaper",GrId); 
      adoptRole(writer)[artifact_id(GrId)];
      focus(GrId).     
-!start
   <- .wait(100);
      !start.
	 
+!join 
   <- .my_name(Me); 
       joinWorkspace("ora4mas",_).
-!join
   <- .wait(200);
      !join.
            
+!quit_mission(M,S)
   <- .print("leaving my mission ",M," on ",S,"....");
      leaveMission(M)[artifact_name(S)].            
              