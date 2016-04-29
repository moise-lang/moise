package moise.os.ss;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import moise.common.MoiseConsistencyException;
import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.os.ns.Norm;
import moise.os.ns.NS.OpTypes;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 Represents a Role Definition (its name and inheritance).
  
 @navassoc - super-roles * Role
 
 @author Jomi Fred Hubner
*/
public class Role extends MoiseElement implements ToXML {
    
    private static final long serialVersionUID = 1L;

    protected Set<Role> superRoles      = new HashSet<Role>();
    protected boolean   isAbstract      = true;
    protected SS        ss              = null;
    
    
    /** 
     * Creates a new Role
     * @param ss the SS this role will belongs to
     * @param id the identification of the role */
    public Role(String id, SS ss) {
        super(id);
        setSS(ss);
    }

    public void setSS(SS ss) {
        this.ss = ss;
    }
    
    public void addSuperRole(String superId) throws MoiseConsistencyException {
        Role superRole = ss.getRoleDef(superId);
        if (superRole == null) {
            throw new MoiseConsistencyException("Failed to extend the role "+getId()+", the super role "+superId+" was not defined!");
        }
        superRoles.add(superRole);
    }
    
    
    /**
     * gets the super roles of this role
     */
    public Collection<Role> getSuperRoles() {
        return superRoles;
    }
    
    /**
     * returns true if some of the direct super roles is equal r
     */
    public boolean containsSuperRole(Role r) {
        return superRoles.contains(r);
    }
    
    
    /**
     * gets the super roles of this role (including the super roles of the super roles....,
     * but excluding this role).
     * The returned map keys are the roles' id (String) and the value is the role
     * (object of the class Role)
     */
    public Map<String,Role> getAllSuperRoles() {
        // i've tried HashSet for the return, but it does not work well (does not use equals in the contains method)
        Set<Role> all = new HashSet<Role>(superRoles);
        for (Role r: superRoles) {
            all.addAll( r.getAllSuperRoles().values());
        }
        
        Map<String,Role> all2 = new HashMap<String,Role>();
        for (Role r: all) {
            all2.put(r.getId(), r);
        }
        
        return all2;
    }
    
    /**
     * gets the super roles of this role (including this role, the super roles of the super roles....)
     * The returned map keys are the roles' id (String) and the value is the role
     */
    public Map<String,Role> getEntailedRoles() {
        Map<String,Role> allAndI = getAllSuperRoles();
        allAndI.put(getId(), this);
        return allAndI;
    }
    
    /**
     * gets the direct specialisations (sub-roles) of this role.
     * It does not return the sub-sub-roles.
     */
    public Collection<Role> getSubRoles() {
        Set<Role> all = new HashSet<Role>();
        for (Role r: ss.getRolesDef()) {
            if (r.containsSuperRole( this )) {
                all.add(r);
            }
        }
        return all;
    }
    
    
    /**
     * gets a list of groups where this role can be played
     */
    public Collection<Group> getGroups() {
        Set<Group> all = new HashSet<Group>();
        for (Group gr: ss.getRootGrSpec().getAllSubGroupsTree()) { 
            if (gr.containsRole( this )) {
                all.add(gr);
            }
        }
        return all;
    }
    
    
    /**
     * gets all compatibilities for this role (and its super roles) in the context of the GrSpec
     */
    public Collection<Compatibility> getCompatibilities(Group gr) {
        Set<Compatibility> all = new HashSet<Compatibility>();
        for (Compatibility c: gr.getUpCompatibilities()) {
            if (c.sourceContains(this) || (c.isBiDir() && c.targetContains(this))) {
                all.add(c);
            }
        }
        return all;
    }
    
    /**
     * gets all links for this role (and its super roles) in the context of the GrSpec
     */
    public Collection<Link> getLinks(Group gr) {
        Set<Link> all = new HashSet<Link>();
        for (Link l: gr.getUpLinks()) {
            if (l.sourceContains(this) || (l.isBiDir() && l.targetContains(this))) {
                all.add(l);
            }
        }
        return all;
    }
    
    /**
     * gets all deontic relations (obligations, permissions, ...) for this role (and its super roles)
     */
    public Collection<Norm> getDeonticRelations() {
        return getNorms(null, null);
    }
    
