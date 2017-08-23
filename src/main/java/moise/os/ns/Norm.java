package moise.os.ns;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import moise.common.MoiseConsistencyException;
import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.os.fs.Mission;
import moise.os.ns.NS.OpTypes;
import moise.os.ss.Role;
import moise.xml.ToXML;

/**
 * Represents a norm (permission, obligation, ...) from a role to a mission.
 *
 * @navassoc - role - Role
 * @navassoc - mission - Mission
 *
 * @author Jomi Fred Hubner
 */
public class Norm extends MoiseElement implements ToXML {

    private static final long serialVersionUID = 1L;

    protected String         condition = "";
    protected Role           role = null;
    protected Mission        mission = null;
    protected TimeConstraint tc = null;
    protected NS             ns = null;
    protected OpTypes        op = OpTypes.obligation;

    public Norm(NS ns) throws MoiseConsistencyException {
        this.ns = ns;
    }

    public Norm(Role r, Mission m, NS ns, OpTypes op) throws MoiseConsistencyException {
        super();
        setRole(r);
        setMission(m);
        setType(op);
        this.ns = ns;
    }

    public void setType(OpTypes op) {
        this.op = op;
    }

    public OpTypes getType() {
        return op;
    }

    public void setRole(Role r) throws MoiseConsistencyException {
        if (r == null) {
            throw new MoiseConsistencyException("the role can not be null in a norm.");
        }
        role = r;
    }

    public void setRole(String roleId) throws MoiseConsistencyException {
        Role r = ns.getOS().getSS().getRoleDef(roleId);
        if (r == null)
            throw new MoiseConsistencyException("the role "+roleId+" is not defined and thus can not be used in a norm.");
        setRole(r);
    }

    public Role getRole() {
        return role;
    }

    public void setMission(Mission m) throws MoiseConsistencyException {
        if (m == null) {
            throw new MoiseConsistencyException("the mission can not be null in a norm.");
        }
        mission = m;
    }

    public void setMission(String missionId) throws MoiseConsistencyException {
        Mission m = ns.getOS().getFS().findMission(missionId);
        if (m == null)
            throw new MoiseConsistencyException("the mission "+missionId+" is not defined and thus can not be used in a norm.");
        setMission(m);
    }

    public Mission getMission() {
        return mission;
    }

    public void setTimeConstraint(TimeConstraint  t) {
        tc = t;
    }
    public TimeConstraint getTimeConstraint() {
        return tc;
    }

    public String getCondition() {
        if (condition == null || condition.length() == 0)
            return "true";
        else
            return condition;
    }
    public void setCondition(String r) {
        condition = r;
    }


    public String toString() {
        return op + "(" + role +","+ mission +","+ tc + ")";
    }

    public static String getXMLTag() {
        return "norm";
    }

    /** returns a string representing the goal in Prolog syntax, format:
     *     norm(id, role, type, mission)
     */
    public String getAsProlog() {
        StringBuilder s = new StringBuilder("norm("+getId()+",");
        s.append(getRole()+",");
        s.append(getType()+",");
        s.append(getMission()+")");
        return s.toString();
    }
    
    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("id", getId());
        ele.setAttribute("type", getType().toString());
        ele.setAttribute("role", getRole().getId());
        ele.setAttribute("mission", getMission().getId());
        if (getTimeConstraint() != null && getTimeConstraint().getTC().length() > 0) {
            ele.setAttribute("time-constraint", getTimeConstraint()+"");
        }
        if (!getCondition().equals("true")) {
            ele.setAttribute("condition", getCondition());
        }
        if (getProperties().size() > 0) {
            ele.appendChild(getPropertiesAsDOM(document));
        }
        return ele;
    }

    public void setFromDOM(Element ele) throws MoiseException {
        setPropertiesFromDOM(ele);
        setId(ele.getAttribute("id"));
        setType(OpTypes.valueOf(ele.getAttribute("type")));
        setRole(ele.getAttribute("role"));
        setMission(ele.getAttribute("mission"));
        if (ele.getAttribute("time-constraint") != null && ele.getAttribute("time-constraint").length()>0) {
            setTimeConstraint(new TimeConstraint(ele.getAttribute("time-constraint")));
        }
        if (ele.getAttribute("condition") != null && ele.getAttribute("condition").length()>0) {
            setCondition(ele.getAttribute("condition"));
        }
    }
}
