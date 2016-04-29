package moise.oe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import moise.common.MoiseConsistencyException;
import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.os.Cardinality;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.fs.Plan;
import moise.os.fs.Scheme;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 Represents the instance of one scheme Specification.

 @navassoc - specification  - Scheme
 @navassoc - root-goal  - GoalInstance
 @composed - players  * MissionPlayer
 @navassoc - responsible-groups  * GroupInstance
 @composed - goals  * GoalInstance
 @composed - plans  * PlanInstance

 @author Jomi Fred Hubner
*/
public class SchemeInstance extends MoiseElement implements ToXML {
    
    private static final long serialVersionUID = 1L;

    public static final String WellFormed = "ok";
    
    protected Scheme       spec      = null;
    protected GoalInstance root      = null;
    protected int          number    = -1;
    protected OE           oe        = null;
    protected Set<MissionPlayer>        players = new HashSet<MissionPlayer>();
    protected Set<GroupInstance>        groups  = new HashSet<GroupInstance>();
    protected Map<String,GoalInstance>  goals   = new HashMap<String,GoalInstance>();
    protected Set<PlanInstance>         plans   = new HashSet<PlanInstance>();
    
    /** since serialisation of maps has a bug, we need to rebuild them after serialisation! */
    public void rebuildHash() {
        players = new HashSet<MissionPlayer>(players);
        groups  = new HashSet<GroupInstance>(groups);
        //goals   = new HashMap<String, GoalInstance>(goals);
        plans   = new HashSet<PlanInstance>(plans);
    }
    

    private static int schCount = 0;
    
    protected SchemeInstance(String id, Scheme sch) throws MoiseConsistencyException {
        if (sch == null) {
            throw new MoiseConsistencyException("Scheme specification can not be null!");
        }
        
        setId(id);
        number = schCount;
        spec   = sch;
        
        // create goal instances
        for (Goal gs: spec.getGoals()) {
            GoalInstance gi = new GoalInstance(gs, this);
            goals.put(gs.getId(), gi);
            
            if (spec.getRoot().equals( gs )) {
                root = gi;
            }
        }
        
        // create plans instances
        for (Plan sp: spec.getPlans()) {
            
            // create the instance
            PlanInstance p = new PlanInstance(sp);
            p.setGoalInstances( this );
            
            plans.add(p);
        }
    }
    
    public static String getUniqueId() {
        schCount++;
        return "sch"+(schCount < 10 ? "_0" + schCount : "_" + schCount);
    }
    
    protected void setOE(OE oe) {
        this.oe = oe;
    }
    
    public OE getOE() {
        return oe;
    }
    
    public GoalInstance getRoot() {
        return root;
    }
    
    /**
     * returns the SCH specification for this SCH instance
     */
    public Scheme getSpec() {
        return spec;
    }
    
    /** returns the unique number of the group (the getId uses this number to form the
     *  unique id.
     */
    public int getNumber() {
        return number;
    }
    
    /**
     * adds an instance group in the set of groups responsible for this SCH.
     *
     * <p>Example: <code>sch.addResponsibleGroup(def.getId());</code>
     *
     * @param grId the group Id
     * @throws MoiseConsistencyException the grIs does not exist in the OE
     */
    public void addResponsibleGroup(String grId) throws MoiseConsistencyException {
        GroupInstance g = oe.findGroup(grId);
        if (g == null)
            throw new MoiseConsistencyException("group "+grId+" does not exist in the OE and thus can not be responsible for scheme "+getId());
        addResponsibleGroup( g );
    }
    
    /**
     * adds an instance group in the set of groups responsible for this SCH.
     *
     * <p>Example: <code>sch.addResponsibleGroup(def);</code>
     *
     * @param g the group object
     * @throws MoiseConsistencyException the group is not well formed 
     */
    public void addResponsibleGroup(GroupInstance g) throws MoiseConsistencyException {
        if (g != null) {
            if (g.isWellFormed())
                groups.add(g);
            else if (spec.getFS().getBoolProperty("check-formation-in-responsible-group", true))
                throw new MoiseConsistencyException("group "+g.getId()+" is not well formed and thus can not be responsible for scheme "+getId());
        }
    }
    
