package moise.os.ss;


/**
 * Represents a Compatibility between two roles.
 * 
 * @author Jomi Fred Hubner
 */
public class Compatibility extends RoleRel {

    
    private static final long serialVersionUID = 1L;


    /** Creates new Compatibility */
    public Compatibility(Role a, Role b, Group grSpec) {
        super(a,b);
        this.grSpec = grSpec;
    }

    /** Creates new Compatibility */
    public Compatibility(Group grSpec) {
        this(null, null, grSpec);
    }
    
    protected String getTypeStr() { 
        return "compatibility"; 
    }
    public String getXMLTag() {
        return "compatibility";
    }


    /**
     * checks if the the role r1 is compatible with r2 (considering the role inheritance), i.e., the
     * agent playing r1 can also play r2
     */
    public boolean areCompatible(Role r1, Role r2) {
        if (sourceContains(r1) && targetContains(r2)) {
            return true;
        }
        if (biDirectional) {
            return sourceContains(r2) && targetContains(r1);
        }
        return false;
    }
    
}
