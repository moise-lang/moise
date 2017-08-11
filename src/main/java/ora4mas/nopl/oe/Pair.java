package ora4mas.nopl.oe;

import java.io.Serializable;

import jaca.ToProlog;

public class Pair<T1,T2> implements ToProlog, Serializable {
    T1 l;
    T2 r;

    public Pair(T1 l, T2 r) {
        this.l = l;
        this.r = r;
    }

    public T1 getLeft()  { return l; }
    public T2 getRight() { return r; }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (! (obj instanceof Pair)) return false;
        Pair<T1,T2> p = (Pair<T1,T2>)obj;
        return p.l.equals(this.l) && p.r.equals(this.r);
    }

    @Override
    public int hashCode() {
        return l.hashCode() + r.hashCode();
    }

    @Override
    public String toString() {
        return l + "," + r;
    }

    public String getAsPrologStr() {
        return "pair(" + l + "," + r +")";
    }
}
