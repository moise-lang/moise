
!start.

+!start : true
   <- makeArtifact(org,"ora4mas.nopl.OrgBoard",["t3.xml"],AId);
      createGroup(g1,wpgroup,GId);
      focus(GId);
      adoptRole(editor);
      adoptRole(writer);

      createScheme(s1,writePaperSch,SId);
      addScheme(s1);

      addFact(t);
      addFact(k);
      addFact(v(33));
   .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
