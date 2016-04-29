package moise.common;

import java.io.Serializable;
import java.util.Date;

/**
 * This class represents an organisational event in an MAS (agent entrance, 
 * role adoption, group creation, etc.)
 *
 * @author Jomi Fred Hubner
 */
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    /** the time the event has started  */    
    protected Date initTime = null;
    
    /** the time the event has finished  */    
    protected Date endTime = null;
    
    /** the agent that generated this event (e.g.: Jomi)   */    
    protected String creator = null;
    

    /** 
     * Creates new Event object
     * 
     * @param init the time this event has started
     * @param ag   the agent that generated this event 
     */
    public Event(Date init, String ag) {
        initTime = init;
        creator = ag;
    }

    /** 
     * Creates new Event object
     * 
     * @param init the time this event has started
     */
    public Event(Date init) {
        initTime = init;
    }

    /** Creates new Event object */
    public Event() {   }

}
