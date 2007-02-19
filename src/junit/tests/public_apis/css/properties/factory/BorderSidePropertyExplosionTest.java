package tests.public_apis.css.properties.factory;

import java.awt.*;
import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.sheet.factory.BorderSidePropertyDeclarationFactory;
import org.xhtmlrenderer.css.sheet.factory.PropertyDeclarationFactory;


/**
 * border-side (-top, -right, -bottom, -left) property assignment tests.
 *
 * @author   Patrick Wright
 */
public class BorderSidePropertyExplosionTest extends AbstractPropertyExplosionTest {
    /**
     * Constructor for the BorderSidePropertyExplosionTest object
     *
     * @param name  PARAM
     */
    public BorderSidePropertyExplosionTest( String name ) {
        super( name );
    }

    /**
     * Returns a new PropertyDeclarationFactory that we are testing against.
     *
     * @return   A new PropertyDeclarationFactory
     */
    protected PropertyDeclarationFactory newPropertyDeclarationFactory() {
        return BorderSidePropertyDeclarationFactory.instance();
    }

    /**
    * Returns the map of tests to run; see {@link AbstractPropertyExplosionTest#buildTestsMap()}.
     *
     * @return   see desc.
     */
    protected Map buildTestsMap() {
        Map temp = new HashMap();
        Map testVals = null;

        // careful about testing for transparent--because it wont
        // decode into a Color instance with Color.decode()--the
        // string is left as 'transparent' in our normalization and
        // only converted to Color(0,0,0,0) when referenced (PWW 19-11-04)

        // these selectors just vary the order of values, but props are
        // the same, so we use the same comparison values in a Map
        testVals = new HashMap();
        testVals.put( CSSName.BORDER_TOP_COLOR, "black" );
        testVals.put( CSSName.BORDER_TOP_WIDTH, "1pt" );
        testVals.put( CSSName.BORDER_TOP_STYLE, "solid" );
        appendTestPermutations(temp,
                               "p#BorderTopAll",
                               CSSName.BORDER_TOP_SHORTHAND,
                               new String[]{"black", "1pt", "solid"},
                               testVals);

        testVals = new HashMap();
        testVals.put( CSSName.BORDER_RIGHT_COLOR, "black" );
        testVals.put( CSSName.BORDER_RIGHT_WIDTH, "1pt" );
        testVals.put( CSSName.BORDER_RIGHT_STYLE, "solid" );
        appendTestPermutations(temp,
                               "p#BorderRightAll",
                               CSSName.BORDER_RIGHT_SHORTHAND,
                               new String[]{"black", "1pt", "solid"},
                               testVals);

        testVals = new HashMap();
        testVals.put( CSSName.BORDER_BOTTOM_COLOR, "black" );
        testVals.put( CSSName.BORDER_BOTTOM_WIDTH, "1pt" );
        testVals.put( CSSName.BORDER_BOTTOM_STYLE, "solid" );
        appendTestPermutations(temp,
                               "p#BorderBottomAll",
                               CSSName.BORDER_BOTTOM_SHORTHAND,
                               new String[]{"black", "1pt", "solid"},
                               testVals);

        testVals = new HashMap();
        testVals.put( CSSName.BORDER_LEFT_COLOR, "black" );
        testVals.put( CSSName.BORDER_LEFT_WIDTH, "1pt" );
        testVals.put( CSSName.BORDER_LEFT_STYLE, "solid" );
        appendTestPermutations(temp,
                               "p#BorderRightAll",
                               CSSName.BORDER_LEFT_SHORTHAND,
                               new String[]{"black", "1pt", "solid"},
                               testVals);

        return temp;
    }

    /**
     * A unit test suite for JUnit
     *
     * @return   The test suite
     */
    public static Test suite() {
        return new TestSuite( BorderSidePropertyExplosionTest.class );
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2007/02/19 14:54:18  peterbrant
 * Integrate new CSS parser
 *
 * Revision 1.3  2005/01/29 16:00:42  pdoubleya
 * No longer use identifier-replaced values on PDs.
 *
 * Revision 1.2  2005/01/24 14:32:29  pdoubleya
 * Cleaned imports, removed references to FSCSSTestCase.
 *
 * Revision 1.1  2005/01/24 14:26:46  pdoubleya
 * Added to CVS.
 *
 *
 */

