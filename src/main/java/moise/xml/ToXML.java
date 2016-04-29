package moise.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * interface for elements that can be transformed to XML
 */
public interface ToXML {
    public Element getAsDOM(Document document);
}
