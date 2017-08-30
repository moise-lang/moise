package moise.os.ss;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import moise.common.MoiseConsistencyException;
import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.common.MoiseXMLParserException;
import moise.os.Cardinality;
import moise.os.CardinalitySet;
import moise.os.OS;
import moise.prolog.ToProlog;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents a Group Specification.
 *
 * @navassoc - roles  * Role
 * @composed - subgroups  * Group
 * @navassoc - super-group  - Group
 * @composed - links * Link
 * @composed - compatibilities * Compatibility
 *
 * @author Jomi Fred Hubner
 */
public class Group extends MoiseElement implements ToXML, ToProlog {

    // the roles that can be played in this group
    protected CardinalitySet<Role>  roles     = new CardinalitySet<Role>();

    protected CardinalitySet<Group> subgroups = new CardinalitySet<Group>();

    protected Group                superGr    = null;
    protected Set<Link>            links      = new HashSet<Link>();
    protected Set<Compatibility>   compatibilities = new HashSet<Compatibility>();
    //protected String               monitoring  = null;
    protected SS                   ss          = null;

    private static final long serialVersionUID = 1L;

    /** Creates new GrSpec */
    public Group(SS ss) {
        super();
        this.ss = ss;
    }

    /** Creates new GrSpec */
    public Group(String id, SS ss) {
        super(id);
        this.ss = ss;
    }

    protected void setSuperGroup(Group gr) {
        superGr = gr;
    }

    public Group getSuperGroup() {
        return superGr;
    }

    /** returns true if this group is not a subgroup of another group */
    public boolean isRoot() {
        return superGr == null;
    }

    public SS getSS() {
        return ss;
    }

    /*
    public void setMonitoringSch(String schId) {
        monitoring = schId;
    }
    public String getMonitoringSch() {
        return monitoring;
    }
    */

    //
    // Role methods
    //

    /**
     * adds the roleId role into the playable roles in this group
     */
    public Role addRole(String roleId) throws MoiseConsistencyException {
        Role r = ss.getRoleDef(roleId);
        if (r == null) {
            throw new MoiseConsistencyException("Failed to add the role "+roleId+" to the GrSpec "+getId()+", the role "+roleId+" was not defined!");
        }
        r.setAbstract(false);
        roles.add(r);
        return r;
    }

    /**
     * removes the roleId role from the playable roles in this group
     */
    public void removeRole(String roleId) throws MoiseConsistencyException {
        Role r = ss.getRoleDef(roleId);
        if (r == null) {
            throw new MoiseConsistencyException("Failed to remove the role "+roleId+" from the GrSpec "+getId()+", the role "+roleId+" was not defined!");
        }
        roles.remove(r);
    }

    /**
     * checks whether the roleId can be played in this group
     */
    public boolean containsRole(Role r) {
        return roles.contains(r);
    }


    public void setRoleCardinality(String roleId, Cardinality c) throws MoiseConsistencyException {
        Role r = ss.getRoleDef(roleId);
        if (r == null) {
            throw new MoiseConsistencyException("Failed to register the cardinality for the role "+roleId+", it was not defined!");
        }
        if (!c.equals(Cardinality.defaultValue)) {
            roles.setCardinality(r, c);
        }
    }


    /**
     * returns the cardinality for the <roleId>. If it is not defined, returns null.
     */
    public Cardinality getRoleCardinality(Role role) {
        return roles.getCardinality(role);
    }

    /**
     * returns a collection of this group's roles
     */
    public CardinalitySet<Role> getRoles() {
        return roles;
    }

    //
    // Link methods
    // ------------------------

    public void addLink(Link l) {
        links.add(l);
    }

    /**
     * returns a collection for the Link objects defined in this group
     */
    public Collection<Link> getLinks() {
        return links;
    }

    public int getLinksQty() {
        return links.size();
    }

    /**
     * gets the links of this group an its supergroups's extendible links
     */
    public Set<Link> getUpLinks() {
        Set<Link> all = new HashSet<Link>(links); // add all links of this group
        Group gr = superGr;                       // and those from super groups
        while (gr != null) {
            all.addAll( gr.getExtendibleLinks() );
            gr = gr.getSuperGroup();
        }
        return all;
    }

