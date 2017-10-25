// this agent bids 3,
// if it looses 3 auctions, it proposes an alliance to
// another agent and therefore it bids 7 (3 from itself + 4 from ag2)
// (see plan ?ally for more information about how the ally is chosen)

{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
{ include("participant.asl") }


default_bid_value(3).
// ally(ag2). // belief used for a pre-defined ally (as in Jason), it was changed here to use organisational reasoning (see plan ?ally below)
threshold(3).

//!test_ally.

// plan for the bid organisational goal
+!bid[scheme(Sch)]
   :  goalArgument(Sch, auction, "N", N) & // get the auction number
      (threshold(T) & N < T)
      |
      (.my_name(I) & winner(_,I) & not alliance(I,_))
   <- !bid_normally.

+!bid[scheme(_)]
   : .my_name(I) & not winner(_,I) & not alliance(I,_)
   <- !alliance;
      !bid_normally.

+!bid[scheme(Sch)]
   :  goalArgument(Sch, auction, "N", N) &    // get the auction number
      commitment(Ag, mAuctioneer, Sch) &  // get the agent committed to mAuctineer
      alliance(_,A)
   <- ?default_bid_value(B);
      ?bid(A,C);
      .send(Ag, tell, place_bid(N,B+C)).

+!bid_normally
   :  goalArgument(Sch, auction, "N", N) &  // get the auction number
      commitment(Ag, mAuctioneer, Sch)  // get the agent committed to mAuctineer
   <- ?default_bid_value(B);
      .send(Ag, tell, place_bid(N,B)).

+!alliance
   <- ?ally(A);
      print("Proposing alliance to ",A);
      .send(A,tell,alliance).

// remember the winners
+goalState(Sch, winner, _, _, satisfied)
   :  goalArgument(Sch, auction, "N", N) &
      goalArgument(Sch, winner, "W", W)
   <- +winner(N,W).

// find and ally from the specification:
//    see all missions with the goal "bid"
//    see all agents committed to that missions
//    remove the winners from those agents
//    propose alliance to the one of the remaining agents
+?ally(Ally)
    : specification(scheme_specification(doAuction,_Root_Goal,Missions)) &
      .findall(Ag,
          .member(mission(MId,_MinCard,_MaxCard,MGoals,_Prefered),Missions) & // for all missions in the OS
          .member(bid,MGoals) &                                               // if 'bid' is a goal of that mission
          commitment(Ag,MId,_) &                                              // for all agents committed to that mission
          not winner(_,Ag) & not .my_name(Ag),                                // that is neither a winner nor myself
          ListAg
      ) &
      .length(ListAg) > 0
   <- .print("Agents commited to the goal 'bid' which are ally candidates: ",ListAg);
      .nth( math.random(.length(ListAg)), ListAg, Ally);                     // randomly select an agent from the options (this will be ag2 in the example)
      .print("Selected ally is ",Ally).
+?ally(ag2). // default strategy to select an ally

+!test_ally : specification(scheme_specification(doAuction,_Root_Goal,Missions))
   <- ?ally(A);
      .print("Ally is ",A).
+!test_ally // no spec yet... wait a bit and try latter
   <- .wait(200);
      !!test_ally.
