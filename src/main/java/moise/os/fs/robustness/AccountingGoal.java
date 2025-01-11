package moise.os.fs.robustness;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSyntax.LogicalFormula;

public class AccountingGoal extends ExceptionGoal {
    
    public AccountingGoal(String goal, LogicalFormula when) {
        super(goal, when);
    }
    
    public static String getXMLTag() {
        return "accounting-goal";
    }
    
    @Override
    public Element getAsDOM(Document document) {
        Element ele = super.getAsDOM(document);
        document.renameNode(ele,null,"accounting-goal");
        return ele;
    }

}
