count(0).

+obligation(Ag,_,done(_,payOrder,Ag),_)[artifact_id(ArtId)]
	 : .my_name(Ag) &
	   group(gcc,credit_card_group,GrArtId) &
	   count(C)
	<- +cu_scheme_id(ArtId);
	   .print("Sending credit card information...");
	   sendCreditCardInfo(123456789,000);
	   .concat(ccsch,C,Scheme);
	   -count(C);
	   +count(C+1);
	   createScheme(Scheme, credit_card_sch, SchArtId);
	   addScheme(Scheme)[artifact_id(GrArtId)].
	   
+result(ok,Sch) : cu_scheme_id(ArtId)
	<- .print("Payment completed!");
	   -result(ok,Sch);
	   goalAchieved(payOrder)[artifact_id(ArtId)].
   
+result(ko,Sch) : cu_scheme_id(ArtId)
	<- .print("Payment failed!");
	   -result(ko,Sch);
	   goalFailed(payOrder)[artifact_id(ArtId)].
	   
+result(R)
	<- .print("======",R,"======").
	   
+obligation(Ag,_,done(_,raisePaymentRefused,Ag),_)[artifact_id(ArtId)]
     : .my_name(Ag)
	<- .print("Raising exception for pay order...");
	   getBalance(B);
	   raiseException(paymentRefused,[balance(B)])[artifact_id(ArtId)];
	   goalAchieved(raisePaymentRefused)[artifact_id(ArtId)].

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
{ include("$moiseJar/asl/org-obedient.asl") }
