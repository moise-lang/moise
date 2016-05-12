package ora4mas;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;

import jason.asSyntax.ASSyntax;
import junit.framework.TestCase;
import moise.os.OS;
import npl.NormativeProgram;
import npl.parser.ParseException;
import npl.parser.nplp;
import ora4mas.nopl.tools.os2nopl;

/** JUnit test case for syntax package */
public class TranslationTest extends TestCase {

    public void testWP() throws ParseException, Exception {
        OS os = OS.loadOSFromURI("src/examples/writePaper/wp-os.xml");
        
        String np = os2nopl.transform(os);
        //System.out.println(np);
        BufferedWriter out = new BufferedWriter(new FileWriter("src/examples/writePaper/wp-gen.npl"));
        out.write(np);
        out.close();
        NormativeProgram p = new NormativeProgram();
        new nplp(new StringReader(np)).program(p, null);
        assertEquals(11, p.getRoot().getScope(ASSyntax.parseLiteral("scheme(writePaperSch)")).getNorms().size());
    }

    public void testGgroupWithoutRole() throws ParseException, Exception {
        OS os = OS.loadOSFromURI("src/examples/test/groupwithoutrole.xml");
        
        String np = os2nopl.transform(os);
        //System.out.println(np);
        BufferedWriter out = new BufferedWriter(new FileWriter("src/examples/test/groupwithoutrole.npl"));
        out.write(np);
        out.close();
        NormativeProgram p = new NormativeProgram();
        new nplp(new StringReader(np)).program(p, null);
    }

}
