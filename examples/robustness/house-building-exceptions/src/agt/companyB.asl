// This company bids for site preparation
// Strategy: decreasing its price by 150 until its minimal value

{ include("common.asl") }

my_price(1500). // initial belief

!discover_art("auction_for_SitePreparation").

+currentBid(V)[artifact_id(Art)]        // there is a new value for current bid
    : not i_am_winning(Art) &           // I am not the winner
      my_price(P) & P < V               // I can offer a better bid
   <- //.print("my bid in auction artifact ", Art, " is ",math.max(V-150,P));
      bid( math.max(V-150,P) ).         // place my bid offering a cheaper service

/* plans for execution phase */

+obligation(Ag,_,done(_,site_prepared,Ag),_)
	: my_name(Ag)
   <- !site_prepared;
      goalAchieved(site_prepared).

+!site_prepared
   <- .print("Preparing site...");
      .wait(2000);
      prepareSite. // simulates the action (in GUI artifact)

-!site_prepared[env_failure_reason(F)]
    : focused(ora4mas,bhsch,ArtId)
   <- .print("The site is flooded due to ",F,"!");
      +failureReason(F);
      goalFailed(site_prepared)[artifact_id(ArtId)];
   	  .fail.

+obligation(Ag,_,done(_,notify_site_preparation_problem,Ag),_)
    : .my_name(Ag) &
      focusing(ArtId,bhsch,_,_,ora4mas,_) &
      failureReason(F)
   <- .print("RAISING SITE PREPARATION EXCEPTION WITH ERROR CODE ",F,"!");
      raiseException(site_preparation_exception,[errorCode(F)])[artifact_id(ArtId)];
      -failureReason(F);
      goalAchieved(notify_site_preparation_problem).

 { include("org_code.asl") }
 { include("$jacamoJar/templates/common-cartago.asl") }
