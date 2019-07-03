package ora4mas.nopl.oe;

import static jason.asSyntax.ASSyntax.createAtom;
import static jason.asSyntax.ASSyntax.createLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExpr;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import npl.DynamicFactsProvider;
import jaca.ToProlog;



/**
 Represents the instance group of one Group Specification

 @navassoc - responsible_for  - Scheme

 @author Jomi Fred Hubner
*/
public class Group extends CollectiveOE implements ToProlog {

    public final static Literal[] dynamicFacts = {
        createLiteral("group_id",    new VarTerm("Gr")),
        createLiteral("play",        new VarTerm("Ag"), new VarTerm("Role"), new VarTerm("Gr")),
        createLiteral("leaved_role", new VarTerm("Ag"), new VarTerm("Role"), new VarTerm("Gr")),
        createLiteral("responsible", new VarTerm("Gr"), new VarTerm("Sch")),
        createLiteral("parent_group", new VarTerm("SuperGr")),
        createLiteral("subgroup_well_formed", new VarTerm("SubGr")),
        createLiteral("subgroup",             new VarTerm("SubGr"), new VarTerm("Type"), new VarTerm("SuperGr"))
    };

    public final static PredicateIndicator groupPI       = dynamicFacts[0].getPredicateIndicator();
    public final static PredicateIndicator playPI        = dynamicFacts[1].getPredicateIndicator();
    public final static PredicateIndicator exPlayPI      = dynamicFacts[2].getPredicateIndicator();
    public final static PredicateIndicator responsiblePI = dynamicFacts[3].getPredicateIndicator();
    public final static PredicateIndicator parentGrPI    = dynamicFacts[4].getPredicateIndicator();
    public final static PredicateIndicator subGrWFPI     = dynamicFacts[5].getPredicateIndicator();
    public final static PredicateIndicator subGrPI       = dynamicFacts[6].getPredicateIndicator();

    private HashSet<String>     schemes   = new HashSet<>();
    private Set<Literal>        schemesAsLiteralList = new ConcurrentSkipListSet<>();
    private Map<String,Group>   subgroups = new HashMap<>();
    private Map<String,Literal> subgroupsAsLiteralList = new ConcurrentSkipListMap<>();
    private Set<Literal>        wellFormedSubGroups = new HashSet<>();
    private String              parentGroup = "root";
    private String              type;

    public Group(String id) {
        super(id);
    }

    public void addResponsibleForScheme(String s) {
        schemes.add(s);
        schemesAsLiteralList.add(createLiteral(responsiblePI.getFunctor(), termId, createAtom(s)));
    }
    public boolean removeResponsibleForScheme(String s) {
        if (schemes.remove(s)) {
            schemesAsLiteralList.remove(createLiteral(responsiblePI.getFunctor(), termId, createAtom(s)));
            return true;
        }
        return false;
    }

    // subgroups

    public Group addSubgroup(String gId, String gType, String parentGr) {
        Group g = new Group(gId);
        g.setType(gType);
        g.setParentGroup(parentGr);
        subgroups.put(gId, g);
        subgroupsAsLiteralList.put(gId, createLiteral(subGrPI.getFunctor(), createAtom(g.getId()), createAtom( g.getGrType()), createAtom(g.getParentGroup())));
        return g;
    }
    public Group removeSubgroup(String gId) {
        subgroups.remove(gId);
        subgroupsAsLiteralList.remove(gId);
        wellFormedSubGroups.remove(getSubGrWFLiteral(gId));
        return subgroups.remove(gId);
    }
    public boolean hasSubgroup() {
        return !subgroups.isEmpty();
    }
    public Collection<Group> getSubgroups() {
        return subgroups.values();
    }
    public Group getSubgroup(String gId) {
        return subgroups.get(gId);
    }
    public void setSubgroupWellformed(String gId, boolean wf) {
        if (wf)
            wellFormedSubGroups.add(getSubGrWFLiteral(gId));
        else
            wellFormedSubGroups.remove(getSubGrWFLiteral(gId));
    }
    public boolean isSubgroupWellformed(String gId) {
        return wellFormedSubGroups.contains(getSubGrWFLiteral(gId));
    }
    private Literal getSubGrWFLiteral(String sg) {
        return createLiteral(subGrWFPI.getFunctor(), createAtom(sg));
    }



