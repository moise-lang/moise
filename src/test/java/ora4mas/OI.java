package ora4mas;

import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.Iterator;

import moise.os.OS;
import npl.DynamicFactsProvider;
import npl.NPLInterpreter;
import npl.NormativeFailureException;
import npl.NormativeProgram;
import npl.Scope;
import npl.parser.ParseException;
import npl.parser.nplp;
import ora4mas.nopl.oe.Group;
import ora4mas.nopl.oe.OE;
import ora4mas.nopl.oe.Scheme;
import ora4mas.nopl.tools.os2nopl;

/** simple OI used to test one scheme */
public class OI implements DynamicFactsProvider {

    NPLInterpreter schInterpreter = new NPLInterpreter();
    Scheme sch;
    moise.os.fs.Scheme spec;
    OE     oe = new OE();
    OS     os;
    
    public OI(String osFile, String type, String schId) throws FileNotFoundException, ParseException {
        os = OS.loadOSFromURI(osFile);
        spec = os.getFS().findScheme(type);
        sch = new Scheme(spec, schId);

        NormativeProgram p = new NormativeProgram();
        new nplp(new StringReader(os2nopl.transform(os))).program(p, this);
        Scope root = p.getRoot();
        Scope scope = root.findScope("scheme("+type+")");
        schInterpreter.setScope(scope);
    }
    
    public void setGroup(Group g) {
        oe.addGroup(g);
    }
    public Scheme getScheme() {
        return sch;
    }
    
    public NPLInterpreter getNPLI() {
        return schInterpreter;
    }
    
    public Literal execute(Literal action)  {
        Scheme schbak = sch.clone();
        if (action.getFunctor().equals("commitMission")) {
            sch.addPlayer(action.getTerm(0).toString(), action.getTerm(1).toString());
        } else if (action.getFunctor().equals("leaveMission")) {
            sch.removePlayer(action.getTerm(0).toString(), action.getTerm(1).toString());
        } else if (action.getFunctor().equals("setGoalAchieved")) {
            sch.addGoalAchieved(action.getTerm(0).toString(), action.getTerm(1).toString());
        }
        //System.out.println("** action "+action+ " oe: "+oe2np.transform(sch));
        try {
            schInterpreter.verifyNorms();
            if (action.getFunctor().equals("setGoalAchieved")) {
                if (sch.computeSatisfiedGoals()) {
                    //schInterpreter.setDynamicFacts(oe2nopl.transform(sch)); 
                    schInterpreter.verifyNorms();
                }
            }
            //System.out.println("new obl:"+obl);
            //System.out.println("all obl:" + schInterpreter.getSource(NPLInterpreter.OBLAtom));
        } catch (NormativeFailureException e) {
            //System.out.println("** "+e.getFail());
            sch = schbak; // takes the backup scheme as the current since the action failed
            return e.getFail();
        }
        return null;
    }

    public boolean isRelevant(PredicateIndicator pi) {
        return sch.isRelevant(pi);
    }
    public Iterator<Unifier> consult(Literal l, Unifier u) {
        return sch.consult(l, u);
    }

}
