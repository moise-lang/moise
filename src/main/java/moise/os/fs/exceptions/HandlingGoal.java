package moise.os.fs.exceptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSyntax.LogicalFormula;

public class HandlingGoal extends ExceptionGoal {

    public HandlingGoal(String goal, LogicalFormula when) {
        super(goal, when);
    }

    public static String getXMLTag() {
        return "handling-goal";
    }
    
    @Override
    public Element getAsDOM(Document document) {
        Element ele = super.getAsDOM(document);
        document.renameNode(ele,null,"handling-goal");
        return ele;
    }

}
