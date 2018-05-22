!start.

+!start
   <- makeArtifact(s,"dorg.DynScheme",["src/org/org.xml", "scheme1"],ArtId);
      .print("scheme created");
      focus(ArtId);
      //debug(inspector_gui(on));
      
      // add goal5 as subgoal of goal1 and as a new mission
      addSubGoal(goal1,goal5);
      addMissionGoal(mission3,goal5);
      !run;
   .

+!run
   <- commitMission(mission1);
      commitMission(mission2);
      commitMission(mission3);
   .

+!goal2 <- .print(2).
+!goal3 <- .print(3).
+!goal4 <- .print(4).
+!goal5 <- .print(5); .wait(2000).
+!goal6 <- .print(6).

+goalState(_,goal5,_,_,satisfied)
        <- .print("adding goal 6");
           addSubGoal(goal1,goal6);
           addMissionGoal(mission3,goal6).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
