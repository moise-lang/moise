package moise.xml;

import jason.util.ToDOM;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import moise.os.ss.Role;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DOMUtils {
    private static Transformer transformer = null;
    private static DocumentBuilder builder = null;
    
    public static String dom2txt(ToXML ele) {
        StringWriter so = new StringWriter();
        try {
            getTransformer().transform(new DOMSource(getAsXmlDocument(ele)), new StreamResult(so));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return so.toString();
    }

    public static String roleDetails(Role r) {
        StringWriter so = new StringWriter();
        try {
            Document document = getBuilder().newDocument();
            document.appendChild( r.getAsDetailedDom( document));
            getTransformer().transform(new DOMSource(document), new StreamResult(so));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return so.toString();
    }
    
    public static Document getAsXmlDocument(ToXML ele) {
        Document document = getBuilder().newDocument();
        document.appendChild( ele.getAsDOM( document ));
        return document;
    }

    public static Document getAsXmlDocument(ToDOM ele) {
        Document document = getBuilder().newDocument();
        document.appendChild( ele.getAsDOM( document ));
        return document;
    }

    public static Element getDOMDirectChild(Element ele, String tagName) {
        NodeList nl = ele.getChildNodes();
        if (nl != null) {
            for (int i=0; i<nl.getLength(); i++) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE && nl.item(i).getNodeName().equals(tagName)) {
                    return (Element)nl.item(i);
                }
            }
        }
        return null;
    }
    public static List<Element> getDOMDirectChilds(Element ele, String tagName) {
        List<Element> r = new ArrayList<Element>();
        NodeList nl = ele.getChildNodes();
        if (nl != null) {
            for (int i=0; i<nl.getLength(); i++) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE && nl.item(i).getNodeName().equals(tagName)) {
                    r.add((Element)nl.item(i));
                }
            }
        }
        return r;
    }
    
    static private DocumentBuilder getBuilder() {
        if (builder == null) {
            try {
                builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (Exception e) {
                System.err.println("Error creating XML builder\n");
                e.printStackTrace();
                return null;
            }
        }
        return builder;
    }
    
    
    static private Transformer getTransformer() {
        if (transformer == null) {
            try {
                transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return transformer;
    }
    
    public static Validator getOSSchemaValidator() throws SAXException, IOException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
        Schema schema = schemaFactory.newSchema(new StreamSource(DOMUtils.class.getResource("/xml/os.xsd").openStream()));
        return schema.newValidator();
    }
    
    public static DocumentBuilder getParser() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        // set up the error handler
        builder.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException exception) throws SAXException {
                System.out.println("**Parsing Warning** "+
                        "\n  Line:    " +  exception.getLineNumber() + 
                        "\n  URI:     " +  exception.getSystemId() + 
                        "\n  Message: " +  exception.getMessage());
                throw new SAXException("Warning encountered");
            }
            public void error(SAXParseException exception) throws SAXException {
                System.out.println("**Parsing Error** "+
                        "\n  Line:    " +  exception.getLineNumber() + 
                        "\n  URI:     " +  exception.getSystemId() + 
                        "\n  Message: " +  exception.getMessage());
                throw new SAXException("Error encountered");
            }
            public void fatalError(SAXParseException exception) throws SAXException {
                System.out.println("**Parsing Fatal Error** "+
                        "\n  Line:    " +  exception.getLineNumber() + 
                        "\n  URI:     " +  exception.getSystemId() + 
                        "\n  Message: " +  exception.getMessage());
                throw new SAXException("Fatal Error encountered");
            }
        });

        return builder;
    }
    
    private static TransformerFactory tFactory = null;
    public static TransformerFactory getTransformerFactory() {
        if (tFactory == null) {
            tFactory = TransformerFactory.newInstance();
            tFactory.setURIResolver(new URIResolver() {
                public Source resolve(String href, String base) throws TransformerException {
                    try {
                        return DOMUtils.getXSL(href);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
        }
        return tFactory;
    }
    
    
    public static StreamSource getXSL(String href) throws IOException {
        if (!href.endsWith(".xsl"))
            href += ".xsl";
        StreamSource ss;
        File f = new File("xml/"+href);
        if (f.exists()) {
            ss = new StreamSource(new FileInputStream(f));
        } else {
            f = new File("src/xml/"+href);
            if (f.exists()) {
                ss = new StreamSource(new FileInputStream(f));
            } else {
                ss = new StreamSource(DOMUtils.class.getResource("/xml/"+href).openStream()); 
            }
        }
        return ss;
    }

}
