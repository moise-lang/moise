package ora4mas.nopl.oe;

import static jason.asSyntax.ASSyntax.createAtom;
import static jason.asSyntax.ASSyntax.createLiteral;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import jaca.ToProlog;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExpr;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Term;
import npl.DynamicFactsProvider;
import ora4mas.nopl.JasonTermWrapper;



/**
 Represents a collective entity (groups and schemes)

 @composed - players * Player

 @author Jomi Fred Hubner
*/
public abstract class CollectiveOE implements Serializable, DynamicFactsProvider, Comparable<CollectiveOE> {

    protected final String id;
    protected final Term   termId;

    protected String monSch;
    protected Set<Player> players   = new ConcurrentSkipListSet<Player>();
    protected Set<Player> exPlayers = new HashSet<Player>();

    protected Set<Literal> playersAsLiteralList   = new ConcurrentSkipListSet<Literal>();
    protected Set<Literal> exPlayersAsLiteralList = new ConcurrentSkipListSet<Literal>();

    public final static PredicateIndicator monitorSchPI = new PredicateIndicator("monitor_scheme", 1);

    public CollectiveOE(String id) {
        if (id.startsWith("\""))
            id = id.substring(1,id.length()-1);
        this.id = id;
        termId = ASSyntax.createAtom(id);
    }
    public String getId() {
        return id;
    }

    abstract PredicateIndicator getPlayerPI();
    abstract PredicateIndicator getExPlayerPI();

    public Player addPlayer(String ag, String obj) {
        Player p = new Player(ag,obj);
        players.add(p);
        playersAsLiteralList.add(getPlayerLiteral(p));
        if (exPlayers.remove(p))
            exPlayersAsLiteralList.remove(getExPlayerLiteral(p));
        return p;
    }

    public boolean hasPlayer(String ag, String obj) {
        return players.contains(new Player(ag,obj));
    }

    public boolean removePlayer(String ag, String obj) {
        Player p = new Player(ag,obj);
        if (players.remove(p)) {
            //rebuildPlayerListAsLiteral();
            playersAsLiteralList.remove(getPlayerLiteral(p));
            exPlayers.add(p);
            exPlayersAsLiteralList.add(getExPlayerLiteral(p));
            return true;
        } else {
            return false;
        }
    }
    public void clearPlayers() {
        players.clear();
        playersAsLiteralList.clear();
    }
    
    public void clearExPlayers() {
    	exPlayers.clear();
    	exPlayersAsLiteralList.clear();
    }

    /*private void rebuildPlayerListAsLiteral() {
        List<Literal> tmp = new ArrayList<Literal>(players.size());
        for (Player p: players)
            tmp.add(getPlayerLiteral(p));
        playersAsLiteralList.clear();
        playersAsLiteralList.addAll(tmp);
    }
    private void rebuildExPlayersAsLiteralList() {
        List<Literal> tmp = new ArrayList<Literal>(players.size());
        for (Player p: exPlayers)
            tmp.add();
        exPlayersAsLiteralList.clear();
        exPlayersAsLiteralList.addAll(tmp);
    }*/
    private Literal getPlayerLiteral(Player p) {
        return createLiteral(getPlayerPI().getFunctor(), createAtom(p.getAg()), createAtom(p.getTarget()), termId);
    }
    private Literal getExPlayerLiteral(Player p) {
        return createLiteral(getExPlayerPI().getFunctor(), createAtom(p.getAg()), createAtom(p.getTarget()), termId);
    }

    public Collection<Player> getPlayers() {
        return players;
    }
    public Collection<Player> getExPlayers() {
        return exPlayers;
    }

    public String getMonitorSch() {
        return monSch;
    }
    public void setMonitorSch(String monSch) {
        this.monSch = monSch;
    }

    public abstract CollectiveOE clone();

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    static protected ToProlog getCollectionAsProlog(Collection<? extends Object> c) {
        StringBuilder r = new StringBuilder("[");
        String v = "";
        for (Object s: c) {
            //r.append(v+"\""+s+"\"");
            r.append(v+new JasonTermWrapper(s.toString()));
            v=",";
        }
        r.append("]");
        final String s = r.toString();
        return new ToProlog() {
            public String getAsPrologStr() {
                return s;
            }
        };
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (! (obj instanceof CollectiveOE)) return false;
        CollectiveOE g = (CollectiveOE)obj;
        return id.equals(g.id);
    }


    public int compareTo(CollectiveOE g) {
        return this.id.compareTo(g.id);
    }

    /** transforms a Scheme Instance into NPL code (dynamic facts) */
    public List<Literal> transform() {
        List<Literal> lg = new ArrayList<Literal>();
        for (Literal l: getDynamicFacts()) {
            Iterator<Unifier> i = consult(l, new Unifier());
            while (i.hasNext()) {
                lg.add((Literal)l.capply(i.next()));
            }
        }
        return lg;
    }

    abstract public Literal[] getDynamicFacts();

    public boolean isRelevant(PredicateIndicator pi) {
        for (Literal l: getDynamicFacts())
            if (pi.equals(l.getPredicateIndicator()))
                return true;
        if (pi.equals(monitorSchPI))
            return true;

        return false;
    }

    public Iterator<Unifier> consult(Literal l, Unifier u) {
        if (l.getPredicateIndicator().equals(getPlayerPI())) {
            return consult(l, u, playersAsLiteralList);

        } else if (l.getPredicateIndicator().equals(getExPlayerPI())) {
            return consult(l, u, exPlayersAsLiteralList);

        } else if (l.getPredicateIndicator().equals(monitorSchPI)) {
            Term lCopy = l.getTerm(0);
            if (getMonitorSch() != null && u.unifies(lCopy, createAtom(getMonitorSch())))
                return LogExpr.createUnifIterator(u);
            else
                return LogExpr.EMPTY_UNIF_LIST.iterator();
        }
        return LogExpr.EMPTY_UNIF_LIST.iterator();
    }

    public Iterator<Unifier> consult(final Literal l, final Unifier u, final Collection<Literal> facts) {
        return new Iterator<Unifier>() {
            Iterator<Literal> i  = facts.iterator();
            Unifier current      = null;
            boolean needsUpdate  = true;

            public boolean hasNext() {
                if (needsUpdate)
                    get();
                return current != null;
            }
            public Unifier next() {
                if (needsUpdate)
                    get();
                Unifier a = current;
                if (current != null)
                    needsUpdate = true;
                return a;
            }
            private void get() {
                needsUpdate = false;
                current     = null;
                while (i.hasNext()) {
                    Unifier uc = u.clone();
                    if (uc.unifiesNoUndo(l, i.next())) {
                        current = uc;
                        return;
                    }
                }
            }
            public void remove() {}
        };
    }

    public Iterator<Unifier> consultProviders(final Literal l, final Unifier u, final Iterator<? extends DynamicFactsProvider> providers) {
        return new Iterator<Unifier>() {
            Iterator<Unifier> i  = null;
            Unifier current      = null;
            boolean needsUpdate  = true;

            public boolean hasNext() {
                if (needsUpdate)
                    get();
                return current != null;
            }
            public Unifier next() {
                if (needsUpdate)
                    get();
                Unifier a = current;
                if (current != null)
                    needsUpdate = true;
                return a;
            }
            private void get() {
                needsUpdate = false;
                current     = null;
                if (i == null)
                    if (providers.hasNext())
                        i = providers.next().consult(l, u);
                    else
                        return;
                if (i.hasNext()) {
                    current = i.next();
                } else {
                    i = null;
                    get();
                }
            }
            public void remove() {}
        };
    }

}
