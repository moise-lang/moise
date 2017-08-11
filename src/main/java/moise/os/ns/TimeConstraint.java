package moise.os.ns;

import java.io.Serializable;

/**
 * Represents a TimeContraint (now it is just a String!).
 *
 * @author Jomi Fred Hubner
 */
public class TimeConstraint implements Serializable {

    private static final long serialVersionUID = 1L;

    String tc = "All";

    public TimeConstraint(String desc) {
        tc = desc;
    }

    public String getTC() {
        return tc;
    }

    public String toString() {
        return tc;
    }
}
