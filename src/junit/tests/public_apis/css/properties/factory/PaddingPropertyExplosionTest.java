package tests.public_apis.css.properties.factory;

import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.sheet.factory.PaddingPropertyDeclarationFactory;
import org.xhtmlrenderer.css.sheet.factory.PropertyDeclarationFactory;

/**
 * Padding property assignment tests.
 *
 * @author   Patrick Wright
 */
public class PaddingPropertyExplosionTest extends AbstractPropertyExplosionTest {
    /**
     * Constructor for the PaddingPropertyExplosionTest object
     *
     * @param name  PARAM
     */
    public PaddingPropertyExplosionTest( String name ) {
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
        return PaddingPropertyDeclarationFactory.instance();
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
        testVals.put(CSSName.PADDING_TOP, "1px");
        testVals.put(CSSName.PADDING_BOTTOM, "1px");
        testVals.put(CSSName.PADDING_RIGHT, "1px");
        testVals.put(CSSName.PADDING_LEFT, "1px");
        temp.put( "p#OneToFour", new Object[]{"{ padding: 1px; }", testVals} );

        testVals = new HashMap();
        testVals.put(CSSName.PADDING_TOP, "1px");
        testVals.put(CSSName.PADDING_BOTTOM, "1px");
        testVals.put(CSSName.PADDING_RIGHT, "2px");
        testVals.put(CSSName.PADDING_LEFT, "2px");
        temp.put( "p#TwoToFour", new Object[]{"{ padding: 1px 2px; }", testVals} );

        testVals = new HashMap();
        testVals.put(CSSName.PADDING_TOP, "1px");
        testVals.put(CSSName.PADDING_RIGHT, "2px");
        testVals.put(CSSName.PADDING_LEFT, "2px");
        testVals.put(CSSName.PADDING_BOTTOM, "3px");
        temp.put( "p#ThreeToFour", new Object[]{"{ padding: 1px 2px 3px; }", testVals} );

        testVals = new HashMap();
        testVals.put(CSSName.PADDING_TOP, "1px");
        testVals.put(CSSName.PADDING_RIGHT, "2px");
        testVals.put(CSSName.PADDING_BOTTOM, "3px");
        testVals.put(CSSName.PADDING_LEFT, "4px");
        temp.put( "p#FourToFour", new Object[]{"{ padding: 1px 2px 3px 4px; }", testVals} );

        return temp;
    }

    /**
     * A unit test suite for JUnit
     *
     * @return   The test suite
     */
    public static Test suite() {
        return new TestSuite( PaddingPropertyExplosionTest.class );
    }
} // end class

/*

 * $Id$

 *

 * $Log$
 * Revision 1.2  2005/01/24 14:32:31  pdoubleya
 * Cleaned imports, removed references to FSCSSTestCase.
 *
 * Revision 1.1  2005/01/24 14:26:48  pdoubleya
 * Added to CVS.
 *

 *

*/


