package moise.prolog;

import java.util.ArrayList;
import java.util.List;


/**
 * A general PrologPredicate:
 * <functor>(id,attributes([<terms>]), <terms>)
 */
public class PrologPredicate implements ToProlog {
    String functor;

    List<String> attributes = new ArrayList<String>();
    List<String> terms = new ArrayList<String>();

    public PrologPredicate(String f) {
        functor = f;
    }

    /** adds a boolean attribute */
    public void addAttribute(String id, boolean value) {
        if (value) {
            attributes.add(id);
        }
    }
    /** adds a string attribute */
    public void addAttribute(String id, String value) {
        if (value.length() > 0) {
            attributes.add(id+("("+value+")"));
        }
    }

    public void addTerm(String t) {
        terms.add(t);
    }

    public String getAsProlog() {
        StringBuffer p = new StringBuffer(functor);
        if (attributes.size() > 0 || terms.size() > 0) {
            p.append("(");
        }
        if (attributes.size() > 0) {
            p.append("attributes([");
            String v = "";
            for (String a: attributes) {
                p.append(v+a);
                v = ",";
            }
            p.append("])");
            if (terms.size()>0) {
                p.append(", ");
            }
        }
        String v = "";
        for (String a: terms) {
            p.append(v+a);
            v = ",";
        }
        if (attributes.size() > 0 || terms.size() > 0) {
            p.append(")");
        }
        return p.toString();
    }

    public String toString() {
        return getAsProlog();
    }
}
