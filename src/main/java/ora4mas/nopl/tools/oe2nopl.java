package ora4mas.nopl.tools;

import jason.asSyntax.Literal;

import java.util.List;

import ora4mas.nopl.oe.Group;
import ora4mas.nopl.oe.Scheme;


/** translates OE simple model to NP facts */
public class oe2nopl {
    
    /** transforms a Group Instance into NPL code (dynamic facts) */    
    public static List<Literal> transform(Group g) {
        return g.transform();
    }

    /** transforms a Scheme Instance into NPL code (dynamic facts) */    
    public static List<Literal> transform(Scheme sch) {
        return sch.transform();
    }
}
