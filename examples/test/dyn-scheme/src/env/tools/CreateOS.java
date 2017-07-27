// CArtAgO artifact code for project dyn-scheme

package tools;

import cartago.*;
import moise.os.*;

public class CreateOS extends Artifact {

    @OPERATION
    void create(String file) throws Exception {
        OSBuilder b = new OSBuilder();
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

        b.addMission("st", "mag1", "go_to_buy,go_to_workshop,go_to_workshop,stop,do_assemble,deliver");
        b.addMission("st", "mag2", "buy_items,assist_assemble");

        b.getOS().getNS().setProperty("mission_permission", "ignore");

        b.save(file);
    }
}
