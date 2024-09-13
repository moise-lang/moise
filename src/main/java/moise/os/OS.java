package moise.os;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jason.runtime.SourcePath;
import moise.common.MoiseConsistencyException;
import moise.common.MoiseElement;
import moise.common.MoiseException;
import moise.common.MoiseXMLParserException;
import moise.os.fs.FS;
import moise.os.fs.Scheme;
import moise.os.ns.NS;
import moise.os.ns.Norm;
import moise.os.ss.SS;
import moise.xml.DOMUtils;
import moise.xml.ToXML;

/**
 * Represents an Organization Specification (SS, FS, NS).
 *
 * @composed - structural  - SS
 * @composed - functional  - FS
 * @composed - normative   - NS
 *
 * @author Jomi Fred Hubner
 */
public class OS extends MoiseElement implements ToXML {

    private static final long serialVersionUID = 1L;

    protected SS ss = null;
    protected FS fs = null;
    protected NS ns = null;

    protected String uri;

    public OS() {
        ss = new SS(this);
        fs = new FS(this);
        ns = new NS(this);
    }


    public void setSS(SS s) { ss = s; }
    public void setFS(FS f) { fs = f; }
    public void setNS(NS n) { ns = n; }

    public void   setURI(String u) { uri = u; }
    public String getURI()         { return uri; }

    /**
     * adds the elements (roles definitions, link types, ...)
     * of another SS into this OS
     */
    public void addSS(SS s) throws MoiseException {
        ss.addRoleDef(s.getRolesDef());
        ss.addLinkType(s.getLinkTypes());
        if (ss.getRootGrSpec() == null) {
            ss.setRootGrSpec(s.getRootGrSpec());
        }
    }

    /**
     * adds the scheme of another FS into this OS
     */
    public void addFS(FS f) {
        fs.addScheme(f.getSchemes());
    }

    /**
     * adds the norms of another NS into this OS
     */
    public void addNS(NS d) {
        ns.addNorm(d.getNorms());
    }

    public SS getSS() { return ss; }
    public FS getFS() { return fs; }
    public NS getNS() { return ns; }
    
    /** returns a string representing the goal in Prolog syntax, format:
     *     os(id, root group, [schemes], [norms])
     */
    public String getAsProlog() {
        StringBuilder s = new StringBuilder("os("+getId()+",");
        if (ss.getRootGrSpec() != null) 
            s.append( ss.getRootGrSpec().getAsProlog());
        else
            s.append("no_group");
        s.append(",[");

        String v = "";
        for (Scheme sch: fs.getSchemes()) {
            s.append(v+sch.getAsProlog());
            v = ",";
        }
        s.append("],[");

        v = "";
        for (Norm n: ns.getNorms()) {
            s.append(v+n.getAsProlog());
            v = ",";
        }
        s.append("])");
        return s.toString();
    }

    // XML methods

    public static String getXMLTag() {
        return "organisational-specification";
    }

    public Element getAsDOM(Document document) {
        Element ele = (Element) document.createElement(getXMLTag());
        ele.setAttribute("id", getId());
        ele.setAttribute("os-version","1.0"); // TODO: get the number from other place
        ele.setAttribute("xmlns","http://moise.sourceforge.net/os");
        ele.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
        ele.setAttribute("xsi:schemaLocation","http://moise.sourceforge.net/os https://moise-lang.github.io/xml/os.xsd");

        if (getProperties().size() > 0) {
            ele.appendChild( getPropertiesAsDOM(document));
        }
        ele.appendChild( getSS().getAsDOM(document));
        ele.appendChild( getFS().getAsDOM(document));
        ele.appendChild( getNS().getAsDOM(document));

        return ele;
    }


    public static OS loadOSFromURI(String uri) {
        try {
            Document doc = null;
            if (uri.startsWith(SourcePath.CRPrefix)) {
                // load for jar
                doc = DOMUtils.getParser().parse(OS.class.getResource(uri.substring(SourcePath.CRPrefix.length())).openStream());
            } else {
                doc = DOMUtils.getParser().parse(uri);
            }
            DOMUtils.getOSSchemaValidator().validate(new DOMSource(doc));

            OS os = new OS();
            os.setFromDOM(doc);
            return os;
        } catch (ParserConfigurationException e) {
            System.err.println("Parser creation error:"+e);
            e.printStackTrace();
        } catch (SAXException e) {
            System.err.println("Parser creation error:"+e);
        } catch (IOException e) {
            System.err.println("IO error:"+e);
            e.printStackTrace();
        } catch (MoiseXMLParserException e) {
            System.err.println("Moise+ error:"+e);
        } catch (MoiseConsistencyException e) {
            System.err.println("Moise+ error:"+e);
        } catch (MoiseException e) {
            System.err.println("Moise+ error:"+e);
            e.printStackTrace();
        }
        return null;
    }

    /** the organisation-specification parent node is the parameter */
    public void setFromDOM(Node node) throws MoiseException {
        NodeList l = node.getChildNodes();
        for (int i=0; i<l.getLength(); i++) {
            node = l.item(i);
            String name = node.getNodeName();
            if (node.getNodeType() == Node.ELEMENT_NODE && name.equals(getXMLTag())) {
                Element osEle = (Element)node;
                setId(osEle.getAttribute("id"));

                setPropertiesFromDOM(osEle);

                SS ss = new SS(this);
                setSS(ss);
                FS fs = new FS(this);
                setFS(fs);
                NS ns = new NS(this);
                setNS(ns);

                // SS
                Element ele = DOMUtils.getDOMDirectChild(osEle, SS.getXMLTag());
                if (ele != null)
                    ss.setFromDOM(ele);

                // FS
                ele = DOMUtils.getDOMDirectChild(osEle, FS.getXMLTag());
                if (ele != null)
                    fs.setFromDOM(ele);

                // NS
                ele = DOMUtils.getDOMDirectChild(osEle, NS.getXMLTag());
                if (ele != null)
                    ns.setFromDOM(ele);

                return; // ok
            }
        }

        throw new MoiseXMLParserException("Error processing OS XML file: expected tag "+getXMLTag());
    }


    /** used to convert old format to the new */
    public static void main(String[] args) throws TransformerException {
        if (args.length != 1) {
            System.err.println("pass an URI as argument for the organization specification file (in xml)");
            System.exit(1);
        }
        //OS o = OSLoadXML.loadOSFromURI(arg[0]);

        OS o = OS.loadOSFromURI(args[0]);
        System.out.println( DOMUtils.dom2txt(o));
    }
}
