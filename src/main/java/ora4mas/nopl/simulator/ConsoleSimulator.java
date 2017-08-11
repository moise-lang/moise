package ora4mas.nopl.simulator;

import cartago.AgentIdCredential;
import cartago.ArtifactId;
import cartago.CartagoContext;
import cartago.CartagoException;
import cartago.CartagoService;
import cartago.Op;
import cartago.WorkspaceId;
import cartago.util.agent.Agent;
import cartago.util.agent.Percept;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.ORA4MASConstants;
import ora4mas.nopl.SchemeBoard;

/** simulates some MAS using Moise */
public class ConsoleSimulator {
    public static void main(String[] args) throws Exception {
        CartagoService.startNode();
        CartagoService.createWorkspace(ORA4MASConstants.ORA4MAS_WSNAME);
        //CartagoService.enableDebug(ORA4MASConstants.ORA4MAS_WSNAME);

        runWritePaper();
        //runAuction();
    }

    static void runWritePaper() throws Exception {
        CartagoContext ctx = CartagoService.startSession(ORA4MASConstants.ORA4MAS_WSNAME, new AgentIdCredential("simulator"));
        WorkspaceId wid = ctx.getJoinedWspId(ORA4MASConstants.ORA4MAS_WSNAME);

        ArtifactId g1 = ctx.makeArtifact(wid, "g1",   GroupBoard.class.getName(),  new Object[] {"examples/writePaper/wp-os.xml", "wpgroup"});
        ArtifactId s1 = ctx.makeArtifact(wid, "sch1", SchemeBoard.class.getName(), new Object[] {"examples/writePaper/wp-os.xml", "writePaperSch"});
        MyAgent jaime = new MyAgent("jaime");      jaime.start();
        MyAgent olivier = new MyAgent("olivier");  olivier.start();
        MyAgent jomi = new MyAgent("jomi");        jomi.start();

        jaime.execOp(g1, "debug", "inspector_gui(on)");
        jaime.execOp(s1, "debug", "inspector_gui(on)");

        Thread.sleep(200);

        jaime.execOp(g1, "adoptRole", "editor");
        jomi.execOp(g1, "adoptRole", "writer");
        olivier.execOp(g1, "adoptRole", "writer");

        jaime.execOp(g1, "addScheme", "sch1");

        //ActionFeedback af = jaime.execOp("g1", "addScheme", "sch1");
        //af.waitForCompletion();

        Thread.sleep(500); // TODO: use some kind of sync, or waitForCompletion as before

        jaime.execOp(s1, "commitMission", "mManager");
        olivier.execOp(s1, "commitMission", "mColaborator");
        olivier.execOp(s1, "commitMission", "mBib");
        jomi.execOp(s1, "commitMission", "mColaborator");

        jaime.execOp(s1, "goalAchieved", "wtitle");
        jaime.execOp(s1, "goalAchieved", "wabs");
        jaime.execOp(s1, "goalAchieved", "wsectitles");

        olivier.execOp(s1, "goalAchieved", "wsecs");
        jomi.execOp(s1, "goalAchieved", "wsecs");

        olivier.execOp(s1, "goalAchieved", "wrefs");
        jaime.execOp(s1, "goalAchieved", "wconc");

        // test resetGoal
        jaime.execOp(s1, "resetGoal", "sv");
        //jaime.execOp(s1, "goalAchieved", "wsectitles");
    }

    static void runAuction() throws Exception {
        CartagoContext ctx = CartagoService.startSession(ORA4MASConstants.ORA4MAS_WSNAME, new AgentIdCredential("simulator"));
        WorkspaceId wid = ctx.getJoinedWspId(ORA4MASConstants.ORA4MAS_WSNAME);

        ArtifactId g1 = ctx.makeArtifact(wid, "g1",   GroupBoard.class.getName(),  new Object[] {"examples/auction/auction-os.xml", "auctionGroup"});
        ArtifactId s1 = ctx.makeArtifact(wid, "sch1", SchemeBoard.class.getName(), new Object[] {"examples/auction/auction-os.xml", "doAuction"});

        MyAgent jaime = new MyAgent("jaime");      jaime.start();
        MyAgent olivier = new MyAgent("olivier");  olivier.start();
        MyAgent jomi = new MyAgent("jomi");        jomi.start();

        Thread.sleep(200);

        jaime.execOp(g1, "debug", "inspector_gui(on)");
        jaime.execOp(s1, "debug", "inspector_gui(on)");

        jaime.execOp(g1, "adoptRole", "auctioneer");
        try {
            jaime.execOp(g1, "adoptRole", "participant"); // should cause an error
            System.out.println("NOK - problem");
        } catch (Exception e) {
            System.out.println("Ok");
        }
        jomi.execOp(g1, "adoptRole", "participant");
        olivier.execOp(g1, "adoptRole", "participant");

        jaime.execOp(g1, "addScheme", "sch1");
        Thread.sleep(500);
        //ActionFeedback af = jaime.execOp("g1", "addScheme", "sch1");
        //af.waitForCompletion();

        jaime.execOp(s1, "commitMission", "mAuctioneer");
        olivier.execOp(s1, "commitMission", "mParticipant");
        jomi.execOp(s1, "commitMission", "mParticipant");
        jaime.execOp(s1, "setArgumentValue", "auction N 45");

        jaime.execOp(s1, "goalAchieved", "start");
        olivier.execOp(s1, "goalAchieved", "bid");
        jomi.execOp(s1, "goalAchieved", "bid");

        jaime.execOp(s1, "setArgumentValue", "winner W olivier");
        jaime.execOp(s1, "goalAchieved", "winner");
    }

}

class MyAgent extends Agent {

    public MyAgent(String name) {
        super(name);
        try {
            joinWorkspace(ORA4MASConstants.ORA4MAS_WSNAME, new AgentIdCredential(getAgentName()));
            System.out.println("in "+ORA4MASConstants.ORA4MAS_WSNAME+" wks");
        } catch (CartagoException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Percept p = waitForPercept();
                System.out.println("Perception: "+p+"\n");
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

    void execOp(ArtifactId aid, final String op, final String args) throws Exception {
        if (args.length() == 0) {
            doAction(aid, new Op(op), -1);
        } else {
            doAction(aid, new Op(op, (Object[])args.split(" ")), -1);
        }
    }
}
