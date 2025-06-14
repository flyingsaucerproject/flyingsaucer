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

import static org.xhtmlrenderer.css.constants.CSSName.FS_PAGE_HEIGHT;
import static org.xhtmlrenderer.css.constants.CSSName.FS_PAGE_ORIENTATION;
import static org.xhtmlrenderer.css.constants.CSSName.FS_PAGE_WIDTH;

public class SizePropertyBuilder extends AbstractPropertyBuilder {
    private static final CSSName[] ALL = { FS_PAGE_ORIENTATION, FS_PAGE_HEIGHT, FS_PAGE_WIDTH };
    private static final Set<String> PAGE_ORIENTATIONS = Set.of("landscape", "portrait");
    private static final PropertyValue AUTO = new PropertyValue(IdentValue.AUTO);

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
                    addPageSize(result, pageSize.getPageWidth(), pageSize.getPageHeight(), origin, important);
                    return result;
                }

                IdentValue ident = checkIdent(value);
                if (ident == IdentValue.LANDSCAPE || ident == IdentValue.PORTRAIT) {
                    addPageSize(result, value, AUTO, AUTO, origin, important);
                    return result;
                } else if (ident == IdentValue.AUTO) {
                    addPageSize(result, value, value, value, origin, important);
                    return result;
                } else {
                    throw new CSSParseException("Identifier " + ident + " is not a valid value for " + cssName, -1);
                }
            } else if (isLength(value)) {
                addPageSize(result, value, value, origin, important);
                return result;
            } else {
                throw new CSSParseException("Value for " + cssName + " must be a length or identifier", -1);
            }
        } else if (values.size() == 2) {
            PropertyValue value1 = (PropertyValue)values.get(0);
            PropertyValue value2 = (PropertyValue)values.get(1);

            checkInheritAllowed(value2, false);

            if (isLength(value1) && isLength(value2)) {
                addPageSize(result, value1, value2, origin, important);
                return result;
            } else if (value1.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT &&
                            value2.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                if (PAGE_ORIENTATIONS.contains(value2.getStringValue())) {
                    PropertyValue temp = value1;
                    value1 = value2;
                    value2 = temp;
                }

                validatePageOrientation(value1);

                PageSize pageSize = PageSize.getPageSize(value2.getStringValue());
                if (pageSize == null) {
                    throw new CSSParseException("Value " + value2 + " is not a valid page size", -1);
                }

                addPageSize(result, value1, pageSize.getPageWidth(), pageSize.getPageHeight(), origin, important);
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
                validatePageOrientation(value3);
                addPageSize(result, value3, value1, value2, origin, important);
                return result;
            } else {
                throw new CSSParseException("Size property parsing error", -1);
            }
        } else {
            throw new CSSParseException("Invalid value count for size property", -1);
        }
    }

    private static void addPageSize(List<PropertyDeclaration> result, CSSPrimitiveValue width, CSSPrimitiveValue height,
                                    Origin origin, boolean important) {
        addPageSize(result, AUTO, width, height, origin, important);
    }

    private static void addPageSize(List<PropertyDeclaration> result, PropertyValue orientation,
                                    CSSPrimitiveValue width, CSSPrimitiveValue height,
                                    Origin origin, boolean important) {
        if (width instanceof PropertyValue widthValue) {
            validatePageDimension(widthValue);
        }
        if (height instanceof PropertyValue heightValue) {
            validatePageDimension(heightValue);
        }

        result.add(new PropertyDeclaration(FS_PAGE_ORIENTATION, orientation, important, origin));
        result.add(new PropertyDeclaration(FS_PAGE_WIDTH, width, important, origin));
        result.add(new PropertyDeclaration(FS_PAGE_HEIGHT, height, important, origin));
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