    /**
     * gets the group's Link objects which are extendible to sub groups
     */
    public Collection<Link> getExtendibleLinks() {
        Set<Link> all = new HashSet<Link>();
        for (Link l: links) {
            if (l.getExtendsToSubGroups()) {
                all.add(l);
            }
        }

        return all;
    }

    //
    // compatibility methods
    // -----------------------------

    public void addCompatibility(Compatibility c) {
        compatibilities.add(c);
    }


    /**
     * gets the group's Compatibility objects which are extendible to sub groups
     */
    public Collection<Compatibility> getExtendibleCompatibilities() {
        Set<Compatibility> all = new HashSet<Compatibility>();
        for (Compatibility c: compatibilities) {
            if (c.getExtendsToSubGroups()) {
                all.add(c);
            }
        }

        return all;
    }

    /**
     * gets the Compatibility objects of this group an its
     * supergroups's Extendible compatibilities
     */
    public Collection<Compatibility> getUpCompatibilities() {
        Set<Compatibility> all = new HashSet<Compatibility>(compatibilities);
        Group gr = superGr;
        while (gr != null) {
            all.addAll( gr.getExtendibleCompatibilities() );
            gr = gr.getSuperGroup();
        }
        return all;
    }

    /**
     * return a collection of this group Compatibility objects
     */
    public Collection<Compatibility> getCompatibilities() {
        return compatibilities;
    }

    public int getCompatibilitiesQty() {
        return compatibilities.size();
    }

    //
    // Subgroups methods
    //
    public void addSubGroup(Group gr) {
        subgroups.add(gr);
        gr.setSuperGroup(this);
    }

    /**
     * gets the direct sub groups of this group
     */
    public CardinalitySet<Group> getSubGroups() {
        return subgroups;
    }

    /**
     * gets this group and its all sub groups, the subgroups of the subgroups, .....
     */
    public Collection<Group> getAllSubGroupsTree() {
        Set<Group> all = new HashSet<Group>();
        all.add(this);
        for (Group gr: subgroups) {
            all.addAll( gr.getAllSubGroupsTree());
        }
        return all;
    }

    /**
     * gets the grId subgroup of this group (does not looks for the subgroups' subgroups)
     */
    public Group getSubGroup(String grId) {
        for (Group gr: subgroups) {
            if (gr.getId().equals(grId)) {
                return gr;
            }
        }
        return null;
    }

    /**
     * looks for grId in this group and in its subgroups
     */
    public Group findSubGroup(String grId) {
        if (this.getId().equals(grId)) {
            return this;
        }
        for (Group gr: subgroups) {
            Group g = gr.findSubGroup(grId);
            if (g != null) {
                return g;
            }
        }
        return null;
    }

    public void setSubGroupCardinality(String grId, Cardinality c) throws MoiseConsistencyException {
        Group gr = findSubGroup(grId);
        if (gr == null) {
            throw new MoiseConsistencyException("Failed to register the cardinality for the group "+grId+", it was not defined!");
        }
        if (! c.equals(Cardinality.defaultValue)) {
            subgroups.setCardinality(gr, c);
        }
    }

    public Cardinality getSubGroupCardinality(Group gr) {
        return subgroups.getCardinality(gr);
    }

    public static String getXMLTag() {
        return "group-specification";
    }

    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("id",getId());
        //if (getMonitoringSch() != null)
        //    ele.setAttribute("monitoring-scheme", getMonitoringSch());

        // properties
        if (getProperties().size() > 0) {
            ele.appendChild( getPropertiesAsDOM(document));
        }

        // roles
        if (!getRoles().isEmpty()) {
            Element rolesEle = (Element)document.createElement("roles");
            for (Role r: getRoles()) {
                Element rEle = (Element)document.createElement("role");
                rEle.setAttribute("id", r.getId());
                rolesEle.appendChild(rEle);
            }
            ele.appendChild(rolesEle);
        }

