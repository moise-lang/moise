
/**
 *    This program creates an OE with the JojOS (RoboCup) and
 *    "runs" some social events on it by calling the OE's methods.
 */

import moise.oe.*;
import moise.xml.DOMUtils;

public class TutorialSS {

    public static void createSS(OE currentOE) {
        try {

            // Group creation
            GroupInstance   team    = currentOE.addGroup("team");
            GroupInstance   att     = team.addSubGroup("attack");
            GroupInstance   def     = team.addSubGroup("defense");

            // Agent entrance
            OEAgent marcos    = currentOE.addAgent("Marcos");
            OEAgent lucio     = currentOE.addAgent("Lucio");
            OEAgent edmilson  = currentOE.addAgent("Edmilson");
            OEAgent roqueJr   = currentOE.addAgent("Roque Jr.");
            OEAgent cafu      = currentOE.addAgent("Cafu");
            OEAgent gilberto  = currentOE.addAgent("Gilberto Silva");
            OEAgent juninho   = currentOE.addAgent("Juninho");
            OEAgent ronaldinho= currentOE.addAgent("Ronaldinho");
            OEAgent roberto   = currentOE.addAgent("Roberto Carlos");
            OEAgent ronaldo   = currentOE.addAgent("Ronaldo");
            OEAgent rivaldo   = currentOE.addAgent("Rivaldo");
            OEAgent scolari   = currentOE.addAgent("Scolari");

            // Role Adoption
            marcos.adoptRole("goalkeeper", def);
            lucio.adoptRole("back", def);
            edmilson.adoptRole("back", def);
            roqueJr.adoptRole("back", def);

            cafu.adoptRole("leader", att);
            cafu.adoptRole("middle", att);
            gilberto.adoptRole("middle", att);
            juninho.adoptRole("middle", att);
            ronaldinho.adoptRole("middle", att);
            roberto.adoptRole("middle", att);
            ronaldo.adoptRole("attacker", att);
            rivaldo.adoptRole("attacker", att);

            scolari.adoptRole("coach", team);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        try {

            // OE creation
            OE currentOE = OE.createOE("winGame", "examples/tutorial/jojOS.xml");

            createSS(currentOE);

            System.out.println("final OE in XML format:\n"+DOMUtils.dom2txt(currentOE));

            new moise.tools.SimOE(currentOE);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
