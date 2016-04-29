In this example we have the same 4 agents of the auction system
presented in the Jason manual (and in the Jason distribution
examples). However, these agents are organised using the Moise+ model
(http://moise.sf.net).

The organisation has only one group (called auctionGroup) where two
roles can be played: 

. auctioneer: the agent that will enact this role will manage the
auction (create the auction, decide the winner, ...); 

. participant: all agents that wants to participate in auctionGroup
need to adopt this role (agents ag1, ag2, and ag3 will adopt this
role).

The file ../auction-os.xml contains the complete definition of this group.


When the auctioneer decides that a new auction should start, it
creates an instance of the doAuction scheme. These scheme is composed
by a sequence of the following goals achievements:
. start: achieved by the auctioneer, that defines the auction Id;
. bid: achieved by all participants, that send their bids to the
  auctioneer; and
. winner: achieved by the auctioneer, that chooses the auction winner.

The main advantage of using Moise+ is that the coordination of the
goals achievements is automatically done by the organisational
tools. For instance, the auctioneer is not concerned about when all
participants have achieved their goals. Since its goal "winner" has
"bid" as its pre-goal, only when "bid" is already achieved the
auctioneer is notified that the goal "winner" can be achieved. Thus
the Moise+ version of auctioneer's code does no need the "checkEnd"
plans that must be used in the non Moise+ version of this example.

For an example of organisational reasoning, see the plans ?ally in
the end of the source file ag3.asl.
