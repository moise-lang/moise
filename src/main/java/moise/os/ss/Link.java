package moise.os.ss;

import moise.common.MoiseConsistencyException;

/**
 * Represents a Link between two roles (its source and destination).
 *
 * @author Jomi Fred Hubner
 */
public class Link extends RoleRel  {

    private static final long serialVersionUID = 1L;

    String linkType = "noType";

    /** Creates new Link */
    public Link(Role s, Role d, Group gr, String linkType) throws MoiseConsistencyException {
        super(s,d);
        this.grSpec = gr;
        if (! gr.getSS().hasLinkType(linkType)) {
            throw new MoiseConsistencyException("the link type "+linkType+" was not defined in this OS!");
        }
        this.linkType = linkType;
    }

    /** Creates new Link */
    public Link(Group gr, String linkType) throws MoiseConsistencyException {
        this(null, null, gr, linkType);
    }

    public String getTypeStr() {
        return linkType;
    }

    public String getXMLTag() {
        return "link";
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o) && o instanceof Link) {
            Link other = (Link)o;
            return other.linkType.equals(this.linkType);
        }
        return false;
    }
 }
