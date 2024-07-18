count(1).

+obligation(Ag,_,done(_,obtainAmount,Ag),_)
     : count(N) &
       amountInt(Amount)
	<- .print("Amount obtained:",Amount);
	   -count(N);
	   +count(1);
	   goalAchieved(obtainAmount).
	  
+obligation(Ag,_,done(_,recoverFromNan,Ag),_)
    : .my_name(Ag) &
      count(N) & N < 3
   <- -count(N);
      +count(N+1);
      .print("Attempt ",N," out of 3. Make another attempt");
      resetGoal(obtainAmount).
      
+obligation(Ag,_,done(_,recoverFromNan,Ag),_)
    : .my_name(Ag) &
      count(N) & N >= 3
   <- //goalReleased(obtainAmount);
      .print("Maximum number of attempts reached");
      goalFailed(recoverFromNan).
   
+obligation(Ag,_,done(_,raiseAmountUnavailable,Ag),_)
	 : .my_name(Ag)
	<- .print("Raising exception AMOUNT UNAVAILABLE!");
	   raiseException(amountUnavailable,[]);
	   goalAchieved(raiseAmountUnavailable).

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
