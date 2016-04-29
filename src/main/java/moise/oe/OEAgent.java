package moise.oe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import moise.common.MoiseCardinalityException;
import moise.common.MoiseConsistencyException;
import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.fs.Goal.GoalType;
import moise.os.ns.Norm;
import moise.os.ss.Compatibility;
import moise.os.ss.Link;
import moise.os.ss.Role;
import moise.os.ss.RoleRel.RoleRelScope;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 Represents an agent that belongs to the OE.

 @composed - roles  * RolePlayer
 @composed - missions  * MissionPlayer

 @author Jomi Fred Hubner
*/
public class OEAgent extends MoiseElement implements ToXML {
    
    protected Map<String,RolePlayer>     roles    = new HashMap<String,RolePlayer>();    // key=grId.roleId,     value=RolePlayer
    protected Map<String,MissionPlayer>  missions = new HashMap<String,MissionPlayer>(); // key=schId.missionId, value=MissionPlayer
    protected OE                         oe       = null;

    private static Logger logger = Logger.getLogger(OEAgent.class.getName());
    private static final long serialVersionUID = 1L;
    
    protected OEAgent(String name) {
        super(name);
    }
    
    /**
     * remove the roles/missions of this agent without checking
     * (in the case the agent leaves the society without finishing its commitments)
     */
    protected void abort()  throws MoiseException {
        removeAllMissions();
        removeAllRoles();
    }
    
    // --------------------------------
    // Role
    //
    
    /**
     * adds a role for an agent.
     *
     *<p>Example: <code>jaime.adoptRole("leader", att.getId())</code>
     *
     * @param roleId the role identification (from OS)
     * @param grId the instance group id (from OE) where the role will be played
     * @throws MoiseConsistencyException  many errors: grId does not exist, the roleId does no exist in grId, the role is
     * not compatible with other agent's role, ...
     * @throws MoiseCardinalityException  the number of roleId players already has the max value in grId
     */
    public RolePlayer adoptRole(String roleId, String grId) throws MoiseConsistencyException, MoiseCardinalityException {
        GroupInstance gr = oe.findGroup(grId);
        if (gr == null) {
            throw new MoiseConsistencyException("the group "+grId+" does not exist in the OE");
        }
        return adoptRole(roleId, gr);
    }
    
    /**
     * adds a role for an agent.
     *
     *<p>Example: <code>jaime.adoptRole("leader", att)</code><br/>
     *where <code>jaime</code> is an handler for an agent, <code>"leader"</code> is
     *the role id, and <code>att</code> is the group handler.
     *
     * @param roleId the role identification (from OS)
     * @param gr the instance group object where the role will be played
     * @throws MoiseConsistencyException  many errors: the roleId does no exist in grId, the role is
     * not compatible with other agent's role, ...
     * @throws MoiseCardinalityException  the number of roleId players already has the max value in gr
     */
    public RolePlayer adoptRole(String roleId, GroupInstance gr) throws MoiseConsistencyException, MoiseCardinalityException {
        logger.fine(getId() + " trying to play "+roleId+" in "+gr);
        
        Role role = oe.getOS().getSS().getRoleDef(roleId);
        // all moise+ checks
        if (role == null) {
            throw new MoiseConsistencyException("the role "+roleId+" does not exist in OS.");
        }
        if (!gr.getGrSpec().containsRole(role)) {
            throw new MoiseConsistencyException("the role "+roleId+" does not exist in the group "+gr+".");
        }
        
        if (playsRole(roleId, gr) != null) {
            throw new MoiseConsistencyException("the role "+roleId+" is already being played by "+getId()+" in the group "+gr);
        }
        
        if (role.isAbstract()) {
            throw new MoiseConsistencyException("the role "+roleId+" is abstract");
        }
        
        // cardinality
        roleCardinalityCheck(role, gr);
        
        // compatibility
        compatibilityCheck(role, gr);
        
        RolePlayer rp = new RolePlayer(role, this, gr);
        gr.addPlayer(rp);
        
        roles.put( gr.getId()+"."+roleId, rp);
        
        return rp;
    }
    
    
    private void compatibilityCheck(Role newRole, GroupInstance newRoleGr) throws MoiseConsistencyException {
        Collection<Compatibility> newRoleCompats  = newRole.getCompatibilities(newRoleGr.getGrSpec());
        
        // all the current roles
        for (RolePlayer curRolePlayer: roles.values()) {
            GroupInstance curRoleGr = curRolePlayer.getGroup();
            Role  curRole   = curRolePlayer.getRole();
            
            //System.out.println("checking compatibilities for role "+newRole+" with "+curRole);
            
            boolean ok = false;
            for (Compatibility c: newRoleCompats) {
                //System.out.println("trying "+c);
                
                
                if ( c.areCompatible(curRole,newRole)) {
                    
                    if (curRoleGr.equals(newRoleGr)) { // the agents wants a new role in a group which it already belongs
                        // will look for an intra-group compatibility
                        if (c.getScope() == RoleRelScope.IntraGroup) {
                            ok = true;
                            break;
                        }
                    } else {
                        // will look for an inter-group compatibility
                        if (c.getScope() == RoleRelScope.InterGroup) {
                            ok = true;
                            break;
                        }
                    }
                }
            }
            if (! ok) {
                throw new MoiseConsistencyException("the role "+newRole+" in the group "+newRoleGr+" is not compatible with the role "+curRole+" in the group "+curRoleGr);
            }
        }
    }
    
