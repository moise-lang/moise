package moise.os.ns;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.os.OS;
import moise.os.ss.Role;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a Normative Specification.
 *
 * @composed - norms * Norm
 *
 * @author Jomi Fred Hubner
 */
public class NS extends MoiseElement implements ToXML {

    private static final long serialVersionUID = 1L;

    public enum OpTypes { obligation, permission };

    protected Set<Norm> norms = new HashSet<Norm>();
    private   OS        os    = null;

    protected String    nplProgram = null;

    //protected Map<String,String>   opTypes   = new HashMap<String,String> ();

    /** Creates new NS */
    public NS(OS os) {
        super();
        this.os = os;
    }

    public OS getOS() {
        return os;
    }

    public void addNorm(Norm n) {
        norms.add(n);
    }
    public void addNorm(Collection<Norm> ns) {
        norms.addAll(ns);
    }

    public Norm getNorm(String nId) {
        for (Norm n: norms) {
            if (n.getId().equals(nId))
                return n;
        }
        return null;
    }

    public Collection<Norm> getNorms() {
        return norms;
    }

    public String getNPLNorms() {
        return nplProgram;
    }

    /** returns true whether there is a <i>type</i> norm from roleId to missionId.
     *  if some of the parameters is null, it will not considered in the comparison
     */
    public boolean hasNorm(String roleId, String missionId, OpTypes type) {
        for (Norm dr: norms) {
            boolean has = true;
            if (roleId != null) {
                if (!dr.getRole().getId().equals(roleId) ) {
                    has = false;
                }
            }
            if (has && missionId != null) {
                if (!dr.getMission().getId().equals(missionId) ) {
                    has = false;
                }
            }
            if (has && type != null) {
                if (dr.getType() != type) {
                    has = false;
                }
            }

            if (has) {
                return true;
            }
        }
        return false;
    }

    /** remove the norms for the role r */
    public void removeNorms(Role r) {
        for (Norm n: norms) {
            if (n.getRole().equals( r )) {
                norms.remove( n );
                // the iterator is invalid, so abort this loop
                removeNorms(r);
                return;
            }
        }
    }


    public static String getXMLTag() {
        return "normative-specification";
    }

    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        if (getProperties().size() > 0) {
            ele.appendChild( getPropertiesAsDOM(document));
        }

        for (Norm n: norms) {
            ele.appendChild(n.getAsDOM(document));
        }
        return ele;
    }

    public void setFromDOM(Element ele) throws MoiseException {
        setPropertiesFromDOM(ele);

        for (Element e: DOMUtils.getDOMDirectChilds(ele, Norm.getXMLTag())) {
            Norm n = new Norm(this);
            n.setFromDOM(e);
            if (getNorm( e.getAttribute("id") ) != null) {
                System.out.println("** OS warning: there already is a norm with ID "+n.getId()+" the former norm will be replaced by the latter!");
            }
            addNorm(n);
        }

        for (Element e: DOMUtils.getDOMDirectChilds(ele, "npl-norms")) {
            if (nplProgram == null)
                nplProgram = "";
            nplProgram += e.getTextContent();
        }
    }
}
