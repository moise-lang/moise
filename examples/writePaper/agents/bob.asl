// This agent creates the group/scheme to write a paper
// and adopts the role "editor" in the group

my_role(editor).

{ include("common.asl") }

/* Initial goals */

!create.

/* Plans */

+!create 
  <- .my_name(Me);
     createWorkspace("ora4mas");
     joinWorkspace("ora4mas",O4MWsp);
     
     makeArtifact(myorg, "ora4mas.nopl.OrgBoard", ["../wp-os.xml"], OrgArtId);
     focus(OrgArtId);
     
     createGroup("mypaper", wpgroup, GrArtId);
     debug(inspector_gui(on))[artifact_id(GrArtId)];
     setOwner(Me);
	 
     .print("group created");
	 
     // wait for alice
     ?play(alice,writer,mypaper);
     
     // wait for carol
     ?play(carol,writer,mypaper);
	 
     !run_scheme(sch1).
     
//+group(_,_,AID)  <- focus(AID).     
//+scheme(_,_,AID) <- focus(AID).

// general error handler for goal start 
-!start[error(I),error_msg(M)] <- .print("failure in starting! ",I,": ",M).
     
+!run_scheme(S)
   <- createScheme(S, writePaperSch, SchArtId);
      debug(inspector_gui(on))[artifact_id(SchArtId)];
      .print("scheme ",S," created");
      addScheme(S)[artifact_name("mypaper")]; 
      .print("scheme is linked to responsible group");	 
      commitMission(mManager)[artifact_id(SchArtId)].
-!run_scheme(S)[error(I),error_msg(M)] <- .print("failure creating scheme ",S," -- ",I,": ",M).

// application domain goals
+!wtitle     <- .wait(500); .print("writing title...").
+!wabs       <- .print("writing abstract...").
+!wsectitles <- .print("writing section title...").
+!wconc	     <- .print("writing conclusion...").
+!wp         <- .print("paper finished!").

+goalState(sch1, wp, _, _, satisfied)         
   <- .wait(1000);
      removeScheme(sch1);
      .print("starting a new scheme...");
      !!run_scheme(sch2).

+?play(A,R,G) <- .wait({+play(_,_,_)},100,_); ?play(A,R,G).
    
// signals
+normFailure(N)  <- .print("norm failure event: ", N).
+destroyed(Art)  <- .print("Artifact ",Art," destroyed").      

// for debug (prints out the new states of goals)
//+goalState(Sch,Goal,CommittedAgs,AchievedBy,State)
//   <- .print("                         goal changed: ", goalState(Sch,Goal,CommittedAgs,AchievedBy,State)).

{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
   