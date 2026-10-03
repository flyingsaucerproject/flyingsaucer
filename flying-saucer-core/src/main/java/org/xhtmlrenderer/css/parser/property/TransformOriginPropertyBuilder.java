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
 * Parses {@code transform-origin}, mirroring the (2-value) {@code background-position} grammar:
 * a single length/percentage/keyword, or a horizontal/vertical pair. A lone {@code top}/{@code bottom}
 * keyword implies a centered horizontal axis, and keyword pairs may appear in either order
 * (e.g. {@code top left} and {@code left top} are equivalent).
 */
public class TransformOriginPropertyBuilder extends AbstractPropertyBuilder {
    @Override
    public List<PropertyDeclaration> buildDeclarations(
            CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin,
            boolean important, boolean inheritAllowed) {
        assertFoundUpToValues(cssName, values, 2);

        PropertyValue first = (PropertyValue) values.get(0);
        PropertyValue second = values.size() == 2 ? (PropertyValue) values.get(1) : null;

        checkInheritAllowed(first, inheritAllowed);
        if (second == null && first.getCssValueType() == CSS_INHERIT) {
            return singletonList(new PropertyDeclaration(cssName, first, important, origin));
        }
        if (second != null) {
            checkInheritAllowed(second, false);
        }

        checkIdentLengthOrPercentType(cssName, first);
        if (second == null) {
            if (first.getPrimitiveType() != CSS_IDENT) {
                return twoValues(cssName, first, percent(50), important, origin);
            }
        } else {
            checkIdentLengthOrPercentType(cssName, second);
        }

        IdentValue firstIdent = first.getPrimitiveType() == CSS_IDENT ? checkKeyword(cssName, first) : null;
        IdentValue secondIdent;
        if (second == null) {
            secondIdent = CENTER;
        } else {
            secondIdent = second.getPrimitiveType() == CSS_IDENT ? checkKeyword(cssName, second) : null;
        }

        if (firstIdent == null && secondIdent == null) {
            return twoValues(cssName, first, second, important, origin);
        } else if (firstIdent != null && secondIdent != null) {
            if (firstIdent == TOP || firstIdent == BOTTOM || secondIdent == LEFT || secondIdent == RIGHT) {
                IdentValue swap = firstIdent;
                firstIdent = secondIdent;
                secondIdent = swap;
            }
            checkAxisCombination(cssName, firstIdent, secondIdent);
            return twoValues(cssName, percentForIdent(firstIdent), percentForIdent(secondIdent), important, origin);
        } else if (firstIdent != null) {
            checkAxisCombination(cssName, firstIdent, null);
            return twoValues(cssName, percentForIdent(firstIdent), second, important, origin);
        } else {
            checkAxisCombination(cssName, null, secondIdent);
            return twoValues(cssName, first, percentForIdent(secondIdent), important, origin);
        }
    }

    private void checkAxisCombination(CSSName cssName, IdentValue firstIdent, IdentValue secondIdent) {
        if (firstIdent == TOP || firstIdent == BOTTOM || secondIdent == LEFT || secondIdent == RIGHT) {
            throw new CSSParseException("Invalid combination of keywords in " + cssName, -1);
        }
    }

    private IdentValue checkKeyword(CSSName cssName, PropertyValue value) {
        IdentValue ident = checkIdent(value);
        if (ident != LEFT && ident != RIGHT && ident != TOP && ident != BOTTOM && ident != CENTER) {
            throw new CSSParseException("Invalid keyword '" + ident + "' for " + cssName, -1);
        }
        return ident;
    }

    private PropertyValue percentForIdent(IdentValue ident) {
        if (ident == LEFT || ident == TOP) {
            return percent(0);
        }
        if (ident == RIGHT || ident == BOTTOM) {
            return percent(100);
        }
        return percent(50);
    }

    private List<PropertyDeclaration> twoValues(
            CSSName cssName, PropertyValue horizontal, PropertyValue vertical, boolean important, Origin origin) {
        return singletonList(new PropertyDeclaration(
                cssName, new PropertyValue(List.of(horizontal, vertical)), important, origin));
    }

    private PropertyValue percent(float percent) {
        return new PropertyValue(CSS_PERCENTAGE, percent, percent + "%");
    }
}
