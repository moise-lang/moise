package ora4mas.light;

import java.util.logging.Logger;

import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import cartago.OperationException;
import jason.asSyntax.Atom;
import jason.util.Config;
import moise.common.MoiseException;
import npl.parser.ParseException;
import ora4mas.nopl.OrgBoard;
import ora4mas.nopl.WebInterface;

/** Artifact that manages an organizational entity (its groups, schemes, ....)
 *  for Moise light
 *
 * <b>Operations</b> (see details in method list below):
 * <ul>
 * <li>createGroup
 * <li>destroyGroup
 * <li>createScheme
 * <li>destroyScheme
 * </ul>
 *
 * <b>Observable properties</b>:
 * <ul>
 * <li>group(group_id, artid): group_id of type group_type exists in the organisational entity
 * <li>scheme(scheme_id, artid): scheme_id of type scheme_type exists in the organisational entity
 * </ul>
 *
 */
public class LightOrgBoard extends OrgBoard {

    protected Logger logger = Logger.getLogger(LightOrgBoard.class.getName());
    
    /**
     * Initialises the org board
     *
     * @throws MoiseException   if grType was not specified
     * @throws OperationException if parentGroupId doesn't exit
     */
    public void init() throws ParseException, MoiseException, OperationException {

        if (! "false".equals(Config.get().getProperty(Config.START_WEB_OI))) {
            WebInterface w = WebInterface.get();
            try {
                this.oeId = this.getCreatorId().getWorkspaceId().getName();

                w.registerOSBrowserView(this.oeId, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        orgBoards.add(this);
    }
    
    @OPERATION public void createGroup(String id, OpFeedbackParam<ArtifactId> gaid) throws OperationException {
        ArtifactId aid;
        try {
            aid = lookupArtifact(id);
            failed("Artifact with id "+id+" already exists!");
        } catch (OperationException e) {
            aid = makeArtifact(id, getGroupBoardClass(), new ArtifactConfig());
            grPostCreation(id, aid);
            aids.put(id, aid);
            defineObsProperty("group", new Atom(id), new Atom("untyped"), aid);
            gaid.set(aid);
        }
    }

    @Override
    protected String getGroupBoardClass() {
        return LightGroupBoard.class.getName();
    }
    
    @OPERATION public void createScheme(String id, OpFeedbackParam<ArtifactId> said) throws OperationException {
        ArtifactId aid;
        try {
            aid = lookupArtifact(id);
            failed("Artifact with id "+id+" already exists!");
        } catch (OperationException e) {
            aid = makeArtifact(id, getSchemeBoardClass(), new ArtifactConfig() );
            schPostCreation(id, aid);
            aids.put(id, aid);
            defineObsProperty("scheme", new Atom(id), new Atom("untyped"), aid);
            said.set(aid);
        }
    }
    
    @Override
    protected String getSchemeBoardClass() {
        return LightSchemeBoard.class.getName();
    }
}
