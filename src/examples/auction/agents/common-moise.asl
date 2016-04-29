/* auxiliary organisational plans for agents */

// keep focused on schemes that my groups are responsible for
+schemes(L)
   <- !focus_on_schemes(L).
   
+!focus_on_schemes([]).
+!focus_on_schemes([S|R])
   <- lookupArtifact(S,ArtId);
      focus(ArtId);
      !focus_on_schemes(R).
-!focus_on_schemes(L)[error_msg("cartago.ArtifactNotAvailableException")]
  <- .wait(100); // try latter
     !focus_on_schemes(L).

/* plans to handle obligations */

// obligation to commit to a mission
+obligation(Ag,Norm,committed(Ag,Mission,Scheme),Deadline)[artifact_id(ArtId)]
    : .my_name(Ag)
   <- .print("I am obliged to commit to ",Mission," on ",Scheme,"... doing so");
      commitMission(Mission)[artifact_id(ArtId)]. 

// obligation to achieve a goal      
+obligation(Ag,Norm,achieved(Scheme,Goal,Ag),Deadline)[artifact_id(ArtId)]
    : .my_name(Ag)
   <- //.print(" ---> working to achieve ",Goal," in scheme ",Scheme);
      !Goal[scheme(Scheme)];
      //.print(" <--- done");
      goalAchieved(Goal)[artifact_id(ArtId)].

// an unknown type of obligation was received
+obligation(Ag,Norm,What,DeadLine)  
   : .my_name(Ag)
   <- .print("I am obliged to ",What,", but I don't know what to do!").
