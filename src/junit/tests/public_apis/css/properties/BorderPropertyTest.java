package tests.public_apis;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Element;

import org.xhtmlrenderer.css.Border;
import tests.public_apis.css.FSCssTestCase;

/**
 * Border property assignment tests.
 *
 * @author   Patrick Wright
 */
public class BorderPropertyTest extends FSCssTestCase {
    /**
     * Constructor for the FSCssTestCase object
     *
     * @param name  PARAM
     */
    public BorderPropertyTest( String name ) {
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

    /** A unit test for JUnit */
    public void testWidthExpandFourFromOne() {
        String bodyP = "//body/p[@id=1]";
        Element pElem = lookupElement(bodyP);
        Border border = _ctx.getStyleReference().getBorderWidth(pElem);
        assertEquals("border top width should be 1px", 1, border.top);
        assertEquals("border bottom width should be 1px", 1, border.bottom);
        assertEquals("border left width should be 1px", 1, border.left);
        assertEquals("border right width should be 1px", 1, border.right);
    }

    /** A unit test for JUnit */
    public void testWidthExpandFourFromTwo() {
        String bodyP = "//body/p[@id=2]";
        Element pElem = lookupElement(bodyP);
        Border border = _ctx.getStyleReference().getBorderWidth(pElem);
        assertEquals("border top width should be 1px", 1, border.top);
        assertEquals("border bottom width should be 1px", 1, border.bottom);
        assertEquals("border left width should be 2px", 2, border.left);
        assertEquals("border right width should be 2px", 2, border.right);
    }

    /** A unit test for JUnit */
    public void testWidthExpandFourFromThree() {
        String bodyP = "//body/p[@id=3]";
        Element pElem = lookupElement(bodyP);
        Border border = _ctx.getStyleReference().getBorderWidth(pElem);
        assertEquals("border top width should be 3px", 3, border.top);
        assertEquals("border right width should be 4px", 4, border.right);
        assertEquals("border bottom width should be 5px", 5, border.bottom);
        assertEquals("border left width should be 4px", 4, border.left);
    }

    /** A unit test for JUnit */
    public void testWidthExplicitFourSides() {
        String bodyP = "//body/p[@id=4]";
        Element pElem = lookupElement(bodyP);
        Border border = _ctx.getStyleReference().getBorderWidth(pElem);
        assertEquals("border top width should be 6px", 6, border.top);
        assertEquals("border right width should be 7px", 7, border.right);
        assertEquals("border bottom width should be 8px", 8, border.bottom);
        assertEquals("border left width should be 9px", 9, border.left);
    }

    protected String getDocumentText() {
        return 
        "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">" +
        "<head>" +
        "<style type=\"text/css\">" +
        "   p#1 { border-width: 1px; }" +
        "   p#2 { border-width: 1px 2px; }" +
        "   p#3 { border-width: 3px 4px 5px; }" +
        "   p#4 { border-width: 6px 7px 8px 9px; }" +
        "</style>" +
        "</head>" +
        "<body>" +
        "<p id=\"1\">width from 1</p>" +
        "<p id=\"2\">width from 2</p>" +
        "<p id=\"3\">width from 3</p>" +
        "<p id=\"4\">width from 4</p>" +
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
        return new TestSuite( BorderPropertyTest.class );
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2004/11/16 15:18:39  pdoubleya
 * Added to CVS.
 *
 *
*/


