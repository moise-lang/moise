
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

+schemes(L)[artifact_name(_,GroupName)] //[workspace(_,_,W)] 
   <- //cartago.set_current_wsp(W);
      for ( .member(S,L) ) {
         lookupArtifact(S,ArtId);
         focus(ArtId);
         .concat(GroupName,".",S,NBName);
         lookupArtifact(NBName,NBId);
         focus(NBId);
         
      }.

+obligation(Ag,Norm,committed(Ag,Mission,Scheme),Deadline)
    : .my_name(Ag)
   <- .print("I am obliged to commit to ",Mission," on ",Scheme,"... doing so");
      commitMission(Mission)[artifact_name(Scheme)]. 

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

// uncomment the include below to have a agent that always complies with its organization  
{ include("$jacamoJar/templates/org-obedient.asl") }
