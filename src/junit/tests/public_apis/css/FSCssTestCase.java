package tests.public_apis.css;

import java.io.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.simple.XHTMLPanel;


/**
 * Simple example of using JUnit to test FS. The XHTML to test is
 * specified as a string in the getDocumentText() method.
 *
 * @author   Patrick Wright
 */
public abstract class FSCssTestCase extends TestCase {
    /** Description of the Field */
    protected XHTMLPanel panel;
    
    protected RenderingContext _ctx;
    
    protected Document _document;

    /**
     * Constructor for the FSCssTestCase object
     *
     * @param name  PARAM
     */
    public FSCssTestCase( String name ) {
        super( name );
    }

    /** The teardown method for JUnit */
    protected void tearDown() { }

    /** The JUnit setup method */
    protected void setUp() {
        try {
            panel = new XHTMLPanel();
            _ctx = panel.getRenderingContext();
            _document = loadTestDocument();
            panel.setDocument( _document, new File(".").toURL() );
        } catch ( Exception ex ) {
            throw new RuntimeException( "Can't setup panel for test.", ex );
        }
    }
    
    protected Element lookupElement(String elementXPath) {
        Element elem = null;
        try {
            Element root = _document.getDocumentElement();
            elem =
                    (Element)XPathAPI.selectSingleNode( root, elementXPath );
            if ( elem == null ) {
                throw new RuntimeException("During JUnit test, could not find element at path: '" + elementXPath + "'");
            }
        } catch (RuntimeException ex) { throw ex; 
        } catch ( Exception ex ) {
            throw new RuntimeException("During JUnit test, could not find element at path: '" + elementXPath + "'");
        }
        return elem;
    }
    
    private Document loadTestDocument() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        
        Document doc = builder.parse( new InputSource(new StringReader(getDocumentText())));
        return doc;
    }
    
    protected abstract String getDocumentText();

    /* sample
    public static void main( String args[] ) {
        junit.textui.TestRunner.run( suite() );
    }
    */
}

/*

 * $Id$

 *

 * $Log$
 * Revision 1.1  2004/11/16 15:18:38  pdoubleya
 * Added to CVS.
 *

 *

*/


