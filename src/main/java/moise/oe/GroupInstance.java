package moise.oe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import moise.common.MoiseCardinalityException;
import moise.common.MoiseConsistencyException;
import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.os.Cardinality;
import moise.os.ss.Group;
import moise.os.ss.Role;
import moise.os.ss.SS;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 Represents the instance group of one Group Specification
 
 @navassoc - specification - Group
 @navassoc - super-group   - GroupInstance
 @navassoc - subgroups     * GroupInstance
 @composed - players       * RolePlayer
 
 @author Jomi Fred Hubner
*/
public class GroupInstance extends MoiseElement implements ToXML {
    
    protected Group           spec      = null;
    protected GroupInstance   superGroup= null;
    protected OE              oe        = null;
    //protected int             numberId  = -1;    // group unique number
    protected Map<String,GroupInstance>  subGroups = new HashMap<String,GroupInstance>();
    protected Set<RolePlayer>            players   = new HashSet<RolePlayer>();
    
    private static Logger logger = Logger.getLogger(GroupInstance.class.getName());
    private static final long serialVersionUID = 1L;

    /** since serialisation of maps has a bug, we need to rebuild them after serialisation! */
    public void rebuildHash() {
        //subGroups = new HashMap<String,GroupInstance>(subGroups);
        players   = new HashSet<RolePlayer>(players);
        for (GroupInstance gi: subGroups.values())
            gi.rebuildHash();
    }
    
    private static AtomicInteger grCount   = new AtomicInteger(0);;
    
    /** create a new group instance identified by id */
    protected GroupInstance(String id, Group spec) throws MoiseConsistencyException {
        try {
            //numberId = grCount.get();
            setId(id);
            this.spec = spec;
        } catch (Exception e) {
            throw new MoiseConsistencyException("group spec can not be null!");
        }        
    }
    
    /** create a new group instance named automatically */
    /*
    protected GroupInstance(Group spec) throws MoiseConsistencyException {
        super();
        try {
            grCount++;
            setId("gr_"+spec.getId()+ (grCount < 10 ? "_0" + grCount : "_" + grCount));
            number = grCount;
            this.spec = spec;
        } catch (Exception e) {
            throw new MoiseConsistencyException("group spec can not be null!");
        }
    }
    */
    
    public static String getUniqueId() {
        int i = grCount.incrementAndGet();
        return "gr"+ (i < 10 ? "_0" + i : "_" + i);
    }
    
    public Group getGrSpec() {
        return spec;
    }
    
    /** returns the unique number of the group (the getId uses this number to form the
     *  unique id.
     */
    /*public int getNumber() {
        return numberId;
    }*/
    
    protected void setOE(OE oe) {
        this.oe = oe;
    }
    
    protected void setSuperGroup(GroupInstance gr) {
        superGroup = gr;
    }
    
    public GroupInstance getSuperGroup() {
        return superGroup;
    }
    
    public boolean isWellFormed() {
        return wellFormedStatus().equals("ok");
    }
    
    /**
     * returns "ok" if the group is well formed, otherwise returns the problems description
     */
    public String wellFormedStatus() {
        // check the cardinality
        // . own roles
        // . subgroups
        // . subgroups' roles
        
        StringBuilder status = new StringBuilder();
        
        // . own roles
        for (Role r: spec.getRoles()) { 
            boolean includeSubGroups = !spec.containsRole(r); // scope is in subgroups' roles
            Cardinality card = spec.getRoleCardinality(r);
            if (card != null && !card.equals(Cardinality.defaultValue)) {
                int qtd = getPlayers(r.getId(), includeSubGroups).size();
                if (qtd < card.getMin())
                    status.append("The number of "+r.getId()+" players ("+qtd+") is less than the minimum ("+card.getMin()+").\n");
                if (qtd > card.getMax())
                    status.append("The number of "+r.getId()+" players ("+qtd+") is greater than the maximum ("+card.getMax()+").\n");
            }
        }
        
        // . subgroups cardinality
        for (Group sgr: spec.getSubGroups()) {
            Cardinality card = spec.getSubGroupCardinality(sgr);
            if (card != null && !card.equals(Cardinality.defaultValue)) {
                int qtd = getSubGroupInstacesQty(sgr.getId());
                if (qtd < card.getMin())
                    status.append("The number of "+sgr.getId()+" groups ("+qtd+") is less than the minimum ("+card.getMin()+").\n");
                if (qtd > card.getMax())
                    status.append("The number of "+sgr.getId()+" groups ("+qtd+") is greater than the maximum ("+card.getMax()+").\n");
            }
        }

        // subgroups well formation
        for (GroupInstance g: getSubGroups()) {
            if (!g.isWellFormed()) {
                status.append("The group "+g.getId()+" is not well formed.\n");
            }
        }
        
        if (status.length() == 0)
            return "ok";
        else
            return status.toString();
    }
    