    /**
     * removes an instance group in the set of groups responsible for this SCH.
     *
     * <p>Example: <code>sch.remResponsibleGroup("gr_def22");</code>
     *
     * @param grId the group id
     * @throws MoiseConsistencyException no agent of group g can be committed to missions in this scheme
     */
    public void remResponsibleGroup(String grId) throws MoiseConsistencyException {
        remResponsibleGroup(oe.findGroup(grId));
    }
    
    
    /**
     * removes an instance group in the set of groups responsible for this SCH.
     *
     * <p>Example: <code>sch.remResponsibleGroup(def);</code>
     *
     * @param g the group object
     * @throws MoiseConsistencyException no agent of group g can be committed to missions in this scheme
     */
    public void remResponsibleGroup(GroupInstance g) throws MoiseConsistencyException {
        if (g != null) {
            
            if (spec.getFS().getBoolProperty("check-players-in-remove-responsible-group", true)) {
                // if no agent of group g is committed to missions in this scheme
                for (MissionPlayer mp: players) {
                    if (mp.getPlayer().playsRole(g).size() > 0) {
                        throw new MoiseConsistencyException("the responsible group "+g+" can not be removed since its agent "+mp.getPlayer()+" belongs to this group and are committed to the mission "+mp.getMission()+" in "+this.getId());
                    }
                }
            }
            groups.remove(g);
        }
    }
    
    
    /**
     * returns a collection of groups that are responsible for this scheme
     */
    public Collection<GroupInstance> getResponsibleGroups() {
        return groups;
    }
    
    
    /**
     * remove all commitments without checking goal state
     */
    public void abort() throws MoiseException {
        for (MissionPlayer mp: new ArrayList<MissionPlayer>(players)) {
            mp.getPlayer().abortMission( mp.getMission().getFullId(), this); // the agent abort mission will remove the mission player from this scheme
        }
    }
    
    
    /**
     * returns "ok" (SchemeInstance.WellFormed) if the sch is well formed, otherwise returns the problems' description
     */
    public String wellFormedStatus() {
        StringBuilder status = new StringBuilder();
        
        // all missions have players
        for (Mission mis: spec.getMissions()) {
            
            Cardinality card = spec.getMissionCardinality(mis.getId());
            if (card != null) {
                int qtd = getPlayersQty(mis.getId());
                if (qtd < card.getMin()) {
                    status.append("The number of "+mis.getId()+" players ("+qtd+") is less than the minimum ("+card.getMin()+").\n");
                }
                if (qtd > card.getMax()) {
                    status.append("The number of "+mis.getId()+" players ("+qtd+") is greater than the maximum ("+card.getMax()+").\n");
                }
            }
        }
        
        // all DS time constrains!!!!!
        
        
        if (status.length() == 0) {
            return WellFormed;
        } else {
            return status.toString();
        }
    }

