package org.xhtmlrenderer.css.parser.property;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.CSSParseException;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.Origin;

import java.util.ArrayList;
import java.util.List;

import static org.w3c.dom.css.CSSValue.CSS_INHERIT;
import static org.xhtmlrenderer.css.constants.IdentValue.AUTO;

public class ColumnsPropertyBuilder extends AbstractPropertyBuilder {
    @Override
    public List<PropertyDeclaration> buildDeclarations(
            CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin, boolean important, boolean inheritAllowed) {
        assertFoundUpToValues(cssName, values, 2);

        if (values.size() == 1 && values.get(0).getCssValueType() == CSS_INHERIT) {
            checkInheritAllowed(values.get(0), inheritAllowed);
            return List.of(
                    new PropertyDeclaration(CSSName.COLUMN_WIDTH, values.get(0), important, origin),
                    new PropertyDeclaration(CSSName.COLUMN_COUNT, values.get(0), important, origin));
        }

        PropertyValue columnWidth = new PropertyValue(AUTO);
        PropertyValue columnCount = new PropertyValue(AUTO);
        boolean widthSpecified = false;
        boolean countSpecified = false;

        for (CSSPrimitiveValue cssPrimitiveValue : values) {
            PropertyValue value = (PropertyValue) cssPrimitiveValue;
            checkInheritAllowed(value, false);
            short type = value.getPrimitiveType();

            if (type == CSSPrimitiveValue.CSS_IDENT) {
                IdentValue ident = checkIdent(value);
                if (ident != AUTO) {
                    throw new CSSParseException("Only auto is allowed as an identifier in columns", -1);
                }
                if (!widthSpecified) {
                    columnWidth = value;
                    widthSpecified = true;
                } else if (!countSpecified) {
                    columnCount = value;
                    countSpecified = true;
                } else {
                    throw new CSSParseException("Duplicate values in columns shorthand", -1);
                }
            } else if (isLength(value)) {
                if (widthSpecified) {
                    throw new CSSParseException("Column width specified more than once", -1);
                }
                if (value.getFloatValue() < 0.0f) {
                    throw new CSSParseException("column-width may not be negative", -1);
                }
                columnWidth = value;
                widthSpecified = true;
            } else if (type == CSSPrimitiveValue.CSS_NUMBER &&
                    (int) value.getFloatValue(CSSPrimitiveValue.CSS_NUMBER) == Math.round(value.getFloatValue(CSSPrimitiveValue.CSS_NUMBER))) {
                if (countSpecified) {
                    throw new CSSParseException("Column count specified more than once", -1);
                }
                if (value.getFloatValue() < 1.0f) {
                    throw new CSSParseException("column-count must be at least 1", -1);
                }
                columnCount = value;
                countSpecified = true;
            } else {
                throw new CSSParseException("Invalid value in columns shorthand", -1);
            }
        }

        List<PropertyDeclaration> result = new ArrayList<>(2);
        result.add(new PropertyDeclaration(CSSName.COLUMN_WIDTH, columnWidth, important, origin));
        result.add(new PropertyDeclaration(CSSName.COLUMN_COUNT, columnCount, important, origin));
        return result;
    }
}
