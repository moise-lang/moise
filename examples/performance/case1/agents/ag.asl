/* Common code for Alice and Carol */

/* useful rules */

/* Initial goals */

!start.
!join.

/* Plans */

+!start
    : role_to_play(R)
   <- lookupArtifact("g1",GrId);
      adoptRole(R)[artifact_id(GrId)];
      focus(GrId).
-!start
   <- .wait(100);
      !start.

+!join
   <- .my_name(Me);
       joinWorkspace("ora4mas",_).
-!join
   <- .wait(200);
      !!join.

// keep focused on schemes my groups are responsible for
+schemes(L)
   <- for ( .member(S,L) ) {
         lookupArtifact(S,ArtId);
         focus(ArtId)
      }.

+!quit_mission(M,S)
   <- .print("leaving my mission ",M," on ",S,"....");
      leaveMission(M)[artifact_name(S)].

// plans to handle obligations
+obligation(Ag,Norm,committed(Ag,M,Scheme),DeadLine)
    : .my_name(Ag)
   <- .print("I am obliged to commit to ",M);
      commitMission(M)[artifact_name(Scheme)].


+obligation(Ag,Norm,achieved(Scheme,Goal,Ag),DeadLine)
    : .my_name(Ag)
   <- .print("I am obliged to achieve goal ",Goal);
      //!Goal[scheme(Scheme)];
      goalAchieved(Goal)[artifact_name(Scheme)];
	  .print("I've done ",Goal).


