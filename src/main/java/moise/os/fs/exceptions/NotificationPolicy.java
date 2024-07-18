package moise.os.fs.exceptions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.parser.ParseException;
import moise.common.MoiseException;
import moise.os.fs.Goal;
import moise.os.fs.Scheme;
import moise.prolog.ToProlog;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

public class NotificationPolicy extends moise.common.MoiseElement implements ToXML, ToProlog {

    private String id;
    private Goal target;
    private LogicalFormula condition;
    private HashMap<String,ExceptionSpecification> exceptionSpecifications = new HashMap<>();
    private Scheme sch;
    
    public NotificationPolicy(String id, String target, LogicalFormula condition, Scheme sch) {
        super();
        this.id = id;
        this.target = sch.getGoal(target);
        this.condition = condition;
        this.sch = sch;
    }

    public String getId() {
        return id;
    }

    public Goal getTarget() {
        return target;
    }

    public LogicalFormula getCondition() {
        return condition;
    }

    public Collection<ExceptionSpecification> getExceptionSpecifications() {
        return exceptionSpecifications.values();
    }
    
    public void addExceptionSpecification(ExceptionSpecification ex) {
        exceptionSpecifications.put(ex.getId(), ex);
    }

    public void setFromDOM(Element ele) throws MoiseException {
        setPropertiesFromDOM(ele);
        for(Element exEle : DOMUtils.getDOMDirectChilds(ele, ExceptionSpecification.getXMLTag())) {
            ExceptionSpecification ex = new ExceptionSpecification(exEle.getAttribute("id"), this, sch);
            ex.setFromDOM(exEle);
            addExceptionSpecification(ex);
        }
    }

    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("id", getId());
        ele.setAttribute("target", target.getId());
        String condString = condition.toString();
        condString.replace("&", "&amp;");
        condString.replace("<", "&lt;");
        condString.replace(">", "&gt;");
        condString.replace("'", "&apos;");
        condString.replace("\"", "&quot;");
        ele.setAttribute("condition", condString);
        if (getProperties().size() > 0) {
            ele.appendChild(getPropertiesAsDOM(document));
        }
        for(ExceptionSpecification ex : exceptionSpecifications.values()) {
            ele.appendChild(ex.getAsDOM(document));
        }
        return ele;
    }

    public static String getXMLTag() {
        return "notification-policy";
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public String getAsProlog() {
        // TODO Auto-generated method stub
        return null;
    }

}
