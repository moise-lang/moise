package ora4mas;

import jason.asSyntax.ASSyntax;

import java.io.StringReader;

import junit.framework.TestCase;
import moise.os.OS;
import npl.NPLInterpreter;
import npl.NPLLiteral;
import npl.NormativeProgram;
import npl.parser.ParseException;
import npl.parser.nplp;
import ora4mas.nopl.oe.Group;
import ora4mas.nopl.tools.os2nopl;

public class NPLInterpreterTestSubGroups extends TestCase {

    public void testWPSubGroup1() throws ParseException, Exception {

        OS os = OS.loadOSFromURI("src/test/jcm/subgroups.xml");

        String np = os2nopl.transform(os);
        //System.out.println(np);
        //BufferedWriter out = new BufferedWriter(new FileWriter("examples/test/subgroups.npl"));
        //out.write(np);
        //out.close();
        Group gr = new Group("ig1");
        NormativeProgram p = new NormativeProgram();
        new nplp(new StringReader(np)).program(p, gr);

        //System.out.println(np);

        NPLInterpreter i = new NPLInterpreter();
        i.loadNP(p.getRoot().getScope(ASSyntax.parseLiteral("group(g1)")));

        assertFalse(i.holds(ASSyntax.parseLiteral("well_formed(ig1)")));

        // add players
        Group ig2 = gr.addSubgroup("ig2", "g2", "ig1");
        gr.addSubgroup("ig4", "g4", "ig1");;
        assertFalse(i.holds(ASSyntax.parseLiteral("well_formed(ig1)")));
        assertTrue(i.holds(new NPLLiteral(ASSyntax.parseLiteral("subgroup(ig2, g2, ig1)"), gr)));

        assertFalse(gr.isSubgroupWellformed("ig2"));
        gr.setSubgroupWellformed("ig2", true);
        assertTrue(gr.isSubgroupWellformed("ig2"));
        gr.setSubgroupWellformed("ig2", false);
        assertFalse(gr.isSubgroupWellformed("ig2"));
        gr.setSubgroupWellformed("ig2", true);
        assertTrue(gr.isSubgroupWellformed("ig2"));

        gr.setSubgroupWellformed("ig4", true);

        gr.addPlayer("bob", "r1");
        ig2.addPlayer("alice", "r2");
        assertTrue(i.holds(new NPLLiteral(ASSyntax.parseLiteral("play(alice, r2, ig2)"), gr)));

        assertTrue(i.holds(new NPLLiteral(ASSyntax.parseLiteral("subgroup_well_formed(ig2)"), gr)));
        assertTrue(i.holds(new NPLLiteral(ASSyntax.parseLiteral("subgroup_well_formed(ig4)"), gr)));
        //System.out.println(gr.toString());
        assertTrue(i.holds(new NPLLiteral(ASSyntax.parseLiteral("subgroup(ig2, g2, ig1)"), gr)));
        assertTrue(i.holds(ASSyntax.parseLiteral("well_formed(ig1)")));
    }

}
