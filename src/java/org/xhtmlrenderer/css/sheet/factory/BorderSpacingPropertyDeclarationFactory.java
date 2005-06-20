package org.xhtmlrenderer.css.sheet.factory;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-jun-19
 * Time: 23:22:57
 * To change this template use File | Settings | File Templates.
 */
public class BorderSpacingPropertyDeclarationFactory extends AbstractPropertyDeclarationFactory {
    /**
     * Singleton instance.
     */
    private static BorderSpacingPropertyDeclarationFactory _instance;

    /**
     * Default constructor; don't use, use instance() instead.
     */
    private BorderSpacingPropertyDeclarationFactory() {
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

        List declarations = new ArrayList();
        CSSPrimitiveValue primitive = null;
        CSSPrimitiveValue primitives[] = null;
        CSSName parts[] = new CSSName[]{CSSName.FS_BORDER_SPACING_HORIZONTAL, CSSName.FS_BORDER_SPACING_VERTICAL};

        switch (primVals.length) {
            case 1:
                primitive = primVals[0];
                primitives = new CSSPrimitiveValue[]{
                    primitive,
                    primitive};

                addProperties(declarations, primitives, parts, origin, important);
                break;
            case 2:
                primitives = new CSSPrimitiveValue[]{
                    primVals[0],
                    primVals[1]};

                addProperties(declarations, primitives, parts, origin, important);
                break;
        }

        return declarations.iterator();
    }

    /**
     * Returns the singleton instance.
     *
     * @return See desc.
     */
    public static synchronized PropertyDeclarationFactory instance() {
        if (_instance == null) {
            _instance = new BorderSpacingPropertyDeclarationFactory();
        }
        return _instance;
    }
}
