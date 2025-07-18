package moise.os.fs.robustness;

import java.util.Collection;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSyntax.LogicalFormula;
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
    private NotificationPolicyType type;
    private HashMap<String,Report> reports = new HashMap<>();
    private Scheme sch;
    
    public NotificationPolicy(String id, String target, LogicalFormula condition, NotificationPolicyType type, Scheme sch) {
        super();
        this.id = id;
        this.target = sch.getGoal(target);
        this.condition = condition;
        this.type = type;

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

    public NotificationPolicyType getType() {
        return type;
    }

    public Collection<Report> getReports() {
        return reports.values();
    }
    
    public void addReport(Report ex) {
        reports.put(ex.getId(), ex);
    }

    public void setFromDOM(Element ele) throws MoiseException {
        setPropertiesFromDOM(ele);
        for(Element repEle : DOMUtils.getDOMDirectChilds(ele, Report.getXMLTag())) {
            Report r = new Report(repEle.getAttribute("id"), this, sch);
            r.setFromDOM(repEle);
            addReport(r);
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
        ele.setAttribute("type", type.toString());
        if (getProperties().size() > 0) {
            ele.appendChild(getPropertiesAsDOM(document));
        }
        for(Report r : reports.values()) {
            ele.appendChild(r.getAsDOM(document));
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