    /**
     * returns true if the sch is well formed
     */
    public boolean isWellFormed() {
        // all missions have players
        for (Mission mis: spec.getMissions()) {
            
            Cardinality card = spec.getMissionCardinality(mis.getId());
            if (card != null) {
                int qtd = getPlayersQty(mis.getId());
                if (qtd < card.getMin()) {
                    return false;
                }
                if (qtd > card.getMax()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /** returns true if this scheme's root goal is neither achieved nor impossible */
    public boolean isCommitable() {
        return !getRoot().isImpossible() && !getRoot().isSatisfied();
    }
    
    // ---------------
    // Players
    //
    
    /**
     * adds a mission player for this SCH
     */
    protected void addPlayer(MissionPlayer mp) {
        players.add(mp);
        
        // add ag in the agents committed to the goal of the mission
        for (Goal gs: mp.getMission().getGoals()) {
            GoalInstance ig = getGoal(gs);
            ig.committed(mp.getPlayer());
        }
    }
    
    /**
     * removes a mission player from this scheme (no checks are done)
     */
    protected void removePlayer(MissionPlayer mp) throws MoiseException {
        if (mp == null) return;
        
        if (!players.remove(mp)) {
            throw new MoiseConsistencyException("There is not a player like '"+mp+"' in this scheme ("+this+"). Players are "+players+".");
        }
        
        // remove ag from the agents committed to the goal of the mission
        for (Goal gs: mp.getMission().getGoals()) {
            GoalInstance ig = getGoal(gs);
            ig.uncommitted(mp.getPlayer());
        }
    }
    
    public Collection<MissionPlayer> getPlayers() {
        return players;
    }
    
    public boolean isPlayer(OEAgent ag) {
        for (MissionPlayer mp: players) {
            if (mp.getPlayer().equals(ag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns the OEAgents committed to the missionId in this scheme
     * (if missionId is null, return all agents)
     */
    public Collection<OEAgent> getPlayers(String missionId) {
        Set<OEAgent> all = new HashSet<OEAgent>();
        for (MissionPlayer mp: players) {
            if (missionId == null || mp.getMission().getId().equals(missionId)) {
                all.add(mp.getPlayer());
            }
        }
        return all;
    }

    /** gets all agents participating in this scheme */
    public Collection<OEAgent> getAgents() {
        return getPlayers(null);
    }
    
    /**
     * returns the total number of players in this sch
     */
    public int getPlayersQty() {
        return players.size();
    }
    
    /**
     * returns the number of missionId players in this scheme
     */
    public int getPlayersQty(String missionId) {
        int n = 0;
        for (MissionPlayer mp: players) {
            if (mp.getMission().getId().equals(missionId)) {
                n++;
            }
        }
        return n;
    }
    
    
    
    //
    // Goals' methods
    //
    
    public GoalInstance getGoal(String goalId) {
        return goals.get(goalId);
    }
    
    public GoalInstance getGoal(Goal gs) {
        return goals.get(gs.getId());
    }
    
    public Collection<GoalInstance> getGoals() {
        return goals.values();
    }
    
    public static String getXMLTag() {
        return "scheme";
    }

    
    public Element getAsDOM(Document document) {
        Element schEle = (Element) document.createElement(getXMLTag());
        schEle.setAttribute("id", getId());
        schEle.setAttribute("specification", getSpec().getId());
        schEle.setAttribute("root-goal", getSpec().getRoot().getId());
        if (getOwner() != null) {
            schEle.setAttribute("owner", getOwner().toString());
        }
        
        // status
        Element wfEle = (Element) document.createElement("well-formed");
        wfEle.appendChild(document.createTextNode(wellFormedStatus()));
        schEle.appendChild(wfEle);
        
        // players
        if (getPlayersQty() > 0) {
            Element plEle = (Element) document.createElement("players");
            for (MissionPlayer mp: getPlayers()) {
                plEle.appendChild( mp.getAsDOM(document));
            }
            schEle.appendChild(plEle);
        }

        // responsible groups
        Element rgEle = (Element) document.createElement("responsible-groups");
        for (GroupInstance gi: getResponsibleGroups()) {
            Element gEle = (Element) document.createElement("group");
            gEle.setAttribute("id", gi.getId());
            rgEle.appendChild(gEle);
        }
        schEle.appendChild(rgEle);

        // goals (with variable values)
        if (getGoals().size() > 0) {
            Element gsEle = (Element) document.createElement("goals");
            for (GoalInstance gi: getGoals()) {
                gsEle.appendChild(gi.getAsDOM(document));
            }
            schEle.appendChild(gsEle);
        }

        return schEle;
    }
    
}
