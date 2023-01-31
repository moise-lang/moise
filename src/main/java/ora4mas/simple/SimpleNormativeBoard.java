package ora4mas.simple;

import java.util.logging.Logger;

import cartago.LINK;
import cartago.OPERATION;
import moise.common.MoiseException;
import npl.INorm;
import npl.Norm;
import npl.NormFactory;
import npl.parser.ParseException;
import ora4mas.nopl.NormativeBoard;
import ora4mas.nopl.SchemeBoard;

/**
 * Artifact to manage a normative program (NPL)
 * <br/><br/>
 *
 * <b>Operations</b> (see details in the methods list below):
 * <ul>
 * <li>load a NPL program
 * <li>addFact
 * <li>removeFact
 * <li>addNorm
 * </ul>
 *
 * <b>Observable properties</b>:
 * <ul>
 * <li>obligation(ag,reason,goal,deadline): current active obligations.</br>
 *     e.g. <code>obligation(bob,n4,committed(ag2,mBib,s1),1475417322254)</code>
 * </ul>
 *
 * <b>Signals</b> the same signals of SchemeBoard.
 * </ul>
 *
 * @see SchemeBoard
 * @author Jomi
 */
public class SimpleNormativeBoard extends NormativeBoard {

    int id = 0;
    NormFactory factory = Norm.getFactory();
    
    protected Logger logger = Logger.getLogger(SimpleNormativeBoard.class.getName());

    @OPERATION @LINK public void addNorm(String type, String role, String mission) throws MoiseException, ParseException {
        try {
            /*
            moise.os.ns.Norm mn = new moise.os.ns.Norm(new Role(role,null), new Mission(mission,null), null, OpTypes.valueOf(type));
            //logger.info("** moise "+mn);
            String nopln = os2nopl.generateNormEntry(mn, new Cardinality(1,1));
            */

            StringBuilder out = new StringBuilder();

            //out.append("scope npl_norms_for_moise_light_"+getId()+" {\n");
            out.append("norm aid"+(id++)+": "+
                    "scheme_id(S) & responsible(Gr,S) & fplay(A,"+role+",Gr) "+
                    "-> "+type+"(A,true,committed(A,"+mission+",S),`never`).");
            //out.append("\n}");
            //load(out.toString());
            
            //logger.info("** nopl "+out);

            
            /*nplp parser = new nplp(new StringReader(out.toString()));
            parser.setDFP(this);
            INorm n = parser.norm();*/
            
            INorm n = factory.parseNorm(out.toString(), this);
            //logger.info("** parsed norm "+n);
            
            nengine.addNorm(n);
            
            nengine.verifyNorms();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
