// plans to handle obligations
+obligation(Ag,Norm,committed(Ag,Mission,Scheme),Deadline)
    : .my_name(Ag)
   <- .print("I am obliged to commit to ",Mission," on ",Scheme);
      commitMission(Mission)[artifact_name(Scheme)].

+obligation(Ag,Norm,achieved(Scheme,Goal,Ag),Deadline)
    : .my_name(Ag)
   <- .print("I am obliged to achieve goal ",Goal);
      !Goal[scheme(Scheme)];
      goalAchieved(Goal)[artifact_name(Scheme)].

+obligation(Ag,Norm,What,Deadline)
   : .my_name(Ag)
   <- .print("I am obliged to ",What,", but I don't know what to do!").

/* (old plans not used anymore)

//!check_obligations.

// see obligation list, if it is for me and I am not done, obey. Repeat it forever.
+!check_obligations
    : active_obligations(ListOfObl)[artifact_name(ArtId,ArtName)]
   <- for( .member(Obl,ListOfObl) ) {
         !check_obligation(Obl);
      }
      .wait(200);
      !!check_obligations.
-!check_obligations
   <- .wait(200);
      !!check_obligations.

+!check_obligation( obligation(Ag,Norm,committed(Ag,Mission,Scheme),Deadline) )
    : .my_name(Ag)
   <- .print("I am obliged to commit to ",Mission);
      commitMission(Mission)[artifact_name(Scheme)].

+!check_obligation( obligation(Ag,Norm,achieved(Scheme,Goal,Ag),Deadline) )
    : .my_name(Ag)
   <- .print("I am obliged to achieve goal ",Goal);
      !Goal[scheme(Scheme)];
      goalAchieved(Goal)[artifact_name(Scheme)].

+!check_obligation( obligation(Ag,Norm,What,Deadline) )
   : .my_name(Ag)
   <- .print("I am obliged to ",What,", but I don't know what to do!").
*/