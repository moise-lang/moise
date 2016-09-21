package ora4mas;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cartago.AgentIdCredential;
import cartago.ArtifactId;
import cartago.CartagoContext;
import cartago.CartagoService;
import cartago.WorkspaceId;
import npl.parser.ParseException;
import ora4mas.nopl.GroupBoard;
import ora4mas.nopl.ORA4MASConstants;

/** JUnit test case for syntax package */
public class CartagoTest {

    @Test
    public void createArt() throws ParseException, Exception {
        CartagoService.startNode();
        CartagoService.createWorkspace(ORA4MASConstants.ORA4MAS_WSNAME);
        CartagoContext ctx = CartagoService.startSession(CartagoService.MAIN_WSP_NAME, new AgentIdCredential("simulator"));
        ctx = CartagoService.startSession(ORA4MASConstants.ORA4MAS_WSNAME, new AgentIdCredential("simulator"));
        WorkspaceId wid = ctx.getJoinedWspId(ORA4MASConstants.ORA4MAS_WSNAME);
        ArtifactId g1 = ctx.makeArtifact(wid, "g1",   GroupBoard.class.getName(),  new Object[] {"examples/writePaper/wp-os.xml", "wpgroup"});
        assertNotNull(g1);
        
        ArtifactId[] arts = CartagoService.getController(ORA4MASConstants.ORA4MAS_WSNAME).getCurrentArtifacts();
        assertTrue(arts.length > 0);
        boolean has = false;
        for (ArtifactId aid: arts) {
            if (aid.getName().equals("g1")) {
                has = true;
                break;
            }
        }
        assertTrue(has);
   }
}