    public void setType(String type) {
        this.type = type;
    }
    public String getGrType() {
        return type;
    }

    public void setParentGroup(String gId) {
        parentGroup = gId;
    }
    public String getParentGroup() {
        return parentGroup;
    }

    @SuppressWarnings("unchecked")
    public Collection<String> getSchemesResponsibleFor() {
        return (Collection<String>)schemes.clone();
    }

    public ToProlog getResponsibleForAsProlog() {
        return getCollectionAsProlog(getSchemesResponsibleFor());
    }

    public ToProlog getSubgroupsAsProlog() {
        List<String> directSubgroups = new ArrayList<>();
        for (Group g: subgroups.values())
            if (g.getParentGroup().equals(getId()))
                directSubgroups.add(g.getId());
        return getCollectionAsProlog(directSubgroups);
    }


    PredicateIndicator getPlayerPI() {
        return playPI;
    }
    PredicateIndicator getExPlayerPI() {
        return exPlayPI;
    }

    public Literal[] getDynamicFacts() {
        return dynamicFacts;
    }

    @Override
    public Iterator<Unifier> consult(Literal l, Unifier u) {
        PredicateIndicator pi = l.getPredicateIndicator();
        if (pi.equals(getExPlayerPI())) // || pi.equals(monitorSchPI))
            return super.consult(l, u);

        if (pi.equals(getPlayerPI())) {
            if (hasSubgroup()) {
                List<DynamicFactsProvider> providers = new ArrayList<>();
                providers.add(new DynamicFactsProvider() {
                    public boolean isRelevant(PredicateIndicator pi)       { return true; }
                    public Iterator<Unifier> consult(Literal l, Unifier u) { return Group.super.consult(l,u); }
                });
                providers.addAll(subgroups.values());

                return consultFromProviders(l, u, providers.iterator());
            } else {
                return super.consult(l, u);
            }
        }

        if (pi.equals(responsiblePI))
            return consultFromCollection(l, u, schemesAsLiteralList);

        Term lCopy;
        if (pi.equals(groupPI)) {
            lCopy = l.getTerm(0);
            if (u.unifies(lCopy, termId))
                return LogExpr.createUnifIterator(u);
            else
                return LogExpr.EMPTY_UNIF_LIST.iterator();
        }

        if (pi.equals(parentGrPI)) {
            lCopy = l.getTerm(0);
            if (u.unifies(lCopy, createAtom(parentGroup)))
                return LogExpr.createUnifIterator(u);
            else
                return LogExpr.EMPTY_UNIF_LIST.iterator();
        }

        if (pi.equals(subGrWFPI))
            return consultFromCollection(l, u, wellFormedSubGroups);

        if (pi.equals(subGrPI))
            return consultFromCollection(l, u, subgroupsAsLiteralList.values());

        return LogExpr.EMPTY_UNIF_LIST.iterator();
    }

    public Group clone() {
        Group g = new Group(id);
        //g.monSch = this.monSch;
        g.players.addAll(this.players);
        g.exPlayers.addAll(this.exPlayers);
        g.schemes.addAll(this.schemes);
        for (String sgId: subgroups.keySet())
            g.subgroups.put(sgId, subgroups.get(sgId).clone());
        //g.subgroups.putAll(this.subgroups);
        for (String k: subgroupsAsLiteralList.keySet())
            g.subgroupsAsLiteralList.put(k, subgroupsAsLiteralList.get(k).copy());
        //g.subgroupsAsLiteralList.putAll(this.subgroupsAsLiteralList);
        g.parentGroup = this.parentGroup;
        g.type = this.type;
        g.wellFormedSubGroups.addAll(this.wellFormedSubGroups);
        g.playersAsLiteralList.addAll(this.playersAsLiteralList);
        g.exPlayersAsLiteralList.addAll(this.exPlayersAsLiteralList);
        return g;
    }


    public String getAsPrologStr() {
        return getId();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("Group: "+getId()+"\n  players:\n");
        for (Player p: players)
            out.append("    "+p+"\n");
        out.append("  schemes: ");
        for (String s: schemes) {
            out.append(s);
            /*if (s.equals(getMonitorSch()))
                out.append("(*) ");
            else
                out.append(" ");*/
        }
        out.append("\n  subgroups: "+getSubgroupsAsProlog().getAsPrologStr());
        out.append("\n  parent group: "+getParentGroup());


        return out.toString();
    }
}
