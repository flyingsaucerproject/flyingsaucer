package tests.public_apis.css.properties.factory;

import java.awt.*;
import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.sheet.factory.BorderColorPropertyDeclarationFactory;
import org.xhtmlrenderer.css.sheet.factory.PropertyDeclarationFactory;


/**
 * border-color (-top, -right, -bottom, -left) property assignment tests.
 *
 * @author   Patrick Wright
 */
public class BorderColorPropertyExplosionTest extends AbstractPropertyExplosionTest {
    /**
     * Constructor for the BorderColorPropertyExplosionTest object
     *
     * @param name  PARAM
     */
    public BorderColorPropertyExplosionTest( String name ) {
        super( name );
    }

    /**
     * Returns a new PropertyDeclarationFactory that we are testing against.
     *
     * @return   A new PropertyDeclarationFactory
     */
    protected PropertyDeclarationFactory newPropertyDeclarationFactory() {
        return BorderColorPropertyDeclarationFactory.instance();
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
        testVals = new HashMap();
        testVals.put(CSSName.BORDER_COLOR_TOP, Color.BLACK);
        testVals.put(CSSName.BORDER_COLOR_BOTTOM, Color.BLACK);
        testVals.put(CSSName.BORDER_COLOR_RIGHT, Color.BLACK);
        testVals.put(CSSName.BORDER_COLOR_LEFT, Color.BLACK);
        temp.put( "p#OneToFour", new Object[]{"{ border-color: black; }", testVals} );

        testVals = new HashMap();
        Color aqua = Color.decode("#00ffff");
        Color blue = Color.decode("#0000ff");
        testVals.put(CSSName.BORDER_COLOR_TOP, aqua);
        testVals.put(CSSName.BORDER_COLOR_BOTTOM, aqua);
        testVals.put(CSSName.BORDER_COLOR_RIGHT, blue);
        testVals.put(CSSName.BORDER_COLOR_LEFT, blue);
        temp.put( "p#TwoToFour", new Object[]{"{ border-color: aqua blue; }", testVals} );

        testVals = new HashMap();
        Color fuchsia = Color.decode("#ff00ff");
        Color gray = Color.decode("#808080");
        Color green = Color.decode("#008000");
        testVals.put(CSSName.BORDER_COLOR_TOP, fuchsia);
        testVals.put(CSSName.BORDER_COLOR_RIGHT, gray);
        testVals.put(CSSName.BORDER_COLOR_LEFT, gray);
        testVals.put(CSSName.BORDER_COLOR_BOTTOM, green);
        temp.put( "p#ThreeToFour", new Object[]{"{ border-color: fuchsia gray green; }", testVals} );

        testVals = new HashMap();
        Color lime = Color.decode("#00ff00");
        Color maroon = Color.decode("#800000");
        Color navy = Color.decode("#000080");
        Color olive = Color.decode("#808000");
        testVals.put(CSSName.BORDER_COLOR_TOP, lime);
        testVals.put(CSSName.BORDER_COLOR_RIGHT, maroon);
        testVals.put(CSSName.BORDER_COLOR_BOTTOM, navy);
        testVals.put(CSSName.BORDER_COLOR_LEFT, olive);
        temp.put( "p#FourToFour", new Object[]{"{ border-color: lime maroon navy olive; }", testVals} );
        return temp;
    }

    /**
     * A unit test suite for JUnit
     *
     * @return   The test suite
     */
    public static Test suite() {
        return new TestSuite( BorderColorPropertyExplosionTest.class );
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2005/01/24 14:32:29  pdoubleya
 * Cleaned imports, removed references to FSCSSTestCase.
 *
 * Revision 1.1  2005/01/24 14:26:45  pdoubleya
 * Added to CVS.
 *
 *
 */