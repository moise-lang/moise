package ora4mas.nopl.simulator;

import c4jason.CAgentArch;

/**
  * Simulator for Organisational Agents
  */
public class SimOrgAgent extends CAgentArch {
    
    private AgentGUI myGUI = null;
    
    @Override
    public void init() throws Exception {
        // creates the general gui
        SimulatorGUI.getInstance();
        
        // creates the agent GUI
        myGUI = new AgentGUI(getTS().getAg(), this);
    }
    
    public AgentGUI getMyGUI() {
        return myGUI;
    }
}
