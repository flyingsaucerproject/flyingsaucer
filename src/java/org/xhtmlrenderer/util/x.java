package org.xhtmlrenderer.util;

import java.io.*;
import java.net.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import java.util.*;

/**
 *  Description of the Class
 *
 *@author     joshy@joshy.org
 *@created    August 27, 2003
 */
public class x {

    /**
     *  Description of the Method
     *
     *@param  node  Description of the Parameter
     *@param  name  Description of the Parameter
     *@return       Description of the Return Value
     */
    public static Element child(Element node, String name) {
        NodeList nl = node.getElementsByTagName(name);
        if (nl == null) {
            return null;
        }
        return (Element) nl.item(0);
    }


    /**
     *  Description of the Method
     *
     *@param  node  Description of the Parameter
     *@param  name  Description of the Parameter
     *@return       Description of the Return Value
     */
    public static NodeList children(Element node, String name) {
        NodeList nl = node.getElementsByTagName(name);
        return nl;
    }


    /**
     *  Description of the Method
     *
     *@param  element  Description of the Parameter
     */
    public static void clear(Element el) {
        while (el.hasChildNodes()) {
            el.removeChild(el.getFirstChild());
        }
    }


    /**
     *  Description of the Method
     *
     *@param  string         Description of the Parameter
     *@return                Description of the Return Value
     *@exception  Exception  Description of the Exception
     */
    public static Document loadDocument(String string) throws Exception {
        return loadDocument(new FileInputStream(string));
    }
    public static Document loadDocument(URL url) throws Exception {
        return loadDocument(url.openStream());
    }


