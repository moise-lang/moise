package moise.common;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import moise.oe.OEAgent;
import moise.xml.DOMUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Represents a basic Moise+ element which has an Id (a "player" Role, for example).
 *
 * Some elements may have an id composed by prefix.id
 *
 * @author Jomi Fred Hubner
 */
@SuppressWarnings("rawtypes")
public class MoiseElement implements Serializable, Identifiable, Comparable {
    
    private static final long serialVersionUID = 1L;

    public static boolean debug = false;

    private String  id = "unknow";
    private String  prefix = null;
    private OEAgent owner = null;
    private Date    creationDate = new Date();
    
    private Map<String, Object>   properties = null;
    private static int lastId = 0;
    
    public MoiseElement() {
        id = "autoId_"+(lastId++);
    }
    
    public MoiseElement(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    
    public void setId(String s) {
        if (s != null && s.length() == 0) {
            MoiseException e = new MoiseException("Id should not be empty!");
            e.printStackTrace();
        } else {
            id = s;
        }
    }
    
    public void setOwner(OEAgent ag) {
        owner = ag;
    }
    
    public OEAgent getOwner() {
        return owner;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setPrefix(String p) {
        prefix = p;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    /**
     * returns a full id : prefix + "." + id if there is a prefix,
     * only <id> otherwise.
     */
    public String getFullId() {
        if (prefix == null) {
            return getId();
        } else {
            return prefix+"."+getId();
        }
    }
    
    public void setProperty(String id, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put(id, value);
    }
    
    public Object getProperty(String id) {
        if (properties == null) {
            return null;
        }
        return properties.get(id);
    }
    
    public String getStrProperty(String id, String defaultReturn) {
        if (properties == null) {
            return defaultReturn;
        }
        String r = (String)properties.get(id);
        if (r == null)
            return defaultReturn;
        else
            return r;
    }
    
    public boolean getBoolProperty(String id) {
        return getBoolProperty(id, false);
    }
    
    public boolean getBoolProperty(String id, boolean defaultReturn) {
        if (properties != null) { 
            Object v = properties.get(id);
            if (v != null && v instanceof String) {
                String s = (String)v;
                return s.equals("true") || s.equals("yes");
            }
        }
        return defaultReturn;
    }
    
    public Map<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        return properties;
    }
    
    @Override
    public int hashCode() {
        String fid = getFullId();
        return (fid == null ? 0 : fid.hashCode());
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Identifiable) {
            Identifiable other = (Identifiable)o;
            if (id.equals( other.getId() )) {
                if (prefix == null && other.getPrefix() == null) {
                    return true;
                } else {
                    return prefix.equals( other.getPrefix());
                }
            }
        }
        return false;
    }

    public int compareTo(Object obj) {
        try {
            Identifiable other = (Identifiable)obj;
            return this.getFullId().compareTo(other.getFullId());
        } catch (Exception e) {}
        return 0;
    }
    
    /**
     * get the prefix part of a string with the format prefix.id, 
     * returns null if there is no prefix
     */
    public static String getPrefix(String s) {
        int posP = s.indexOf(".");
        if (posP > 0) {
            return s.substring(0,posP);
        } else {
            return null;
        }
    }
    
    /**
     * get the id part of a string with the format prefix.id, return "id" if there is no prefix
     */
    public static String getId(String s) {
        int posP = s.indexOf(".");
        if (posP > 0) {
            return s.substring(posP);
        } else {
            return s;
        }
    }
    
    public String toString() {
        return getFullId();
    }

    
    public Element getPropertiesAsDOM(Document document) {
        Element ele = (Element) document.createElement("properties");
        for (String id: getProperties().keySet()) {
            Element p = (Element) document.createElement("property");
            p.setAttribute("id", id);
            p.setAttribute("value", getProperty(id).toString());
            ele.appendChild(p);
        }
        return ele;
    }

    public String getPropertiesAsProlog() {
        StringBuilder s = new StringBuilder("properties([");
        String v = "";
        for (String id: getProperties().keySet()) {
            s.append(v+"property("+id+",\""+getProperty(id)+"\")");
        }
        s.append("])");
        return s.toString();
    }

    /* the parent node of "properties" tag is the argument */
    public void setPropertiesFromDOM(Element parent) {
        for (Element e: DOMUtils.getDOMDirectChilds(parent, "properties")) {
            for (Element p: DOMUtils.getDOMDirectChilds(e, "property")) {
                setProperty(p.getAttribute("id"), p.getAttribute("value"));
            }
        }
    }
}