    private void roleCardinalityCheck(Role role, GroupInstance newRoleGr) throws MoiseCardinalityException {
        int maxRoles = newRoleGr.getGrSpec().getRoleCardinality(role).getMax();
        int currNbPlayers = newRoleGr.getPlayers(role.getId(), false).size();
        //debug("the group "+newRoleGr+" has "+currNbPlayers+"/"+maxRoles+" number of the "+role+" role players.");
        if (currNbPlayers >= maxRoles) {
            throw new MoiseCardinalityException("the group "+newRoleGr+" already has the maximun ("+maxRoles+") number of the "+role+" role players.");
        }
    }
    
    
    /**
     * removes a role from an agent.
     *
     *<p>Example: <code>jaime.removeRole("leader", "gr_att2")</code><br/>
     *where <code>jaime</code> is an handler for an agent, <code>"leader"</code> is
     *the role id, and <code>"gr_att2"</code> is the group id.
     *
     * @param roleId the role identification (from OS)
     * @param gr the instance group id where the role will not be played anymore
     * @throws MoiseConsistencyException  the
     * role is necessary for some mission, ...
     */
    public RolePlayer removeRole(String roleId, String grId) throws MoiseConsistencyException {
        GroupInstance gi = oe.findGroup(grId);
        if (gi == null)
            throw new MoiseConsistencyException("the group "+grId+" does not exists.");
        return removeRole(roleId, gi);
    }
    
    /**
     * removes a role from an agent.
     *
     *<p>Example: <code>jaime.removeRole("leader", att)</code><br/>
     *where <code>jaime</code> is an handler for an agent, <code>"leader"</code> is
     *the role id, and <code>att</code> is the group handler.
     *
     * @param roleId the role identification (from OS)
     * @param gr the instance group object where the role will not be played anymore
     * @throws MoiseConsistencyException  the
     * role is necessary for some mission, ...
     */
    public RolePlayer removeRole(String roleId, GroupInstance gr) throws MoiseConsistencyException {
        RolePlayer rp = playsRole(roleId, gr);
        if (rp == null) {
            throw new MoiseConsistencyException("the role "+roleId+" is not being played by "+getId()+" in the group "+gr);
        }
        
        // checks if this role is necessary for some mission
        if (oe.getOS().getSS().getBoolProperty("check-missions-in-remove-role", true)) {
            // for all missions
            //     try to find a permission from this tole to the mission
            for (MissionPlayer mp: missions.values()) {
                
                // check if rp is in the SCH's responsible groups
                if (mp.getScheme().getResponsibleGroups().contains(gr)) {
                    
                    // for all deontic relation from this role to the mission
                    Collection<Norm> cm = rp.getRole().getNorms(null, mp.getMission().getId());
                    if (cm.size() > 0) {
                        throw new MoiseConsistencyException("the "+getId()+"'s "+roleId+" role is necessary for his mission(s) "+cm+" in the scheme "+mp.getScheme());
                    }
                }
            }
        }
        abortRole( rp );
        return rp;
    }
    
