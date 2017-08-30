
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

      // all Roles
      .print("All available roles:")
      for (role(R,SR)) {
          .print(R," inherit from ",SR);
          for(role_mission(R,S,M)) {
              .print("   mission ",M," in scheme ",S);
          }
      }
   .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
{ include("$moiseJar/asl/org-rules.asl") }
