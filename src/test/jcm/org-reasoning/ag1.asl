!start.

+!start
   <- .my_name(Me); .wait(100);
      ?play(Me,Role,Gr);
      .print("I play role ",Role);
      for(role_mission(Role,S,M)) {
          .print("my mission will be ",M," in scheme ",S);
          for (mission_goal(M,G)) {
              .print("     goal ",G);
          }
      }
   .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
