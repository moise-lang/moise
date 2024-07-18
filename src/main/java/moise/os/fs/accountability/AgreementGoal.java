package moise.os.fs.accountability;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.parser.ParseException;
import moise.common.MoiseException;
import moise.os.fs.Goal;
import moise.os.fs.Scheme;

public abstract class AgreementGoal extends Goal {
    
    private LogicalFormula when;
    
    public AgreementGoal(String goal) {
        super(goal);
    }
    
    public LogicalFormula getWhenCondition() {
        return when;
    }

    @Override
    public void setFromDOM(Element ele, Scheme sch) throws MoiseException {
        super.setFromDOM(ele,sch);
        String cond = ele.getAttribute("when");
        try {
            if(cond != "") {
                when = ASSyntax.parseFormula(cond);
            }
            else {
                when = ASSyntax.parseFormula("true");
            }
        } catch (ParseException e) {
            throw new MoiseException("Inconsistent when condition in goal " + getId());
        }
    }
    
    @Override
    public Element getAsDOM(Document document) {
        Element ele = super.getAsDOM(document);
        ele.setAttribute("when",when.toString());
        return ele;
    }

}
