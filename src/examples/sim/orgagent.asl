// almost everything is done in the architecture

+using(ArtName)
   <- lookupArtifact(ArtName,ToolId);
      focus(ToolId).

+!doOrgAct(ArtName,OpName,Args)
   <- lookupArtifact(ArtName,ToolId);
      Op =.. [OpName, Args, [artifact_id(ToolId)]];
      //.print("doing ",Op);
      Op.