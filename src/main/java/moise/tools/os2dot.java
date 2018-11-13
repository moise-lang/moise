package moise.tools;

import java.io.File;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;
import moise.os.Cardinality;
import moise.os.OS;
import moise.os.fs.FS;
import moise.os.fs.Goal;
import moise.os.fs.Mission;
import moise.os.fs.Plan.PlanOpType;
import moise.os.fs.Scheme;
import moise.os.ns.NS;
import moise.os.ns.NS.OpTypes;
import moise.os.ns.Norm;
import moise.os.ss.Compatibility;
import moise.os.ss.Group;
import moise.os.ss.Link;
import moise.os.ss.Role;
import moise.os.ss.RoleRel.RoleRelScope;
import moise.os.ss.SS;
import ora4mas.nopl.SchemeBoard;
import ora4mas.nopl.oe.Player;



/**
 * Convert OS/OE into DOT code (to plot a graph)
 *
 * @author Jomi
 */
public class os2dot {

    public boolean showSS = true, showFS = true, showNS = true;
    public boolean showLinks      = true;
    public boolean showMissions   = true;
    public boolean showConditions = false;
    protected File    osFile;

    public os2dot() {
    }

    public String transform(OS os)  throws Exception {
        StringWriter so = new StringWriter();

        so.append("digraph "+os.getId()+" {\n");
        if (!showSS && !showFS && showNS)
            so.append("    rankdir=LR;\n");
        else
            so.append("    rankdir=BT;\n");
        so.append("    compound=true;\n\n");

        if (showSS) so.append(transform(os.getSS(), null));
        if (showFS) so.append(transform(os.getFS()));
        if (showNS) so.append(transform(os.getNS()));

        so.append("}\n");

        //System.out.println(so);

        return so.toString();
    }

    public String transform(SS ss, ora4mas.nopl.oe.Group grInstance) {
        StringWriter so = new StringWriter();

        so.append("\n    subgraph cluster_SS { \n"); // label=\"Structure\" labelloc=t labeljust=r fontname=\"Italic\" \n");
        // Roles
        so.append(transformRolesDef(ss));

        // groups
        so.append( transform(ss.getRootGrSpec(), grInstance) );
        so.append("    }\n");

        return so.toString();
    }

    public String transformRolesDef(SS ss) {
        StringWriter so = new StringWriter();
        so.append("        // role hierarchy\n");
        for (Role r: ss.getRolesDef()) {
            if (!r.getId().equals("soc"))
                so.append(transform(r));
            for (Role e: r.getSuperRoles()) {
                if (!e.getId().equals("soc"))
                    so.append("        "+r.getId()+" -> "+e.getId()+" [arrowhead=onormal,arrowsize=1.5];\n");
            }
        }
        return so.toString();
    }

    String transform(Role r) {
        String font = ",fontname=\"Helvetic\"";
        if (r.isAbstract()) {
            font=",fontname=\"Italic\""; //,style=filled,fillcolor=wheat
        }
        return "        "+r.getId()+" [shape=box,style=rounded"+font+"];\n";
    }

