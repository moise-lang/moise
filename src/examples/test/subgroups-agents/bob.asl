// This agent creates the group/scheme to write a paper
// and adopts the role "editor" in the group

{ include("check_obl.asl") } // some common plans for obedient agents

/* Initial goals */

!start.

/* Plans */

+!start 
  <- .my_name(Me);
     createWorkspace("ora4mas");
     joinWorkspace("ora4mas",O4MWsp);
     
     makeArtifact("ig1","ora4mas.nopl.GroupBoard",["../subgroups.xml", g1, false, true ],G1);
	 setOwner(Me);	 
     focus(G1);
     .print("group ig1 created");
	 
     makeArtifact("ig2","ora4mas.nopl.GroupBoard",["../subgroups.xml", g2, false, true ],G2);
	 focus(G2);
	 setParentGroup(ig1)[artifact_id(G2)];
     .print("group ig2 created");

	 makeArtifact("ig3","ora4mas.nopl.GroupBoard",["../subgroups.xml", g3, false, true ],G3);
	 setOwner(Me)[artifact_id(G3)];	 
     focus(G3);
	 setParentGroup(ig2)[artifact_id(G3)];
     .print("group ig3 created");
	 
	 makeArtifact("ig4","ora4mas.nopl.GroupBoard",["../subgroups.xml", g4, false, true ],G4);
     focus(G4);
	 setParentGroup(ig1)[artifact_id(G4)];
     .print("group ig4 created");
	 
	 // should produce a cardinality failure
	 /*
	 makeArtifact("ig4b","ora4mas.nopl.GroupBoard",["../subgroups.xml", g4, false, true ],G4b);
     focus(G4b);
	 setParentGroup(ig1)[artifact_id(G4b)];
     .print("group ig4b created");
	 */
	 
	 // should produce a subgroup_in_group failure
	 //setParentGroup(ig1)[artifact_id(G3)];
	 
     adoptRole(r3)[artifact_id(G3)];
	 adoptRole(r2)[artifact_id(G2)];
	 adoptRole(r4)[artifact_id(G4)];
	 	 
     !run_scheme(sch1).

+goalState(sch1, wp, _, _, satisfied) 
   <- destroy[artifact_name(sch1)]; lookupArtifact(sch1, SId); disposeArtifact(SId);
      leaveRole(r3)[artifact_name(ig3)];
      destroy[artifact_name(ig3)];  lookupArtifact(ig3, G3);   disposeArtifact(G3);
	  destroy[artifact_name(ig2)];  lookupArtifact(ig2, G2);   disposeArtifact(G2).
   
// general error handler for goal start 
-!start[error(I),error_msg(M),reason(R)] <- .print("failure in starting! ",I,": ",M, " -- ",R).
-!start[error(I),error_msg(M)] <- .print("failure in starting! ",I,": ",M).
     
+!run_scheme(S)
   <- makeArtifact(S,"ora4mas.nopl.SchemeBoard",["../subgroups.xml", writePaperSch, false, true ],SchArtId);
      focus(SchArtId);
      .print("scheme ",S," created");
      addScheme(S)[artifact_name("ig1")]; 
      .print("scheme is linked to responsible group").
-!run_scheme(S)[error(I),error_msg(M)] <- .print("failure creating scheme ",S," -- ",I,": ",M).

// application domain goals
+!wtitle     <- .wait(500); .print("writing title...").
+!wabs       <- .print("writing abstract...").
+!wsectitles <- .print("writing section title...").
+!wsecs      <- .print("writing sections...").
+!wconc	     <- .print("writing conclusion...").
+!wrefs      <- .print("preparing refs...").
+!wp         <- .print("paper finished!").


//+?play(A,R,G) <- .wait({+play(_,_,_)},100,_); ?play(A,R,G).
    
// signals
+normFailure(N)  <- .print("norm failure event: ", N).
+destroyed(Art)  <- .print("Artifact ",Art," destroyed").      

// for debug (prints out the new states of goals)
//+goalState(Sch,Goal,CommittedAgs,AchievedBy,State)
//   <- .print("                         goal changed: ", goalState(Sch,Goal,CommittedAgs,AchievedBy,State)).
   