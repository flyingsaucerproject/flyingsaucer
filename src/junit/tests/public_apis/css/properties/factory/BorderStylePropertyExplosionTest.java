package tests.public_apis.css.properties.factory;

import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.sheet.factory.BorderStylePropertyDeclarationFactory;
import org.xhtmlrenderer.css.sheet.factory.PropertyDeclarationFactory;

/**
 * Border property assignment tests.
 *
 * @author   Patrick Wright
 */
public class BorderStylePropertyExplosionTest extends AbstractPropertyExplosionTest {
    /**
     * Constructor for the BorderStylePropertyExplosionTest object
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
        testVals.put(CSSName.BORDER_TOP_STYLE, "none");
        testVals.put(CSSName.BORDER_BOTTOM_STYLE, "none");
        testVals.put(CSSName.BORDER_RIGHT_STYLE, "none");
        testVals.put(CSSName.BORDER_LEFT_STYLE, "none");
        temp.put( "p#OneToFour", new Object[]{"{ border-style: none; }", testVals} );

        testVals = new HashMap();
        testVals.put(CSSName.BORDER_TOP_STYLE, "hidden");
        testVals.put(CSSName.BORDER_BOTTOM_STYLE, "hidden");
        testVals.put(CSSName.BORDER_RIGHT_STYLE, "dotted");
        testVals.put(CSSName.BORDER_LEFT_STYLE, "dotted");
        temp.put( "p#TwoToFour", new Object[]{"{ border-style: hidden dotted; }", testVals} );

        testVals = new HashMap();
        testVals.put(CSSName.BORDER_TOP_STYLE, "dashed");
        testVals.put(CSSName.BORDER_RIGHT_STYLE, "solid");
        testVals.put(CSSName.BORDER_LEFT_STYLE, "solid");
        testVals.put(CSSName.BORDER_BOTTOM_STYLE, "double");
        temp.put( "p#ThreeToFour", new Object[]{"{ border-style: dashed solid double; }", testVals} );

        testVals = new HashMap();
        testVals.put(CSSName.BORDER_TOP_STYLE, "groove");
        testVals.put(CSSName.BORDER_RIGHT_STYLE, "ridge");
        testVals.put(CSSName.BORDER_BOTTOM_STYLE, "inset");
        testVals.put(CSSName.BORDER_LEFT_STYLE, "outset");
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
 * Revision 1.3  2007/02/19 14:54:18  peterbrant
 * Integrate new CSS parser
 *
 * Revision 1.2  2005/01/24 14:32:30  pdoubleya
 * Cleaned imports, removed references to FSCSSTestCase.
 *
 * Revision 1.1  2005/01/24 14:26:46  pdoubleya
 * Added to CVS.
 *

 *

*/


