/*
 * {{{ header & license
 * FontPropertyFactory.java
 * Copyright (c) 2004 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.css.factory;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;
import org.xhtmlrenderer.css.RuleNormalizer;
import org.xhtmlrenderer.css.constants.CSSName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A PropertyFactory for CSS 2 "margin" shorthand property, instantiating
 * XRProperties; Singleton, use {@link #instance()}.
 *
 * @author Patrick Wright
 */
public class FontPropertyFactory extends AbstractPropertyFactory {
    /**
     * Singleton instance.
     */
    private static FontPropertyFactory _instance;


    /**
     * Constructor for the FontPropertyFactory object
     */
    private FontPropertyFactory() {
    }

    /**
     * If <code>propName</code> describes a shorthand property, explodes it into
     * the specific properties it is a shorthand for, and returns those as an
     * Iterator of {@link org.xhtmlrenderer.css.XRProperty} instances; or just
     * instantiates a single <code>XRProperty</code> for non-shorthand props.
     *
     * @param style    The CSSStyleDeclaration from the SAC parser.
     * @param propName The String property name for the property to explode.
     * @param sequence Sequence in which the declaration was found in the
     *                 containing stylesheet.
     * @return Iterator of one or more XRProperty instances
     *         representing the exploded values.
     */
    public Iterator explodeProperties(CSSStyleDeclaration style, String propName, int sequence) {
        List list = new ArrayList();
        CSSValue cssValue = style.getPropertyCSSValue(propName);
        String priority = style.getPropertyPriority(propName);

        // CAREFUL: note that with steadyState parser impl, their value class impl
        // both primitive and value list interfaces! use getCssValueType(), not instanceof!!
        if (cssValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
            addPrimitive(style, (CSSPrimitiveValue) cssValue, priority, sequence, list);
        } else {
            // is a value list
            CSSValueList vList = (CSSValueList) cssValue;

            // background shorthand can have color, image, repeat,
            // attachment, position in any order; so loop whatever's
            // provided and sniff for the value-type
            CSSPrimitiveValue primitive = null;
            CSSPrimitiveValue familyPrimitive = null;
            boolean hasSize = false;
            List families = new ArrayList();

            for (int i = 0, len = vList.getLength(); i < len; i++) {
                primitive = (CSSPrimitiveValue) vList.item(i);

                String val = primitive.getCssText().trim();
                //System.out.println( "  font val: " + val );
                String exp_propName = "";
                if (RuleNormalizer.looksLikeAFontStyle(val)) {
                    exp_propName = CSSName.FONT_STYLE;
                } else if (RuleNormalizer.looksLikeAFontVariant(val)) {
                    exp_propName = CSSName.FONT_VARIANT;
                } else if (RuleNormalizer.looksLikeAFontWeight(val)) {
                    exp_propName = CSSName.FONT_WEIGHT;
                } else if (!hasSize && RuleNormalizer.looksLikeAFontSize(val)) {
                    exp_propName = CSSName.FONT_SIZE;
                    hasSize = true;
                } else if (hasSize && RuleNormalizer.looksLikeALineHeight(val)) {
                    exp_propName = CSSName.LINE_HEIGHT;
                } else {
                    // HACK: assume it is a font-family
                    families.add(val);
                    if (familyPrimitive == null) {
                        familyPrimitive = primitive;
                    }
                    continue;
                }
                list.add(newProperty(exp_propName,
                        primitive,
                        priority,
                        style,
                        sequence));
            }
            if (families.size() > 0) {
                StringBuffer sb = new StringBuffer();
                String sep = "";
                Iterator iter = families.iterator();
                while (iter.hasNext()) {
                    sb.append(sep).append(iter.next());
                    sep = ", ";
                }
                familyPrimitive.setCssText(sb.toString());
                list.add(newProperty(CSSName.FONT_FAMILY,
                        familyPrimitive,
                        priority,
                        style,
                        sequence));
            }
        }// is a value list
        return list.iterator();
    }

    /**
     * Adds a feature to the Primitive attribute of the FontPropertyFactory
     * object
     *
     * @param style     The feature to be added to the Primitive attribute
     * @param primitive The feature to be added to the Primitive attribute
     * @param priority  The feature to be added to the Primitive attribute
     * @param sequence  The feature to be added to the Primitive attribute
     * @param list      The feature to be added to the Primitive attribute
     */
    private void addPrimitive(CSSStyleDeclaration style,
                              CSSPrimitiveValue primitive,
                              String priority,
                              int sequence,
                              List list) {
    }

    /**
     * Returns the singleton instance.
     *
     * @return Returns
     */
    public static synchronized PropertyFactory instance() {
        if (_instance == null) {
            _instance = new FontPropertyFactory();
        }
        return _instance;
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2005/01/05 17:56:33  tobega
 * Reduced memory more, especially by using WeakHashMap for caching Mappers. Look over other caching to use similar schemes (cache when memory available).
 *
 * Revision 1.3  2004/10/23 13:14:12  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

