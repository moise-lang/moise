package moise.common;

/**
 * Moise exception
 *
 * @author Jomi Fred Hubner
 */

public class MoiseXMLParserException extends moise.common.MoiseException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates new <code>MoiseXMLParserException</code> without detail message.
     */
    public MoiseXMLParserException() {
    }


    /**
     * Constructs a <code>MoiseXMLParserException</code> with the specified message.
     * @param msg the detail message.
     */
    public MoiseXMLParserException(String msg) {
        super(msg);
    }
}


