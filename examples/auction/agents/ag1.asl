// this agent always bids 6

{ include("common-moise.asl") }
{ include("participant.asl") }

// plan for the bid organisational goal
+!bid[scheme(Sch)] 
   :  goalArgument(Sch, auction, "N", N) &   // get the auction number
      commitment(Ag, mAuctioneer, Sch)   // get the agent committed to mAuctineer
   <- .send(Ag, tell, place_bid(N,6)).
