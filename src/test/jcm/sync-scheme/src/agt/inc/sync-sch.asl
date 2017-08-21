
+!send_update(A)
   <- getState(SchSt);
      .send(A,update_sch,SchSt).

// whenever some goal becomes satisfied, informs alice
+goalState(S,_,_,_,satisfied)
   <- .my_name(Me);
      for (::commitment(A,_,S) & A \== Me) {
         .print("send update to ",A)
         !send_update(A);
      }.

+!default::kqml_received(Sender, update_sch, SchSt, _)
   <- .print("Merging with state sent by ", Sender);
      this_ns::mergeState(SchSt).
