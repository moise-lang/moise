// This company bids for Plumbing only
// Strategy: fixed price


{ include("common.asl") }

my_price(300). // initial belief

!discover_art("auction_for_Plumbing").

+currentBid(V)[artifact_id(Art)]         // there is a new value for current bid
    : not i_am_winning(Art)  &           // I am not the current winner
      my_price(P) & P < V                // I can offer a better bid
   <- //.print("my bid in auction artifact ", Art, " is ",P);
      bid( P ).                          // place my bid offering a cheaper service

/* plans for execution phase */

{ include("org_code.asl") }

// plan to execute organisational goals (not implemented)

+!plumbing_installed   // the organisational goal (created from an obligation)
   <- .print("Installing plumbing...");
      installPlumbing;
      .print("Done!").

{ include("$jacamoJar/templates/common-cartago.asl") }