    public static Document loadDocument(InputStream in) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        MyErrorHandler eh = new MyErrorHandler();
        builder.setErrorHandler(eh);
        //u.p("before"+Thread.currentThread());
        Document doc = builder.parse(in);
        //u.p("after" + Thread.currentThread());
        //u.p("ex = " + eh.err);
        if(eh.err != null) {
            //u.p("throwing");
            throw eh.err;
        }
        return doc;
    }

    public static Document loadDocumentFromResource(String string, Object marker) throws Exception {
        InputStream in = marker.getClass().getResourceAsStream(string);
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
        return doc;
    }


    /**
     *  Description of the Method
     *
     *@param  doc    Description of the Parameter
     *@param  name   Description of the Parameter
     *@param  value  Description of the Parameter
     *@return        Description of the Return Value
     */
    public static Element ne(Document doc, String name, String value) {
        return newElement(doc, name, value);
    }
    public static Element ne(Document doc, String name) {
        return newElement(doc,name);
    }
    public static Element ne(Element element, String name, String value) {
        return newElement(element.getOwnerDocument(),name,value);
    }
    public static Element ne(Element element, String name) {
        return newElement(element.getOwnerDocument(),name);
    }


    /**
     *  Description of the Method
     *
     *@return                Description of the Return Value
     *@exception  Exception  Description of the Exception
     */
    public static Document newDocument() throws Exception {
        Document output = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        return output;
    }

    public static Element newElement(Document doc, String name) {
        Element elem = doc.createElement(name);
        return elem;
    }

    public static Element newElement(Document doc, String name, String value) {
        if (value == null) {
            value = "";
        }
        Element elem = doc.createElement(name);
        Text text = doc.createTextNode(value);
        elem.appendChild(text);
        return elem;
    }


    public static void p(Node node) {
        p(node, "");
    }

    /**
     *  Description of the Method
     *
     *@param  doc  Description of the Parameter
     */
    public static void p(Document doc) {
        u.p("as document");
        u.p("<?xml version=\"1.0\"?>");
        DocumentType dt = doc.getDoctype();
        if (dt != null) {
            u.p("<!DOCTYPE " + dt.getName() + " PUBLIC \"" +
                dt.getPublicId() + "\" \"" +
                dt.getSystemId() + ">");
        }
        p((Node) doc);
    }


    /**
     *  Description of the Method
     *
     *@param  node  Description of the Parameter
     */
    public static void p(Node node, String tab) {
        if (node == null) {
            return;
        }
        //u.p(tab + "node type = " + node.getNodeType());
        if (node.getNodeType() == node.ENTITY_NODE) {
            u.p(tab + "entity node");
        }

        if (node.getNodeType() == node.ENTITY_REFERENCE_NODE) {
            u.p(tab + "------ entity ref node");
            u.p(tab + "ent: " + node.getNodeName());
            u.p(tab + node.getNodeValue());
            NodeList c = node.getChildNodes();
            Node n = c.item(0);
            //u.print_as_bytes(n.getNodeValue());
            //n.setNodeValue("blahfoo");
        }

        if (node.getNodeType() == node.TEXT_NODE) {
            if (!node.getNodeValue().trim().equals("")) {
                u.p(tab + node.getNodeValue());
            }
        }

        if (node.getNodeType() == node.ELEMENT_NODE) {
            NamedNodeMap atts = node.getAttributes();
            NodeList c = node.getChildNodes();
            u.pr(tab + "<" + node.getNodeName());

            if (atts.getLength() == 0) {
                if (c.getLength() == 0) {
                    u.p("/>");
                } else {
                    u.p(">");
                }
            }

            if (atts.getLength() == 1) {
                u.pr(" " + atts.item(0));
                if (c.getLength() == 0) {
                    u.p("/>");
                } else {
                    u.p(tab + ">");
                }
            }

            if (atts.getLength() > 1) {
                // print the attributes
                u.p(" " + atts.item(0));
                for (int i = 1; i < atts.getLength(); i++) {
                    u.p(tab + "  " + atts.item(i));
                }
                if (c.getLength() == 0) {
                    u.p(tab + "/>");
                } else {
                    u.p(tab + ">");
                }
            }
        }

        NodeList c = node.getChildNodes();
        for (int i = 0; i < c.getLength(); i++) {
            Node n = c.item(i);
            p(n, tab + "  ");
        }

        if ((node.getNodeType() == node.ELEMENT_NODE) && (node.getChildNodes().getLength() > 0)) {
            u.p(tab + "</" + node.getNodeName() + ">");
        }
    }


    /**
     *  Description of the Method
     *
     *@param  node  Description of the Parameter
     *@param  pw    Description of the Parameter
     */
    public static void p(Node node, PrintWriter pw) {
        pw.println("name=" + node.getNodeName() + "<br>");
        NodeList c = node.getChildNodes();
        for (int i = 0; i < c.getLength(); i++) {
            Node n = c.item(i);
            p(n, pw);
        }
    }


    /**
     *  Description of the Method
     *
     *@param  list  Description of the Parameter
     */
    public static void p(NodeList list) {
        u.p("node list: " + list.toString());
        u.p("size = " + list.getLength());
        for (int i = 0; i < list.getLength(); i++) {
            p(list.item(i));
        }
    }


    /**
     *  Description of the Method
     *
     *@param  doc            Description of the Parameter
     *@param  filename       Description of the Parameter
     *@exception  Exception  Description of the Exception
     */
    public static void saveDocument(Document doc, String filename) throws Exception {
        throw new Exception("Saving not supported");
        /*
        //u.p("about to save: " + doc);
        //x.p(doc);
        XMLSerializer xs = new XMLSerializer();
        xs.setOutputByteStream(new FileOutputStream(filename));
        OutputFormat of = new OutputFormat();
        of.setMethod("xml");
        of.setIndenting(true);
        //of.setPreserveEmptyAttributes(true);
        //of.setPreserveEmptyAttributes()
        of.setIndent(4);
        of.setPreserveSpace(false);
        of.setOmitDocumentType(true);
        xs.setOutputFormat(of);
        xs.serialize(doc);
        */
    }
    


    public static void writeDocument(PrintWriter writer, Document doc) throws IOException{
        write(writer,doc.getDocumentElement());
    }

    public static void write(PrintWriter writer, Node node) throws IOException {
        // handle atts
        if(node.getNodeType() == node.ATTRIBUTE_NODE) {
            //u.p("att node = " + node);
            writer.print(" " + node.getNodeName() +"=\""+node.getNodeValue()+"\" ");
        }

        // <open tag>
        if(node.getNodeType() == node.ELEMENT_NODE) {
            //u.p("element = " + node);
            writer.print("<"+node.getNodeName());
            NamedNodeMap atts = node.getAttributes();
            for(int i=0; i<atts.getLength(); i++) {
                Node att = atts.item(i);
                write(writer,att);
            }

            writer.print(">");
        }

        // if it's a text node
        if(node.getNodeType() == node.TEXT_NODE) {
            //u.p("text node: " + node);
            writer.print(node.getNodeValue());
        }

        // if it's a cdata node
        if(node.getNodeType() == node.CDATA_SECTION_NODE) {
            writer.print("<![CDATA[");
            writer.print(node.getNodeValue());
            writer.print("]]>");
        }
        if(node.getNodeType() != node.ATTRIBUTE_NODE) {
            NodeList children = node.getChildNodes();
            for(int i=0; i<children.getLength(); i++) {
                write(writer,children.item(i));
            }
        }

        // </close tag>
        if(node.getNodeType() == node.ELEMENT_NODE) {
            //u.p("close tag: " + node);
            writer.print("</"+node.getNodeName()+">\n");
        }
    }

    /**
     *  Description of the Method
     *
     *@param  node  Description of the Parameter
     *@return       Description of the Return Value
     */
    public static String text(Node node) {
        return text_child(node);
    }

    public static String textChild(Element node, String name) {
        return text_child(child(node,name));
    }
    public static String text_child(Node node) {
        if(node == null) {
            return "";
        }
        NodeList nl = node.getChildNodes();
        Node item = nl.item(0);
        if(item == null) {
            return "";
        }
        return item.getNodeValue();
    }

    public static String getAtt(Element node, String name) {
        return node.getAttribute(name);
    }
    public static String getChildAtt(Element node, String name, String attr) {
        Element child = child(node,name);
        if(child == null) { return null; }
        return getAtt(child,attr);
    }

    public static void transform(Document doc, java.net.URL xsl, OutputStream out,
        String pname, String pvalue)
        throws Exception {
        u.p("transforming with xsl: " + xsl);
        u.p("doc = " + doc);
        DOMSource xml_source = new DOMSource(doc);
        StreamSource xsl_source = new StreamSource(xsl.toString());
        Transformer transformer = TransformerFactory.newInstance().newTransformer(xsl_source);
        if (pname != null) {
            transformer.setParameter(pname, pvalue);
        }
        StreamResult result = new StreamResult(out);//pw);
        transformer.transform(xml_source, result);
    }

    public static void transform(Document doc, StreamSource xsl, StreamResult out,
            String pname, String pvalue) throws Exception {
        u.p("transforming with xsl: " + xsl);
        u.p("doc = " + doc);
        DOMSource xml_source = new DOMSource(doc);
        Transformer transformer = TransformerFactory.newInstance().newTransformer(xsl);
        if (pname != null) {
            transformer.setParameter(pname, pvalue);
        }
        transformer.transform(xml_source, out);
    }

    public static void transform(Document doc, InputStream xsl, OutputStream out, String pname, String pvalue) throws Exception {
        StreamSource xsl_source = new StreamSource(xsl);
        transform(doc,xsl_source,new StreamResult(out),pname,pvalue);
        //StreamResult result = new StreamResult(out);//pw);
    }

    public static void transform(Document doc, String xsl_filename, java.io.OutputStream out, String pname, String pvalue) throws Exception {
        transform(doc,new FileInputStream(xsl_filename),out,pname,pvalue);

    }

    public static void transform(Document doc, String xsl_filename, java.io.Writer out) throws Exception {
        transform(doc, new StreamSource(new FileInputStream(xsl_filename)), new StreamResult(out),null,null);
    }
    public static void transform(Document doc, String xsl_filename, java.io.OutputStream out) throws Exception {
        transform(doc, xsl_filename, out, null, null);
    }
    
    
    
    /* pseudo xpath stuff */
    
    public static String path(Node node, String xpath) {
        List paths = breakPath(xpath);
        for(int i=0; i<paths.size(); i++) {
            String sec = (String) paths.get(i);
            //u.p("testing: " + sec);
            //u.p("node = " + node.getNodeName());
            
            if(sec.equals("text()")) {
                u.p("returning text(): " + node.getFirstChild().getNodeValue());
                return node.getFirstChild().getNodeValue();
            }
            
            NodeList nodes = node.getChildNodes();
            for(int j=0; j<nodes.getLength(); j++) {
                Node child = nodes.item(j);
                if(child.getNodeName().equals(sec)) {
                    //u.p("matched: " + sec);
                    node = child;
                    break;
                }
            }
        }
        return null;
    }
    public static String[] paths(Node node, String xpath) {
        List paths = breakPath(xpath);
        return null;
    }
    
    private static List breakPath(String path) {
        path = path.trim();
        List list = new ArrayList();
        // trim starting / if present
        if(path.startsWith("/")) {
            path = path.substring(1,path.length());
        }
        //u.p("breaking: " + path);
        // pull out each section between the /'s
        while(true) {
            //u.p("path = " + path);
            int n = path.indexOf("/");
            if(n < 0) { 
                list.add(path);
                break; 
            }
            String sec = path.substring(0,n);
            //u.p("found: " + sec);
            //u.p("n = " + n);
            list.add(sec);
            path = path.substring(n+1,path.length());
        }
        //u.p("returning: " + list);
        return list;
    }

}

    class MyErrorHandler implements ErrorHandler {
            public Exception err;
            public void error(SAXParseException exception) {
                u.p("MyErrorHandler: error:");
                u.p(exception);
                err = exception;
            }

            public void fatalError(SAXParseException exception) {
                u.p("MyErrorHandler: fatal error:");
                u.p(exception);
                err = exception;
                u.p("set exception");
            }

            public void warning(SAXParseException exception) {
                u.p("MyErrorHandler: warning:");
                u.p(exception);
                //err = exception;
            }
        }