    public String transform(Group g, ora4mas.nopl.oe.Group gInstance) {
        StringWriter so = new StringWriter();

        so.append("\n        // group "+g.getId()+"\n");
        String id    = g.getId();
        String label = g.getId();
        if (gInstance != null) {
            id    = gInstance.getId();
            label = gInstance.getId() + ": " + label;
        }
        so.append("        "+id+" [label=\""+label+"\",shape=tab, fontname=\"Courier-Bold\",style=filled];\n");
        //so.append("     "+g.getId()+" [shape=box, fontname=\"Courier-Bold\",style=filled,fillcolor=lightyellow];\n");
        for (Role r: g.getRoles().getAll()) {
            String card = g.getRoleCardinality(r).toStringFormat2();
            so.append("        "+id+" -> "+r.getId()+"  [arrowtail=odiamond, arrowhead=none, dir=both, label=\""+card+"\",fontname=\"Times\",arrowsize=1.5];\n");
        }
        for (Group sg: g.getSubGroups()) {
            String card = g.getSubGroupCardinality(sg).toStringFormat2();
            if (gInstance == null) {
                so.append("        "+id+" -> "+sg.getId()+"  [arrowtail=odiamond, arrowhead=none, dir=both, label=\""+card+"\",fontname=\"Times\",arrowsize=1.5];\n");
                so.append(transform(sg, null));
            } else {
                for (ora4mas.nopl.oe.Group sgi: gInstance.getSubgroups()) {
                    if (sgi.getGrType().equals(sg.getId())) {
                        so.append("        "+id+" -> "+sgi.getId()+"  [arrowtail=odiamond, arrowhead=none, dir=both, label=\""+card+"\",fontname=\"Times\",arrowsize=1.5];\n");
                        so.append(transform(sg, sgi));
                    }
                }
            }
        }

        if (showLinks) {
            for (Link l: g.getLinks()) {
                String type = "normal";
                if (l.getTypeStr().equals("communication"))
                    type = "dot";
                else if (l.getTypeStr().equals("acquaintance"))
                    type = "vee";

                String dir = "";
                if (l.isBiDir())
                    dir += ",arrowtail="+type;
                String shape = "";
                if (l.getScope() == RoleRelScope.IntraGroup)
                    shape = ",style=dotted";
                so.append("        "+l.getSource()+" -> "+l.getTarget()+" [arrowhead="+type+dir+shape+"];\n");
            }
            for (Compatibility c: g.getCompatibilities()) {
                String dir = "arrowhead=diamond";
                if (c.isBiDir())
                    dir += ",arrowtail=diamond";
                String shape = "";
                if (c.getScope() == RoleRelScope.IntraGroup)
                    shape = ",style=dotted";
                so.append("        "+c.getSource()+" -> "+c.getTarget()+"  ["+dir+shape+"];\n");
            }
        }

        if (gInstance != null) {
            for (Player p: gInstance.getPlayers()) {
                so.append("        "+p.getAg()+ " [URL=\"/agents/"+p.getAg()+"/all\"];\n"); // [shape=plaintext]
                so.append("        "+p.getAg()+" -> "+p.getTarget()+" [arrowsize=0.5];\n");
                //so.append("        "+p.getAg()+" -> "+p.getTarget()+" [label=\""+id+"\",arrowsize=0.5];\n");
            }

            for (String s: gInstance.getSchemesResponsibleFor()) {
                so.append("        "+s+ "[shape=hexagon, style=filled, fontname=\"Courier\", URL=\"/scheme/"+s+"\"];\n");
                so.append("        "+id+" -> "+s+" [label=\"responsible\nfor\",labelfontsize=8,fontname=\"Italic\",arrowhead=open];\n");

            }
        }

        return so.toString();
    }

    public String transform(FS fs) {
        StringWriter so = new StringWriter();
        // schemes
        for (Scheme s: fs.getSchemes()) {
            //so.append("\n    subgraph cluster_"+s.getId()+" {label=\""+s.getId()+"\" labelloc=t labeljust=r fontname=\"Italic\" \n");
            so.append("        // goals\n");
            so.append(transform(s.getRoot(), 0, null));

            if (showMissions) {
                so.append("\n        // missions\n");
                for (Mission m: s.getMissions()) {
                    so.append(transform(m,s));
                    for (Goal g: m.getGoals()) {
                        so.append("        "+m.getId()+" -> "+g.getId()+" [arrowsize=0.5];\n");
                    }
                }
            }
            //so.append("    }\n");
        }

        return so.toString();
    }

    String transform(Mission m) {
        return "        "+m.getId()+" [fontname=\"Helvetic\", shape=diamond, style=rounded];\n";
    }

