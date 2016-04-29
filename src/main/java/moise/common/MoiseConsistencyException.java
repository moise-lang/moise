package moise.common;

/**
 * Moise exception
 *
 * @author Jomi Fred Hubner
 */

public class MoiseConsistencyException extends MoiseException {

    private static final long serialVersionUID = 1L;


    /**
     * Creates new <code>MoiseXMLParserException</code> without detail message.
     */
    public MoiseConsistencyException() {
    }


    /**
     * Constructs a <code>MoiseXMLParserException</code> with the specified message.
     * @param msg the detail message.
     */
    public MoiseConsistencyException(String msg) {
        super(msg);
    }
}