        // links
        if (getLinksQty() > 0) {
            Element linksEle = (Element)document.createElement("links");
            for (Link l: getLinks()) {
                linksEle.appendChild(l.getAsDOM(document));
            }
            ele.appendChild(linksEle);
        }


        // subgroups
        if (!getSubGroups().isEmpty()) {
            Element sgrsEle = (Element)document.createElement("subgroups");
            for (Group subGr: getSubGroups()) {
                sgrsEle.appendChild( subGr.getAsDOM(document));
            }
            ele.appendChild(sgrsEle);
        }

        Element cfEle = (Element)document.createElement("formation-constraints");

        // role cardinality
        for (Role rId: roles) {
            Cardinality card = roles.getCardinality(rId);
            if (! card.equals(Cardinality.defaultValue)) {
                Element cardEle = card.getAsDOM(document);
                cardEle.setAttribute("object","role");
                cardEle.setAttribute("id",rId.getId());
                cfEle.appendChild(cardEle);
            }
        }

        // sub group cardinality
        for (Group gr: subgroups) {
            Cardinality card = subgroups.getCardinality(gr);
            if (card != null && ! card.equals(Cardinality.defaultValue)) {
                Element cardEle = card.getAsDOM(document);
                cardEle.setAttribute("object","group");
                cardEle.setAttribute("id",gr.getId());
                cfEle.appendChild(cardEle);
            }
        }

        // the cardinality of this group
        Cardinality card = subgroups.getCardinality(this);
        if (card != null && ! card.equals(Cardinality.defaultValue)) {
            Element cardEle = card.getAsDOM(document);
            cardEle.setAttribute("object","group");
            cardEle.setAttribute("id",getId());
            cfEle.appendChild(cardEle);
        }


        // compatibilities
        for (Compatibility c: getCompatibilities()) {
            cfEle.appendChild(c.getAsDOM(document));
        }
        ele.appendChild(cfEle);

