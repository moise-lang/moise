package ora4mas.nopl.oe;

import java.io.Serializable;

import jaca.ToProlog;

public class Player implements ToProlog, Serializable, Comparable<Player> {

    private String ag; // the agent
    private String rm; // the role/mission
    
    public Player(String ag, String t) {
        this.ag = ag;
        this.rm = t;
    }
    
    public String getAg() {
        return ag;
    }
    public String getTarget() {
        return rm;
    }
    
    @Override
    public int hashCode() {
        return ag.hashCode() + rm.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (! (obj instanceof Player)) return false;
        Player o = (Player)obj;
        return o.ag.equals(this.ag) && o.rm.equals(this.rm);
    }

    public int compareTo(Player o) {
        int a = ag.compareTo(o.ag);
        if (a != 0)
            return a;
        return rm.compareTo(o.rm);
    }
    
    @Override
    public String toString() {
        return ag + " -> " + rm;
    }
    
    public String getAsPrologStr() {
        return "player(" + ag + "," + rm + ")";
    }
}
