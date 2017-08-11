// Agent starter in project performance.mas2j

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start
  <- .my_name(Me);
     createWorkspace("ora4mas");
     joinWorkspace("ora4mas",O4MWsp);
	 .print("Workspace created");

     makeArtifact("g1","ora4mas.nopl.GroupBoard",["../os.xml", gspec, false, false ],GrArtId);
	 .print("group created");
	 makeArtifact("s1","ora4mas.nopl.SchemeBoard",["../os.xml", sspec, false, false ],SchArtId);

     focus(GrArtId);

	 ?formationStatus(ok)[artifact_id(GrArtId)]; // see plan belows to ensure we wait until it is well formed

	 addScheme("s1")[artifact_id(GrArtId)];
	 focus(SchArtId).

+?formationStatus(ok)[artifact_id(G)]
   <- .wait({+formationStatus(ok)[artifact_id(G)]}).

+goalState("s1",rg,_,_,satisfied) <- .stopMAS.

