/* Common code for Alice and Carol */

/* useful rules */

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
   <- .wait(100);
      !play.
	 
+!join 
   <- .my_name(Me); 
       joinWorkspace("ora4mas",_).
-!join
   <- .wait(200);
      !join.
            
+!quit_mission(M,S)
   <- .print("leaving my mission ",M," on ",S,"....");
      leaveMission(M)[artifact_name(S)].            

// keep focused on schemes that my groups are responsible for
+schemes(L)
   <- !focus_on_schemes(L).
   
+!focus_on_schemes([]).
+!focus_on_schemes([S|R])
   <- lookupArtifact(S,ArtId);
      focus(ArtId);
      !focus_on_schemes(R).
-!focus_on_schemes(L)[error_msg("Artifact Not Available.")]
  <- .wait(100); // try latter
     !focus_on_schemes(L).
             