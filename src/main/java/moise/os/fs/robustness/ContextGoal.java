package moise.os.fs.robustness;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSyntax.LogicalFormula;

public class ContextGoal extends ExceptionGoal {
    
    public ContextGoal(String goal, LogicalFormula when) {
        super(goal, when);
    }
    
    public static String getXMLTag() {
        return "context-goal";
    }
        
    @Override
    public Element getAsDOM(Document document) {
        Element ele = super.getAsDOM(document);
        document.renameNode(ele,null,"context-goal");
        return ele;
    }
    
}
