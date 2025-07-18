package moise.os.fs.robustness;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSyntax.LogicalFormula;

public class RaisingGoal extends ExceptionGoal {

    public RaisingGoal(String goal, LogicalFormula when) {
        super(goal, when);
    }

    public static String getXMLTag() {
        return "raise-goal";
    }
    
    @Override
    public Element getAsDOM(Document document) {
        Element ele = super.getAsDOM(document);
        document.renameNode(ele,null,"raise-goal");
        return ele;
    }

}
