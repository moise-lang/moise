
+!adopt_role(R,G)
   <- lookupArtifact(G,A);
      focus(A);
      adoptRole(R);
   .

+!g2[scheme(S)] <- .print("doing g2 in scheme ",S); .wait(2000).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
