package moise.os.fs.exceptions;

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
import jason.asSyntax.parser.TokenMgrError;
import moise.common.MoiseException;
import moise.os.fs.Goal;
import moise.os.fs.Scheme;
import moise.prolog.ToProlog;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

public class ExceptionSpecification extends moise.common.MoiseElement implements ToXML, ToProlog {
    
    private String id;
    
    private NotificationPolicy inPolicy;
    
    private List<Literal> exceptionArguments = new ArrayList<>();
    
    private HashMap<String, RaisingGoal> raisingGoals = new HashMap<>();
    private HashMap<String, HandlingGoal> handlingGoals = new HashMap<>();
    
    private Scheme sch;
    
    public ExceptionSpecification(String id, NotificationPolicy inPolicy, Scheme sch) {
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
    
    public List<Literal> getExceptionArguments() {
        return exceptionArguments;
    }
    
    public Collection<RaisingGoal> getRaisingGoals() {
        return raisingGoals.values();
    }
    
    public Collection<HandlingGoal> getHandlingGoals() {
        return handlingGoals.values();
    }

    public static String getXMLTag() {
        return "exception-specification";
    }
    
    public void setFromDOM(Element ele) throws MoiseException {
        
        setPropertiesFromDOM(ele);
        
        for(Element ea: DOMUtils.getDOMDirectChilds(ele, "exception-argument")) {
            Literal l = new LiteralImpl(ea.getAttribute("id"));
            int nArgs = Integer.parseInt(ea.getAttribute("arity"));
            for(int i = 0; i < nArgs; i++) {
                l.addTerm(new VarTerm("Arg"+i));
            }
            exceptionArguments.add(l);
        }
        
        for(Element tgEle: DOMUtils.getDOMDirectChilds(ele, "raising-goal")) {
            try {
                LogicalFormula whenFormula = ASSyntax.parseFormula("true");
                String when = tgEle.getAttribute("when");
                if(when != null && when != "") {
                    whenFormula = ASSyntax.parseFormula(when);
                }
                RaisingGoal tg = new RaisingGoal(tgEle.getAttribute("id"), whenFormula);
                tg.setFromDOM(tgEle, sch);
                raisingGoals.put(tg.getId(),tg);
                sch.addGoal(tg);
            }
            catch(ParseException e) {
                throw new MoiseException(e.getMessage());
            }  
        }
        
        for(Element cgEle: DOMUtils.getDOMDirectChilds(ele, "handling-goal")) {
            try {
                LogicalFormula whenFormula = ASSyntax.parseFormula("true");
                String when = cgEle.getAttribute("when");
                if(when != null && when != "") {
                    whenFormula = ASSyntax.parseFormula(when);
                }
                HandlingGoal cg = new HandlingGoal(cgEle.getAttribute("id"), whenFormula);
                cg.setFromDOM(cgEle, sch);
                handlingGoals.put(cg.getId(),cg);
                sch.addGoal(cg);
            }
            catch(ParseException e) {
                throw new MoiseException(e.getMessage());
            }  
        }
        
    }
    
    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("id", getId());
        if (getProperties().size() > 0) {
            ele.appendChild(getPropertiesAsDOM(document));
        }
        for(Literal arg : exceptionArguments) {
            Element argEle = (Element) document.createElement("exception-argument");
            argEle.setAttribute("id", arg.getFunctor());
            argEle.setAttribute("arity", String.valueOf(arg.getArity()));
            ele.appendChild(argEle);
        }
        for(RaisingGoal tg : raisingGoals.values()) {
            ele.appendChild(tg.getAsDOM(document));
        }
        for(HandlingGoal cg : handlingGoals.values()) {
            ele.appendChild(cg.getAsDOM(document));
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
