// This agent usually bids 4, 
// when it has an alliance with ag3, it bids 0

{ include("common-moise.asl") }
{ include("participant.asl") }

default_bid_value(4).
ally(ag3).
 
// plan for the bid organisational goal
+!bid[scheme(Sch)] 
   :  goalState(Sch, auction(N), _, _, _) & // get the auction number
      commitment(Ag, mAuctioneer, Sch) & // get the agent committed to mAuctineer
      not alliance
   <- ?default_bid_value(B);
      .send(Ag, tell, place_bid(N,B)).

+!bid[scheme(Sch)] 
   :  goalState(Sch, auction(N), _, _, _) & 
      commitment(Ag, mAuctioneer, Sch) & // get the agent committed to mAuctineer
      alliance
   <- .send(Ag, tell, place_bid(N,0)).

// alliance proposal from another agent
+alliance[source(A)] 
   :  .my_name(I) & ally(A)
   <- .print("Alliance proposed by ", A);
      ?default_bid_value(B);
      .send(A,tell,bid(I,B));
      .send(A,tell,alliance(A,I)).

+alliance[source(A)] <- .print("----",A). 
