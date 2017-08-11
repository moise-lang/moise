package moise.oe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import moise.common.Event;
import moise.common.MoiseCardinalityException;
import moise.common.MoiseConsistencyException;
import moise.common.MoiseException;
import moise.os.OS;
import moise.os.fs.Goal;
import moise.os.fs.Scheme;
import moise.os.ss.Group;
import moise.os.ss.Role;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 Represents an Organisational Entity

 @navassoc - purpose       - GoalInstance
 @navassoc - specification - OS
 @composed - agents   * OEAgent
 @composed - groups   * GroupInstance
 @composed - schemes  * SchemeInstance

 @author Jomi Fred Hubner
*/
public class OE extends Event implements Cloneable, ToXML {

    private static final long serialVersionUID = 1L;

    protected GoalInstance         purpose = null;
    protected OS                   os      = null;
    protected Map<String,OEAgent>  agents  = new HashMap<String,OEAgent>();
    protected Map<String,GroupInstance>    groups  = new HashMap<String,GroupInstance>();
    protected Map<String,SchemeInstance>   schs    = new HashMap<String,SchemeInstance>();


    public OE(GoalInstance purpose, OS os) throws MoiseConsistencyException {
        if (os == null) {
            throw new MoiseConsistencyException("OS can not be null!");
        }
        this.purpose = purpose;
        this.os      = os;
    }

    /**
     * Creates a new organisational entity with <i>purpose</i> and organisation specification as
     * state in the file <i>OSxmlURI</i>. This XML file must be written in accordance with
     * the XML Schema specified in file os.xsd.
     *
     * <p>Example: <pre>OE currentOE = OE.createOE("winGame", "jojOS.xml");
     *
     * @param purpose the purpose of the entity
     * @param OSxmlURI the organisation specification
     * @throws MoiseConsistencyException in case the XML file is not well formed
     * @return an OE object representing this new entity
     */
    public static OE createOE(String purpose, String OSxmlURI) throws MoiseConsistencyException {
        OS os = OS.loadOSFromURI(OSxmlURI);
        if (os != null) {
            Goal gs = new Goal(purpose);
            return new OE(new GoalInstance(gs, null), os);
        } else {
            return null;
        }
    }

