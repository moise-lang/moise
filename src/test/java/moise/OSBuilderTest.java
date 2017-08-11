package moise;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import moise.os.OS;
import moise.os.OSBuilder;

public class OSBuilderTest {


    @Test
    public void testOS1() throws Exception {
        OSBuilder b = new OSBuilder();

        b.addRootGroup("r");
        b.addSubGroup("r", "g1");
        b.addRole("g1", "r1");

        b.addScheme("st", "job_delivered");

        /*
        <goal id="job_delivered">
            <plan operator="parallel">
                <goal id="assist">
                    <plan operator="sequence">
                        <goal id="go_to_buy" />
                        <goal id="buy_items" />
                        <goal id="go_to_workshop" />
                        <goal id="assist_assemble" />
                    </plan>
                </goal>
                <goal id="assemble">
                    <plan operator="sequence">
                        <goal id="go_to_workshop" />
                        <goal id="assemble" />
                        <goal id="stop_assist_assemble" />
                        <goal id="go_to_storage" />
                        <goal id="deliver_job" />
                    </plan>
                </goal>
            </plan>
        </goal>
        */

        b.addGoal("st", "job_delivered", "assist || assemble");
        b.addGoal("st", "assist", "go_to_buy, buy_items, go_to_workshop, assist_assemble");
        b.addGoal("st", "assemble", "go_to_workshop, do_assemble, stop, deliver");
        b.addGoalArg("st", "buy_items", "Item", "");
        b.addMission("st", "mag1", "go_to_buy,go_to_workshop,go_to_workshop,stop");
        b.addMission("st", "mag2", "buy_items,do_assemble");

        b.getOS().getNS().setProperty("mission_permission", "ignore");

        b.save("test.xml");
        assertNotNull( OS.loadOSFromURI("test.xml") );
    }
}
