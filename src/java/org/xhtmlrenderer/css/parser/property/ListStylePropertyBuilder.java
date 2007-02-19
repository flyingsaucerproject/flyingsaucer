/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.css.parser.property;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.CSSParseException;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;

public class ListStylePropertyBuilder extends AbstractPropertyBuilder {
    private static final CSSName[] ALL = new CSSName[] {
        CSSName.LIST_STYLE_TYPE, CSSName.LIST_STYLE_POSITION, CSSName.LIST_STYLE_IMAGE }; 
    
    public List buildDeclarations(CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
        List result = checkInheritAll(ALL, values, origin, important, inheritAllowed);
        if (result != null) {
            return result;
        }
        
        PropertyDeclaration listStyleType = null;
        PropertyDeclaration listStylePosition = null;
        PropertyDeclaration listStyleImage = null;
        
        for (Iterator i = values.iterator(); i.hasNext(); ) {
            PropertyValue value = (PropertyValue)i.next();
            checkInheritAllowed(value, false);
            short type = value.getPrimitiveType();
            if (type == CSSPrimitiveValue.CSS_IDENT) {
                IdentValue ident = checkIdent(CSSName.LIST_STYLE_SHORTHAND, value);
                
                if (ident == IdentValue.NONE) {
                    if (listStyleType == null) {
                        listStyleType = new PropertyDeclaration(
                                CSSName.LIST_STYLE_TYPE, value, important, origin);
                    }
                    
                    if (listStyleImage == null) {
                        listStyleImage = new PropertyDeclaration(
                                CSSName.LIST_STYLE_IMAGE, value, important, origin);
                    }
                } else if (PrimitivePropertyBuilders.LIST_STYLE_POSITIONS.get(ident.FS_ID)) {
                    if (listStylePosition != null) {
                        throw new CSSParseException("A list-style-position value cannot be set twice", -1);
                    }
                    
                    listStylePosition = new PropertyDeclaration(
                            CSSName.LIST_STYLE_POSITION, value, important, origin);
                } else if (PrimitivePropertyBuilders.LIST_STYLE_TYPES.get(ident.FS_ID)) {
                    if (listStyleType != null) {
                        throw new CSSParseException("A list-style-type value cannot be set twice", -1);
                    }
                    
                    listStyleType = new PropertyDeclaration(
                            CSSName.LIST_STYLE_TYPE, value, important, origin);
                }
            } else if (type == CSSPrimitiveValue.CSS_URI) {
                if (listStyleImage != null) {
                    throw new CSSParseException("A list-style-image value cannot be set twice", -1);
                }
                
                listStyleImage = new PropertyDeclaration(
                        CSSName.LIST_STYLE_IMAGE, value, important, origin);
            }
        }
        
        result = new ArrayList(3);
        if (listStyleType != null) {
            result.add(listStyleType);
        }
        if (listStylePosition != null) {
            result.add(listStylePosition);
        }
        if (listStyleImage != null) {
            result.add(listStyleImage);
        }
        
        return result;
    }
}
