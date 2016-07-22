package ora4mas.nopl.simulator;

import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.CartagoException;
import cartago.CartagoNode;
import cartago.CartagoService;
import cartago.CartagoWorkspace;
import cartago.ICartagoContext;
import cartago.Op;
import cartago.WorkspaceKernel;
import cartago.security.AgentIdCredential;
import cartago.util.agent.Agent;
import cartago.util.agent.Percept;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.ORA4MASConstants;
import ora4mas.nopl.SchemeBoard;

/** simulates some MAS using moise */
public class ConsoleSimulator {
    public static void main(String[] args) throws Exception {
        CartagoService.startNode();
        CartagoService.installInfrastructureLayer("default"); 
        CartagoService.startInfrastructureService("default");
        
        CartagoWorkspace wsp = CartagoNode.getInstance().createWorkspace(ORA4MASConstants.ORA4MAS_WSNAME);
        ICartagoContext ora4masWS = wsp.join(new AgentIdCredential("simulator"), null);
        runWritePaper(wsp.getKernel(), ora4masWS);
        //runAuction(wsp.getKernel(), ora4masWS);
    }

    static void runWritePaper(WorkspaceKernel ora4masKernel, ICartagoContext ora4masCxt) throws Exception {
        ora4masKernel.makeArtifact(ora4masCxt.getAgentId(), "g1",   GroupBoard.class.getName(),  new ArtifactConfig("src/examples/writePaper/wp-os.xml", "wpgroup"));
        ora4masKernel.makeArtifact(ora4masCxt.getAgentId(), "sch1", SchemeBoard.class.getName(), new ArtifactConfig("src/examples/writePaper/wp-os.xml", "writePaperSch"));
        MyAgent jaime = new MyAgent("jaime", ora4masKernel);      jaime.start();
        MyAgent olivier = new MyAgent("olivier", ora4masKernel);  olivier.start();
        MyAgent jomi = new MyAgent("jomi", ora4masKernel);        jomi.start();

        jaime.execOp("g1", "debug", "inspector_gui(on)");
        jaime.execOp("sch1", "debug", "inspector_gui(on)");

        Thread.sleep(200);
        
        jaime.execOp("g1", "adoptRole", "editor");
        jomi.execOp("g1", "adoptRole", "writer");
        olivier.execOp("g1", "adoptRole", "writer");
        
        jaime.execOp("g1", "addScheme", "sch1");
        
        //ActionFeedback af = jaime.execOp("g1", "addScheme", "sch1");
        //af.waitForCompletion(); 

        Thread.sleep(500); // TODO: use some kind of sync, or waitForCompletion as before
        
        jaime.execOp("sch1", "commitMission", "mManager");
        olivier.execOp("sch1", "commitMission", "mColaborator");
        olivier.execOp("sch1", "commitMission", "mBib");
        jomi.execOp("sch1", "commitMission", "mColaborator");
        
        jaime.execOp("sch1", "goalAchieved", "wtitle");
        jaime.execOp("sch1", "goalAchieved", "wabs");
        jaime.execOp("sch1", "goalAchieved", "wsectitles");
        
        olivier.execOp("sch1", "goalAchieved", "wsecs");
        jomi.execOp("sch1", "goalAchieved", "wsecs");

        olivier.execOp("sch1", "goalAchieved", "wrefs");
        jaime.execOp("sch1", "goalAchieved", "wconc");
        
        // test resetGoal
        jaime.execOp("sch1", "resetGoal", "sv");
        //jaime.execOp("sch1", "goalAchieved", "wsectitles");
    }

    static void runAuction(WorkspaceKernel ora4masKernel, ICartagoContext ora4masCxt) throws Exception {
        ora4masKernel.makeArtifact(ora4masCxt.getAgentId(), "g1",   GroupBoard.class.getName(),  new ArtifactConfig("examples/auction/auction-os.xml", "auctionGroup", false, true));
        ora4masKernel.makeArtifact(ora4masCxt.getAgentId(), "sch1", SchemeBoard.class.getName(), new ArtifactConfig("examples/auction/auction-os.xml", "doAuction", false, true));

        MyAgent jaime = new MyAgent("jaime", ora4masKernel);      jaime.start();
        MyAgent olivier = new MyAgent("olivier", ora4masKernel);  olivier.start();
        MyAgent jomi = new MyAgent("jomi", ora4masKernel);        jomi.start();

        Thread.sleep(200);
        
        jaime.execOp("g1", "adoptRole", "auctioneer");
        try {
            jaime.execOp("g1", "adoptRole", "participant"); // should cause an error
            System.out.println("NOK - problem");
        } catch (Exception e) {
            System.out.println("Ok");
        }
        jomi.execOp("g1", "adoptRole", "participant");
        olivier.execOp("g1", "adoptRole", "participant");
        
        jaime.execOp("g1", "addScheme", "sch1");
        Thread.sleep(500);
        //ActionFeedback af = jaime.execOp("g1", "addScheme", "sch1");
        //af.waitForCompletion();

        jaime.execOp("sch1", "commitMission", "mAuctioneer");
        olivier.execOp("sch1", "commitMission", "mParticipant");
        jomi.execOp("sch1", "commitMission", "mParticipant");
        jaime.execOp("sch1", "setArgumentValue", "auction N 45");

        jaime.execOp("sch1", "goalAchieved", "start");
        olivier.execOp("sch1", "goalAchieved", "bid");
        jomi.execOp("sch1", "goalAchieved", "bid");

        jaime.execOp("sch1", "setArgumentValue", "winner W olivier");
        jaime.execOp("sch1", "goalAchieved", "winner");
    }

}

class MyAgent extends Agent {
    
    public MyAgent(String name, WorkspaceKernel kernel) {
        super(name);
        try {
            joinWorkspace(ORA4MASConstants.ORA4MAS_WSNAME, new AgentIdCredential(getAgentName()));
        } catch (CartagoException e) {
            e.printStackTrace();
        }        
    }
    
    @Override
    public void run() {
        try {
            while (true) {
                Percept p = waitForPercept();
                System.out.println(p+"\n");
                /*
                Exception fail = containsNormativeFailure(p);
                if (fail == null)
                    fail = containsORA4MASFailure(p);
                if (fail != null) {
                    txtLog.append("** "+fail+"\n");
                }*/
            }
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
            e.printStackTrace();
        }
    }
        
    void execOp(final String art, final String op, final String args) throws Exception {
        ArtifactId aid = lookupArtifact(art);
        if (args.length() == 0) {
            doAction(aid, new Op(op), -1);
        } else {
            doAction(aid, new Op(op, (Object[])args.split(" ")), -1);
        }
    }
    
}
