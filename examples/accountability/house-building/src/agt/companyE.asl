// This company bids for Floors, Walls, and Roof
// Strategy: a fixed lowest price for doing all 3 tasks,
//           decrease the current bid by a fixed value

{ include("common.asl") }

my_price(800). // minimum price for the 3 tasks

// a rule to calculate the sum of the current bids place by this agent
sum_of_my_offers(S) :-
   .my_name(Me) & .term2string(Me,MeS) &
   .findall( V,      // artifacts/auctions I am currently winning
             currentWinner(MeS)[artifact_id(ArtId)] &
             currentBid(V)[artifact_id(ArtId)],
             L) &
   S = math.sum(L).

/* Plans for Auction phase */

!discover_art("auction_for_Floors").
!discover_art("auction_for_Walls").
!discover_art("auction_for_Roof").


+currentBid(V)[artifact_id(Art)]      // there is a new value for current bid
    : not i_am_winning(Art) &         // I am not the winner
      my_price(P) &
      sum_of_my_offers(Sum) &
      task(S)[artifact_id(Art)] &
      P < Sum + V                     // I can still offer a better bid
   <- //.print("my bid in auction artifact ", Art, ", Task ", S,", is ",math.max(V-10,P));
      bid( math.max(V-10,P) )[ artifact_id(Art) ].  // place my bid offering a cheaper service

/* plans for execution phase */

{ include("org_code.asl") }
{ include("org_goals.asl") }
{ include("$jacamoJar/templates/common-cartago.asl") }
