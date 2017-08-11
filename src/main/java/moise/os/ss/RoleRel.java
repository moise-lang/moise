package moise.os.ss;

import moise.common.MoiseConsistencyException;
import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.common.MoiseXMLParserException;
import moise.prolog.ToProlog;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a relation between roles (links, compatibilities, etc).
 *
 * @navassoc - source  - Role
 * @navassoc - target  - Role
 * @navassoc - scope  - RoleRelScope
 *
 * @author Jomi Fred Hubner
 */
public abstract class RoleRel extends MoiseElement implements ToXML {

    public enum RoleRelScope implements ToProlog {
        IntraGroup {
            public String toString() { return "intra-group"; }
            public String getAsProlog() { return "intra_group"; }
        },
        InterGroup {
            public String toString() { return "inter-group"; }
            public String getAsProlog() { return "inter_group"; }
        }
    }

    protected Role source = null;
    protected Role target = null;
    protected RoleRelScope scope = RoleRelScope.IntraGroup;
    protected boolean extendsToSubGroups  = false;
    protected boolean biDirectional = false;

    protected Group grSpec = null; // the group where the rel is defined

    /** Creates new Link */
    public RoleRel() {
    }

    /** Creates new Link */
    public RoleRel(Role s, Role d) {
        source = s;
        target = d;
        setId();
    }

    private void setId() {
        if (source != null || target != null) {
            String s1 = source != null ? source.getFullId() : "";
            String s2 = target != null ? target.getFullId() : "";
            setId(s1+s2);
        }
    }

    public Role getSource() {
        return source;
    }

    public Role getTarget() {
        return target;
    }

    protected String getTypeStr() {
        return "";
    }

    public String getXMLTag() {
        return "noTag";
    }

    public Group getGrSpec() {
        return grSpec;
    }

    public void setScope(RoleRelScope s) throws MoiseConsistencyException {
        scope = s;
    }

    public void setScope(String s) throws MoiseConsistencyException {
        if (s == null || s.isEmpty())
            return;
        boolean ok = false;
        for (RoleRelScope op: RoleRelScope.values()) {
            if (op.toString().equals(s)) {
                scope = op;
                ok = true;
            }
        }
        if (!ok) {
            throw new MoiseConsistencyException(s+" is not a valid value for link scope");
        }
    }

    public RoleRelScope getScope() {
        return scope;
    }


    public void setExtendsToSubGroups(boolean b) {
        extendsToSubGroups = b;
    }

    public boolean getExtendsToSubGroups() {
        return extendsToSubGroups;
    }

    public String getExtendsToSubGroupsStr() {
        if (extendsToSubGroups) {
            return "true";
        } else {
            return "false";
        }
    }

    public void setBiDir(boolean b) {
        biDirectional = b;
    }

    public boolean isBiDir() {
        return biDirectional;
    }

    public String getBiDirStr() {
        if (biDirectional) {
            return "true";
        } else {
            return "false";
        }
    }


    /**
     * checks if the source role is equal to r (or its super roles)
     * (in case this relation is symmetric, this methods also checks the destination)
     */
    public boolean sourceContains(Role r) {
        return r.getEntailedRoles().containsValue( source );
    }

    /**
     * checks if the target role is equal to r (or its super roles)
     * (in case this relation is bi-dir, this methods also checks the source)
     */
    public boolean targetContains(Role r) {
        return r.getEntailedRoles().containsValue( target );
    }


    /**
     * checks if one of this relation's role are equal to r (or its super roles)
     */
    public boolean contains(Role r) {
        return sourceContains(r) || targetContains(r);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof RoleRel) {
            RoleRel other = (RoleRel)o;
            return
                other.source.equals(this.source) &&
                other.target.equals(this.target) &&
                other.scope.equals(this.scope) &&
                other.biDirectional == this.biDirectional &&
                other.extendsToSubGroups == this.extendsToSubGroups;
        }
        return false;
    }

    public String toString() {
        return source + " "+scope+ " " + getTypeStr() + " link to " + target + " (extendsToSubGroups=" + getExtendsToSubGroupsStr()+", bi-dir="+getBiDirStr()+")";
    }

    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("from",  getSource().getId());
        ele.setAttribute("to",    getTarget().getId());
        ele.setAttribute("type",  getTypeStr());
        ele.setAttribute("scope", scope.toString());
        ele.setAttribute("extends-subgroups", getExtendsToSubGroupsStr());
        ele.setAttribute("bi-dir", getBiDirStr());
        if (getProperties().size() > 0) {
            ele.appendChild( getPropertiesAsDOM(document));
        }
        return ele;
    }

    public void setFromDOM(Element ele) throws MoiseException {
        setPropertiesFromDOM(ele);
        Role source = grSpec.getSS().getRoleDef(ele.getAttribute("from"));
        if (source == null) {
            throw new MoiseXMLParserException("The source role "+ele.getAttribute("from")+" does not exist!");
        }
        this.source = source;

        Role destination = grSpec.getSS().getRoleDef(ele.getAttribute("to"));
        if (destination == null) {
            throw new MoiseXMLParserException("The destination role "+ele.getAttribute("to")+" does not exist!");
        }
        this.target =  destination;

        setScope(ele.getAttribute("scope"));
        String subGroups = ele.getAttribute("extends-subgroups");
        if (!subGroups.isEmpty())
            setExtendsToSubGroups(Boolean.valueOf(subGroups).booleanValue());
        String biDir = ele.getAttribute("bi-dir");
        if (!biDir.isEmpty())
            setBiDir( Boolean.valueOf(biDir).booleanValue());
        setId();
    }
}
