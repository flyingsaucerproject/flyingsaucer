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
import java.util.List;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.CSSParseException;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;

public class BorderSpacingPropertyBuilder extends AbstractPropertyBuilder {
    private static final CSSName[] ALL = new CSSName[] {
        CSSName.FS_BORDER_SPACING_HORIZONTAL, CSSName.FS_BORDER_SPACING_VERTICAL };
    
    public List buildDeclarations(CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
        List result = checkInheritAll(ALL, values, origin, important, inheritAllowed);
        if (result != null) {
            return result;
        }
        
        checkValueCount(CSSName.BORDER_SPACING, 1, 2, values.size());
        
        PropertyDeclaration horizontalSpacing = null;
        PropertyDeclaration verticalSpacing = null;
        
        if (values.size() == 1) {
            PropertyValue value = (PropertyValue)values.get(0);
            checkLengthType(cssName, value);
            if (value.getFloatValue() < 0.0f) {
                throw new CSSParseException("border-spacing may not be negative", -1);
            }
            horizontalSpacing = new PropertyDeclaration(
                    CSSName.FS_BORDER_SPACING_HORIZONTAL, value, important, origin);
            verticalSpacing = new PropertyDeclaration(
                    CSSName.FS_BORDER_SPACING_VERTICAL, value, important, origin);            
        } else { /* values.size() == 2 */
            PropertyValue horizontal = (PropertyValue)values.get(0);
            checkLengthType(cssName, horizontal);
            if (horizontal.getFloatValue() < 0.0f) {
                throw new CSSParseException("border-spacing may not be negative", -1);
            }
            horizontalSpacing = new PropertyDeclaration(
                    CSSName.FS_BORDER_SPACING_HORIZONTAL, horizontal, important, origin);
            
            PropertyValue vertical = (PropertyValue)values.get(1);
            checkLengthType(cssName, vertical);
            if (vertical.getFloatValue() < 0.0f) {
                throw new CSSParseException("border-spacing may not be negative", -1);
            }
            verticalSpacing = new PropertyDeclaration(
                    CSSName.FS_BORDER_SPACING_VERTICAL, vertical, important, origin);            
        }
        
        result = new ArrayList(2);
        result.add(horizontalSpacing);
        result.add(verticalSpacing);
        
        return result;
    }
}
