package moise.os.fs.accountability;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TreatmentGoal extends AgreementGoal {
    
    public TreatmentGoal(String goal) {
        super(goal);
    }
    
    @Override
    public Element getAsDOM(Document document) {
        Element ele = super.getAsDOM(document);
        document.renameNode(ele,null,"treatment-goal");
        return ele;
    }
    
    public static String getXMLTag() {
        return "treatment-goal";
    }
    
}