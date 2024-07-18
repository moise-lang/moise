package moise.os.fs.accountability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSyntax.LogicalFormula;
import moise.common.MoiseException;
import moise.os.fs.Goal;
import moise.os.fs.Scheme;
import moise.prolog.ToProlog;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

public class AccountabilityAgreement extends moise.common.MoiseElement implements ToXML, ToProlog {

    private String id;
    

    private Scheme sch;
    
    private Goal target;
    private LogicalFormula condition;
    private HashMap<String,AccountTemplate> accountTemplates = new HashMap<>();
    private List<ContextGoal> contextGoals = new ArrayList<ContextGoal>();
    
    public AccountabilityAgreement(String id, String target, LogicalFormula condition, Scheme sch) {
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

    public Collection<AccountTemplate> getAccountTemplates() {
        return accountTemplates.values();
    }
    
    public void addAccountTemplate(AccountTemplate at) {
        accountTemplates.put(at.getId(), at);
    }

    public List<ContextGoal> getContextGoals() {
        return contextGoals;
    }
    
    public void setFromDOM(Element ele) throws MoiseException {
        setPropertiesFromDOM(ele);
        for(Element contextGoalEle : DOMUtils.getDOMDirectChilds(ele, ContextGoal.getXMLTag())) {
            ContextGoal cg = new ContextGoal(contextGoalEle.getAttribute("id"));
            cg.setFromDOM(contextGoalEle, sch);
            sch.addGoal(cg);
            contextGoals.add(cg);
        }
        for(Element atEle : DOMUtils.getDOMDirectChilds(ele, AccountTemplate.getXMLTag())) {
            AccountTemplate at = new AccountTemplate(atEle.getAttribute("id"), this, sch);
            at.setFromDOM(atEle);
            addAccountTemplate(at);
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
        for(ContextGoal cg : contextGoals) {
            ele.appendChild(cg.getAsDOM(document));
        }
        for(AccountTemplate at : accountTemplates.values()) {
            ele.appendChild(at.getAsDOM(document));
        }
        return ele;
    }

    public static String getXMLTag() {
        return "accountability-agreement";
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
