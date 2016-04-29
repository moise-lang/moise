package moise.common;

/**
 * Moise exception
 *
 * @author Jomi Fred Hubner
 */
public class MoiseException extends java.lang.Exception {

    private static final long serialVersionUID = 1L;

    public MoiseException() {
        
    }
    /**
     * Constructs a <code>MoiseException</code> with the specified message.
     * @param msg the detail message.
     */
    public MoiseException(String msg) {
        super(msg);
    }
}