    /**
     * removes a role of an agent without checking
     */
    public void abortRole(RolePlayer rp) throws MoiseConsistencyException {
        if (rp != null) {
            rp.getGroup().removePlayer(rp);
            roles.remove( rp.getGroup().getId()+"."+rp.getRole().getId());
        }
    }
    
    
    /**
     * removes the roles without checking
     */
    private void removeAllRoles()  {
        Iterator<RolePlayer> i = roles.values().iterator();
        if (i.hasNext()) {
            RolePlayer rp = i.next();
            
            rp.getGroup().removePlayer(rp);
            roles.remove( rp.getGroup().getId()+"."+rp.getRole().getId());
            removeAllRoles();
        }
    }
    
    
    /**
     * returns an Iterator for RolePlayers objects
     */
    public Collection<RolePlayer> getRoles() {
        return roles.values();
    }
    
    /**
     * returns the set of groups (class Group) where the roleId is being played
     */
    public Collection<GroupInstance> playsRole(String roleId) {
        HashSet<GroupInstance> all = new HashSet<GroupInstance>();
        for (RolePlayer rp: roles.values()) {
            if(rp.getRole().getId().equals(roleId)) {
                all.add(rp.getGroup());
            }
        }
        return all;
    }
    
    /**
     * returns an object representing the roleId played by this agent
     * in the group gr. returns null if roleId is not played by this
     * agent.
     */
    public RolePlayer playsRole(String roleId, GroupInstance gr) {
        return roles.get( gr.getId()+"."+roleId);
    }
    
    
    /**
     * returns a collection of RolePlayer objects representing the
     * roles this agent plays in the group gr
     */
    public Collection<RolePlayer> playsRole(GroupInstance gr) {
        HashSet<RolePlayer> all = new HashSet<RolePlayer>();
        for (RolePlayer rp: roles.values()) {
            if (rp.getGroup().equals(gr)) {
                all.add(rp);
            }
        }
        return all;
    }
    
    
    public int getNumberOfRoles() {
        return roles.size();
    }
    
    
    // --------------------------------
    // Mission
    //
    
    
    /**
     * adds a mission for an agent.
     *
     * <p>Example: <code>gomi.commitToMission("m7", sch.getId());</code>
     *
     * @param missionId the mission id (from OS)
     * @param schId the scheme id (from OE)
     * @throws MoiseConsistencyException
     * @throws MoiseCardinalityException
     */
    public MissionPlayer commitToMission(String missionId, String schId) throws MoiseConsistencyException, MoiseCardinalityException {
        SchemeInstance sch = oe.findScheme(schId);
        if (sch == null) {
            throw new MoiseConsistencyException("the instance sch "+schId+" does not exist in the OE");
        }
        return commitToMission(missionId, sch);
    }
    
    /**
     * adds a mission for an agent.
     *
     * <p>Example: <code>gomi.commitToMission("m7", sch);</code>
     * @param missionId the mission id (from OS)
     * @param sch the scheme object
     * @throws MoiseConsistencyException
     * @throws MoiseCardinalityException
     */
    public MissionPlayer commitToMission(String missionId, SchemeInstance sch) throws MoiseConsistencyException, MoiseCardinalityException {
        if (MoiseElement.getPrefix(missionId) == null) {
            missionId = sch.getSpec().getId()+"."+ missionId;
        }
        
        Mission mis = sch.getSpec().getMission(missionId);
        if (mis == null) {
            throw new MoiseConsistencyException("the mission "+missionId+" was not found in the scheme "+sch.getSpec());
        }
        
        if (getMission(missionId, sch) != null) {
            throw new MoiseConsistencyException("the agent is already committed to the mission "+missionId+" in  scheme "+sch);
        }
        
        if (sch.getRoot().isSatisfied() || sch.getRoot().isImpossible()) {
            String reason = "satisfied";
            if (sch.getRoot().isImpossible()) {
                reason = "impossible";
            }
            throw new MoiseConsistencyException("the scheme "+sch+" is finished (the goal "+sch.getRoot()+" is "+reason+"), so you can not commit to its missions.");
        }
        
        checkDS(mis, sch);
        
        missionMaxCardinalityCheck(mis, sch);
        
        MissionPlayer mp = new MissionPlayer(mis, this, sch);
        
        sch.addPlayer(mp);
        
        missions.put( sch.getId()+"."+missionId, mp);
        return mp;
    }
    
    
    /**
     * removes a mission from an agent.
     *
     * <p>Example: <code>gomi.removeMission("m7", "sch_test4");</code>
     * @param missionId the mission id (from OS)
     * @param sch the scheme identification
     * @throws MoiseConsistencyException when the agent tries to give up a mission with unsatisfied goals
     */
    public MissionPlayer removeMission(String missionId, String schId) throws MoiseException {
        SchemeInstance sch = oe.findScheme(schId);
        if (sch == null) {
            throw new MoiseConsistencyException("the instance scheme "+schId+" does not exist in the OE");
        }
        return removeMission(missionId, sch);
    }
    
    
    /**
     * removes a mission from an agent.
     *
     * <p>Example: <code>gomi.removeMission("m7", sch);</code>
     * or <br>
     * <code>gomi.removeMission("test.m7", sch);</code> <br/>
     * (the mission id is prefixed by the scheme id separated by ".")
     *
     * @param missionId the mission id (from OS)
     * @param sch the scheme object
     * @throws MoiseConsistencyException when the agent tries to give up a mission with unsatisfied goals
     */
    public MissionPlayer removeMission(String missionId, SchemeInstance sch) throws MoiseException {
        if (MoiseElement.getPrefix(missionId) == null) {
            missionId = sch.getSpec().getId()+"."+ missionId;
        }
        
        MissionPlayer mp = getMission(missionId, sch);
        if (mp == null) {
            throw new MoiseConsistencyException( this + " is not committed to the mission "+missionId+" in the scheme "+sch);
        }
        
        if (sch.getSpec().getFS().getBoolProperty("check-goals-in-remove-mission", true)) {
            // for all mission's goals of a "running" scheme, 
            //   the goal must 
            //          be already satisfied or 
            //          be impossible or
            //          be maintenance (they are never achieved)
            if (mp.getScheme().isCommitable()) {
                for (Goal gs: mp.getMission().getGoals()) {
                    GoalInstance ig = sch.getGoal( gs );
                    if (! (ig.isSatisfied() || ig.isImpossible() || gs.getType() == GoalType.maintenance)) {
                        throw new MoiseConsistencyException(this+" can not remove the committment to the mission "+missionId+" in the scheme "+sch+" since the goal "+ig+" is not satisfied or impossible");
                    }
                }
            }
        }
        
        sch.removePlayer(mp);
        missions.remove(sch.getId()+"."+missionId);
        
        return mp;
    }
    
