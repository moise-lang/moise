role(R,Super) :-
   specification(os(_,G,_,_)) &
   role(R,Super,G).
role(R,SuperRoles,group_specification(Gr,Roles,SubGroups,Props)) :-
   .member( role(R,SubRoles,SuperRoles,Min,Max,Compats,Links), Roles).
role(R,Super,group_specification(Gr,Roles,SubGroups,Props)) :-
   .member( subgroup(Min,Max,G), SubGroups) & role(R,Super,G).

role_mission(Role,S,MT) :-
   specification(os(_,_,_,Norms)) &
   role(Role,Super) &
   .member(R,[Role|Super]) & // for all super roles
   .member(norm(Id,R,_,MS),Norms) &
   .substring(".",MS,P) &
   .substring(MS,M,P+1) &
   .term2string(MT,M) &
   .substring(MS,SS,0,P) &
   .term2string(S,SS).

mission_goal(MT,G) :-
   specification(os(_,_,Schemes,_)) &
   .member(scheme_specification(S,RootGoal,Missions,Pros),Schemes) &
   .member(mission(MT,Min,Max,Goals,_),Missions) &
   .member(G,Goals).

role_cardinality(R,Min,Max) :-
   specification(os(_,G,_,_)) &
   role_cardinality(R,Min,Max,G).
role_cardinality(R,Min,Max,group_specification(Gr,Roles,SubGroups,Props)) :-
   .member( role(R,SubRoles,SuperRoles,Min,Max,Compats,Links), Roles).
role_cardinality(R,Min,Max,group_specification(Gr,Roles,SubGroups,Props)) :-
   .member( subgroup(_,_,G), SubGroups) & role_cardinality(R,Min,Max,G).
