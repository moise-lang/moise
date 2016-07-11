{ include("common-moise.asl") }

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
     createWorkspace("ora4mas");
     joinWorkspace("ora4mas",O4MWsp);
     
     makeArtifact(myorg, "ora4mas.nopl.OrgBoard", ["../auction-os.xml"], OrgArtId);
     focus(OrgArtId);
     
     createGroup(auction, auctionGroup, GrArtId);
     startGUI[artifact_id(GrArtId)];
	 adoptRole(auctioneer);
	 focus(GrArtId).
-!create_group[error(E), error_msg(M), reason(R)]
   <- .print("** Error ",E," creating auction group: ",M);
      .print("** The reason is ",R).

// when I start playing the role "auctioneer",
// create a doAuction scheme.
// My group will be the responsible group for the scheme
+play(Me,auctioneer,GId) 
   :  .my_name(Me)
   <- !create_scheme.

+!create_scheme 
   <- ?auction_id(Id); .concat("sch",Id,Sch); // create a new scheme id
      createScheme(Sch, doAuction, SchArtId);
      startGUI[artifact_id(SchArtId)];
      focus(SchArtId);
      addScheme(Sch);
	  commitMission(mAuctioneer)[artifact_id(SchArtId)].
-!create_scheme[error(Id), error_msg(M), code_line(Line)] 
   <- .print("Error ",Id, " ",M," -- at line ", Line).

// when a scheme has finished, start another
+destroyed(Art) 
   :  auction_id(N) & N < 5
   <- !create_scheme.

/*   
   Organisational Goals' plans
   ---------------------------
*/

+!start[scheme(Sch)] 
   :  auction_id(N)
   <- .print("Start scheme ",Sch," for ",auction_id(N+1));
      -+auction_id(N+1); 
      -+winner(N+1,no,0);
	  //.term2string(N+1,NS);
      setArgumentValue(auction,"N",N+1)[artifact_name(Sch)];
      //.print("Waiting participants for 1 second....");
      //.wait(1000);
      .print("Waiting for 3 participants....");
      !wait_participants(3);
      .print("Go!").
      
+!wait_participants(N) : .count(play(_,participant,_), N).
+!wait_participants(N) <- .wait( { +play(_,participant,_) }, 100, _); !wait_participants(N).
      
+!winner[scheme(Sch)] 
   :  auction_id(N) & winner(N,W,_) 
   <- setArgumentValue(winner,"W",W)[artifact_name(Sch)].
//-!winner[error(Id), error_msg(M), code_line(Line)] 
//   <- .print("Error ",Id, " ",M," -- at line ", Line); .fail.

// the root goal is ready (it means that all sub-goals were achieved)
+!auction[scheme(Sch)] 
   :  auction_id(N) & winner(N,W,Vl) 
   <- .print("***** Auction ", N," is finished. The winner is ",W,", value is ",Vl," *****");
      .println.
	  
+goalState(Sch, auction(X), _, _, satisfied)
    :  auction_id(N) & N < 5         
   <- .wait(1000);	  
      removeScheme(Sch).
      
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
	  
+formationStatus(ok)[artifact_name(X)] <- .print("Org Art ",X," is well formed").	  

