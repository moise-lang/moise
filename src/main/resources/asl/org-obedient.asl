/*   Organisational Plans
     (for an obedient agent)    */


// plans to handle obligations
// obligation to commit to a mission
+obligation(Ag,Norm,committed(Ag,Mission,Scheme),Deadline)[artifact_id(ArtId),workspace(_,W)]
    : .my_name(Ag)
   <- .print("I am obliged to commit to ",Mission," on ",Scheme,"... doing so");
      commitMission(Mission)[artifact_name(Scheme), wid(W)].

// obligation to achieve a goal
+obligation(Ag,Norm,What,Deadline)[artifact_id(ArtId),norm(_,Un)]
    : .my_name(Ag) & (satisfied(Scheme,Goal)=What | done(Scheme,Goal,Ag)=What)
   <- .member(["M",Mission],Un);
      //.print(" ---> working to achieve ",Goal," in scheme ",Scheme," mission ",Mission);
      !fulfill_obligation(Scheme,Goal,ArtId,Mission);
      //.print(" <--- done");
   .

+!fulfill_obligation(Scheme,Goal,ArtId,Mission)
  <- !Goal[scheme(Scheme)];
     goalAchieved(Goal)[artifact_id(ArtId)].

// an unknown type of obligation was received
+obligation(Ag,Norm,What,DeadLine)
   : .my_name(Ag)
   <- .print("I am obliged to ",What,", but I don't know what to do!").

// drop intentions for obligations that are not active anymore
-obligation(Ag,_,What,_)
   :  .my_name(Ag) & (satisfied(Scheme,Goal)=What | done(Scheme,Goal,Ag)=What) &
      .intend(Goal)
   <- .print("I am not obliged to achieve ",Goal," for scheme ",Scheme," anymore, dropping the intention.");
      .drop_intention(Goal[scheme(Scheme)]).
