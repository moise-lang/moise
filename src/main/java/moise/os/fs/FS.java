package moise.os.fs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.os.OS;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 Represents a Functional Specification.

 @composed - schemes * Scheme

 @author Jomi Fred Hubner
*/
public class FS extends MoiseElement implements ToXML {

    private static final long serialVersionUID = 1L;

    protected Map<String,Scheme> schs = new HashMap<String,Scheme>();
    protected OS                 os   = null;

    /** Creates new SS */
    public FS(OS os) {
        super();
        this.os = os;
    }

    public void addScheme(Scheme s) {
        schs.put(s.getId(),s);
    }

    public void addScheme(Collection<Scheme> cs) {
        for (Scheme sch: cs) {
            addScheme( sch);
        }
    }

    public Scheme findScheme(String id) {
        return schs.get(id);
    }

    public Collection<Scheme> getSchemes() {
        return schs.values();
    }

    public OS getOS() {
        return os;
    }

    /**
     * find a mission in all schemes
     */
    public Mission findMission(String id) {
        for (Scheme sch: schs.values()) {
            Mission m = sch.getMission(id);
            if (m != null) {
                return m;
            }
        }
        return null;
    }

    public Collection<Mission> getAllMissions() {
        List<Mission> all = new ArrayList<Mission>();
        for (Scheme sch: schs.values()) {
            all.addAll( sch.getMissions() );
        }
        return all;
    }

    /**
     * find a goal in all schemes
     */
    public Goal findGoal(String id) {
        for (Scheme sch: schs.values()) {
            Goal g = sch.getGoal(id);
            if (g != null) {
                return g;
            }
        }
        return null;
    }

    public static String getXMLTag() {
        return "functional-specification";
    }

    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());

        if (getProperties().size() > 0) {
            ele.appendChild( getPropertiesAsDOM(document));
        }

        // schemes
        for (Scheme sspec: getSchemes()) {
            ele.appendChild(sspec.getAsDOM(document));
        }

        return ele;
    }


    public void setFromDOM(Element ele) throws MoiseException {
        setPropertiesFromDOM(ele);

        // schemes
        for (Element sEle: DOMUtils.getDOMDirectChilds(ele, Scheme.getXMLTag())) {
            Scheme sspec = new Scheme(sEle.getAttribute("id"), this);
            addScheme(sspec);
            sspec.setFromDOM(sEle);
        }
    }

}


