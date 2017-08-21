package tools;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;

/**
 *      Artifact that implements the auction. 
 */
public class AuctionArt extends Artifact {
    
    
    @OPERATION public void init(String taskDs, int maxValue)  {
        // observable properties   
        defineObsProperty("task",          taskDs);
        defineObsProperty("maxValue",      maxValue);
        defineObsProperty("currentBid",    maxValue);
        defineObsProperty("currentWinner", "no_winner");       
    }

    @OPERATION public void bid(double bidValue) {
        ObsProperty opCurrentValue  = getObsProperty("currentBid");
        ObsProperty opCurrentWinner = getObsProperty("currentWinner");
        if (bidValue < opCurrentValue.doubleValue()) {  // the bid is better than the previous
            opCurrentValue.updateValue(bidValue);
            opCurrentWinner.updateValue(getOpUserName());
        }
    }
    
}

