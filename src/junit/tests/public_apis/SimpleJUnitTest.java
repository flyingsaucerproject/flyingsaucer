package tests.public_apis;

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

import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.simple.XHTMLPanel;


/**
 * Simple example of using JUnit to test FS. The XHTML to test is
 * specified as a string in the getDocumentText() method.
 *
 * @author   Patrick Wright
 */
public class SimpleJUnitTest extends TestCase {
    /** Description of the Field */
    private XHTMLPanel panel;
    
    private RenderingContext _ctx;
    
    private Document _document;

    /**
     * Constructor for the SimpleJUnitTest object
     *
     * @param name  PARAM
     */
    public SimpleJUnitTest( String name ) {
        super( name );
    }

    /** A unit test for JUnit */
    public void testRenderingContext() {
        assertTrue( "RenderingContext should be instantiated.", _ctx != null );
    }

    /** A unit test for JUnit */
    public void testElementExists() {
        String bodyP = "//body/p[@id=1]";
        Element pElem = lookupElement(bodyP);
        assertNotNull("Failed to find element " + bodyP, pElem);
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
    
    private Element lookupElement(String elementXPath) {
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
    
    private String getDocumentText() {
        return 
        "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">" +
        "<head>" +
        "<style type=\"text/css\">" +
        "   p { border: 1px solid black; }" +
        "</style>" +
        "</head>" +
        "<body>" +
        "<p id=\"1\">this is a sentence</p>" +
        "<p>this is a sentence</p>" +
        "</body>" +
        "</html>";
    }

    /**
     * A unit test suite for JUnit
     *
     * @return   The test suite
     */
    public static Test suite() {
        return new TestSuite( SimpleJUnitTest.class );
    }

    /**
     * Description of the Method
     *
     * @param args  PARAM
     */
    public static void main( String args[] ) {
        junit.textui.TestRunner.run( suite() );
    }
}

/*

 * $Id$

 *

 * $Log$
 * Revision 1.1  2004/11/16 14:36:44  pdoubleya
 * Added to CVS.
 *

 *

*/


