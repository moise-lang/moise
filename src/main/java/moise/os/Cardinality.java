package moise.os;

import java.io.Serializable;

import moise.common.MoiseException;
import moise.common.MoiseXMLParserException;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a cardinality of the Moise+ model (maximum and minumum values).
 *
 * @author Jomi Fred Hubner
 */
public class Cardinality implements Serializable, ToXML {

    private static final long serialVersionUID = 1L;

    public static final Cardinality defaultValue = new Cardinality();
    
    protected int max = Integer.MAX_VALUE;
    protected int min = 0;
    
    public Cardinality() {
    }
    
    /** Creates new Cardinality */
    public Cardinality(int minimum, int maximum) {
        max = maximum;
        min = minimum;
    }
    
    public int getMin() {
        return min;
    }
    
    public int getMax() {
        return max;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;

        if (o instanceof Cardinality) {
            Cardinality other = (Cardinality)o;
            return min == other.getMin() && max == other.getMax();
        }
        return false;
    }
    
    public String toString() {
        String sMin = "0";
        if (min != defaultValue.getMin()) {
            sMin = min+"";
        }
        String sMax = "*";
        if (max != defaultValue.getMax()) {
            sMax = max+"";
        }
        return "("+sMin+","+sMax+")"; 
    }
    
    /** returns cardinality in format Min..Max */
    public String toStringFormat2() {
        String sMin = "0";
        if (min != defaultValue.getMin()) {
            sMin = min+"";
        }
        String sMax = "*";
        if (max != defaultValue.getMax()) {
            sMax = max+"";
        }
        return sMin+".."+sMax; 
    }

    public String getXMLTag() {
        return "cardinality";
    }
    
    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        if (getMin() != defaultValue.getMin()) {
            ele.setAttribute("min", getMin()+"");
        }
        if (getMax() != defaultValue.getMax()) {
            ele.setAttribute("max", getMax()+"");
        }
        return ele;
    }
    
    public void setFromDOM(Element ele) throws MoiseException {
        try {
            String sMin = ele.getAttribute("min");
            if (sMin != null && sMin.length() > 0) {
                min = Integer.parseInt(sMin);
            }
        } catch (Exception ex) {
            throw new MoiseXMLParserException("the value ("+ele.getAttribute("min")+") for min is not numeric!");
        }
        
        try {
            String sMax = ele.getAttribute("max");
            if (sMax != null && sMax.length() > 0) {
                max = Integer.parseInt(sMax);
            }
            if (max < min) {
                throw new MoiseXMLParserException("the max value ("+max+") is less than min value ("+min+")!");                
            }
        } catch (Exception ex) {
            throw new MoiseXMLParserException("the value ("+ele.getAttribute("max")+") for max is not numeric!");
        }
    }    
}
