package moise;

import moise.os.OS;
import moise.os.fs.FS;
import moise.os.fs.Goal;
import org.junit.Test;

import java.util.Collection;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DependenciesOrderTest {
    @Test
    public void dependenciesOrderTest() {
        FS orderedFS = Objects.requireNonNull(OS.loadOSFromURI("examples/smart-farming/ordered.xml")).getFS();
        FS unorderedFS = Objects.requireNonNull(OS.loadOSFromURI("examples/smart-farming/unordered.xml")).getFS();

        dependenciesEqualityTest(orderedFS, unorderedFS);
        schemeGoalsEqualityTest(orderedFS, unorderedFS);
    }

    private void dependenciesEqualityTest(FS o, FS u) {
        assertEquals(
                o.findGoal("SprayPesticides").getDependencies(),
                u.findGoal("SprayPesticides").getDependencies());
    }

    private void schemeGoalsEqualityTest(FS o, FS u) {
        Collection<Goal> oGoals = o.findScheme("FarmScheme").getGoals();
        Collection<Goal> uGoals = u.findScheme("FarmScheme").getGoals();
        assertTrue(oGoals.size() == uGoals.size() && oGoals.containsAll(uGoals) && uGoals.containsAll(oGoals));
    }
}
