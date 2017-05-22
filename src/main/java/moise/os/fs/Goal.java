package moise.os.fs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.os.fs.Plan.PlanOpType;
import moise.prolog.ToProlog;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 Represents a Goal (in the specification).

 @composed - plan-to-achieve - Plan
 @navassoc - type - GoalType
 
 @author Jomi Fred Hubner
*/
public class Goal extends MoiseElement implements ToXML, ToProlog {
    
    private static final long serialVersionUID = 1L;

    public enum GoalType { performance, achievement, maintenance };
    
    protected Plan     plan = null;   // the plan to achieve this goal (in case the goal is the head of a plan)
    protected Plan     inPlan = null; // the plan where this goal belongs
    protected Scheme   sch = null;    // the scheme of this goal
    protected String   desc = null;
    protected GoalType type = GoalType.performance;
    protected int      minAgToSat = -1; // the minimum number of agents to satisfy the goal (-1 means all committed)
    protected Map<String,Object> args = null; // arguments and their default values
    protected String   ttf = ""; // time to fulfill
    protected List<Goal> dependencies = null; // explicit goals this goal depend on to be enabled
    protected String   location = "";
        
    public Goal(String goal) {
        setId(goal);
    }
    
    public boolean hasArguments() {
        return args != null && !args.isEmpty();
    }
    
    public void setScheme(Scheme sch) {
        this.sch = sch;
    }
    public Scheme getScheme() {
        return sch;
    }
    
    public void setInPlan(Plan p) {
        inPlan = p;
    }
    public Plan getInPlan() {
        return inPlan;
    }
    
    /**
     * returns a map of the goal's arguments (key is the argument, value is the default value)
     */
    public Map<String,Object> getArguments() {
        return args;
    }
    
    public void setPlan(Plan p) {
        plan = p;
    }
     
    /** gets the plan to achieve this goal (in case the goal is the head of a plan) */
    public Plan getPlan() {
        return plan;
    }

    public boolean hasPlan() {
        return plan != null;
    }
    
    public void setDescription(String s) {
        if (s != null && s.trim().length() > 0)
            desc = s.trim();
    }
    
    
    public String getDescription() {
        return desc;
    }
    
    public void addDependence(Goal g) {
        if (dependencies == null)
            dependencies = new ArrayList<Goal>();
        dependencies.add(g);
    }
    public boolean hasDependence() {
        return dependencies != null && !dependencies.isEmpty();        
    }
    public List<Goal> getDependencies() {
        return dependencies;
    }

    public List<Goal> getPreConditionGoals() {
        List<Goal> r = new ArrayList<Goal>();
        
        if (hasDependence())  // add explicit dependencies
            r.addAll(getDependencies());
        
        //if (sch.getRoot().getId().equals(this.getId())) { // this goal is the SCH root
        //    return true;
        //}
        
        // there is plan (sequence) that defines if the goal is permitted
        // (choice or parallel does not defines permission)
        if (plan != null) {
            if (plan.getOp() == PlanOpType.sequence) {
                // the last goal is the condition
                r.add( plan.getSubGoals().get( plan.getSubGoals().size()-1 ));
            } else if (plan.getOp() == PlanOpType.parallel) {
                // all goals are condition
                r.addAll( plan.getSubGoals() );
            } else if (plan.getOp() == PlanOpType.choice) {
                // all goals are condition
                r.addAll( plan.getSubGoals() );
            } 
        } else if (inPlan != null) {
            Goal tg = this;
            while (tg != null) {
                Plan tplan = tg.getInPlan();
                if (tplan != null) {
                    if (tplan.getOp() == PlanOpType.sequence) {
                        Goal previous = tplan.getPreviousSubGoals(tg.getId());
                        if (previous != null) {
                            // the previous goal is the pre-condition
                            r.add(previous);
                            break;
                        }
                    }
                    tg = tplan.getTargetGoal();
                } else {
                    break; // quit loop
                }
            }
        }
        return r;
    }

    /** sets the minimum number of committed agents that should satisfy the goal for the goal to be considered globally satisfied */
    public void setMinAgToSatisfy(int n) {
        minAgToSat = n;
    }

    /** gets the minimum number of committed agents that should satisfy the goal for the goal to be considered globally satisfied */
    public int getMinAgToSatisfy() {
        return minAgToSat;
    }

    
    public GoalType getType() {
        return type;
    }
    public void setType(GoalType t) {
        type = t;
        if (t == GoalType.achievement) {
            setMinAgToSatisfy(1);
        }
    }
    
    public String getTTF() {
        return ttf;
    }
    public void setTTF(String ttf) {
        this.ttf = ttf;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String l) {
        location = l;
    }
    
    
    public static String getXMLTag() {
        return "goal";
    }
    
