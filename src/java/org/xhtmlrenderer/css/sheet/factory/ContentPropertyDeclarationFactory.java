package org.xhtmlrenderer.css.sheet.factory;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.value.FSCssValue;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * A PropertyDeclarationFactory for CSS 2 quote property, which, although not a shorthand,
 * can have multiple values assigned when parsed (open and close quotes, nested);
 * Singleton, use {@link #instance()}.
 *
 * @author Patrick Wright
 */
public class ContentPropertyDeclarationFactory extends AbstractPropertyDeclarationFactory {
    /**
     * Singleton instance.
     */
    private static ContentPropertyDeclarationFactory _instance;

    /**
     * Constructor for the QuotesPropertyDeclarationFactory object
     */
    private ContentPropertyDeclarationFactory() {
    }

    /**
     * Subclassed implementation of redirected buildDeclarations() from abstract
     * superclass.
     *
     * @param primVals  The SAC value for this property
     * @param important True if author-marked important!
     * @param cssName   property name
     * @param origin    The origin of the stylesheet; constant from {@link
     *                  org.xhtmlrenderer.css.sheet.Stylesheet}, e.g. Stylesheet.AUTHOR
     * @return Iterator of PropertyDeclarations for the shorthand
     *         margin property.
     */
    protected Iterator doBuildDeclarations(CSSPrimitiveValue[] primVals,
                                           boolean important,
                                           CSSName cssName,
                                           int origin) {

        StringBuffer pos = new StringBuffer();
        String suffix = " ";
        for (int i = 0; i < primVals.length; i++) {
            CSSPrimitiveValue primVal = primVals[i];
            pos.append(primVal.getCssText().trim() + suffix);
        }
        pos.deleteCharAt(pos.length() - suffix.length());// remove ,spc
        FSCssValue fsCssValue = new FSCssValue(primVals[0], pos.toString().trim());
        List declarations = new ArrayList(1);
        declarations.add(newPropertyDeclaration(cssName, fsCssValue, origin, important));
        return declarations.iterator();
    }

    /**
     * Returns the singleton instance.
     *
     * @return Returns
     */
    public static synchronized PropertyDeclarationFactory instance() {
        if (ContentPropertyDeclarationFactory._instance == null) {
            ContentPropertyDeclarationFactory._instance = new ContentPropertyDeclarationFactory();
        }
        return ContentPropertyDeclarationFactory._instance;
    }
}// end class
