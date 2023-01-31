package moise.os;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import moise.common.Identifiable;

/**
 * Represents a collection with cardinality (e.g. RoleA needs (1,50) players).
 *
 * @author Jomi Fred Hubner
 */
public class CardinalitySet<T extends Identifiable> implements java.io.Serializable, Iterable<T> {

    private static final long serialVersionUID = 1L;

    // Map for content (for fast retrival)
    protected HashMap<String,T>      contents      = new HashMap<String,T>();

    // Map for cardinalities
    protected HashMap<T,Cardinality> cardinalities = new HashMap<T,Cardinality>();

    /**
     * adds an object with default cardinality
     */
    public void add(T o) {
        contents.put(o.getFullId(), o);
    }

    /**
     * adds an object with a specific cardinality
     */
    public void add(T o, Cardinality c)  {
        setCardinality(o, c);
        contents.put(o.getFullId(), o);
    }

    public boolean contains(T o) {
        return contents.get(o.getFullId()) != null;
    }
    public boolean contains(String id) {
        return contents.get(id) != null;
    }

    public T get(String id) {
        return contents.get(id);
    }

    public Collection<T> getAll() {
        return contents.values();
    }

    public boolean remove(T o) {
        cardinalities.remove(o);
        return contents.remove(o) != null;
    }

    /**
     * sets the cardinality on an object already in the collection*
     */
    public void setCardinality(T o, Cardinality c)  {
        cardinalities.put(o, c);
    }


    /**
     * returns the cardinality for one object collect here, if not specified, returns the default cardinality
     */
    public Cardinality getCardinality(T o) {
        Cardinality c = cardinalities.get(o);
        if (c != null) {
            return c;
        } else {
            return Cardinality.defaultValue;
        }
    }

    public boolean isEmpty() {
        return contents.isEmpty();
    }

    public int size() {
        return contents.size();
    }

    public Iterator<T> iterator() {
        return contents.values().iterator();
    }
}
