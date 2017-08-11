package moise;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import moise.common.MoiseCardinalityException;
import moise.common.MoiseConsistencyException;
import moise.oe.GoalInstance;
import moise.oe.GoalInstance.GoalState;
import moise.oe.GroupInstance;
import moise.oe.OE;
import moise.oe.OEAgent;
import moise.oe.SchemeInstance;

public class OETest {

    OE currentOE;
    OEAgent lucio, roberto, rivaldo;
    SchemeInstance sa;

    @Before
    public void setUp() throws Exception {
        try {
            currentOE = OE.createOE("winGame", "examples/tutorial/jojOS.xml");

            // Group creation
            GroupInstance   team    = currentOE.addGroup("team");
            GroupInstance   att     = team.addSubGroup("attack");
            GroupInstance   def     = team.addSubGroup("defense");

            // Agent entrance
            OEAgent marcos    = currentOE.addAgent("Marcos");
            lucio     = currentOE.addAgent("Lucio");
            OEAgent edmilson  = currentOE.addAgent("Edmilson");
            OEAgent roqueJr   = currentOE.addAgent("Roque Jr.");
            OEAgent cafu      = currentOE.addAgent("Cafu");
            OEAgent gilberto  = currentOE.addAgent("Gilberto Silva");
            OEAgent juninho   = currentOE.addAgent("Juninho");
            OEAgent ronaldinho= currentOE.addAgent("Ronaldinho");
            roberto   = currentOE.addAgent("Roberto Carlos");
            OEAgent ronaldo   = currentOE.addAgent("Ronaldo");
            rivaldo   = currentOE.addAgent("Rivaldo");
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

            // start a scheme from the specification sideAttack
            sa = currentOE.startScheme("sideAttack");

            // set the argument for the goal g3
            GoalInstance g3 = sa.getGoal("g3");
            g3.setArgumentValue("M2Ag", "Cafu");

            sa.addResponsibleGroup(team);

            lucio.commitToMission("m1", sa);
            roberto.commitToMission("m2", sa);
            rivaldo.commitToMission("m3", sa);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testGroupWellForm() {
        GroupInstance team = currentOE.findInstancesOf("team").iterator().next();
        assertTrue(team != null);
        assertEquals(team.wellFormedStatus(),"ok");
    }

    @Test
    public void testSchemeWellFormed() {
        SchemeInstance sa = currentOE.findInstancesOfSchSpec("sideAttack").iterator().next();
        assertTrue(sa != null);
        assertEquals(sa.wellFormedStatus(),"ok");
    }

    @Test
    public void testMissionQty() {
        try {
            SchemeInstance sa = currentOE.findInstancesOfSchSpec("sideAttack").iterator().next();
            assertEquals(sa.getPlayers().size(),3);
            OEAgent lucio = currentOE.getAgent("Lucio");
            assertTrue(lucio != null);
            GoalInstance g1 = sa.getGoal("g1");
            g1.setImpossible(lucio);
            assertTrue(lucio.getPossibleGoals().isEmpty());
            lucio.removeMission("m1", sa);
            assertEquals(2,sa.getPlayersQty());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testPossibleGoals() throws MoiseConsistencyException, MoiseCardinalityException {
        GoalInstance g1 = sa.getGoal("g1");
        GoalInstance g2 = sa.getGoal("g2");
        GoalInstance g7 = sa.getGoal("g7");
        GoalInstance g8 = sa.getGoal("g8");
        GoalInstance g9 = sa.getGoal("g9");
        assertEquals(GoalState.enabled,g1.getState());
        assertEquals(GoalState.waiting,g2.getState());
        assertEquals(GoalState.waiting,g7.getState());
        assertTrue(g1.isEnabled());
        assertFalse(g2.isEnabled());
        assertFalse(g7.isEnabled());
        assertTrue(g1.isCommitted());
        assertFalse(g2.hasComittedAgents());

        assertEquals("[g1]", lucio.getPossibleGoals().toString());
        assertEquals("[]", roberto.getPossibleGoals().toString());
        assertEquals("[]", rivaldo.getPossibleGoals().toString());

        g1.setAchieved(lucio);

        assertEquals("[g7]", lucio.getPossibleGoals().toString());
        assertEquals("[g8]", roberto.getPossibleGoals().toString());
        assertEquals("[g9]", rivaldo.getPossibleGoals().toString());
        assertEquals(GoalState.satisfied,g1.getState());
        assertEquals(GoalState.waiting,g2.getState());
        assertFalse(g2.isEnabled());
        assertEquals(GoalState.enabled,g7.getState());
        assertEquals(GoalState.enabled,g8.getState());
        assertEquals(GoalState.enabled,g9.getState());

        g7.setAchieved(lucio);

        assertEquals("[]", lucio.getPossibleGoals().toString());
        assertEquals("[g8]", roberto.getPossibleGoals().toString());
        assertEquals("[g9]", rivaldo.getPossibleGoals().toString());
        assertEquals(GoalState.satisfied,g1.getState());
        assertEquals(GoalState.satisfied,g7.getState());
        assertEquals(GoalState.enabled,g8.getState());
        assertEquals(GoalState.enabled,g9.getState());

        g8.setAchieved(roberto);
        g9.setAchieved(rivaldo);

        // g2 should be automatically achieved
        assertEquals(GoalState.satisfied,g2.getState());

        assertEquals(GoalState.satisfied,g2.getState());
        assertEquals("[g3]", lucio.getPossibleGoals().toString());
        assertEquals("[]", roberto.getPossibleGoals().toString());
        assertEquals("[]", rivaldo.getPossibleGoals().toString());

    }

}
