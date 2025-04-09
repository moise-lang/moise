// plan to execute organisational goals

+!site_prepared      // the goal (introduced by the organisational obligation)
   <- .print("Preparing site...");
      .wait(2000);
      prepareSite;
      .print("Done!").

+!floors_laid                   <- .print("Laying floors...");.wait(2000);layFloors;.print("Done!").
+!walls_built                   <- .print("Building walls...");.wait(2000);buildWalls;.print("Done!").
+!roof_built                    <- .print("Building roof...");.wait(2000);buildRoof;.print("Done!").
//+!windows_fitted                <- .print("Fitting windows...");.wait(2000);fitWindows;.print("Done!").
+!doors_fitted                  <- .print("Fitting doors...");.wait(2000);fitDoors;.print("Done!").
+!electrical_system_installed   <- .print("Installing electrical system...");.wait(2000);installElectricalSystem;.print("Done!").
+!plumbing_installed            <- .print("Installing plumbing...");.wait(2000);installPlumbing;.print("Done!").
+!exterior_painted              <- .print("Painting exterior...");.wait(2000);paintExterior;.print("Done!").
+!interior_painted              <- .print("Painting interior...");.wait(2000);paintInterior;.print("Done!").
