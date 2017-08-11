package moise.oe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import moise.common.MoiseCardinalityException;
import moise.common.MoiseConsistencyException;
import moise.os.fs.Mission;
import moise.os.ns.NS.OpTypes;
import moise.os.ss.Link;
import moise.os.ss.Role;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 Represents the Role that an agent is playing in a group.

 @navassoc - agent   - OEAgent
 @navassoc - role-specification - Role
 @navassoc - group   - GroupInstance

 @author Jomi Fred Hubner
*/
public class RolePlayer extends Player implements ToXML {

    private static final long serialVersionUID = 1L;

    final private Role          role;
    final private GroupInstance gr;

    protected RolePlayer(Role role, OEAgent ag, GroupInstance gr) throws MoiseConsistencyException {
        super(ag);
        if (role == null) {
            throw new MoiseConsistencyException("role can not be null!");
        }
        this.role = role;
        this.gr   = gr;
    }

    public Role            getRole()    { return role; }
    public GroupInstance   getGroup()   { return gr; }

    /**
     * gives all links of type <code>type</code> (e.g. "communication")
     * that this role player has.
     * if type is null, all links are given
     */
    public Collection<Link> getLinks(String type) {
        List<Link> all = new ArrayList<Link>();
        for (Link link: role.getLinks( gr.getGrSpec() )) {
            if (type != null) {
                if (link.getTypeStr().equals(type)) {
                    all.add(link);
                } else if (link.getTypeStr().equals("communication") && type.equals("acquaintance")) {
                    all.add(link);
                } else if (link.getTypeStr().equals("authority") && type.equals("acquaintance")) {
                    all.add(link);
                } else if (link.getTypeStr().equals("authority") && type.equals("communication")) {
                    all.add(link);
                }
            } else {
                all.add(link);
            }
        }
        return all;
    }

    /**
     *   returns a collection of missions this role player is obliged to commit to.
     *
     *   each element in the returned collection is an Permission Object
     *   where:
     *     getRolePlayer() is the RolePlayer (rp), in this case "this" role player;
     *     getMission() is the Mission (m); and
     *     getScheme() is the Scheme instance (sch) where
     *     the rp is obligated to commit to m.
     */
    public Collection<Permission> getObligations() {
        return getNorms(OpTypes.obligation);
    }

    /**
     *   returns a collection of missions where this role player
     *   is permitted to commit to.
     *
     *   each element in the returned collection is a Permission Object
     *   where:
     *     getRolePlayer() is the RolePlayer (rp), in this case "this" role player;
     *     getMission() is the Mission (m); and
     *     getScheme() is the Scheme instance (sch) where
     *     the rp is obligated to commit to m.
     */
    public Collection<Permission> getPermissions() {
        return getNorms(OpTypes.permission);
    }

    private Collection<Permission> getNorms(OpTypes type) {
        //    for all schemes where responsible group includes my roles group
        //        for all missions in the scheme that the role is obligated/permitted and this agent is not yet committed to
        //           if mission cardinality is not ok
        //               oooops, the agent must/can commit to!
        List<Permission> all = new ArrayList<Permission>();

        // all schemes
        for (SchemeInstance sch: gr.getRespSchemes()) {
            if (sch.isCommitable()) {
                for (Mission mis: sch.getSpec().getMissions()) { // the mission in preferable order

                    // is the mission an obligation/permission for the role
                    if (role.getNorms( type, mis.getId()).size() > 0) {

                        // am i not commit to
                        if (player.getMission( mis.getId(), sch) == null) {

                            // is cardinality not ok

                            if (type == OpTypes.obligation) {
                                // test the minimum number of players
                                if (! player.missionMinCardinalityCheck(mis, sch)) {
                                    all.add( new Permission( this, mis, sch) );
                                }
                            } else {
                                // permissions
                                // test the maximum number of players
                                try {
                                    player.missionMaxCardinalityCheck(mis, sch);
                                    all.add( new Permission( this, mis, sch ) );
                                }  catch (MoiseCardinalityException e) {}
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(all, new ObligationComparator());
        return all;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + ((gr == null) ? 0 : gr.hashCode());
        result = PRIME * result + ((role == null) ? 0 : role.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)            return true;
        if (!super.equals(obj))     return false;
        if (getClass() != obj.getClass())   return false;
        final RolePlayer other = (RolePlayer) obj;
        if (gr == null) {
            if (other.gr != null)
                return false;
        } else if (!gr.equals(other.gr))
            return false;
        if (role == null) {
            if (other.role != null)
                return false;
        } else if (!role.equals(other.role))
            return false;
        return true;
    }


    public static String getXMLTag() {
        return "role-player";
    }


    public Element getAsDOM(Document document) {
        Element rpEle = (Element) document.createElement(getXMLTag());
        rpEle.setAttribute("role", getRole().getId());
        rpEle.setAttribute("agent", getPlayer().getId());
        return rpEle;
    }

    public String toString() {
        return player + "->" + role  + "(" + gr + ")";
    }
}