        return ele;
    }

    public void setFromDOM(Element ele) throws MoiseException {
        NodeList nl;

        setPropertiesFromDOM(ele);

        // monitoring-scheme
        //if (ele.getAttribute("monitoring-scheme").length() > 0)
        //    setMonitoringSch(ele.getAttribute("monitoring-scheme"));

        // roles
        Element e = DOMUtils.getDOMDirectChild(ele, "roles");
        if (e != null) {
            nl = e.getElementsByTagName(Role.getXMLTag());
            for (int i=0; i<nl.getLength(); i++) {
                Element rEle = (Element)nl.item(i);
                String roleId = rEle.getAttribute("id");
                if (getSS().getRoleDef(roleId) == null) { // add the role def is not done yet
                    getSS().addRoleDef(new Role(roleId, getSS())).addSuperRole("soc");
                }
                addRole(roleId);

                // reads role cardinality
                Cardinality c = new Cardinality();
                c.setFromDOM(rEle);
                setRoleCardinality(roleId, c);
            }
        }

        // links
        e = DOMUtils.getDOMDirectChild(ele, "links");
        if (e != null) {
            nl = e.getElementsByTagName("link");
            for (int i=0; i<nl.getLength(); i++) {
                Element lEle = (Element)nl.item(i);
                Link l = new Link(this, lEle.getAttribute("type"));
                l.setFromDOM(lEle);
                addLink(l);
            }
        }

        // subgroups
        e = DOMUtils.getDOMDirectChild(ele,"subgroups");
        if (e != null) {
            for (Element gEle: DOMUtils.getDOMDirectChilds(e,Group.getXMLTag())) { //e.getElementsByTagName(Group.getXMLTag());
                Group gr = new Group(gEle.getAttribute("id"), ss);
                gr.setFromDOM(gEle);
                addSubGroup(gr);

                // reads subgroups cardinality
                Cardinality c = new Cardinality();
                c.setFromDOM(gEle);
                setSubGroupCardinality(gr.getId(),c);
            }
        }

        // include
        nl = ele.getElementsByTagName("include-group-specification");
        for (int i=0; i<nl.getLength(); i++) {
            Element iEle = (Element)nl.item(i);
            String uri = iEle.getAttribute("uri");

            // is the uri full path?
            if (uri.indexOf( System.getProperty("file.separator")) < 0) {
                String fatherURI = ss.getOS().getURI();
                if (fatherURI != null) {
                    fatherURI = fatherURI.substring(0, fatherURI.lastIndexOf(System.getProperty("file.separator"))+1).trim();
                    uri = fatherURI + uri;
                }
            }

            OS incOS = OS.loadOSFromURI(uri);
            if (incOS != null) {
                ss.importRoleDef(incOS.getSS().getRolesDef());
                ss.addLinkType(incOS.getSS().getLinkTypes());

                ss.getOS().addFS(incOS.getFS());
                ss.getOS().addNS(incOS.getNS());
                addSubGroup(incOS.getSS().getRootGrSpec());
            } else {
                throw new MoiseXMLParserException("Error in included URI "+uri+".");
            }
        }


        e = DOMUtils.getDOMDirectChild(ele,"formation-constraints");
        if (e != null) {
            // role cardinality
            // sub group cardinality
            // the cardinality of this group

            nl = e.getElementsByTagName("cardinality");
            for (int i=0; i<nl.getLength(); i++) {
                Element cEle = (Element)nl.item(i);

                Cardinality card = new Cardinality();
                card.setFromDOM(cEle);

                if (cEle.getAttribute("object").equals("role")) {
                    setRoleCardinality(cEle.getAttribute("id"), card);
                } else if (cEle.getAttribute("object").equals("group")) {
                    setSubGroupCardinality(cEle.getAttribute("id"),card);
                }
            }

            // compatibilities
            nl = e.getElementsByTagName("compatibility");
            for (int i=0; i<nl.getLength(); i++) {
                Element cEle = (Element)nl.item(i);
                Compatibility compat = new Compatibility(this);
                compat.setFromDOM(cEle);
                addCompatibility(compat);
            }
        }
    }

    /** returns a string as a prolog predicate representing the group specification.
     *  <p>The format is: group_specification(group type id, list of role, list of subgroups, properties).<br/>
     *  each role in the list is: role(id, list of sub-roles, list of super-roles, min cardinality, max cardinality, list of compatible roles, list of links).<br/>
     *  each link is: link(type, target, scope).
     */
    public String getAsProlog() {
        StringBuilder s = new StringBuilder("group_specification("+getId()+",[");

        // roles
        String v = "";
        for (Role r: getRoles()) {

            s.append(v+"role("+r.getId()+",[");

            // inheritance
            String v1 = "";
            for (Role subr: r.getSubRoles()) {
                s.append(v1+ subr.getId());
                v1 = ",";
            }

            // super-roles
            s.append("],[");
            v1 = "";
            for (Role subr: r.getAllSuperRoles().values()) {
                s.append(v1+ subr.getId());
                v1 = ",";
            }

            // cardinality
            Cardinality card = roles.getCardinality(r);
            s.append("],"+card.getMin()+","+card.getMax()+",[");

            // compatibilities of the role
            v1 = "";
            for (Compatibility c: r.getCompatibilities(this)) {
                if (c.getSource().equals(r))
                    s.append(v1+c.getTarget().getId()); // use target
                else
                    s.append(v1+c.getSource().getId()); // use source
                v1 = ",";
            }
            s.append("],[");

            // links of the role
            v1 = "";
            for (Link l: r.getLinks(this)) {
                String other = "";
                if (l.sourceContains(r))
                    other = l.getTarget().getId(); // use target
                else
                    other = l.getSource().getId(); // use source
                s.append(v1+"link("+l.getTypeStr()+","+other+","+l.getScope().getAsProlog()+")");
                v1=",";
            }
            s.append("])");

            v=",";
        }
        s.append("],");

        // subgroups
        s.append("[");
        v = "";
        for (Group gr: getSubGroups()) {
            // sub group cardinality
            Cardinality card = subgroups.getCardinality(gr);
            s.append(v+"subgroup("+card.getMin()+","+card.getMax()+","+gr.getAsProlog()+")");
            v = ",";
        }
        s.append("],");

        // properties
        s.append(getPropertiesAsProlog());

        s.append(")");
        return s.toString();
    }
}