    /**
     * removes a mission commitment without checking
     */
    public void abortMission(String missionId, SchemeInstance sch) throws MoiseException {
        if (MoiseElement.getPrefix(missionId) == null) {
            missionId = sch.getSpec().getId()+"."+ missionId;
        }
        MissionPlayer mp = missions.remove(sch.getId()+"."+missionId);
        sch.removePlayer(mp);
    }
    

    /**
     * finds a mission player object for the mission "missionId" in scheme sch
     */
    protected MissionPlayer getMission(String missionId, SchemeInstance sch) {
        // if missionId has no prefix, adds it
        if (MoiseElement.getPrefix(missionId) == null) {
            missionId = sch.getSpec().getId()+"."+ missionId;
        }
        
        return missions.get(sch.getId()+"."+missionId);
    }
    
    /**
     * finds a mission player object for the mission "missionId" in some scheme
     */
    public MissionPlayer getMission(String missionId) {
        for (MissionPlayer mp: missions.values()) {
            if (mp.getMission().getId().equals( missionId )) {
                return mp;
            }
        }
        return null;
    }
    
    /**
     * checks if this agent's roles (in the scheme groups) gives him permission for the mission
     */
    private void checkDS(Mission mis, SchemeInstance sch) throws MoiseConsistencyException {
        // for all roles
        //     try to find a permission for the mission
        for (RolePlayer rp: roles.values()) {

            if (rp.getRole().hasNorm(null, mis.getId())) { // the role has permission for the mission

                // check if rp is in the SCH's responsible groups
                if (sch.getResponsibleGroups().contains(rp.getGroup())) {
                    return; // ok!
                }
                
                // try also in rp super groups
                GroupInstance supergr = rp.getGroup().getSuperGroup();
                while (supergr != null) {
                    if (sch.getResponsibleGroups().contains(supergr)) {
                        return; // ok!
                    }
                    supergr = supergr.getSuperGroup();
                }
            }
        }
        
        
        throw new MoiseConsistencyException("the "+getId()+"'s roles (in the "+sch+" responsible groups) do not give him permission for the mission "+mis);
    }
    
