{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl", g) }
{ include("$jacamoJar/templates/org-obedient.asl", s) }

/*
   Beliefs
*/

auction_id(0).

/*
   Initial goals
*/

!create_group. // initial goal

// create a group to execute the auction
+!create_group
   <- .my_name(Me);
     createWorkspace(ora4mas);
     joinWorkspace(ora4mas,O4MWsp);

     makeArtifact(myorg, "ora4mas.nopl.OrgBoard", ["../auction-os.xml"], OrgArtId)[wid(O4MWsp)];
     o::focus(OrgArtId);

     o::createGroup(auction, auctionGroup, GrArtId);
	 g::focus(GrArtId);
     g::debug(inspector_gui(on));
	 g::adoptRole(auctioneer);
   .
-!create_group[error(E), error_msg(M), reason(R)]
   <- .print("** Error ",E," creating auction group: ",M);
      .print("** The reason is ",R).

// when I start playing the role "auctioneer",
// create a doAuction scheme.
// My group will be the responsible group for the scheme
+g::play(Me,auctioneer,GId)
   :  .my_name(Me)
   <- !create_scheme.

+!create_scheme
   <- ?auction_id(Id); .concat("sch",Id,Sch); // create a new scheme id
      o::createScheme(Sch, doAuction, SchArtId);
      s::focus(SchArtId);
      s::debug(inspector_gui(on));
      g::addScheme(Sch);
	  s::commitMission(mAuctioneer);
   .
-!create_scheme[error(Id), error_msg(M), code_line(Line)]
   <- .print("Error ",Id, " ",M," -- at line ", Line).

// when a scheme has finished, start another
+_::destroyed(Art)
   :  not .substring("auction.sch", Art, _) & auction_id(N) & N < 5
   <- !create_scheme.

/*
   Organisational Goals' plans
   ---------------------------
*/

+!s::start[scheme(Sch)]
   :  auction_id(N)
   <- .print("Start scheme ",Sch," for ",auction_id(N+1));
      -+auction_id(N+1);
      -+winner(N+1,no,0);
      s::setArgumentValue(auction,"N",N+1);
      //.print("Waiting participants for 1 second....");
      //.wait(1000);
      .print("Waiting for 3 participants....");
      !wait_participants(3);
      .print("Go!").

+!wait_participants(N) : .count(g::play(_,participant,_), N).
+!wait_participants(N) <- .wait( { +g::play(_,participant,_) }, 100, _); !wait_participants(N).

+!s::winner[scheme(Sch)]
   :  auction_id(N) & winner(N,W,_)
   <- s::setArgumentValue(winner,"W",W).
//-!winner[error(Id), error_msg(M), code_line(Line)]
//   <- .print("Error ",Id, " ",M," -- at line ", Line); .fail.

// the root goal is ready (it means that all sub-goals were achieved)
+!s::auction[scheme(Sch)]
   :  auction_id(N) & winner(N,W,Vl)
   <- .print("***** Auction ", N," is finished. The winner is ",W,", value is ",Vl," *****");
      .println.

+s::goalState(Sch, auction, _, _, satisfied)
   :  auction_id(N) & N < 5
   <- .wait(1000);
      .abolish(place_bid(_,_));
      o::removeScheme(Sch);
   .

/*
   Communication protocol for bids
*/

// receive bid and check for new winner
@pb1[atomic]
+place_bid(N,V)[source(S)]
   :  auction_id(N) & winner(N,_,CurVl) & V > CurVl
   <- .print("+ Bid from ", S, " is ", V);
      -+winner(N,S,V).
+place_bid(_,V)[source(S)]
   <- .print("- Bid from ", S, " is ", V).

/*
   plans to react to organisational events
*/

+_::formationStatus(ok)[artifact_name(_,X)] <- .print("Org Art ",X," is well formed").
