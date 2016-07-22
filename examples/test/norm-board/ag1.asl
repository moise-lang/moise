// Agent sample_agent in project t1

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start : true 
   <- .print("hello world.");
      makeArtifact(nb1,"ora4mas.nopl.NormativeBoard",[],AId);
      focus(AId);
      debug(inspector_gui(on));
      load("e1.npl");
      addFact(b(3));
      .wait(5000);
      removeFact(b(3))
   .

+oblUnfulfilled(O) <- .print("Unfulfilled ",O).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have a agent that always complies with its organization  
//{ include("$jacamoJar/templates/org-obedient.asl") }
