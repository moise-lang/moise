
!start.

+!start : true 
   <- admCommand(setCardinality(role,editor,0,10));
      admCommand(setCardinality(role,writer,0,20));
      
      lookupArtifact("s1", SId);
      focus(SId);
      admCommand(setCardinality(mission,mColaborator,0,3))[aid(SId)];
      admCommand(setCardinality(mission,mManager,0,2))[aid(SId)];
      admCommand(setCardinality(mission,mBib,0,1))[aid(SId)];
      
/*       
      createScheme(s1,writePaperSch,SId);
      addScheme(s1);
      debug(inspector_gui(on))[artifact_name(s1)];
      commitMission(mManager)[artifact_name(s1)];*/
    .
    
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
    