package tests.public_apis.css.properties.factory;

import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.sheet.factory.BackgroundPropertyDeclarationFactory;
import org.xhtmlrenderer.css.sheet.factory.PropertyDeclarationFactory;

/**
 * CSS "background" shorthand property assignment tests.
 *
 * @author   Patrick Wright
 */
public class BackgroundPropertyExplosionTest extends AbstractPropertyExplosionTest {
    /**
     * Constructor for the BackgroundPropertyExplosionTest object
     *
     * @param name  PARAM
     */
    public BackgroundPropertyExplosionTest( String name ) {
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
        return BackgroundPropertyDeclarationFactory.instance();
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

        // for this first test we vary the order of the background elements
        // using the same test vals map in each case. we use the superclass
        // appendTestPermutations() which adds all permutations on the input array
        // using a permutations algorithm.
        //
        // feel free to add other variations, where defaults are normally expected
        // if you leave a shorthand out.
        testVals = new HashMap();
        testVals.put(CSSName.BACKGROUND_COLOR, "#808080");
        testVals.put(CSSName.BACKGROUND_IMAGE, "url(chess.png)");
        testVals.put(CSSName.BACKGROUND_REPEAT, "repeat");
        testVals.put(CSSName.BACKGROUND_ATTACHMENT, "fixed");
        testVals.put(CSSName.BACKGROUND_POSITION, "50% 50%"); // note that spec treats a single len as a horiz value with 50% height
        appendTestPermutations(temp,
                               "p#FullBackground",
                               CSSName.BACKGROUND_SHORTHAND,
                               new String[]{"url(\"chess.png\")", "gray", "50%", "repeat", "fixed"},
                               testVals);

        testVals = new HashMap(testVals);
        testVals.put(CSSName.BACKGROUND_POSITION, "100% 0%");
        appendTestPermutations(temp,
                               "p#FullBackgroundRT",
                               CSSName.BACKGROUND_SHORTHAND,
                               new String[]{"url(\"chess.png\")", "gray", "right top", "repeat", "fixed"},
                               testVals);

        testVals = new HashMap();
        testVals.put(CSSName.BACKGROUND_POSITION, "50% 50%");
        temp.put( "p#BGPositions", new Object[]{"{ background: 50%; }", testVals});

        testVals = new HashMap();
        testVals.put(CSSName.BACKGROUND_POSITION, "0% 0%");
        temp.put( "p#BGPosTopLeft", new Object[]{"{ background: top left; }", testVals});

        return temp;
    }

    /**
     * A unit test suite for JUnit
     *
     * @return   The test suite
     */
    public static Test suite() {
        return new TestSuite( BackgroundPropertyExplosionTest.class );
    }
} // end class

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


