package tests.public_apis.css.properties.factory;

import java.awt.Color;
import java.io.*;
import java.util.*;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import com.steadystate.css.parser.CSSOMParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.factory.PropertyDeclarationFactory;
import org.xhtmlrenderer.css.sheet.factory.BorderSidePropertyDeclarationFactory;
import org.xhtmlrenderer.util.XRRuntimeException;


/**
 * border-side (-top, -right, -bottom, -left) property assignment tests.
 *
 * @author   Patrick Wright
 */
public class BorderSidePropertyExplosionTest extends AbstractPropertyExplosionTest {
    /**
     * Constructor for the FSCssTestCase object
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
        testVals.put( CSSName.BORDER_COLOR_TOP, Color.BLACK );
        testVals.put( CSSName.BORDER_WIDTH_TOP, "1pt" );
        testVals.put( CSSName.BORDER_STYLE_TOP, "solid" );
        appendTestPermutations(temp,
                               "p#BorderTopAll",
                               CSSName.BORDER_TOP_SHORTHAND,
                               new String[]{"black", "1pt", "solid"},
                               testVals);

        testVals = new HashMap();
        testVals.put( CSSName.BORDER_COLOR_RIGHT, Color.BLACK );
        testVals.put( CSSName.BORDER_WIDTH_RIGHT, "1pt" );
        testVals.put( CSSName.BORDER_STYLE_RIGHT, "solid" );
        appendTestPermutations(temp,
                               "p#BorderRightAll",
                               CSSName.BORDER_RIGHT_SHORTHAND,
                               new String[]{"black", "1pt", "solid"},
                               testVals);

        testVals = new HashMap();
        testVals.put( CSSName.BORDER_COLOR_BOTTOM, Color.BLACK );
        testVals.put( CSSName.BORDER_WIDTH_BOTTOM, "1pt" );
        testVals.put( CSSName.BORDER_STYLE_BOTTOM, "solid" );
        appendTestPermutations(temp,
                               "p#BorderBottomAll",
                               CSSName.BORDER_BOTTOM_SHORTHAND,
                               new String[]{"black", "1pt", "solid"},
                               testVals);

        testVals = new HashMap();
        testVals.put( CSSName.BORDER_COLOR_LEFT, Color.BLACK );
        testVals.put( CSSName.BORDER_WIDTH_LEFT, "1pt" );
        testVals.put( CSSName.BORDER_STYLE_LEFT, "solid" );
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
 * Revision 1.1  2005/01/24 14:26:46  pdoubleya
 * Added to CVS.
 *
 *
 */