    public boolean isRoot() {
        return getInPlan() == null;
    }
    public int getDepth() {
        if (isRoot())
            return 0;
        else
            return getInPlan().getTargetGoal().getDepth()+1;
    }
    
    
    /** returns a string representing the goal in Prolog syntax, format:
     *     goal(id, type, description, #ags to satisfy,time to fulfill, list of arguments, plan)[location(L)]
     */ 
    public String getAsProlog() {
        StringBuilder s = new StringBuilder("goal("+getId()+","+type);

        s.append(",\"");
        if (getDescription() != null)
            s.append(getDescription());
        s.append("\",");
        
        // agents to satisfy
        if (getMinAgToSatisfy() != -1)
            s.append(getMinAgToSatisfy());
        else
            s.append("all");
        
        // ttf
        if (ttf == null || ttf.length() == 0)
            s.append(",\"infinity");
        else
            s.append(",\""+ttf);
        
        // arguments
        s.append("\",[");
        if (hasArguments()) {
            String v = "";
            for (String id: getArguments().keySet()) {
                s.append(v+id);
                v=",";
            }
        }
        s.append("],");

        if (getPlan() == null)
            s.append("noplan");
        else
            s.append(getPlan().getAsProlog());

        s.append(")");
        
        // use annotation for location
        /*if (location != null && location.length() > 0) {
        	s.append("[location(\""+location+"\")]");
        }*/
        
        return s.toString();
    }
    
    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("id", getId());
        String min = "all";
        if (getMinAgToSatisfy() != -1) {
            min = String.valueOf(getMinAgToSatisfy());
        }
        ele.setAttribute("type", type.toString());
        ele.setAttribute("min", min);
        if (getDescription() != null) {
            ele.setAttribute("ds", getDescription());
        }
        if (getProperties().size() > 0) {
            ele.appendChild(getPropertiesAsDOM(document));
        }
        if (hasArguments()) {
            for (String id: getArguments().keySet()) {
                Element ea = (Element) document.createElement("argument");
                ea.setAttribute("id", id);
                String vl = args.get(id).toString();
                if (vl.length() > 0) {
                    ea.setAttribute("value", vl.toString());                    
                }
                ele.appendChild(ea);
            }
        }
        if (hasDependence()) {
            for (Goal dg: getDependencies()) {
                Element ea = (Element) document.createElement("depends-on");
                ea.setAttribute("goal", dg.getId());
                ele.appendChild(ea);                
            }
        }
        ele.setAttribute("ttf", ttf);
        ele.setAttribute("location", location);
        if (getPlan() != null) {
            ele.appendChild(getPlan().getAsDOM(document));
        }
        return ele;
    }
    
    public void setFromDOM(Element ele, Scheme sch) throws MoiseException {
        setPropertiesFromDOM(ele);
        setScheme(sch);

        if (ele.getAttribute("type").length() > 0) {
            setType(GoalType.valueOf(ele.getAttribute("type")));
        }
        if (ele.getAttribute("min").length() > 0) {
            if (getType() == GoalType.achievement) {
                System.out.println("Achievement goal "+getId()+" should not have the min attribute defined!");
            }
            setMinAgToSatisfy(Integer.parseInt(ele.getAttribute("min")));
        }
        if (ele.getAttribute("ttf").length() > 0) {
            setTTF(ele.getAttribute("ttf"));
        }
        if (ele.getAttribute("location").length() > 0) {
            setLocation(ele.getAttribute("location"));
        }
        setDescription(ele.getAttribute("ds"));

        // arguments
        for (Element ea: DOMUtils.getDOMDirectChilds(ele, "argument")) {
            Object value = "";
            if (ea.hasAttribute("value")) {
                value = ea.getAttribute("value");
            }
            if (args == null) {
                args = new LinkedHashMap<String,Object>(); // use linked to preserve order
            }
            args.put(ea.getAttribute("id"), value);
        }
        
        // dependencies
        for (Element ea: DOMUtils.getDOMDirectChilds(ele, "depends-on")) {
            if (ea.hasAttribute("goal")) {
                String goalId = ea.getAttribute("goal");
                Goal dog = sch.getGoal(goalId);
                if (dog != null)
                    addDependence(dog);
                else
                    System.out.println("The goal "+goalId+" was not declared in the scheme and thus can not be used as a depends-on argument for "+this);
            }
        }

        // the plan of this goal
        Element ep = DOMUtils.getDOMDirectChild(ele, Plan.getXMLTag());
        if (ep != null) {
            Plan plan = new Plan(sch);
            plan.setFromDOM(ep, this);
            sch.addPlan(plan);
        }        
    }
}
