package ora4mas.nopl;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;

import java.io.Serializable;

import jaca.ToProlog;

public class JasonTermWrapper implements ToProlog, Serializable {
    //Term t;
    String t;

    public JasonTermWrapper(Term t) {
        this.t = t.toString();
    }

    public JasonTermWrapper(String l) {
        try {
            ASSyntax.parseTerm(l); // try to parse as a term, if succeed, use the string, otherwise, create a enclosed string
            t = l;
        } catch (Exception e) {
            //t = new StringTermImpl(l);
            t = "\""+l+"\"";
        }
    }
    //public Term getTerm() {
    //    return t;
    //}
    public String getAsPrologStr() {
        return t;
    }
    public String toString() {
        return t;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof JasonTermWrapper && this.t.equals( ((JasonTermWrapper)obj).t);
    }

    @Override
    public int hashCode() {
        return t.hashCode();
    }

    @Override
    protected JasonTermWrapper clone() throws CloneNotSupportedException {
        return new JasonTermWrapper(t);
    }
}
