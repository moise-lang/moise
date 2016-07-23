
!start.

+!start : true 
   <- makeArtifact(org,"ora4mas.nopl.OrgBoard",["../../writePaper/wp-os.xml"],AId);
      createGroup(g1,wpgroup,GId);
      focus(GId);
      adoptRole(editor);
      adoptRole(writer);
      debug(inspector_gui(on));
      
      createScheme(s1,writePaperSch,SId);
      addScheme(s1);
      debug(inspector_gui(on))[artifact_name(s1)];
      commitMission(mManager)[artifact_name(s1)];
   .

/* application domain goals */
+!wtitle     <- .wait(500); .print("writing title...").
+!wabs       <- .print("writing abstract...").
+!wsectitles <- .print("writing section title...").
+!wsecs      <- .print("writing sections...").
+!wconc      <- .print("writing conclusion...").
+!wrefs      <- .print("doing refs...").
+!wp         <- .print("paper finished!").

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
