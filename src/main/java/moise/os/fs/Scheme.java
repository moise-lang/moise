package moise.os.fs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import moise.common.MoiseConsistencyException;
import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.os.Cardinality;
import moise.os.CardinalitySet;
import moise.prolog.ToProlog;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

/**
 Represents a Scheme specification.

 @composed - missions * Mission
 @navassoc - goal - Goal

 @author Jomi Fred Hubner
*/
public class Scheme extends MoiseElement implements ToXML, ToProlog {

    private static final long serialVersionUID = 1L;

    protected CardinalitySet<Mission>  missions = new CardinalitySet<Mission>();
    protected Set<Plan>                plans    = new HashSet<Plan>();
    protected Map<String,Goal>         goals    = new HashMap<String,Goal>();
    protected Goal                     root     = null;
    //protected String                   monitoring  = null;
    protected FS                       fs       = null;

    public Scheme(String id, FS fs) {
        super(id);
        this.fs = fs;
    }


    public void setRoot(Goal g) {
        root = g;
        if (getGoal(g.getId()) == null)
            addGoal(g);
    }

    public Goal getRoot() {
        return root;
    }

    public FS getFS() {
        return fs;
    }

    /*
    public void setMonitoringSch(String schId) {
        monitoring = schId;
    }
    public String getMonitoringSch() {
        return monitoring;
    }
    public boolean isMonitorSch() {
        // search in groups
        for (Group g: getFS().getOS().getSS().getRootGrSpec().getAllSubGroupsTree())
            if (g.getMonitoringSch() != null && g.getMonitoringSch().equals( this.getId()) )
                return true;

        // search in schemes
        for (Scheme s: getFS().getSchemes())
            if (s.getMonitoringSch() != null && s.getMonitoringSch().equals( this.getId()) )
                return true;
        return false;
    }*/

    //
    // Plan methods
    //
    public void addPlan(Plan p) {
        plans.add(p);
    }

    // TODO: compute from goal tree
    public Collection<Plan> getPlans() {
        return plans;
    }

    //
    // Mission methods
    //
    public void addMission(Mission m) {
        missions.add(m);
    }

    public void setMissionCardinality(String missionId, Cardinality c) throws MoiseConsistencyException {
        Mission m = getMission(missionId);
        if (m == null) {
            throw new MoiseConsistencyException("Failed to register the cardinality for the mission "+missionId+", it was not defined!");
        }
        setMissionCardinality(m, c);
    }

    public void setMissionCardinality(Mission m, Cardinality c) {
        missions.setCardinality(m, c);
    }

    public Cardinality getMissionCardinality(String missionId) {
        return getMissionCardinality(getMission(missionId));
    }

    public Cardinality getMissionCardinality(Mission m) {
        return missions.getCardinality(m);
    }


    /** gets the scheme missions ordered by the preference relation */
    @SuppressWarnings("unchecked")
    public Collection<Mission> getMissions() {
        List<Mission> l = new ArrayList<Mission>( missions.getAll() );
        Collections.sort( l );
        return l;
    }

    public Mission getMission(String id) {
        if (MoiseElement.getPrefix(id) == null) {
            id = getId() + "." + id;
        }
        return missions.get(id);
    }


    //
    // Goal methods
    //
    public void addGoal(Goal g) {
        goals.put(g.getId(), g);
    }

    /**
     * returns an iterator for GoalSpec objects of this SCH
     */
    public Collection<Goal> getGoals() {
        return goals.values();
    }

    public Goal getGoal(String id) {
        return goals.get(id);
    }

    /** 
     * returns the missions where goal g is
     */
    public Set<String> getGoalMissionsId(Goal g) {
        Set<String> ms = new HashSet<String>();
        for (Mission m: missions)
            if (m.getGoals().contains(g))
                ms.add(m.getId());
        return ms;
    }

    /** returns a string representing the goal in Prolog syntax, format:
     *     scheme_specification(id, goals tree starting by root goal, missions, properties)
     */
    public String getAsProlog() {
        StringBuilder s = new StringBuilder("scheme_specification("+getId()+",");

        // goals
        s.append(getRoot().getAsProlog());

        // missions
        s.append(",[");
        String v="";
        for (Mission m: getMissions()) {
            s.append(v+m.getAsProlog());
            v=",";
        }
        s.append("],");

        // properties
        s.append(getPropertiesAsProlog());
        s.append(")");

        return s.toString();
    }

    public static String getXMLTag() {
        return "scheme";
    }

    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("id", getId());
        //if (getMonitoringSch() != null)
        //    ele.setAttribute("monitoring-scheme", getMonitoringSch());

        // properties
        if (getProperties().size() > 0) {
            ele.appendChild(getPropertiesAsDOM(document));
        }

        // goals
        ele.appendChild(getRoot().getAsDOM(document));

        // missions
        for (Mission m: getMissions()) {
            ele.appendChild(m.getAsDOM(document));
        }

        return ele;
    }

    public void setFromDOM(Element ele) throws MoiseException {
        setPropertiesFromDOM(ele);

        // monitoring-scheme
        //if (ele.getAttribute("monitoring-scheme").length() > 0)
        //    setMonitoringSch(ele.getAttribute("monitoring-scheme"));

        // root goal
        Element grEle = DOMUtils.getDOMDirectChild(ele, Goal.getXMLTag());
        Goal rootG = new Goal(grEle.getAttribute("id"));
        rootG.setFromDOM(grEle, this);
        addGoal(rootG);
        setRoot(rootG);

        // missions
        for (Element mEle: DOMUtils.getDOMDirectChilds(ele, Mission.getXMLTag())) {
            Mission m = new Mission(mEle.getAttribute("id"), this);
            m.setFromDOM(mEle);
            addMission(m);
        }

        // for goal without missions, set the minToSatisfy as 0
        for (Goal g: getGoals()) {
            if (g.getMinAgToSatisfy() != -1) // ignore goals with explicit cardinality
                continue;
            boolean hasg = false;
            for (Mission m: getMissions()) {
                if (m.getGoals().contains(g)) {
                    hasg = true;
                    break;
                }
            }
            if (!hasg) {
                g.setMinAgToSatisfy(0);
            }
        }
    }

}