    protected void missionMaxCardinalityCheck(Mission mis, SchemeInstance sch) throws MoiseCardinalityException {
        int maxMissions = sch.getSpec().getMissionCardinality(mis.getId()).getMax();
        int currNbPlayers = sch.getPlayersQty(mis.getId());
        //debug("the group "+newRoleGr+" has "+currNbPlayers+"/"+maxRoles+" number of the "+role+" role players.");
        if (currNbPlayers >= maxMissions) {
            throw new MoiseCardinalityException("the scheme "+sch+" already has the maximun ("+maxMissions+") number of the "+mis+" mission players.");
        }
    }
    
    protected boolean missionMinCardinalityCheck(Mission mis, SchemeInstance sch) {
        int minMissions   = sch.getSpec().getMissionCardinality(mis.getId()).getMin();
        int currNbPlayers = sch.getPlayersQty(mis.getId());
        return (currNbPlayers >= minMissions);
    }
    
    /**
     * returns an Iterator for MissionPlayers objects
     */
    public Collection<MissionPlayer> getMissions() {
        return missions.values();
    }
    
    /**
     * removes all missions without checking
     */
    private void removeAllMissions() throws MoiseException {
        // for all missions, uncommit
        Iterator<MissionPlayer> imis = missions.values().iterator();
        if (imis.hasNext()) {
            MissionPlayer mp = imis.next();
            abortMission( mp.getMission().getId(), mp.getScheme() );
            removeAllMissions();
        }
    }
    
    public int getNumberOfMissions() {
        return missions.size();
    }
    
    // --------------------------------
    // useful methods
    //
    
    
    /**
     * returns the possible global goals for this agent
     * (see GoalInstance.isPossible method).
     */
    public Collection<GoalInstance> getPossibleGoals() {
        // for all missions
        //    for all missions goals
        //       if goal isPossible
        //           ok
        ArrayList<GoalInstance> all = new ArrayList<GoalInstance>();
        
        for (MissionPlayer mp: missions.values()) {
            
            for (Goal gs: mp.getMission().getGoals()) {
                GoalInstance ig = mp.getScheme().getGoal( gs );
                if (ig.isEnabled()) {
                    all.add(ig);
                }
            }
        }
        return all;
    }

    /** returns a set of SCH where this agents has a mission */
    public Set<SchemeInstance> getAllMySchemes() {
        Set<SchemeInstance> allSch = new HashSet<SchemeInstance>();
        for (MissionPlayer mp: getMissions()) {
            allSch.add( mp.getScheme() );
        }
        return allSch;
    }
    
    
    /** get the left first leaf possible goal in the agent's schemes */
    public GoalInstance getLeafestPossibleGoal() {
        for (SchemeInstance sch: getAllMySchemes()) {
            GoalInstance gi = getLeafestPossibleGoal( sch );
            if (gi != null) {
                return gi;
            }
        }
        return null;
    }

