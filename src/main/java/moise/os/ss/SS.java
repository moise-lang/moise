package moise.os.ss;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import moise.common.MoiseConsistencyException;
import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.os.OS;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents a Structural Specification.
 *
 * @navassoc - root-group  - Group
 * @composed - roles-definition  * Role
 *
 * @author Jomi Fred Hubner
 */
public class SS extends MoiseElement implements ToXML {

    private static final long serialVersionUID = 1L;

    protected Group              rootGrSpec  = null;
    protected Map<String,Role>   roles      = new HashMap<String,Role>();
    protected Set<String>        linkTypes  = new HashSet<String>();
    protected OS                 os         = null;

    /** Creates a new SS */
    public SS(OS os) {
        super();
        this.os = os;
        try {
            Role soc = new Role("soc", this);
            roles.put(soc.getId(),soc);
        } catch (Exception e) {
            System.out.println("something unexpected happens!");
            e.printStackTrace();
        }
        addLinkType("authority");
        addLinkType("communication");
        addLinkType("acquaintance");
    }

    /**
     * calls addRoleDef(r, true)
     */
    public Role addRoleDef(Role r) throws MoiseConsistencyException {
        return addRoleDef(r, true);
    }
    public Role addRoleDef(Role r, boolean check) throws MoiseConsistencyException {
        if (check && roles.get(r.getId()) != null) {
            throw new MoiseConsistencyException("the role "+r.getId()+" already exists.");
        }
        if (check && r.getId().equals("soc")) {
            throw new MoiseConsistencyException("the role 'soc' can not be added!");
        }
        if (! r.getId().equals("soc")) {
            roles.put(r.getId(), r);
        }
        return r;
    }

    /**
     * add all roles of iRole in this SS (check = false)
     */
    public void addRoleDef(Collection<Role> roles) throws MoiseConsistencyException {
        for (Role r: roles) {
            addRoleDef( r, false);
        }
    }

    /**
     * import all roles of iRole in this SS (check = false)
     * This method fixes links to super-roles from another SS and
     * do not include roles that already is in the SS
     */
    public void importRoleDef(Collection<Role> roles) throws MoiseConsistencyException {
        for (Role r: roles) {
            if (getRoleDef(r.getId()) == null) {
                r.setSS(this);
                Iterator<Role> sri = r.getSuperRoles().iterator();
                while (sri.hasNext()) {
                    Role sr = sri.next();
                    Role thisSR = getRoleDef(sr.getId());
                    if (thisSR != null) {
                        sri.remove();
                        r.addSuperRole(thisSR.getId());
                    }
                }
                addRoleDef(r, false);
            }
        }
    }


    public Role getRoleDef(String id) {
        return roles.get(id);
    }

    public Collection<Role> getRolesDef() {
        return roles.values();
    }

    public void addLinkType(String lt) {
        linkTypes.add(lt);
    }
    public void addLinkType(Collection<String> lt) {
        linkTypes.addAll(lt);
    }
    public boolean hasLinkType(String lt) {
        return linkTypes.contains(lt);
    }

    public Collection<String> getLinkTypes() {
        return linkTypes;
    }

    public void setRootGrSpec(Group gr) {
        rootGrSpec = gr;
    }

    public Group getRootGrSpec() {
        return rootGrSpec;
    }

    public OS getOS() {
        return os;
    }

    public static String getXMLTag() {
        return "structural-specification";
    }

    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());

        if (getProperties().size() > 0) {
            ele.appendChild( getPropertiesAsDOM(document));
        }

        // roles def
        if (getRolesDef().size() > 1) {
            Element rdefsEle = (Element) document.createElement("role-definitions");
            DFSRoles(getRoleDef("soc"), rdefsEle, document);
            ele.appendChild(rdefsEle);
        }

        // link types
        if (getLinkTypes().size() > 0) {
            Element ltypesEle = (Element) document.createElement("link-types");
            for (String lt: getLinkTypes()) {
                Element ltEle = (Element) document.createElement("link-type");
                ltEle.setAttribute("id",lt);
                ltypesEle.appendChild(ltEle);
            }
            ele.appendChild(ltypesEle);
        }

        // groups
        if (getRootGrSpec() != null) {
            ele.appendChild( getRootGrSpec().getAsDOM(document));
        }

        return ele;
    }

    /** make a DFS in the roles hierarchy to generate the right order of roles */
    private void DFSRoles(Role r, Element rdefsEle, Document document) {
        Element rEle = r.getAsDOM(document);
        if (rEle != null) {
            rdefsEle.appendChild(rEle);
        }
        for (Role sr: r.getSubRoles()) {
            DFSRoles(sr, rdefsEle, document);
        }
    }

    public void setFromDOM(Element ele) throws MoiseException {
        NodeList nl;
        setPropertiesFromDOM(ele);

        // role defs
        Element rdEle = DOMUtils.getDOMDirectChild(ele,"role-definitions");
        if (rdEle != null) {
            nl = rdEle.getElementsByTagName(Role.getXMLTag());
            for (int i=0; i<nl.getLength(); i++) {
                Element rEle = (Element)nl.item(i);
                Role r = new Role(rEle.getAttribute("id"), this);
                r.setFromDOM(rEle);
                addRoleDef(r);
            }
        }

        // link types
        Element ltEle = DOMUtils.getDOMDirectChild(ele,"link-types");
        if (ltEle != null) {
            nl = ltEle.getElementsByTagName("link-type");
            for (int i=0; i<nl.getLength(); i++) {
                Element lEle = (Element)nl.item(i);
                addLinkType(lEle.getAttribute("id"));
            }
        }

        // group specifications
        Element gEle = DOMUtils.getDOMDirectChild(ele, Group.getXMLTag());
        if (gEle != null) {
            Group gr = new Group(gEle.getAttribute("id"), this);
            gr.setFromDOM(gEle);
            setRootGrSpec(gr);
        }

    }
}
