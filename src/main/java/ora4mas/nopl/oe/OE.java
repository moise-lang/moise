package ora4mas.nopl.oe;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
/**
 * Organisational Entity
 *
 * @composed - groups * Group
 * @composed - schemes * Scheme
 *
 * @author jomi
 */
public class OE implements Serializable {
    Map<String, Group>  groups  = new HashMap<>();
    Map<String, Scheme> schemes = new HashMap<>();

    public void addGroup(Group g) {
        groups.put(g.getId(), g);
    }
    public Group getGroup(String id) {
        return groups.get(id);
    }
    public void addScheme(Scheme s) {
        schemes.put(s.getId(), s);
    }
    public Scheme getScheme(String id) {
        return schemes.get(id);
    }
    public Scheme removeSch(String id) {
        return schemes.remove(id);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("--- (simple) OE ---\n\n");
        for (Group g: groups.values())
            out.append(g+"\n\n");
        for (Scheme s: schemes.values())
            out.append(s+"\n\n");
        return out.toString();
    }
}
