package tests.public_apis.css.properties.factory;

import java.awt.*;
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
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.css.sheet.factory.PropertyDeclarationFactory;
import org.xhtmlrenderer.util.PermutationGenerator;
import org.xhtmlrenderer.util.XRRuntimeException;


/**
 * Abstract test class for property explosion tests. It parses a stylesheet and
 * lets you test the properties that are then read from the sheet. Note, this
 * does <b>not</b> test against a complete XHTML document. There is no selector
 * matching test, for example. All we are doing is testing the ability of XR to
 * parse stylesheets into properties, above the basic SAC parsing that we depend
 * on otherwise. To do this, we test PropertyDeclarationFactories in their
 * ability to explode and clean properties read from the stylesheet. This class
 * simplifies the testing in that you just need to define the
 * PropertyDeclarationFactory instance you want to use, and the test cases. You
 * must define {@link #newPropertyDeclarationFactory()} and {@link
 * #buildTestsMap()} in your subclass, as well as the standard {@link #suite()}
 * method for {@link TestCase}. The test map matches a given CSS selector with
 * an associated property, which may be a shorthand property, and then
 * associates the expected properties that should be read when the input
 * property is parse. Samples are provided here, in method comments.
 *
 * @author   Patrick Wright
 */
public abstract class AbstractPropertyExplosionTest extends TestCase {
    /**
     * The SAC CSSStyleSheet we have after parsing our input test data. Used to
     * read all rules that were parsed from the test input.
     */
    private CSSStyleSheet cssStyleSheet;

    /** The SAC parser used to load the rules.  */
    private CSSOMParser parser = new CSSOMParser();

    /**
     * The PropertyDeclarationFactory instance we use for exploding our
     * properties
     */
    private PropertyDeclarationFactory factory;

    /**
     * Our Map of test data, used to build the parsable test input. The key is
     * the selector, the value is a two-dimensional Object array, first element
     * the property assignment, second element a Map of exploded
     * property/values. See the buildTestsMap() for examples.
     */
    private Map testsMap;
    /** Description of the Field */
    private boolean showLog;

    /**
     * Constructor for the AbstractPropertyExplosionTest object
     *
     * @param name  PARAM
     */
    public AbstractPropertyExplosionTest( String name ) {
        this( name, false );
    }

    /**
     * Constructor for the AbstractPropertyExplosionTest object
     *
     * @param name     PARAM
     * @param showLog  PARAM
     */
    public AbstractPropertyExplosionTest( String name, boolean showLog ) {
        super( name );
        this.showLog = showLog;
    }

    /**
     * The main test. Tests explosion of all test data in the testsMap map. This
     * is purely generic: we walk through the parsed selectors, and compare with
     * the exploded values we expect to see, from the testsMap Map.
     */
    public void testExplode() {
        log( "@@@ Executing testExplode() in " + this.getClass().getName() );

        String sel = null;
        CSSStyleDeclaration decl = null;
        String propName = null;
        int origin = StylesheetInfo.USER_AGENT;
        Map testMap = null;

        // walk through all parsed rules
        CSSRuleList rlist = cssStyleSheet.getCssRules();
        for ( int i = 0; i < rlist.getLength(); i++ ) {
            CSSStyleRule rule = (CSSStyleRule)rlist.item( i );

            // current selector, declaration, and property name
            sel = rule.getSelectorText();
            decl = rule.getStyle();
            propName = decl.item( 0 );
            CSSName cssName = CSSName.getByPropertyName( propName );

            // get the comparison data using the current selector
            // testMap is a Map of exploded property name to expected value
            Object dat[] = (Object[])testsMap.get( sel );
            testMap = (Map)dat[1];

            // explode the property using the property factory initialized above
            Iterator iter = factory.buildDeclarations( decl, cssName, origin );
            int explodedCnt = 0;
            while ( iter.hasNext() ) {
                PropertyDeclaration pd = (PropertyDeclaration)iter.next();
                // DEBUG
                log( "   " + sel + "=> " + pd );

                // get the expected value
                Object expected = testMap.get( pd.getCSSName() );
                Object actual = null;
                actual = pd.getValue().toString();
                
                // compare actual with expected
                assertEquals( sel + " ::Value for " + pd.getPropertyName() + " should be " +
                        expected, expected, actual );
                explodedCnt++;
            }
            int expectedCnt = testMap.size();
            assertTrue( "Expected " + expectedCnt + " properties from shorthand, but got only " + explodedCnt +
                    " " + sel, expectedCnt == explodedCnt );

        }
    }

    /** The teardown method for JUnit  */
    protected void tearDown() { }

    /**
     * Returns a new PropertyDeclarationFactory that we are testing against.
     * Example: <pre>
     *   protected PropertyDeclarationFactory newPropertyDeclarationFactory() {
     *     return BorderSidePropertyDeclarationFactory.instance();
     *   }
     * </pre>
     *
     * @return   A new PropertyDeclarationFactory
     */
    protected abstract PropertyDeclarationFactory newPropertyDeclarationFactory();

    /**
     * Returns an initialized Map with test cases. The key is the selector, the
     * value is a two-dimensional Object array, first element the property
     * assignment, second element a Map of exploded property/values. Example:
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
     * @return   Initialized test map; see desc.
     */
    protected abstract Map buildTestsMap();