    /** get the left first leaf possible goal in the scheme sch */
    public GoalInstance getLeafestPossibleGoal(SchemeInstance sch) {
        // deph-first search the plan's tree
        //     if the node (goal) is possible
        //          return this goal
        GoalInstance gi = getLeafestPossibleGoal( sch.getRoot().getPlanToAchieve() );
        if (gi != null) {
            return gi;
        } else {
            // tries the scheme root goal
            if (sch.getRoot().isEnabled() && isMyGoal(sch.getRoot(), sch)) {
                return sch.getRoot();
            }
        }
        return null;
    }
    private GoalInstance getLeafestPossibleGoal(PlanInstance p) {
        // for each plan goal
        //    if it has a plan, go recursively to this goal
        //    else (a leaf goal)
        //       if possible and my goal, return it
        for (GoalInstance g: p.getGoals()) {
            if (g.getPlanToAchieve() != null) {
                GoalInstance r = getLeafestPossibleGoal( g.getPlanToAchieve() );
                if (r != null) {
                    return r;
                }
            }
            //System.out.println(g.isPermitted() +":"+ g.isPossible() +":"+ isMyGoal(g, p.getSCH()));
            if (g.isEnabled() && isMyGoal(g, p.getScheme())) {
                return g;
            }
            
        }
        return null;
    }
    
    
    /** returns true if i am committed to the goal <i>g</i>. */
    public boolean isMyGoal(GoalInstance g, SchemeInstance sch) {
        for (MissionPlayer mp: missions.values()) {
            //System.out.println("1="+mp.getSCH().equals(sch));
            //System.out.println("2="+mp.getMission().getGoals().contains( g.getSpec() ));
            //System.out.println("3="+mp.getMission().getGoals() + "/" + g.getSpec() );
            if (mp.getScheme().equals(sch) && mp.getMission().getGoals().contains( g.getSpec() )) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     *   returns a collection of mission i am obligated to commit to.
     *
     *   each element in the returned collection is an Permission Object
     *   where:
     *     getRolePlayer() is the RolePlayer (rp), in this case "this" role player;
     *     getMission() is the Mission (m); and
     *     getScheme() is the Scheme instance (sch) where
     *     the rp is obligated to commit to m.
    */
    public Collection<Permission> getObligations() {
        // for all roles
        //    for all schemes where responsible group includes my roles group
        //        for all missions in the scheme that the role is obligated and this agent is not yet committed to
        //           if mission cardinality is not ok
        //               oooops, the agent must commit to!
        List<Permission> all = new ArrayList<Permission>();
        
        for (RolePlayer rp: roles.values()) { // all roles
            all.addAll( rp.getObligations() );
        }

        Collections.sort(all, new ObligationComparator());
        return all;
    }
    
    /**
     *   returns a collection of mission i am permitted to commit to.
     *
     *   each element in the returned collection is an Permission Object
     *   where:
     *     getRolePlayer() is the RolePlayer (rp), in this case "this" role player;
     *     getMission() is the Mission (m); and
     *     getScheme() is the Scheme instance (sch) where
     *     the rp is obligated to commit to m.
     */
    public Collection<Permission> getPermissions() {
        // for all roles
        //    for all schemes where responsible group includes my roles group
        //        for all missions in the scheme that the role is obligated and this agent is not yet committed to
        //           if mission cardinality is not ok
        //               oooops, the agent must commit to!
        List<Permission> all = new ArrayList<Permission>();
        for (RolePlayer rp: roles.values()) { // all roles
            all.addAll( rp.getPermissions() );
        }
        Collections.sort(all, new ObligationComparator());
        return all;
    }
    
    
    /**
     * returns a string describing this agent status regarding its obligations
     */
    public String getDeonticStatus() {
        StringBuffer s = new StringBuffer();
        
        for (Permission p: getObligations()) {
            s.append("The "+p.getRolePlayer().getRole()+" obligation for the mission "+p.getMission()+" in "+p.getScheme()+" is not ok. \n");
        }
        
        if (s.length() == 0) {
            s.append("ok");
        }
        return s.toString();
    }
    
    /**
     * returns true if this agent has a role with a <code>type</code> link to 
     * <code>other</code> agent. If <code>type = null</code>, any kind of link 
     * can be considered.
     */
    public boolean hasLink(String type, OEAgent other) {
        // for all roles
        //   for roles' links
        //     if intra-group
        //        if other is a link target in the same group than my role
        //          ok
        //     if inter-group
        //        if other is a link target
        //          ok
        if (type != null && type.equals("acquaintance") && other.equals(this)) { // acquaintance is reflexive
            return true;
        }
        for (RolePlayer rp: roles.values()) { // all roles
            for (Link link: rp.getLinks(type)) {
                GroupInstance gr = null;
                if (link.getScope() == RoleRelScope.IntraGroup) {
                    gr = rp.getGroup();
                }
                if (link.isBiDir()) {
                    for (OEAgent ag: oe.getAgents( gr, link.getSource())) {
                        if (ag.equals(other) && playsRole(rp.getRole().getId(), gr) != null) 
                            return true;
                    }
                } else {
                    for (OEAgent ag: oe.getAgents( gr, link.getTarget())) {
                        if (ag.equals(other))
                            return true;
                    }                    
                }
            }
        }
        return false;
    }
    
    public void setOE(OE oe) {
        this.oe = oe;
    }

    public String getXMLTag() {
        return "agent";
    }

    public Element getAsDOM(Document document) {
        Element ag = (Element) document.createElement(getXMLTag());
        ag.setAttribute("id", getId());
        ag.appendChild(document.createTextNode(getDeonticStatus()));
        
        // obligations
        for (Permission p: getObligations()) {
            ag.appendChild(p.getAsDOM(document, "obligation"));
        }

        // permissions
        for (Permission p: getPermissions()) {
            ag.appendChild(p.getAsDOM(document, "permission"));
        }
        
        // possible goals
        for (GoalInstance gi: getPossibleGoals()) {
            Element posg = (Element) document.createElement("possible-goal");
            posg.setAttribute("goal", gi.getSpec().getId());
            posg.setAttribute("scheme", gi.getScheme().getId());
            ag.appendChild(posg);
        }
        return ag;
    }
    
}
