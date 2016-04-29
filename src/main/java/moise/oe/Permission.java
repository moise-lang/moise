package moise.oe;

import java.io.Serializable;

import moise.os.fs.Mission;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/** represents an agent's obligation or permission */

public class Permission implements Serializable {
    RolePlayer     rp;
    Mission        mis;
    SchemeInstance sch;
    
    public Permission(RolePlayer rp, Mission mis, SchemeInstance sch) {
        this.rp = rp;
        this.mis = mis;
        this.sch = sch;
    }
    
    public RolePlayer getRolePlayer() {
        return rp;
    }
    public Mission getMission() {
        return mis;
    }
    public SchemeInstance getScheme() {
        return sch;
    }
    
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((mis == null) ? 0 : mis.hashCode());
        result = PRIME * result + ((rp == null) ? 0 : rp.hashCode());
        result = PRIME * result + ((sch == null) ? 0 : sch.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)            return true;
        if (obj == null)            return false;
        if (getClass() != obj.getClass()) return false;
        final Permission other = (Permission) obj;
        if (mis == null) {
            if (other.mis != null)
                return false;
        } else if (!mis.equals(other.mis))
            return false;
        if (rp == null) {
            if (other.rp != null)
                return false;
        } else if (!rp.equals(other.rp))
            return false;
        if (sch == null) {
            if (other.sch != null)
                return false;
        } else if (!sch.equals(other.sch))
            return false;
        return true;
    }

    public Element getAsDOM(Document document, String tag) {
            Element permEle = (Element) document.createElement(tag);
            permEle.setAttribute("role", getRolePlayer().getRole().getId());
            permEle.setAttribute("group", getRolePlayer().getGroup().getId());
            permEle.setAttribute("mission", getMission().getId());
            permEle.setAttribute("scheme", getScheme().getId());
            return permEle;
    }
    
    public String toString() {
        return "("+rp+"->"+mis+" in "+sch+")";
    }
    
}
