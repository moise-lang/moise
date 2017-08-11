package moise;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import moise.oe.GoalInstance;
import moise.oe.OE;
import moise.oe.SchemeInstance;
import moise.os.fs.Goal;

public class ToPrologTest {

    OE oe;
    SchemeInstance sch;

    @Before
    public void setUp() throws Exception {
        oe = OE.createOE("test", "examples/tutorial/jojOS.xml");
        sch = oe.startScheme("sideAttack");
    }

    @Test
    public void testGoalInstance() throws Exception {
        GoalInstance gi = sch.getGoal("g3");
        assertEquals(gi.getAsProlog(),"g3(M2Ag)");
        gi.setArgumentValue("M2Ag", "carlos");
        assertEquals(gi.getAsProlog(),"g3(carlos)");
    }

    @Test
    public void testGoalSpec() {
        Goal g = sch.getSpec().getGoal("g3");
        assertEquals("goal(g3,performance,\"kick the ball to the m2Ag\",1,\"infinity\",[M2Ag],noplan)", g.getAsProlog());
        g = sch.getSpec().getGoal("g2");
        System.out.println(g.getAsProlog());
        System.out.println(sch.getSpec().getAsProlog());
    }

    @Test
    public void testGroupToProlog() {
        //Group g = oe.getOS().getSS().getRootGrSpec().findSubGroup("attack");
        // the order changes always, improve this test
        //assertEquals("group_specification(attack,[role(middle,5,5,[leader],[link(communication,player,inter_group),link(communication,coach,inter_group)]),role(leader,0,1,[middle],[link(communication,player,inter_group),link(authority,player,inter_group),link(communication,coach,inter_group)]),role(attacker,2,2,[],[link(communication,player,inter_group),link(communication,coach,inter_group)])],[],properties([]))", g.getAsProlog());

        //g = oe.getOS().getSS().getRootGrSpec();
        // the order changes always, improve this test
        //assertEquals("group_specification(team,[role(coach,1,2,[],[link(authority,player,inter_group)])],[attack(1,1),defense(1,1)],properties([]))", g.getAsProlog());
    }

}
