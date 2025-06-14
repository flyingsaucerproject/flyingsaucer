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

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.CSSParseException;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.Origin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SizePropertyBuilder extends AbstractPropertyBuilder {
    private static final CSSName[] ALL = { CSSName.FS_PAGE_ORIENTATION, CSSName.FS_PAGE_HEIGHT, CSSName.FS_PAGE_WIDTH };
    private static final Set<String> PAGE_ORIENTATIONS = Set.of("landscape", "portrait");

    @Override
    public List<PropertyDeclaration> buildDeclarations(
            CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
        List<PropertyDeclaration> result = new ArrayList<>(3);
        assertFoundUpToValues(cssName, values, 3);

        if (values.size() == 1) {
            PropertyValue value = (PropertyValue)values.get(0);

            checkInheritAllowed(value, inheritAllowed);

            if (value.getCssValueType() == CSSValue.CSS_INHERIT) {
                return checkInheritAll(ALL, values, origin, important, inheritAllowed);
            } else if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                PageSize pageSize = PageSize.getPageSize(value.getStringValue());
                if (pageSize != null) {
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_ORIENTATION, new PropertyValue(IdentValue.AUTO), important, origin));
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_WIDTH, pageSize.getPageWidth(), important, origin));
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_HEIGHT, pageSize.getPageHeight(), important, origin));
                    return result;
                }

                IdentValue ident = checkIdent(value);
                if (ident == IdentValue.LANDSCAPE || ident == IdentValue.PORTRAIT) {
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_ORIENTATION, value, important, origin));
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_WIDTH, new PropertyValue(IdentValue.AUTO), important, origin));
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_HEIGHT, new PropertyValue(IdentValue.AUTO), important, origin));
                    return result;
                } else if (ident == IdentValue.AUTO) {
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_ORIENTATION, value, important, origin));
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_WIDTH, value, important, origin));
                    result.add(new PropertyDeclaration(
                            CSSName.FS_PAGE_HEIGHT, value, important, origin));
                    return result;
                } else {
                    throw new CSSParseException("Identifier " + ident + " is not a valid value for " + cssName, -1);
                }
            } else if (isLength(value)) {
                validatePageDimension(value);

                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_ORIENTATION, new PropertyValue(IdentValue.AUTO), important, origin));
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_WIDTH, value, important, origin));
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_HEIGHT, value, important, origin));

                return result;
            } else {
                throw new CSSParseException("Value for " + cssName + " must be a length or identifier", -1);
            }
        } else if (values.size() == 2) {
            PropertyValue value1 = (PropertyValue)values.get(0);
            PropertyValue value2 = (PropertyValue)values.get(1);

            checkInheritAllowed(value2, false);

            if (isLength(value1) && isLength(value2)) {
                validatePageDimension(value1);
                validatePageDimension(value2);

                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_ORIENTATION, new PropertyValue(IdentValue.AUTO), important, origin));
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_WIDTH, value1, important, origin));
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_HEIGHT, value2, important, origin));

                return result;
            } else if (value1.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT &&
                            value2.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                if (PAGE_ORIENTATIONS.contains(value2.getStringValue())) {
                    PropertyValue temp = value1;
                    value1 = value2;
                    value2 = temp;
                }

                validatePageOrientation(value1);

                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_ORIENTATION, value1, important, origin));

                PageSize pageSize = PageSize.getPageSize(value2.getStringValue());
                if (pageSize == null) {
                    throw new CSSParseException("Value " + value2 + " is not a valid page size", -1);
                }

                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_WIDTH, pageSize.getPageWidth(), important, origin));
                result.add(new PropertyDeclaration(
                        CSSName.FS_PAGE_HEIGHT, pageSize.getPageHeight(), important, origin));

                return result;
            } else {
                throw new CSSParseException("Invalid value for size property", -1);
            }
        }  else if (values.size() == 3) {
            PropertyValue value1 = (PropertyValue) values.get(0);
            PropertyValue value2 = (PropertyValue) values.get(1);
            PropertyValue value3 = (PropertyValue) values.get(2);

            checkInheritAllowed(value3, false);

            if (isLength(value1) && isLength(value2) && value3.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                validatePageDimension(value1);
                validatePageDimension(value2);
                validatePageOrientation(value3);

                result.add(new PropertyDeclaration(CSSName.FS_PAGE_WIDTH, value1, important, origin));
                result.add(new PropertyDeclaration(CSSName.FS_PAGE_HEIGHT, value2, important, origin));
                result.add(new PropertyDeclaration(CSSName.FS_PAGE_ORIENTATION, value3, important, origin));
                return result;
            } else {
                throw new CSSParseException("Size property parsing error", -1);
            }
        } else {
            throw new CSSParseException("Invalid value count for size property", -1);
        }
    }

    private static void validatePageDimension(PropertyValue value) {
        if (value.getFloatValue() < 0.0f) {
            throw new CSSParseException("A page dimension may not be negative: " + value.getFloatValue(), -1);
        }
    }

    private static void validatePageOrientation(PropertyValue orientation) {
        if (!PAGE_ORIENTATIONS.contains(orientation.toString())) {
            throw new CSSParseException("Value " + orientation + " is not a valid page orientation", -1);
        }
    }
}