    /**
     * gets a partial view of this OE, only entities allowed for the ag will be shown.
     */
    public OE partialOE(OEAgent ag) throws MoiseConsistencyException {
        OE newOE = null;
        try {
            newOE = (OE)this.clone();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // change the agents' name
        int i = 1;
        for (OEAgent other: newOE.getAgents()) {
            if (!other.equals(ag) && !other.getId().equals("OrgManager")) {
                if (! ag.hasLink(null, other)) { // any kind of link is enough
                    other.setId("someone"+(i++));
                }
            }
        }

        return newOE;
    }

    /**
     * the clone object is a full/independent copy of this object,i.e.,
     * all OE inner objects are also cloned.
     */
    public Object clone() {
        try {
            // Serialise
            ByteArrayOutputStream sout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(sout);
            oout.writeObject(this);
            oout.close();

            // de-serialise
            ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream( sout.toByteArray() ));
            OE oe = (OE)oin.readObject();
            oe.rebuildHash();
            return oe;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** since serialisation of maps has a bug, we need to rebuild them after serialisation! */
    public void rebuildHash() {
        for (GroupInstance gi: getGroups())
            gi.rebuildHash();
        for (SchemeInstance si: getSchemes())
            si.rebuildHash();
    }

    public void changePurpose(String newPurpose) {
        Goal gs = new Goal(newPurpose);
        purpose = new GoalInstance(gs, null);
    }

    public GoalInstance getPurpose() {
        return purpose;
    }

    /**
     * gets the OS of this entity
     * @return the OS of this entity
     */
    public OS getOS() {
        return os;
    }

    //
    // Agent methods
    //

    /**
     * Adds an agent in the OE.
     *
     * <p>Example: <coce>OEAgent jaime = currentOE.addAgent("Jaime");</code>
     *
     * @param agName the agent name
     * @return an OEAgent object reference
     */
    public OEAgent addAgent(String agName) throws MoiseException {
        if (getAgent(agName) != null) {
            throw new MoiseException("An agent named '"+agName+"' already exists in this OE!");
        }
        OEAgent ag = new OEAgent(agName);
        agents.put(ag.getId(),ag);
        ag.setOE(this);
        return ag;
    }

    /**
     * Removes an agent from the OE.
     *
     * <p>Example: <code>currentOE.removeAgent("Jomi");</code>
     *
     * @param agId the agent id (the id is in OE)
     * @param check if true, the moise consistencies will be checked
     * @throws MoiseConsistencyException the must not have roles or missions
     */
    public void removeAgent(String agId, boolean check) throws MoiseException {
        OEAgent ag = getAgent(agId);
        if (ag == null) {
            throw new MoiseConsistencyException(agId+" is not an agent belonging to this OE");
        }

        if (ag.getNumberOfRoles() > 0) {
            if (check) {
                throw new MoiseConsistencyException(agId+" has "+ag.getNumberOfRoles()+" role(s) "+ag.getRoles()+", since it can not leave the OE");
            } else {
                ag.abort();
            }
        }

        agents.remove(agId);
    }

    public OEAgent getAgent(String agId) {
        return agents.get(agId);
    }

    public Collection<OEAgent> getAgents() {
        return agents.values();
    }

    /**
     * gets all agents that plays <code>role</code> in <code>gr</code>.
     * if gr == null, does not consider the group
     */
    public Collection<OEAgent> getAgents(GroupInstance gr, String roleId) {
        return getAgents(gr, os.getSS().getRoleDef(roleId) );
    }

    /**
     * gets all agents that plays <code>role</code> in <code>gr</code>.
     * if gr == null, the group is not considered
     */
    public Collection<OEAgent> getAgents(GroupInstance gr, Role role) {
        List<OEAgent> all = new ArrayList<OEAgent>();
        for (OEAgent ag: agents.values()) {

            for (RolePlayer rp: ag.getRoles()) {

                if (gr == null) {
                    if (rp.getRole().getEntailedRoles().values().contains(role)) {
                        all.add(ag);
                    }
                } else if (rp.getGroup().equals(gr) && rp.getRole().getEntailedRoles().values().contains(role)) {
                    all.add(ag);
                }
            }
        }
        return all;
    }

    //
    // Group methods
    //

    /**
     * Creates a new root group instance from the specification denoted by <i>grSpecId</i>.
     *
     * <p>Example: <PRE>Group   team    = currentOE.addGroup("team");</PRE>
     *
     * @param grSpecId the group specification id (the id is in OS)
     * @throws MoiseConsistencyException the grSpecId is not a root group
     * @throws MoiseCardinalityException the cardinality (the max subgroup is already achieved)
     * @return a reference for the new Group
     */
    public GroupInstance addGroup(String grSpecId) throws MoiseException {
        return addGroup(GroupInstance.getUniqueId()+"_"+grSpecId, grSpecId);
    }

    /**
     * Creates a new root group instance (identified by grId) from the specification denoted by <i>grSpecId</i>.
     *
     * <p>Example: <PRE>Group   team    = currentOE.addGroup("g1", "team");</PRE>
     *
     * @param grId the id of the new group
     * @param grSpecId the group specification id (the id is in OS)
     * @throws MoiseConsistencyException the grSpecId is not a root group
     * @throws MoiseCardinalityException the cardinality (the max subgroup is already achieved)
     * @return a reference for the new Group
     */
    public GroupInstance addGroup(String grId, String grSpecId) throws MoiseException {
        Group rootSpec = os.getSS().getRootGrSpec();
        if (! rootSpec.getId().equals(grSpecId)) {
            throw new MoiseConsistencyException(grSpecId+" is not a root group specification");
        }

        // cardinality
        int maxSubGr = rootSpec.getSubGroupCardinality(rootSpec).getMax();
        int currSubGr = getSubGroupInstancesQty(grSpecId);
        if (currSubGr >= maxSubGr) {
            throw new MoiseCardinalityException("the group "+grSpecId+" already has the maximun ("+maxSubGr+") number of instances in "+this);
        }

        if (findGroup(grId) != null) {
            throw new MoiseException("A group with id "+grId+" already exists in the OE.");
        }

        GroupInstance gr = new GroupInstance(grId, rootSpec);
        groups.put(gr.getId(), gr);
        gr.setOE(this);
        return gr;
    }

    /**
     * Removes a group instance from this OE. It works for subgroups and root groups.
     *
     * <p>Example: <code>currentOE.removeGroup("gr_team0");</code>
     *
     * @param grId the group instance id
     * @throws MoiseConsistencyException the group has players,  the group has subgroups
     */
    public void removeGroup(String grId) throws MoiseConsistencyException {
        GroupInstance gr = findGroup(grId);
        if (gr == null)
            throw new MoiseConsistencyException(gr+" is not an instance group");
        if (gr.getSuperGroup() == null) { // is root group
            gr.checkRemove();
            gr.removeRelations();
            groups.remove(grId);
        } else { // subgroup
            gr.getSuperGroup().removeSubGroup(grId);
        }
    }

    /** returns the root groups */
    public Collection<GroupInstance> getGroups() {
        return groups.values();
    }

    /** return all groups of the OE, even subgroups */
    public Collection<GroupInstance> getAllSubGroupsTree() {
        Set<GroupInstance> all = new HashSet<GroupInstance>();
        for (GroupInstance gr: groups.values()) {
            all.addAll( gr.getAllSubGroupsTree());
        }
        return all;
    }

    /**
     * returns the number of grSpecId instances
     */
    public int getSubGroupInstancesQty(String grSpecId) {
        int n = 0;
        for (GroupInstance g: groups.values()) {
            if (g.getGrSpec().getId().equals(grSpecId)) {
                n++;
            }
        }
        return n;
    }

    /**
     * looks for a group with grId in this OE (and inside all its groups)
     */
    public GroupInstance findGroup(String grId) {
        for (GroupInstance g: groups.values()) {
            if (g.getId().equals(grId)) {
                return g;
            }
            g = g.findGroup(grId);
            if (g != null) {
                return g;
            }
        }
        return null;
    }

    /**
     * finds all groups (and subgroups) that instantiates grSpec
     */
    public Collection<GroupInstance> findInstancesOf(Group grSpec) {
        Set<GroupInstance> res = new HashSet<GroupInstance>();
        for(GroupInstance group: groups.values()) {
            res.addAll(group.findInstancesOf(grSpec));
        }
        return res;
    }

    /**
     * finds all groups (and subgroups) that instantiates grSpec
     */
    public Collection<GroupInstance> findInstancesOf(String grSpec) {
        return findInstancesOf( os.getSS().getRootGrSpec().findSubGroup(grSpec));
    }

    //
    // Scheme methods
    //

    /**
     * Creates a new scheme instance.
     *
     * <p>Example: <code>Scheme sch = currentOE.startScheme("sideAttack");</code>
     *
     * @param schSpecId the scheme specification (from OS)
     * @throws MoiseException
     * @return a SCH object
     */
    public SchemeInstance startScheme(String schSpecId) throws MoiseException {
        return startScheme(SchemeInstance.getUniqueId()+"_"+schSpecId, schSpecId);
    }

    /**
     * Creates a new scheme instance with a particular id.
     *
     * <p>Example: <code>Scheme sch = currentOE.startScheme("s1", "sideAttack");</code>
     *
     * @param schId the name of the new scheme
     * @param schSpecId the scheme specification (from OS)
     * @throws MoiseException
     * @return a SCH object
     */
    public SchemeInstance startScheme(String schId, String schSpecId) throws MoiseException {
        Scheme schSpec = os.getFS().findScheme(schSpecId);
        if (schSpec == null) {
            throw new MoiseConsistencyException(schSpecId+" is not a known scheme specification");
        }
        if (findScheme(schId) != null) {
            throw new MoiseConsistencyException("There already is a scheme with id "+schId);
        }

        SchemeInstance sch = new SchemeInstance(schId, schSpec);
        schs.put(sch.getId(), sch);
        sch.setOE(this);

        return sch;
    }

    /**
     * Removes the scheme instance from the OE's Schemes.
     *
     * <p>Example: <code>currentOE.finischScheme(satt);</code></br>
     * where <code>satt</code> is a Scheme handler.
     *
     * @param sch the scheme instance object handler
     * @throws MoiseException only schemes without players can be normally finished
     */
    public void finishScheme(SchemeInstance sch) throws MoiseException {
        if (sch != null) {
            if (schs.get( sch.getId()) == null) {
                throw new MoiseConsistencyException("the scheme "+sch+" is not 'running'");
            }
            if (getOS().getFS().getBoolProperty("check-players-in-remove-scheme", true) && sch.getPlayersQty() > 0) {
                throw new MoiseConsistencyException( sch+" has "+sch.getPlayersQty()+" players, so it can not be finished.");
            }
            for (MissionPlayer mp: new ArrayList<MissionPlayer>(sch.getPlayers())) {
                mp.getPlayer().abortMission( mp.getMission().getId(), sch);
            }
            schs.remove(sch.getId());
        }
    }

    /**
     * Aborts (and removes) the scheme instance from the OE's Schemes. All agents committed to
     * this scheme missions will lost their commitment.
     *
     * <p>Example: <code>currentOE.abortScheme(satt);</code></br>
     * where <code>satt</code> is a Scheme handler.
     *
     * @param sch the scheme instance object handler
     */
    public void abortScheme(SchemeInstance sch) throws MoiseException {
        if (sch != null) {
            if (schs.get( sch.getId()) == null) {
                throw new MoiseConsistencyException("the scheme "+sch+" is not 'running'");
            }
            sch.abort();
            schs.remove(sch.getId());
        }
    }

    /**
     * looks for a Scheme with schId
     */
    public SchemeInstance findScheme(String schId) {
        return schs.get(schId);
    }


    /**
     * finds all schemes that instantiates schSpecId
     */
    public Collection<SchemeInstance> findInstancesOfSchSpec(String schSpecId) {
        List<SchemeInstance> all = new ArrayList<SchemeInstance>();
        for (SchemeInstance sch: schs.values()) {
            if (sch.getSpec().getId().equals(schSpecId)) {
                all.add(sch);
            }
        }
        return all;
    }

    public Collection<SchemeInstance> getSchemes() {
        return schs.values();
    }

    public String getXMLTag() {
        return "organisational-entity";
    }

    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("os", getOS().getId());

        ele.appendChild(getPurpose().getAsDOM(document));

        // agents
        Element oeAgs = (Element) document.createElement("agents");
        for (OEAgent oea: getAgents()) {
            oeAgs.appendChild(oea.getAsDOM(document));
        }
        ele.appendChild(oeAgs);

        // groups
        Element oeGrs = (Element) document.createElement("groups");
        for (GroupInstance gi: getGroups()) {
            oeGrs.appendChild( gi.getAsDOM(document));
        }
        ele.appendChild(oeGrs);

        // groups
        Element oeSchs = (Element) document.createElement("schemes");
        for (SchemeInstance sch: getSchemes()) {
            oeSchs.appendChild(sch.getAsDOM(document));
        }
        ele.appendChild(oeSchs);

        return ele;
    }

    public String toString() {
        return "OE (with OS "+os.getId()+")";
    }
}
