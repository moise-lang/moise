/*
 * MoiseCardinalityException.java
 *
 * Created on 11 de Junho de 2002, 14:01
 */

package moise.common;

/**
 * Moise exception
 *
 * @author Jomi Fred Hubner
 */
public class MoiseCardinalityException extends MoiseException {

    private static final long serialVersionUID = 1L;


    /**
     * Creates new <code>MoiseCardinalityException</code> without detail message.
     */
    public MoiseCardinalityException() {
    }


    /**
     * Constructs a <code>MoiseCardinalityException</code> with the specified message.
     * @param msg the detail message.
     */
    public MoiseCardinalityException(String msg) {
        super(msg);
    }
}