    public static String transform(Mission m, Scheme spec) {
        String card = "";
        if (! spec.getMissionCardinality(m).equals(Cardinality.defaultValue)) {
            card = "\n("+spec.getMissionCardinality(m).toStringFormat2()+")";
        }
        return "        "+m.getId()+" [label=\""+m.getId()+card+"\", fontname=\"Helvetic\", shape=plaintext,fontsize=10];\n";
    }


    public static String transform(Goal g, int pos, SchemeBoard sch) {
        StringBuilder so = new StringBuilder();
        String color = "black";
        if (sch != null) {
            if (sch.getSchState().isSatisfied(g)) {
                color = "blue";
            } else {
                Term tSch = ASSyntax.createString(sch.getSchState().getId());
                Atom aGoal  = new Atom(g.getId());
                if (sch.getNormativeEngine().holds(ASSyntax.createLiteral("well_formed", tSch)) && sch.getNormativeEngine().holds(ASSyntax.createLiteral("enabled", tSch, aGoal))) {
                    color = "green4";
                }
            }
        }
        String label = g.getId();
        if (pos > 0)
            label = pos+":"+label;
        String shape = "plaintext";
        String peri = "0";
        if (g.hasPlan()) {
            if (g.getPlan().getOp() == PlanOpType.choice) {
                shape = "underline";
                peri  = "1";
            } else if (g.getPlan().getOp() == PlanOpType.parallel) {
                shape = "underline";
                peri = "2";
            }
        }
        so.append("        "+g.getId()+" [label=\""+label+"\", shape="+shape+",peripheries="+peri+",fontname=\"Helvetic\",fontcolor="+color+"]; \n");
        if (g.hasPlan()) {
            String type=",arrowhead=none";

            Goal previous = null;
            int ppos = 0;
            if (g.getPlan().getOp() == PlanOpType.sequence)
                ppos = 1;
            for (Goal sg: g.getPlan().getSubGoals()) {
                so.append(transform(sg,ppos,sch));
                so.append("        "+sg.getId()+" -> "+g.getId()+" [samehead=true"+type+"];\n");
                if (ppos > 0) {
                    ppos++;
                    if (previous != null)
                        so.append("        "+previous.getId()+" -> "+sg.getId()+" [style=dotted, constraint=false, arrowhead=empty,arrowsize=0.5,color=lightgrey];\n");
                    previous = sg;
                }
            }
        }

        return so.toString();
    }

    /*
    String transform(Goal g) {
        StringBuilder so = new StringBuilder();
        so.append("        "+g.getId()+" [shape=plaintext,fontname=\"Helvetic\"]; \n");
        if (g.hasPlan()) {
            String type=",arrowhead=none";
            if (g.getPlan().getOp() == PlanOpType.parallel)
                type=",arrowhead=tee";
            else if (g.getPlan().getOp() == PlanOpType.choice)
                type=",arrowhead=vee";

            for (Goal sg: g.getPlan().getSubGoals()) {
                so.append("        "+sg.getId()+" -> "+g.getId()+" [samehead=true"+type+"];\n");
                so.append(transform(sg));
            }
        }

        return so.toString();
    }
    */

    public String transform(NS ns) {
        StringWriter so = new StringWriter();
        so.append("\n\n    // NS\n");
        Set<String> done = new HashSet<>();
        for (Norm n: ns.getNorms()) {
            String e = n.getRole().toString()+n.getMission();
            if (!done.contains(e) || showConditions) {
                done.add(e);

                String s = "bold";
                if (n.getType() == OpTypes.permission)
                    s = "filled";
                String cond = "";
                if (showConditions) {
                    cond = "plays role";
                    if (!n.getCondition().equals("true"))
                        cond = n.getCondition();
                    if (n.getTimeConstraint() != null)
                        cond += "@"+n.getTimeConstraint();
                }

                so.append( transform(n.getRole()));
                so.append( transform(n.getMission()));

                so.append("        "+n.getRole()+" -> "+n.getMission().getId()+" [arrowhead=inv,style="+s+",label=\""+cond+"\"];\n"); // decorate=true,
            }
        }

        return so.toString();
    }

}