    /**
     * returns a set of schemes which this group is responsible for
     */
    public Collection<SchemeInstance> getRespSchemes() {
        HashSet<SchemeInstance> schs = new HashSet<SchemeInstance>();
        for (SchemeInstance sch: oe.getSchemes()) {
            if (sch.getResponsibleGroups().contains(this)) {
                schs.add(sch);
            }
        }
        return schs;
    }
    
    //
    // Sub groups
    //
    
    /**
     * Adds a subgroup in a group, the id of the subgroup is defined automatically.
     *
     * <p>Example: <pre>Group def = team.addSubGroup("defense");</pre>
     *
     * @param grId the id of the new group
     * @param grSpecId the group specification identification (from OS)
     * @throws MoiseConsistencyException the grSpecId is not a subgroup of this group
     * @throws MoiseCardinalityException the cardinality (the max subgroup is already achieved)
     * @return the Group object created 
     */    
    public GroupInstance addSubGroup(String grSpecId) throws MoiseException {
        return addSubGroup(getUniqueId()+"_"+grSpecId, grSpecId);
    }
    
    
    /**
     * Adds a subgroup in a group.
     *
     * <p>Example: <pre>Group def = team.addSubGroup("d1", "defense");</pre>
     *
     * @param grId the id of the new group
     * @param grSpecId the group specification identification (from OS)
     * @throws MoiseConsistencyException the grSpecId is not a subgroup of this group
     * @throws MoiseCardinalityException the cardinality (the max subgroup is already achieved)
     * @return the Group object created 
     */    
    public GroupInstance addSubGroup(String grId, String grSpecId) throws MoiseException {
        logger.fine("trying to add subgroup "+grSpecId+" to "+this);
        
        Group subSpec = spec.getSubGroup(grSpecId);
        if (subSpec == null) {
            throw new MoiseConsistencyException(grSpecId+" is not an allowed subgroup of "+spec);
        }
        
        
        // cardinality
        int maxSubGr = spec.getSubGroupCardinality(subSpec).getMax();
        int currSubGr = getSubGroupInstacesQty(grSpecId);
        if (currSubGr >= maxSubGr) {
            throw new MoiseCardinalityException("the group "+grSpecId+" already has the maximun ("+maxSubGr+") number of instances in "+this);
        }
        if (oe.findGroup(grId) != null) {
            throw new MoiseException("A group with id "+grId+" already exists in the OE.");
        }
        
        GroupInstance gr = new GroupInstance(grId, subSpec);
        gr.setSuperGroup(this);
        
        subGroups.put(gr.getId(), gr);
        gr.setOE(oe);
        return gr;
    }

    /**
     * Removes a subgroup instance from this group.
     *
     * <p>Example: <code>att.removeSubGroup("gr_defense1");</code>
     *
     * @param grId the group instance id
     * @throws MoiseConsistencyException the grId is not a subgroup, the group has players,  the group has subgroups
     */
    public void removeSubGroup(String grId) throws MoiseConsistencyException {
        GroupInstance gr = (GroupInstance)subGroups.get(grId);
        if (gr == null)
            throw new MoiseConsistencyException(grId+" is not an instance group");
        gr.checkRemove();
        gr.removeRelations();
        subGroups.remove(grId);
    }

    protected void removeRelations() throws MoiseConsistencyException {
        // remove the schemes
        for (SchemeInstance sch: getRespSchemes()) {
            sch.remResponsibleGroup(this);
        }

        // remove all subgroups
        for (GroupInstance g: new ArrayList<GroupInstance>(getSubGroups())) {
            removeSubGroup(g.getId());
        }
        
        // remove players
        for (RolePlayer rp: new ArrayList<RolePlayer>(getPlayers())) {
            rp.getPlayer().abortRole(rp);
        }        
    }
    
    public void checkRemove() throws MoiseConsistencyException {
        SS ss = spec.getSS();
        
        String error = "";
        if (ss.getBoolProperty("check-players-in-remove-group", true)) {
            if (getPlayersQty() > 0) {
                error = getPlayersQty()+" players "+getAgents(false);
            }
        }
        
        if (ss.getBoolProperty("check-subgroup-in-remove-group", true)) {
            if (getSubGroupInstacesQty() > 0) {
                if (error.length() > 0) {
                    error += " and ";
                }
                error += getSubGroupInstacesQty()+" subgroups "+getSubGroups();
            }
        }
        if (error.length() > 0) {
            error = getId()+" can not be removed because it still has "+error+".";
            throw new MoiseConsistencyException(error);
        }
    }
    
    
    /**
     * returns the number of subgroups instances
     */
    public int getSubGroupInstacesQty() {
        return subGroups.size();
    }
    
