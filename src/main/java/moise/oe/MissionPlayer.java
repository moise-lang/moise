package moise.oe;

import moise.common.MoiseConsistencyException;
import moise.os.fs.Mission;
import moise.xml.ToXML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 Represents a mission an agent is playing.

 @navassoc - agent   - OEAgent
 @navassoc - mission-specification - Mission
 @navassoc - scheme   - SchemeInstance
 
 @author Jomi Fred Hubner
*/
public class MissionPlayer extends Player implements ToXML {

    private static final long serialVersionUID = 1L;

    final private Mission        mission;
    final private SchemeInstance sch;
    
    protected MissionPlayer(Mission mission, OEAgent ag, SchemeInstance sch) throws MoiseConsistencyException {
        super(ag);
        if (mission == null) {
            throw new MoiseConsistencyException("mission can not be null!");
        }
        this.mission = mission;
        this.sch     = sch;
    }

    public Mission getMission() { return mission; }
    public SchemeInstance getScheme() { return sch; }
    
    public static String getXMLTag() {
        return "mission-player";
    }

    public Element getAsDOM(Document document) {
        Element mpEle = (Element) document.createElement(getXMLTag());
        mpEle.setAttribute("mission", getMission().getId());
        mpEle.setAttribute("agent", getPlayer().getId());
        return mpEle;
    }

    public String toString() {
        return player + "->" + mission + "(" + sch + ")";
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + ((mission == null) ? 0 : mission.hashCode());
        result = PRIME * result + ((sch == null) ? 0 : sch.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)        return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        final MissionPlayer other = (MissionPlayer) obj;
        if (mission == null) {
            if (other.mission != null)
                return false;
        } else if (!mission.equals(other.mission))
            return false;
        if (sch == null) {
            if (other.sch != null)
                return false;
        } else if (!sch.equals(other.sch))
            return false;
        return true;
    }
}