    /**
     * This appends a set of tests to a test map you are building. You supply
     * the tests Map, and the array of elements you want permutations on. All
     * possible permutations are added. Each permutation is then ready to be
     * tested against the test values Map which you supply. You can use this for
     * some shorthand property tests where the shorthand properties are not
     * order-dependent. For example, the meaning of the shorthand border
     * property is dependent on the order of the values assigned to it. The
     * meaning of font, and background, are not. To be able to test parsing for
     * those properties against a variation of inputs, this can save some time.
     * <p/>
     *
     * Likely you would call this within {@link #buildTestsMap()} to add
     * permutations to your tests. <p/>
     *
     * Here is an example of how to use it: <pre>
     * Map tests = new HashMap();
     * Map testVals = new TreeMap();
     * testVals.put(CSSName.BACKGROUND_COLOR, "#808080");
     * testVals.put(CSSName.BACKGROUND_IMAGE, "url(chess.png)");
     * testVals.put(CSSName.BACKGROUND_REPEAT, "repeat");
     * testVals.put(CSSName.BACKGROUND_ATTACHMENT, "fixed");
     * testVals.put(CSSName.BACKGROUND_POSITION, "50%");
     * appendTestPermutations(tests,
     * "p#FullBackground",
     * CSSName.BACKGROUND_SHORTHAND,
     * new String[]{"url(\"chess.png\")", "gray", "50%", "repeat", "fixed"},
     * testVals);
     * </pre> The <code>tests</code> map will then have all the permutations on
     * inputs for the array we gave.
     *
     * @param testsMap    The Map of tests you are building (see class JavaDoc).
     * @param selector    The *prefix* for the selector you want to use. An
     *      integer counter will be appended to this selector for each
     *      permutation generated. Supply a decent prefix if you want meaningful
     *      logging.
     * @param cssName     The CSS property shorthand name you want to use. See
     *      {@link org.xhtmlrenderer.css.constants.CSSName}.
     * @param elem        Array of elements which form the basis of the
     *      permutations. The number of permutations is the factorial of
     *      elem.length, so be careful: over 20, you have reached the limit of a
     *      long datatype (we can handle a little bit more, but the tests will
     *      run awhile).
     * @param testValues  The Map of values to test against. This is a Map of
     *      CSS property names that are to be expanded from the shorthand
     *      property, assigned to the value you expect to be parsed from the
     */
    protected final void appendTestPermutations( Map testsMap, String selector, CSSName cssName, String elem[], Map testValues ) {
        PermutationGenerator pg = new PermutationGenerator( elem.length );

        int[] indices;
        StringBuffer permutation;
        int cnt = 0;
        while ( pg.hasMore() ) {
            permutation = new StringBuffer( "{ " + cssName + ":" );
            indices = pg.getNext();
            for ( int i = 0; i < indices.length; i++ ) {
                permutation.append( " " + elem[indices[i]] );
            }
            permutation.append( "; }" );
            testsMap.put( selector + "pmt" + cnt, new Object[]{permutation, testValues} );
            cnt++;
        }
    }

    /** The JUnit setup method  */
    protected void setUp() {
        factory = newPropertyDeclarationFactory();
        InputSource is = new InputSource( new StringReader( getCSSText() ) );
        try {
            cssStyleSheet = parser.parseStyleSheet( is );
        } catch ( java.io.IOException e ) {
            throw new XRRuntimeException( "IOException on parsing style seet from a Reader; don't know the URI.", e );
        }
    }

    /**
     * Returns a CSS stylesheet built from the testsMap map. This is called
     * during {@link #setUp()}. The tests Map is built in {@link
     * #buildTestsMap()}.
     *
     * @return   A valid, parseable CSS string containing selectors and property
     *      declarations.
     */
    protected String getCSSText() {
        this.testsMap = buildTestsMap();

        // iterate over the keys in our tests, which are selectors
        Iterator iter = testsMap.keySet().iterator();
        StringBuffer sb = new StringBuffer();
        while ( iter.hasNext() ) {
            // for each selector, append the selector and the property assignment
            String selector = (String)iter.next();
            Object dat[] = (Object[])testsMap.get( selector );
            sb.append( selector ).append( " " ).append( dat[0] ).append( "\n" );
        }
        // DEBUG
        //log( "CSS:\n" + sb.toString() );
        return sb.toString();
    }

    /**
     * Workaround. XRLog seems to cause problems when executed via Ant within
     * jEdit, probably a treading problem or conflict
     *
     * @param msg  PARAM
     */
    private void log( String msg ) {
        if ( showLog ) {
            System.out.println( msg );
        }
    }

    /**
     * A unit test suite for JUnit. Define this in your subclass, example: <pre>
     *   public static Test suite() {
     *     return new TestSuite( MyFactoryPropertyExplosionTest.class );
     *   }
     * </pre>
     *
     * @return   The test suite
     */
    public static Test suite() {
        return new TestSuite( AbstractPropertyExplosionTest.class );
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2005/01/29 16:00:11  pdoubleya
 * No longer expect Color instance for color properties.
 *
 * Revision 1.3  2005/01/24 19:01:01  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.2  2005/01/24 14:32:29  pdoubleya
 * Cleaned imports, removed references to FSCSSTestCase.
 *
 * Revision 1.1  2005/01/24 14:26:45  pdoubleya
 * Added to CVS.
 *
 *
 */

