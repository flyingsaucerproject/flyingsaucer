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
public class BorderStylePropertyExplosionTest extends AbstractPropertyExplosionTest {
    /**
     * Constructor for the FSCssTestCase object
     *
     * @param name  PARAM
     */
    public BorderStylePropertyExplosionTest( String name ) {
        super( name );
    }
    
    protected void tearDown() {}

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
        return BorderStylePropertyDeclarationFactory.instance();
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
        Map testVals = new TreeMap();

        testVals = new HashMap();
        testVals.put(CSSName.BORDER_STYLE_TOP, "none");
        testVals.put(CSSName.BORDER_STYLE_BOTTOM, "none");
        testVals.put(CSSName.BORDER_STYLE_RIGHT, "none");
        testVals.put(CSSName.BORDER_STYLE_LEFT, "none");
        temp.put( "p#OneToFour", new Object[]{"{ border-style: none; }", testVals} );

        testVals = new HashMap();
        testVals.put(CSSName.BORDER_STYLE_TOP, "hidden");
        testVals.put(CSSName.BORDER_STYLE_BOTTOM, "hidden");
        testVals.put(CSSName.BORDER_STYLE_RIGHT, "dotted");
        testVals.put(CSSName.BORDER_STYLE_LEFT, "dotted");
        temp.put( "p#TwoToFour", new Object[]{"{ border-style: hidden dotted; }", testVals} );

        testVals = new HashMap();
        testVals.put(CSSName.BORDER_STYLE_TOP, "dashed");
        testVals.put(CSSName.BORDER_STYLE_RIGHT, "solid");
        testVals.put(CSSName.BORDER_STYLE_LEFT, "solid");
        testVals.put(CSSName.BORDER_STYLE_BOTTOM, "double");
        temp.put( "p#ThreeToFour", new Object[]{"{ border-style: dashed solid double; }", testVals} );

        testVals = new HashMap();
        testVals.put(CSSName.BORDER_STYLE_TOP, "groove");
        testVals.put(CSSName.BORDER_STYLE_RIGHT, "ridge");
        testVals.put(CSSName.BORDER_STYLE_BOTTOM, "inset");
        testVals.put(CSSName.BORDER_STYLE_LEFT, "outset");
        temp.put( "p#FourToFour", new Object[]{"{ border-style: groove ridge inset outset; }", testVals} );
        return temp;
    }

/* valid border styles from spec
* none
* hidden
* dotted
* dashed
* solid
* double
* groove
* ridge
* inset
* outset
*/

    /**
     * A unit test suite for JUnit
     *
     * @return   The test suite
     */
    public static Test suite() {
        return new TestSuite( BorderStylePropertyExplosionTest.class );
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


