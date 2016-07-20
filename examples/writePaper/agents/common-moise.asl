/* auxiliary organisational plans for obedient agents 
   (they always do whatever they are asked to)
*/

/* plans to handle obligations */

// obligation to commit to a mission
+obligation(Ag,Norm,committed(Ag,Mission,Scheme),Deadline)[artifact_id(ArtId)]
    : .my_name(Ag)
   <- .print("I am obliged to commit to ",Mission," on ",Scheme,"... doing so");
      commitMission(Mission)[artifact_id(ArtId)]. 

// obligation to achieve a goal      
+obligation(Ag,Norm,done(Scheme,Goal,Ag),Deadline)[artifact_id(ArtId)]
    : .my_name(Ag)
   <- //.print(" ---> working to achieve ",Goal," in scheme ",Scheme);
      !Goal[scheme(Scheme)];
      //.print(" <--- done");
      goalAchieved(Goal)[artifact_id(ArtId)].

// an unknown type of obligation was received
+obligation(Ag,Norm,What,DeadLine)  
   : .my_name(Ag)
   <- .print("I am obliged to ",What,", but I don't know what to do!").
