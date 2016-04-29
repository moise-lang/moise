package moise.reorg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import moise.common.MoiseException;
import moise.oe.OE;
import moise.os.OS;
import moise.reorg.commands.ChangeOS;
import moise.reorg.commands.ChangeRoleCardinality;

/**
 * A reorganisation plan
 *
 * @author  jomi
 */
@SuppressWarnings("unchecked")
public class ReorgPlan extends ArrayList implements Serializable {

    private static final long serialVersionUID = 1L;

    public static Class classChangeRoleDefProperties;
    public static Class classChangeDS;

    static {
        try {
            classChangeRoleDefProperties = Class.forName("moise.reorg.commands.ChangeRoleDefProperties");
            classChangeDS                = Class.forName("moise.reorg.commands.ChangeDS");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    String proposer;
    
    public ReorgPlan(String proposer) {
        this.proposer = proposer;
    }
    
    public String getProposer() {
        return proposer;
    }
    
    public Class getFocus() {
        Class c = null;
        Iterator i = iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (c == null) {
                c = o.getClass();
            } else {
                // see the most general between c and o
                c = mostGeneral(c, o.getClass());
            }
        }
        return c;
    }
    
    /** returns true if the plan contains some action of the class <i>c</i> */
    public boolean contains(Class c) {
        return contains( getFocus(), c);
    }
    
    public void execute(OE oe) throws MoiseException {
        OS os = oe.getOS();
        os.setProperty("creator", proposer);
        Iterator i = iterator();
        while (i.hasNext()) {
            ChangeOS c = (ChangeOS) i.next();
            c.execute( os, oe );
        }
    }
    
    public String toString() {
        return "reorganization plan from "+proposer+" (focus="+getFocus()+") ";//+super.toString();
    }
    
    static private Class mostGeneral(Class c1, Class c2) {
        while ( ! contains(c1, c2)) {
            c1 = c1.getSuperclass();
        }
        return c1;
    }
    
    /** returns true if c1 is a super class of c2 */
    static public boolean contains(Class c1, Class c2) {
        if (c1 == null || c2 == null) {
            return false;
        }
        if (c2.getName().equals("java.lang.Object")) {
            return false;
        } else if (c2.equals(c1)) {
            return true;
        } else {
            return contains(c1, c2.getSuperclass());
        }
    }
    
    // for testing
    
    public static void main(String[] a) {
        try {
            //Class o1 = Class.forName("moise.reorg.commands.ChangeRoleDefProperties"); //new ChangeRoleDefProperties().getClass();
            Class o2 = new ChangeRoleCardinality().getClass();
            Class o3 = new ChangeOS().getClass();
            System.out.println("mostGeneral(ChangeRoleProperties,ChangeRoleProperties)="+mostGeneral(classChangeRoleDefProperties,classChangeRoleDefProperties));
            System.out.println("mostGeneral(ChangeRoleProperties,ChangeRoleCardinality)="+mostGeneral(classChangeRoleDefProperties,o2));
            System.out.println("mostGeneral(ChangeRoleProperties,ChangeOS)="+mostGeneral(classChangeRoleDefProperties,o3));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
