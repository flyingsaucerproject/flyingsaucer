package org.xhtmlrenderer.css.parser.property;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.CSSParseException;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.Origin;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_IDENT;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PERCENTAGE;
import static org.w3c.dom.css.CSSValue.CSS_INHERIT;
import static org.xhtmlrenderer.css.constants.IdentValue.BOTTOM;
import static org.xhtmlrenderer.css.constants.IdentValue.CENTER;
import static org.xhtmlrenderer.css.constants.IdentValue.LEFT;
import static org.xhtmlrenderer.css.constants.IdentValue.RIGHT;
import static org.xhtmlrenderer.css.constants.IdentValue.TOP;

/**
 * Parses {@code transform-origin: [ <length-percentage> | left | center | right ]
 * [ <length-percentage> | top | center | bottom ]?}. The vertical value defaults to
 * {@code center} when omitted; keyword reordering (e.g. {@code top left}) is not supported.
 */
public class TransformOriginPropertyBuilder extends AbstractPropertyBuilder {
    @Override
    public List<PropertyDeclaration> buildDeclarations(
            CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin,
            boolean important, boolean inheritAllowed) {
        assertFoundUpToValues(cssName, values, 2);

        PropertyValue first = (PropertyValue) values.get(0);
        checkInheritAllowed(first, inheritAllowed);
        if (values.size() == 1 && first.getCssValueType() == CSS_INHERIT) {
            return singletonList(new PropertyDeclaration(cssName, first, important, origin));
        }

        PropertyValue horizontal = resolveComponent(cssName, first, true);
        PropertyValue vertical = values.size() == 2
                ? resolveComponent(cssName, (PropertyValue) values.get(1), false)
                : percent(50);

        return singletonList(new PropertyDeclaration(
                cssName, new PropertyValue(List.of(horizontal, vertical)), important, origin));
    }

    private PropertyValue resolveComponent(CSSName cssName, PropertyValue value, boolean horizontal) {
        if (value.getPrimitiveType() == CSS_IDENT) {
            IdentValue ident = checkIdent(value);

            if (ident == CENTER) {
                return percent(50);
            }
            if (horizontal && ident == LEFT) {
                return percent(0);
            }
            if (horizontal && ident == RIGHT) {
                return percent(100);
            }
            if (!horizontal && ident == TOP) {
                return percent(0);
            }
            if (!horizontal && ident == BOTTOM) {
                return percent(100);
            }

            throw new CSSParseException("Invalid keyword '" + ident + "' for " + cssName, -1);
        }

        checkLengthOrPercentType(cssName, value);
        return value;
    }

    private PropertyValue percent(float percent) {
        return new PropertyValue(CSS_PERCENTAGE, percent, percent + "%");
    }
}
