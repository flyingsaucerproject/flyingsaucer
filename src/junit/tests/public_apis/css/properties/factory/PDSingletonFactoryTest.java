package tests.public_apis.css.properties.factory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.factory.*;


/**
 * Border property assignment tests.
 *
 * @author   Patrick Wright
 */
public class PDSingletonFactoryTest extends TestCase {
    /**
     * Constructor for the PDSingletonFactoryTest object
     *
     * @param name  PARAM
     */
    public PDSingletonFactoryTest( String name ) {
        super( name );
    }

    /** A unit test for JUnit */
    public void testDefaultSingletonLookup() {
        assertEquals( "Non-existent properties use DefaultPropertyDeclarationFactory",
                    DefaultPropertyDeclarationFactory.instance(),
                    PropertyDeclaration.newFactory( "NOT_A_PROPERTY" ) );
    }

    /** A unit test for JUnit */
    public void testBackgroundSingletonLookup() {
        PropertyDeclarationFactory inst = BackgroundPropertyDeclarationFactory.instance();
        assertEquals( "'background' property uses BackgroundPropertyDeclarationFactory",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.BACKGROUND_SHORTHAND ) );

        assertEquals( "BackgroundPropertyDeclarationFactory.instance() should return a singleton",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.BACKGROUND_SHORTHAND ) );
    }

    /** A unit test for JUnit */
    public void testBorderColorSingletonLookup() {
        PropertyDeclarationFactory inst = BorderColorPropertyDeclarationFactory.instance();
        assertEquals( "'border-color' property uses BorderColorPropertyDeclarationFactory",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.BORDER_COLOR_SHORTHAND ) );

        assertEquals( "BorderColorPropertyDeclarationFactory.instance() should return a singleton",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.BORDER_COLOR_SHORTHAND ) );
    }

    /** A unit test for JUnit */
    public void testBorderSideSingletonLookup() {
        PropertyDeclarationFactory inst = BorderSidePropertyDeclarationFactory.instance();
        assertEquals( "'border-top' property uses BorderSidePropertyDeclarationFactory",
                    BorderSidePropertyDeclarationFactory.instance(),
                    PropertyDeclaration.newFactory( CSSName.BORDER_TOP_SHORTHAND ) );

        assertEquals( "BorderSidePropertyDeclarationFactory.instance() should return a singleton",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.BORDER_TOP_SHORTHAND ) );

        assertEquals( "'border-right' property uses BorderSidePropertyDeclarationFactory",
                    BorderSidePropertyDeclarationFactory.instance(),
                    PropertyDeclaration.newFactory( CSSName.BORDER_RIGHT_SHORTHAND ) );

        assertEquals( "BorderSidePropertyDeclarationFactory.instance() should return a singleton",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.BORDER_RIGHT_SHORTHAND ) );

        assertEquals( "'border-bottom' property uses BorderSidePropertyDeclarationFactory",
                    BorderSidePropertyDeclarationFactory.instance(),
                    PropertyDeclaration.newFactory( CSSName.BORDER_BOTTOM_SHORTHAND ) );

        assertEquals( "BorderSidePropertyDeclarationFactory.instance() should return a singleton",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.BORDER_BOTTOM_SHORTHAND ) );

        assertEquals( "'border-left' property uses BorderSidePropertyDeclarationFactory",
                    BorderSidePropertyDeclarationFactory.instance(),
                    PropertyDeclaration.newFactory( CSSName.BORDER_LEFT_SHORTHAND ) );

        assertEquals( "BorderSidePropertyDeclarationFactory.instance() should return a singleton",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.BORDER_LEFT_SHORTHAND ) );
    }

    /** A unit test for JUnit */
    public void testBorderStyleSingletonLookup() {
        PropertyDeclarationFactory inst = BorderStylePropertyDeclarationFactory.instance();
        assertEquals( "'border-style' property uses BorderStylePropertyDeclarationFactory",
                    BorderStylePropertyDeclarationFactory.instance(),
                    PropertyDeclaration.newFactory( CSSName.BORDER_STYLE_SHORTHAND ) );

        assertEquals( "BorderStylePropertyDeclarationFactory.instance() should return a singleton",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.BORDER_STYLE_SHORTHAND ) );
    }

    /** A unit test for JUnit */
    public void testBorderWidthSingletonLookup() {
        PropertyDeclarationFactory inst = BorderWidthPropertyDeclarationFactory.instance();
        assertEquals( "'border-width' property uses BorderWidthPropertyDeclarationFactory",
                    BorderWidthPropertyDeclarationFactory.instance(),
                    PropertyDeclaration.newFactory( CSSName.BORDER_WIDTH_SHORTHAND ) );

        assertEquals( "BorderWidthPropertyDeclarationFactory.instance() should return a singleton",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.BORDER_WIDTH_SHORTHAND ) );
    }

    /** A unit test for JUnit */
    public void testFontSingletonLookup() {
        PropertyDeclarationFactory inst = FontPropertyDeclarationFactory.instance();
        assertEquals( "'font' property uses FontPropertyDeclarationFactory",
                    FontPropertyDeclarationFactory.instance(),
                    PropertyDeclaration.newFactory( CSSName.FONT_SHORTHAND ) );

        assertEquals( "FontPropertyDeclarationFactory.instance() should return a singleton",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.FONT_SHORTHAND ) );
    }

    /** A unit test for JUnit */
    public void testListStyleSingletonLookup() {
        PropertyDeclarationFactory inst = ListStylePropertyDeclarationFactory.instance();
        assertEquals( "'list-style' property uses ListStylePropertyDeclarationFactory",
                    ListStylePropertyDeclarationFactory.instance(),
                    PropertyDeclaration.newFactory( CSSName.LIST_STYLE_SHORTHAND ) );

        assertEquals( "ListStylePropertyDeclarationFactory.instance() should return a singleton",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.LIST_STYLE_SHORTHAND ) );
    }

    /** A unit test for JUnit */
    public void testMarginSingletonLookup() {
        PropertyDeclarationFactory inst = MarginPropertyDeclarationFactory.instance();
        assertEquals( "'margin' property uses MarginPropertyDeclarationFactory",
                    MarginPropertyDeclarationFactory.instance(),
                    PropertyDeclaration.newFactory( CSSName.MARGIN_SHORTHAND ) );

        assertEquals( "MarginPropertyDeclarationFactory.instance() should return a singleton",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.MARGIN_SHORTHAND ) );
    }

    /** A unit test for JUnit */
    public void testOutlineSingletonLookup() {
        PropertyDeclarationFactory inst = OutlinePropertyDeclarationFactory.instance();
        assertEquals( "'outline' property uses OutlinePropertyDeclarationFactory",
                    OutlinePropertyDeclarationFactory.instance(),
                    PropertyDeclaration.newFactory( CSSName.OUTLINE_SHORTHAND ) );

        assertEquals( "OutlinePropertyDeclarationFactory.instance() should return a singleton",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.OUTLINE_SHORTHAND ) );
    }

    /** A unit test for JUnit */
    public void testPaddingSingletonLookup() {
        PropertyDeclarationFactory inst = PaddingPropertyDeclarationFactory.instance();
        assertEquals( "'padding' property uses PaddingPropertyDeclarationFactory",
                    PaddingPropertyDeclarationFactory.instance(),
                    PropertyDeclaration.newFactory( CSSName.PADDING_SHORTHAND ) );

        assertEquals( "PaddingPropertyDeclarationFactory.instance() should return a singleton",
                    inst,
                    PropertyDeclaration.newFactory( CSSName.PADDING_SHORTHAND ) );
    }

    /** The teardown method for JUnit */
    protected void tearDown() { }

    /** */
    protected void setUp() { }

    /**
     * A unit test suite for JUnit
     *
     * @return   The test suite
     */
    public static Test suite() {
        return new TestSuite( PDSingletonFactoryTest.class );
    }
}// end class

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

