package moise.os.fs.robustness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import moise.common.MoiseException;
import moise.os.fs.Scheme;
import moise.prolog.ToProlog;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

public class Report extends moise.common.MoiseElement implements ToXML, ToProlog {
    
    private String id;
    
    private NotificationPolicy inPolicy;
    
    private List<Literal> arguments = new ArrayList<>();
    
    private HashMap<String, RaisingGoal>    raisingGoals    = new HashMap<>();
    private HashMap<String, HandlingGoal>   handlingGoals   = new HashMap<>();
    
    
    private HashMap<String, ContextGoal> contextGoals = new HashMap<>();
    private HashMap<String, RequestingGoal> requestingGoals = new HashMap<>();
    private HashMap<String, AccountingGoal> accountingGoals = new HashMap<>();
    private HashMap<String, TreatmentGoal>  treatmentGoals  = new HashMap<>();
    
    private Scheme sch;
    
    public Report(String id, NotificationPolicy inPolicy, Scheme sch) {
        super();
        this.id = id;
        this.inPolicy = inPolicy;
        this.sch = sch;
    }
    
    public String getId() {
        return id;
    }

    public NotificationPolicy getInPolicy() {
        return inPolicy;
    }
    
    public List<Literal> getArguments() {
        return arguments;
    }
    
    public Collection<RaisingGoal> getRaisingGoals() {
        return raisingGoals.values();
    }
    
    public Collection<HandlingGoal> getHandlingGoals() {
        return handlingGoals.values();
    }
    
    public Collection<ContextGoal> getContextGoals() {
        return contextGoals.values();
    }
    
    public Collection<RequestingGoal> getRequestingGoals() {
        return requestingGoals.values();
    }
    
    public Collection<AccountingGoal> getAccountingGoals() {
        return accountingGoals.values();
    }
    
    public Collection<TreatmentGoal> getTreatmentGoals() {
        return treatmentGoals.values();
    }

    public static String getXMLTag() {
        return "report";
    }
    
    public void setFromDOM(Element ele) throws MoiseException {
        
        setPropertiesFromDOM(ele);
        
        for(Element ea: DOMUtils.getDOMDirectChilds(ele, "argument")) {
            Literal l = new LiteralImpl(ea.getAttribute("id"));
            int nArgs = Integer.parseInt(ea.getAttribute("arity"));
            for(int i = 0; i < nArgs; i++) {
                l.addTerm(new VarTerm("Arg"+i));
            }
            arguments.add(l);
        }
        
        List<Element> raiseGoalElements = DOMUtils.getDOMDirectChilds(ele, RaisingGoal.getXMLTag());
        for(Element raisgEle : raiseGoalElements) {
            try {
                String when = raisgEle.getAttribute("when");
                RaisingGoal raisg = new RaisingGoal(raisgEle.getAttribute("id"), parseFormula(when));
                raisg.setFromDOM(raisgEle, sch);
                raisingGoals.put(raisg.getId(), raisg);
                sch.addGoal(raisg);
            }
            catch(ParseException e) {
                throw new MoiseException(e.getMessage());
            }  
        }
        
        List<Element> handleGoalElements = DOMUtils.getDOMDirectChilds(ele, HandlingGoal.getXMLTag());
        for(Element hgEle : handleGoalElements) {
            try {
                String when = hgEle.getAttribute("when");
                HandlingGoal hg = new HandlingGoal(hgEle.getAttribute("id"), parseFormula(when));
                hg.setFromDOM(hgEle, sch);
                handlingGoals.put(hg.getId(), hg);
                sch.addGoal(hg);
            }
            catch(ParseException e) {
                throw new MoiseException(e.getMessage());
            }  
        }
        
        List<Element> contextGoalElements = DOMUtils.getDOMDirectChilds(ele, ContextGoal.getXMLTag());
        for(Element cgEle : contextGoalElements) {
            try {
                String when = cgEle.getAttribute("when");
                ContextGoal cg = new ContextGoal(cgEle.getAttribute("id"), parseFormula(when));
                cg.setFromDOM(cgEle, sch);
                contextGoals.put(cg.getId(), cg);
                sch.addGoal(cg);
            }
            catch(ParseException e) {
                throw new MoiseException(e.getMessage());
            }  
        }
        
        List<Element> requestingGoalElements = DOMUtils.getDOMDirectChilds(ele, RequestingGoal.getXMLTag());
        for(Element reqgEle : requestingGoalElements) {
            try {
                String when = reqgEle.getAttribute("when");
                RequestingGoal reqg = new RequestingGoal(reqgEle.getAttribute("id"), parseFormula(when));
                reqg.setFromDOM(reqgEle, sch);
                requestingGoals.put(reqg.getId(), reqg);
                sch.addGoal(reqg);
            }
            catch(ParseException e) {
                throw new MoiseException(e.getMessage());
            }  
        }
       
        
        List<Element> accountingGoalElements = DOMUtils.getDOMDirectChilds(ele, AccountingGoal.getXMLTag());
        for(Element agEle : accountingGoalElements) {
            try {
                String when = agEle.getAttribute("when");
                AccountingGoal ag = new AccountingGoal(agEle.getAttribute("id"), parseFormula(when));
                ag.setFromDOM(agEle, sch);
                accountingGoals.put(ag.getId(), ag);
                sch.addGoal(ag);
            }
            catch(ParseException e) {
                throw new MoiseException(e.getMessage());
            }  
        }
        
        List<Element> treatmentGoalElements = DOMUtils.getDOMDirectChilds(ele, TreatmentGoal.getXMLTag());
        for(Element tgEle : treatmentGoalElements) {
            try {
                String when = tgEle.getAttribute("when");
                TreatmentGoal tg = new TreatmentGoal(tgEle.getAttribute("id"), parseFormula(when));
                tg.setFromDOM(tgEle, sch);
                treatmentGoals.put(tg.getId(), tg);
                sch.addGoal(tg);
            }
            catch(ParseException e) {
                throw new MoiseException(e.getMessage());
            }  
        }
          
    }
    
    private static LogicalFormula parseFormula(String formulaString) throws ParseException {
        if(formulaString != null && formulaString != "") {
            return ASSyntax.parseFormula(formulaString);
        }
        else return ASSyntax.parseFormula("true");
    }
    
    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("id", getId());
        if (getProperties().size() > 0) {
            ele.appendChild(getPropertiesAsDOM(document));
        }
        for(Literal arg : arguments) {
            Element argEle = (Element) document.createElement("argument");
            argEle.setAttribute("id", arg.getFunctor());
            argEle.setAttribute("arity", String.valueOf(arg.getArity()));
            ele.appendChild(argEle);
        }
        for(RaisingGoal raisg : raisingGoals.values()) {
            ele.appendChild(raisg.getAsDOM(document));
        }
        for(HandlingGoal hg : handlingGoals.values()) {
            ele.appendChild(hg.getAsDOM(document));
        }
        for(ContextGoal cg : contextGoals.values()) {
            ele.appendChild(cg.getAsDOM(document));
        }
        for(RequestingGoal reqg : requestingGoals.values()) {
            ele.appendChild(reqg.getAsDOM(document));
        }
        for(AccountingGoal ag : accountingGoals.values()) {
            ele.appendChild(ag.getAsDOM(document));
        }
        return ele;
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
