/*
 * {{{ header & license
 * AbstractPropertyFactory.java
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

import org.xhtmlrenderer.css.XRProperty;
import org.xhtmlrenderer.css.XRValue;
import org.xhtmlrenderer.css.impl.XRPropertyImpl;
import org.xhtmlrenderer.css.impl.XRValueImpl;
import org.xhtmlrenderer.css.RuleNormalizer;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * Base class for <code>PropertyFactories</code>.
 *
 * @author    Patrick Wright
 *
 */
public abstract class AbstractPropertyFactory implements PropertyFactory {
    /**
     * Creates a new XRProperty instance.
     *
     * @param newPropertyName  PARAM
     * @param primitive        PARAM
     * @param priority         PARAM
     * @param style            PARAM
     * @param sequence         PARAM
     * @return                 Returns
     */
    protected XRProperty newProperty(
            String newPropertyName,
            CSSPrimitiveValue primitive,
            String priority,
            CSSStyleDeclaration style,
            int sequence ) {

        if ( newPropertyName.indexOf("color") >=0 ) {
            try {
            if ( !primitive.getCssText().equals("transparent"))
                primitive.setCssText(RuleNormalizer.getColorHex(primitive.getCssText()));
            } catch ( Exception ex ) {
                System.out.println("can't set color: " + primitive.getCssText());   
            }
        }
        XRValue val = new XRValueImpl( primitive, priority );
        return new XRPropertyImpl( newPropertyName, sequence, val );
    }
}

