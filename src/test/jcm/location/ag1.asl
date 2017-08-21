
goal_location(S,G,L) :-
   scheme(S,Type,_) &
   specification(scheme_specification(Type,RootGoal, Missions, Properties)) &
   .print(RootGoal) &
   sgoal_location(G,RootGoal,L).
sgoal_location(G,goal(G,_,_,_,_,_,_)[location(L)],L) :- .print("ACHEI ",L).
sgoal_location(G,goal(_,_,_,_,_,_,plan(_,Goals)),L) :- .print("Gs:",Goals) & sgoal_location(G,Goals,L).
// BUG? unification with F lost annotations
sgoal_location(G,[F[A|B]|_],L) :- .print(plans," ",G," and ",F, " ",A,"|",B) & sgoal_location(G,F[A|B],L).
sgoal_location(G,[_|T],L) :- sgoal_location(G,T,L).

//goal_spec(Scheme,Goal,Type,Desc,Min,TTF,_,Plan)

goal_location(_,_,"anywhere").

/* application domain goals */
+!wtitle     <- .wait(500); .print("writing title...").
+!wabs       <- .print("writing abstract...").
+!wsectitles <- .print("writing section title...").
+!wsecs      <- .print("writing sections...").
+!wrefs      <- .print("doing refs...").
+!wp         <- .print("paper finished!").
+!present    <- .print("presented!").
+!wconc      <- .print("writing conclusion ").
//+!wconc[scheme(S)]  : goal_location(S,submit,L) <- .print("writing conclusion at ",L).
+!submit[scheme(S)] : goal_location(S,submit,L) <- .print("submit at ",L).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
