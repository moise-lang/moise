 package moise.os.fs.accountability;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.VarTerm;
import moise.common.MoiseException;
import moise.os.fs.Scheme;
import moise.prolog.ToProlog;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

public class AccountTemplate extends moise.common.MoiseElement implements ToXML, ToProlog {
    
    private String id;
    
    private AccountabilityAgreement inAgreement;
    
    private List<Literal> accountArguments = new ArrayList<>();
    
    private List<RequestingGoal> requestingGoals = new ArrayList<>();
    private AccountingGoal accountingGoal;
    private List<TreatmentGoal> treatmentGoals = new ArrayList<>();
    
    Scheme sch;
    
    public AccountTemplate(String id, AccountabilityAgreement inAgreement, Scheme sch) {
        super();
        this.id = id;
        this.inAgreement = inAgreement;
        this.sch = sch;
    }
    
    public String getId() {
        return id;
    }

    public AccountabilityAgreement getInAgreement() {
        return inAgreement;
    }    
    
    public AccountingGoal getAccountingGoal() {
        return accountingGoal;
    }

    public List<Literal> getAccountArguments() {
        return accountArguments;
    }

    public List<RequestingGoal> getRequestingGoals() {
        return requestingGoals;
    }

    public List<TreatmentGoal> getTreatmentGoals() {
        return treatmentGoals;
    }

    public static String getXMLTag() {
        return "account-template";
    }
    
    public void setFromDOM(Element ele) throws MoiseException {
        
        setPropertiesFromDOM(ele);
        
        for(Element ea: DOMUtils.getDOMDirectChilds(ele, "account-argument")) {
            Literal l = new LiteralImpl(ea.getAttribute("id"));
            int nArgs = Integer.parseInt(ea.getAttribute("arity"));
            for(int i = 0; i < nArgs; i++) {
                l.addTerm(new VarTerm("Arg"+i));
            }
            accountArguments.add(l);
        }
        
        for(Element gEle: DOMUtils.getDOMDirectChilds(ele, RequestingGoal.getXMLTag())) {
            RequestingGoal rg = new RequestingGoal(gEle.getAttribute("id"));
            rg.setFromDOM(gEle, sch);
            sch.addGoal(rg);
            requestingGoals.add(rg);
        } 
        
        for(Element gEle: DOMUtils.getDOMDirectChilds(ele, TreatmentGoal.getXMLTag())) {
            TreatmentGoal tg = new TreatmentGoal(gEle.getAttribute("id"));
            tg.setFromDOM(gEle, sch);
            sch.addGoal(tg);
            treatmentGoals.add(tg);
        }
        
        for(Element gEle: DOMUtils.getDOMDirectChilds(ele, AccountingGoal.getXMLTag())) {
            if(accountingGoal != null) {
                throw new MoiseException("Cannot have multiple accounting goals in accountability agreement " + inAgreement.getId());
            }
            accountingGoal = new AccountingGoal(gEle.getAttribute("id"));
            accountingGoal.setFromDOM(gEle, sch);
            sch.addGoal(accountingGoal);
        } 
        if(accountingGoal == null) {
            throw new MoiseException("Accounting goal missing in account template in agreement " + inAgreement.getId());
        }
        
    }
    
    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("id", getId());
        if (getProperties().size() > 0) {
            ele.appendChild(getPropertiesAsDOM(document));
        }   
        for(Literal arg : accountArguments) {
            Element argEle = (Element) document.createElement("account-argument");
            argEle.setAttribute("id", arg.getFunctor());
            argEle.setAttribute("arity", String.valueOf(arg.getArity()));
            ele.appendChild(argEle);
        }
        for(RequestingGoal rg : requestingGoals) {
            ele.appendChild(rg.getAsDOM(document));
        }
        ele.appendChild(accountingGoal.getAsDOM(document));
        for(TreatmentGoal tg : treatmentGoals) {
            ele.appendChild(tg.getAsDOM(document));
        }
        return ele;
    }
   
    @Override
    public String getAsProlog() {
        // TODO Auto-generated method stub
        return null;
    }

}
