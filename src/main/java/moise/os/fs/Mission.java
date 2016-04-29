package moise.os.fs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import moise.common.MoiseConsistencyException;
import moise.common.MoiseException;
import moise.common.MoiseXMLParserException;
import moise.os.Cardinality;
import moise.prolog.ToProlog;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 Represents a Mission. The mission id is prefixed by the scheme id.

 @navassoc - goals * Goal
 @navassoc - scheme - Scheme
 
 @author Jomi Fred Hubner
*/
public class Mission extends moise.common.MoiseElement implements ToXML, ToProlog {
    
    private static final long serialVersionUID = 1L;

    protected Set<Goal>     goals      = new HashSet<Goal>();
    protected Set<Mission>  preferable = new HashSet<Mission>();
    
    protected Scheme sch = null;
    
    /** 
     * Creates a new Mission
     * @param id the identification of the role
     */
    public Mission(String id, Scheme sch) {
        super(id);
        if (sch != null) {
            setPrefix(sch.getId());
            this.sch = sch;
        }
    }
    
    
    public void addGoal(String goalSpecId) throws MoiseConsistencyException {
        Goal g = sch.getGoal( goalSpecId );
        if (g == null) {
            throw new MoiseConsistencyException("Mission definition error: goal "+goalSpecId+" does not belongs to the SCH "+sch.getId());
        }
        goals.add(g);
    }
    
    /**
     * returns a collection of GoalSpec objects of this Mission
     */
    public Collection<Goal> getGoals() {
        return goals;
    }
    
    public void addPreferable(String missionId) throws MoiseConsistencyException {
        Mission m = sch.getFS().findMission(missionId);
        if (m == null) {
            throw new MoiseConsistencyException("Preference definition error: mission "+missionId+" does not belongs to the FS");
        }
        preferable.add(m);
    }
    
    /**
     * returns a collection of Mission objects preferable to this mission
     */
    public Collection<Mission> getPreferables() {
        return preferable;
    }
    
    /**
     * returns a collection of Mission objects preferable to this mission
     * including the transitivity of the preference relation. For instance,
     * if one has m1 < m2 (m2 has greater preference); m2 < m3; m2 < m4; m4 < m5,
     * for m1, this method will return {m2, m3, m4, m5}
     */
    public Collection<Mission> getAllPreferables() {
        Set<Mission> all = new HashSet<Mission>();
        for (Mission m: preferable) {
            if (! all.contains( m )) { // to avoid loops
                all.add(m);
                all.addAll( m.getAllPreferables() );
            }
        }
        
        return all;
    }
    
    public int compareTo(Object o) {
        Mission mo = (Mission)o;
        if (getAllPreferables().contains( mo )) { // the other mission is preferable
            return 1;
        } else if (mo.getAllPreferables().contains( this )) {
            return -1;
        } else {
            return 0;
        }
    }

    /** returns a string representing the goal in Prolog syntax, format:
     *     mission(id,min,max cardinality,list of goals,list of preferred missions)
     */ 
    public String getAsProlog() {
        Cardinality card = sch.getMissionCardinality(this);
        StringBuilder s = new StringBuilder("mission("+getId()+","+card.getMin()+","+card.getMax()+",[");
        
        // goals
        String v="";
        for (Goal gs: getGoals()) {
            s.append(v+gs.getId());
            v=",";
        }        
        
        // Preferable
        s.append("],[");
        v="";
        for (Mission misPref: getPreferables()) {
            s.append(v+misPref.getId());
            v=",";
        }
        
        s.append("])");
        return s.toString();
    }
    
    public static String getXMLTag() {
        return "mission";
    }

    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("id", getId());
        Cardinality card = sch.getMissionCardinality(this);
        if (! card.equals(Cardinality.defaultValue)) {
            ele.setAttribute("min", card.getMin()+"");
            ele.setAttribute("max", card.getMax()+"");
        }
        if (getProperties().size() > 0) {
            ele.appendChild(getPropertiesAsDOM(document));
        }
        // goals
        for (Goal gs: getGoals()) {
            Element eg = (Element) document.createElement(Goal.getXMLTag());
            eg.setAttribute("id", gs.getId());
            ele.appendChild(eg);
        }

        // Preferable
        for (Mission misPref: getPreferables()) {
            Element prefEle = (Element) document.createElement("preferred");
            prefEle.setAttribute("mission", misPref.getId());
            ele.appendChild(prefEle);
        }
        
        return ele;
    }
    
    public void setFromDOM(Element ele) throws MoiseException {
        setPropertiesFromDOM(ele);
        
        Cardinality card = new Cardinality();
        card.setFromDOM(ele);
        sch.setMissionCardinality(this, card);
        
        for (Element g: DOMUtils.getDOMDirectChilds(ele, Goal.getXMLTag())) {
            Goal gs = sch.getGoal(g.getAttribute("id"));
            if (gs == null) {
                //sch.addGoal(new GoalSpec(g.getAttribute("id")));
                throw new MoiseXMLParserException("the goal '"+g.getAttribute("id")+"' in mission '"+getId()+"' is not in any plan!");
            }
            addGoal(g.getAttribute("id"));
        }

        for (Element p: DOMUtils.getDOMDirectChilds(ele, "preferred")) {
            addPreferable(p.getAttribute("mission"));
        }
    }
    
    public String toString() {
        return getFullId();
    }
}
