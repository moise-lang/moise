
!start.

+!start : true
   <- makeArtifact(org,"ora4mas.nopl.OrgBoard",["t3.xml"],AId);
      createGroup(g1,wpgroup,GId);
      g::focus(GId);
      g::adoptRole(editor);

      makeArtifact(nb1,"ora4mas.nopl.NormativeBoard",[],NBId);
      nb::focus(NBId);
      nb::debug(inspector_gui(on));
      load("scope main { norm n1: play(X,editor,G) -> obligation(X,n1, play(X,writer,G), `now`+`30 seconds`). }");

      nb::doSubscribeDFP(g1); // starts getting facts from g1

      .wait(50000);
      g::adoptRole(writer);  // filfills the obligation a bit latter
   .
