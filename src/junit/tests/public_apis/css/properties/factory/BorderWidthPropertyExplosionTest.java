package tests.public_apis.css.properties.factory;

import java.io.*;
import java.util.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.steadystate.css.parser.CSSOMParser;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.*;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;

import org.xhtmlrenderer.css.sheet.*;
import org.xhtmlrenderer.css.sheet.factory.*;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.util.*;
import tests.public_apis.css.FSCssTestCase;

/**
 * Border property assignment tests.
 *
 * @author   Patrick Wright
 */
public class BorderWidthPropertyExplosionTest extends AbstractPropertyExplosionTest {
    /**
     * Constructor for the FSCssTestCase object
     *
     * @param name  PARAM
     */
    public BorderWidthPropertyExplosionTest( String name ) {
        super( name );
    }
    
    /**
     * Returns a new PropertyDeclarationFactory that we are testing against.
     * Example: <pre>
     *   protected PropertyDeclarationFactory newPropertyDeclarationFactory() {
     *     return BorderSidePropertyDeclarationFactory.instance();
     *   }
     * </pre>
     *
     * @return A new PropertyDeclarationFactory
     */
    protected PropertyDeclarationFactory newPropertyDeclarationFactory() {
        return BorderWidthPropertyDeclarationFactory.instance();
    }

    /**
     * Returns an initialized Map with test cases. The key is the selector, the value is a two-dimensional Object array,
     * first element the property assignment, second element a Map of exploded property/values. Example:
     * <pre>
     *  Map temp = new HashMap();
     *  Map testVals = null;
     *  testVals = new HashMap();
     *  testVals.put( CSSName.BORDER_COLOR_TOP, Color.BLACK );
     *  testVals.put( CSSName.BORDER_WIDTH_TOP, "1pt" );
     *  testVals.put( CSSName.BORDER_STYLE_TOP, "solid" );
     *  temp.put( "p#BTAllV1", new Object[]{"{ border-top: black 1pt solid; }", testVals} );
     * </pre> You can define as many selectors and comparison cases as you like.
     *
     * @return Initialized test map; see desc.
     */
    protected Map buildTestsMap() {
        Map temp = new TreeMap();
        Map testVals = null;

        testVals = new HashMap();
        testVals.put(CSSName.BORDER_WIDTH_TOP, "1px");
        testVals.put(CSSName.BORDER_WIDTH_BOTTOM, "1px");
        testVals.put(CSSName.BORDER_WIDTH_RIGHT, "1px");
        testVals.put(CSSName.BORDER_WIDTH_LEFT, "1px");
        temp.put( "p#OneToFour", new Object[]{"{ border-width: 1px; }", testVals} );

        testVals = new HashMap();
        testVals.put(CSSName.BORDER_WIDTH_TOP, "1px");
        testVals.put(CSSName.BORDER_WIDTH_BOTTOM, "1px");
        testVals.put(CSSName.BORDER_WIDTH_RIGHT, "2px");
        testVals.put(CSSName.BORDER_WIDTH_LEFT, "2px");
        temp.put( "p#TwoToFour", new Object[]{"{ border-width: 1px 2px; }", testVals} );

        testVals = new HashMap();
        testVals.put(CSSName.BORDER_WIDTH_TOP, "1px");
        testVals.put(CSSName.BORDER_WIDTH_RIGHT, "2px");
        testVals.put(CSSName.BORDER_WIDTH_LEFT, "2px");
        testVals.put(CSSName.BORDER_WIDTH_BOTTOM, "3px");
        temp.put( "p#ThreeToFour", new Object[]{"{ border-width: 1px 2px 3px; }", testVals} );

        testVals = new HashMap();
        testVals.put(CSSName.BORDER_WIDTH_TOP, "1px");
        testVals.put(CSSName.BORDER_WIDTH_RIGHT, "2px");
        testVals.put(CSSName.BORDER_WIDTH_BOTTOM, "3px");
        testVals.put(CSSName.BORDER_WIDTH_LEFT, "4px");
        temp.put( "p#FourToFour", new Object[]{"{ border-width: 1px 2px 3px 4px; }", testVals} );
        return temp;
    }

    /**
     * A unit test suite for JUnit
     *
     * @return   The test suite
     */
    public static Test suite() {
        return new TestSuite( BorderWidthPropertyExplosionTest.class );
    }
} // end class

/*

 * $Id$

 *

 * $Log$
 * Revision 1.1  2005/01/24 14:26:46  pdoubleya
 * Added to CVS.
 *

 *

*/