    /**
     * returns the number of grSpecId instances
     */
    public int getSubGroupInstacesQty(String grSpecId) {
        int n = 0;
        for (GroupInstance g: subGroups.values()) { 
            if (g.getGrSpec().getId().equals(grSpecId)) {
                n++;
            }
        }
        return n;
    }
    
    
    public Collection<GroupInstance> getSubGroups() {
        return subGroups.values();
    }
    
    /**
     * gets this group and all its sub groups, the sob-groups of the subgroups, .....
     */
    public Collection<GroupInstance> getAllSubGroupsTree() {
        HashSet<GroupInstance> all = new HashSet<GroupInstance>();
        all.add(this);
        for (GroupInstance gr: subGroups.values()) {
            all.addAll( gr.getAllSubGroupsTree());
        }
        return all;
    }
    
    /**
     * looks for a group with grId in this Group (and its subgroups)
     */
    public GroupInstance findGroup(String grId) {
        if (getId().equals(grId)) {
            return this;
        }
        for (GroupInstance g: subGroups.values()) {
            g = g.findGroup(grId);
            if (g != null) {
                return g;
            }
        }
        return null;
    }
    
    /**
     * get all groups (and subgroups) that instantiates grSpec
     */
    public Collection<GroupInstance> findInstancesOf(Group grSpec) {
        HashSet<GroupInstance> res = new HashSet<GroupInstance>();
        if (spec.equals(grSpec)) {
            res.add(this);
        }
        for (GroupInstance group: subGroups.values()) {
            res.addAll(group.findInstancesOf(grSpec));
        }
        return res;
    }
    
    
    //
    // Players
    //
    
    
    /**
     * adds a role player in this group
     */
    public void addPlayer(RolePlayer rp) {
        players.add(rp);
    }
    
    /**
     * removes a role player from this group
     */
    public void removePlayer(RolePlayer rp) {
        players.remove(rp);
    }

    /** returns an iterator for RolePlayer objects
     */
    public Collection<RolePlayer> getPlayers() {
        return players;
    }

    /**
     * returns the total number of players in this group (does not consider players in subgroups)
     */
    public int getPlayersQty() {
        return players.size();
    }
    
    /**
     * returns the roleId players in this group (includeSubGroups==false)
     * or in this group and its subgroups (includeSubGroups==true). It
     * returns a collection of OEAgent objects.
     * 
     * If roleId is null, all roles are included in the result.
     */
    public Collection<RolePlayer> getPlayers(String roleId, boolean includeSubGroups) {
        Set<RolePlayer> all = new HashSet<RolePlayer>();
        
        for (RolePlayer rp: players)
            if (roleId == null || rp.getRole().getId().equals(roleId))
                all.add(rp);
        
        if (includeSubGroups)
            for (GroupInstance g: subGroups.values())
                all.addAll( g.getPlayers(roleId, includeSubGroups) );
        
        return all;
    }
    
    
    /** 
     * returns a collection with OEAgents belonging to this group
     */
    public Set<OEAgent> getAgents(boolean includeSubGroups) {
        Set<OEAgent> ags = new HashSet<OEAgent>();
        for (RolePlayer rp: players)
            ags.add( rp.getPlayer());
        if (includeSubGroups)
            for (GroupInstance sg: subGroups.values()) 
                ags.addAll(sg.getAgents(true));         
        return ags;
    }

    public static String getXMLTag() {
        return "group";
    }


    public Element getAsDOM(Document document) {
        Element grEle = (Element) document.createElement(getXMLTag());
        grEle.setAttribute("id",getId());
        grEle.setAttribute("specification", getGrSpec().getId());
        if (getOwner() != null) {
            grEle.setAttribute("owner", getOwner().toString());
        }

        // status
        Element wfEle = (Element) document.createElement("well-formed");
        wfEle.appendChild(document.createTextNode(wellFormedStatus()));
        grEle.appendChild(wfEle);
        
        // players
        if (getPlayersQty() > 0) {
            Element plEle = (Element) document.createElement("players");
            for (RolePlayer rp: getPlayers()) {
                plEle.appendChild( rp.getAsDOM(document));
            }
            grEle.appendChild(plEle);
        }
            
        // subgroups
        if (getSubGroupInstacesQty() > 0) {
            Element sgEle = (Element) document.createElement("subgroups");
            for (GroupInstance gi: getSubGroups()) {
                sgEle.appendChild( gi.getAsDOM(document) );
            }
            grEle.appendChild(sgEle);
        }
        return grEle;
    }   
    
}