    /**
     * gets norms for this role (and its super roles).
     * if type != null, the norm type must be equals to type (all obligation is a permission).
     * if mission != null, the norm mission must be equals to mission
     */
    public Collection<Norm> getNorms(OpTypes type, String mission) {
        Set<Norm> all = new HashSet<Norm>();
        for (Norm op: ss.getOS().getNS().getNorms()) {
            if (type != null && 
                type != op.getType() &&
                ! (type == OpTypes.permission && op.getType() == OpTypes.obligation)) {
                continue;
            }
            if (mission != null && ! mission.equals(op.getMission().getId())) {
                continue;
            }
            if (this.getEntailedRoles().containsValue(op.getRole())) {
                all.add(op);
            }
        }
        return all;
    }

    /**
     * returns true if this role has a norm towards the mission.
     * if type != null, the norm type must be equals to type (all obligation is a permission).
     * if mission != null, the norm mission must be equals to mission
     */
    public boolean hasNorm(OpTypes type, String mission) {
        for (Norm op: ss.getOS().getNS().getNorms()) {
            if (type != null && 
                    type != op.getType() &&
                    ! (type == OpTypes.permission && op.getType() == OpTypes.obligation)) {
                continue;
            }
            if (mission != null && ! mission.equals(op.getMission().getId())) {
                continue;
            }
            if (getEntailedRoles().containsValue(op.getRole())) {
                return true;
            }
        }
        return false;
    }
    
    
    /** gets properties of this role (it also looks at
     *  super roles properties not "over written"
     */
    public Object getProperty(String id) {
        Object o = super.getProperty(id);
        if (o != null) {
            return o;
        } else {
            // looks at super roles
            for (Role r: superRoles) {
                o = r.getProperty(id);
                if (o != null) {
                    return o;
                }
            }
        }
        return null;
    }
    
    
    protected void setAbstract(boolean a) {
        isAbstract = a;
    }
    
    public boolean isAbstract() {
        return isAbstract;
    }

    public static String getXMLTag() {
        return "role";
    }
    
    public Element getAsDOM(Document document) {
        Element ele = null;
        if (! getId().equals("soc")) {
            ele =  (Element) document.createElement(getXMLTag());
            ele.setAttribute("id", getId());
            if (getProperties().size() > 0) {
                ele.appendChild( getPropertiesAsDOM(document));
            }

            // super roles
            for (Role r: getSuperRoles()) {
                Element ext = (Element) document.createElement("extends");
                ext.setAttribute("role",r.getId());
                ele.appendChild(ext);
            }
        }            
        return ele;
    }    

    public Element getAsDetailedDom(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("id", getId());
        if (getProperties().size() > 0) {
            ele.appendChild( getPropertiesAsDOM(document));
        }

        // super roles
        generateFullSuperRolesBranch(ele, getSuperRoles(), document);
        
        // Specialisations
        for (Role r: getSubRoles()) {
            Element ext = (Element) document.createElement("specialization");
            ext.setAttribute("role",r.getId());
            ele.appendChild(ext);
        }
        
        // groups
        for (Group gr: getGroups()) {
            Element ext = (Element) document.createElement("group");
            ext.setAttribute("id", gr.getId());
            
            // Links
            for (Link l: getLinks(gr)) {
                Element le = l.getAsDOM(document);
                le.setAttribute("gr-id", l.getGrSpec().getId());
                ext.appendChild(le);
            }
            
            // Compatibilities
            for (Compatibility c: getCompatibilities(gr)) {
                Element ce = c.getAsDOM(document);
                ce.setAttribute("gr-id", c.getGrSpec().getId());
                ext.appendChild(ce);
            }
            ele.appendChild(ext);
        }
        
        // Obligations Permissions
        for (Norm dr: getDeonticRelations()) {
            ele.appendChild(dr.getAsDOM(document));
        }
        return ele;
    }
    private void generateFullSuperRolesBranch(Element ele, Collection<Role> roles, Document document) {
        for (Role r: roles) {
            Element ext = (Element) document.createElement("extends");
            ext.setAttribute("role",r.getId());
            generateFullSuperRolesBranch(ext, r.getSuperRoles(), document);
            ele.appendChild(ext);
        }
    }

    
    public void setFromDOM(Element ele) throws MoiseException {
        setPropertiesFromDOM(ele);
            
        // role defs
        NodeList nl = ele.getElementsByTagName("extends");
        if (nl.getLength() == 0) { // no extends
            addSuperRole("soc");
        }
        for (int i=0; i<nl.getLength(); i++) {
            Element rEx = (Element)nl.item(i);
            addSuperRole(rEx.getAttribute("role"));
        }
    }
}
