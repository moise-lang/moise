// almost everything is done in the architecture
!join.

+!join <- .wait(500); joinWorkspace(ora4mas_ws,_).

+using(ArtName) : joined(ora4mas_ws,W)
   <- lookupArtifact(ArtName,ToolId)[wid(W)];
      focus(ToolId)[wid(W)].

+!doOrgAct(ArtName,OpName,Args) : joined(ora4mas_ws,W)
   <- lookupArtifact(ArtName,ToolId)[wid(W)];
      Op =.. [OpName, Args, [artifact_id(ToolId)]];
      //.print("doing ",Op);
      Op.