package ora4mas.nopl;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import cartago.Artifact;
import cartago.ArtifactConfig;
import cartago.ArtifactId;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import cartago.OperationException;
import jason.asSyntax.Atom;
import jason.util.Config;
import moise.common.MoiseException;
import moise.os.OS;
import moise.xml.DOMUtils;
import moise.xml.ToXML;
import npl.parser.ParseException;

/** Artifact that manages an organizational entity (its groups, schemes, ....)
 *
 * <b>Operations</b> (see details in method list below):
 * <ul>
 * <li>createGroup
 * <li>removeGroup
 * <li>createScheme
 * <li>removeScheme
 * </ul>
 *
 * <b>Observable properties</b>:
 * <ul>
 * <li>group(group_id, group_type, artid): group_id of type group_type exists in the organisational entity
 * <li>scheme(scheme_id, scheme_type, artid): scheme_id of type scheme_type exists in the organisational entity
 * <li>specification: the OS in a prolog like representation.
 * </ul>
 *
 */
public class OrgBoard extends Artifact {

    String osFile = null;

    Map<String,ArtifactId> aids = new HashMap<String,ArtifactId>();
    protected Logger logger = Logger.getLogger(OrgBoard.class.getName());

    /**
     * Initialises the org board
     *
     * @param osFile            the organisation specification file (path and file name)
     *
     * @throws ParseException   if the OS file is not correct
     * @throws MoiseException   if grType was not specified
     * @throws OperationException if parentGroupId doesn't exit
     */
    public void init(String osFile) throws ParseException, MoiseException, OperationException {
        osFile = OrgArt.fixOSFile(osFile);
        this.osFile = osFile;
        OS os = OS.loadOSFromURI(osFile);

        defineObsProperty(SchemeBoard.obsPropSpec, new JasonTermWrapper(os.getAsProlog()));

        if (! "false".equals(Config.get().getProperty(Config.START_WEB_OI))) {
            WebInterface w = WebInterface.get();
            try {
                String osSpec = specToStr(os, DOMUtils.getTransformerFactory().newTransformer(DOMUtils.getXSL("os")));
                String oeId = getCreatorId().getWorkspaceId().getName();

                w.registerOSBrowserView(oeId, os.getId(), osSpec);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public String specToStr(ToXML spec, Transformer transformer) throws Exception {
        StringWriter so = new StringWriter();
        InputSource si = new InputSource(new StringReader(DOMUtils.dom2txt(spec)));
        transformer.transform(new DOMSource(getParser().parse(si)), new StreamResult(so));
        return so.toString();
    }

    private DocumentBuilder parser;
    public DocumentBuilder getParser() throws ParserConfigurationException {
        if (parser == null)
            parser = DOMUtils.getParser();
        return parser;
    }

    @OPERATION public void createGroup(String id, String type, OpFeedbackParam<ArtifactId> gaid) throws OperationException {
        ArtifactId aid;
        try {
            aid = lookupArtifact(id);
            failed("Artifact with id "+id+" already exists!");
        } catch (OperationException e) {
            aid = makeArtifact(id, GroupBoard.class.getName(), new ArtifactConfig(osFile, type) );
            aids.put(id, aid);
            defineObsProperty("group", new Atom(id), new Atom(type), aid);
            gaid.set(aid);
        }
    }

    @OPERATION public void removeGroup(String id) {
        try {
            ArtifactId aid = aids.remove(id);
            if (aid == null) {
                failed("there is no group board for "+id);
            }
            removeObsPropertyByTemplate("group", new Atom(id), null, null);

            execLinkedOp(aid, "destroy");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OPERATION public void createScheme(String id, String type, OpFeedbackParam<ArtifactId> said) throws OperationException {
        ArtifactId aid;
        try {
            aid = lookupArtifact(id);
            failed("Artifact with id "+id+" already exists!");
        } catch (OperationException e) {
            aid = makeArtifact(id, SchemeBoard.class.getName(), new ArtifactConfig(osFile, type) );
            aids.put(id, aid);
            defineObsProperty("scheme", new Atom(id), new Atom(type), aid);
            said.set(aid);
        }
    }

    @OPERATION public void removeScheme(String id) {
        try {
            ArtifactId aid = aids.remove(id);
            if (aid == null) {
                failed("there is no scheme board for "+id);
            }
            removeObsPropertyByTemplate("scheme", new Atom(id), null, null);

            execLinkedOp(aid, "destroy");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
