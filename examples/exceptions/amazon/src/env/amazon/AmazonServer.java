package amazon;

import java.util.ArrayList;
import java.util.List;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class AmazonServer extends Artifact {
    
    private static int count = 0;
    
    private List<Order> orders = new ArrayList<>();
    
    @OPERATION
    public void addOrder(String recipient) {
        orders.add(new Order(recipient));
    }
    
    @OPERATION
    public void getOrderRecipient(OpFeedbackParam<String> res) {
        Order o = orders.get(orders.size()-1);
        res.set(o.recipient);
    }
    
    private class Order {
        
        private int id;
        private String recipient;
        
        private Order(String recipient) {
            id = count++;
            this.recipient = recipient;
        }
    }

}
