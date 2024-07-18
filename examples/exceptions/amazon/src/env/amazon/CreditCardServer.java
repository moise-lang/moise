package amazon;

import java.util.Random;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class CreditCardServer extends Artifact {
    
    private int number;
    private int securityCode;
    private double balance;
    private String owner;
    
    @OPERATION
    public void sendCreditCardInfo(int number, int securityCode) {
        this.owner = getCurrentOpAgentId().getAgentName();
        this.number = number;
        this.securityCode = securityCode;
        Random r = new Random();
        balance = r.nextDouble() * 1000;
    }
    
    @OPERATION
    public void getBalance(OpFeedbackParam<Double> res) {
        res.set(balance);
    }
    
    @OPERATION
    public void takePayment(double amount) {
        balance -= amount;
    }
    
    @OPERATION
    public void getCreditCardOwner(OpFeedbackParam<String> res) {
        res.set(owner);
    }

}
