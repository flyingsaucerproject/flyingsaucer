package org.xhtmlrenderer.css.parser.property;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.CSSParseException;
import org.xhtmlrenderer.css.parser.FSFunction;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.StylesheetInfo.Origin;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_DEG;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_GRAD;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_NUMBER;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PERCENTAGE;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_RAD;
import static org.w3c.dom.css.CSSValue.CSS_INHERIT;
import static org.xhtmlrenderer.css.parser.PropertyValue.Type.VALUE_TYPE_FUNCTION;
import static org.xhtmlrenderer.css.parser.PropertyValue.Type.VALUE_TYPE_IDENT;
import static org.xhtmlrenderer.css.parser.property.TransformPropertyBuilder.ArgumentKind.ANGLE;
import static org.xhtmlrenderer.css.parser.property.TransformPropertyBuilder.ArgumentKind.LENGTH_PERCENTAGE;
import static org.xhtmlrenderer.css.parser.property.TransformPropertyBuilder.ArgumentKind.NUMBER;

/**
 * Parses {@code transform: none | <transform-function>+}, e.g.
 * {@code translate(10px, 10px) rotate(45deg) scale(2)}.
 * <p>
 * Only 2D transform functions are supported, which is all that is meaningful on a flat PDF page:
 * {@code matrix}, {@code translate}/{@code translateX}/{@code translateY},
 * {@code scale}/{@code scaleX}/{@code scaleY}, {@code rotate}, {@code skew}/{@code skewX}/{@code skewY}.
 */
public class TransformPropertyBuilder extends AbstractPropertyBuilder {
    enum ArgumentKind { LENGTH_PERCENTAGE, NUMBER, ANGLE }

    private record FunctionSpec(int minArgs, int maxArgs, ArgumentKind argumentKind) {
    }

    // CSSParser lower-cases FUNCTION tokens (CSS identifiers are case-insensitive), so e.g. "skewX"
    // always arrives here as "skewx".
    private static final Map<String, FunctionSpec> FUNCTIONS = Map.ofEntries(
            Map.entry("matrix", new FunctionSpec(6, 6, NUMBER)),
            Map.entry("translate", new FunctionSpec(1, 2, LENGTH_PERCENTAGE)),
            Map.entry("translatex", new FunctionSpec(1, 1, LENGTH_PERCENTAGE)),
            Map.entry("translatey", new FunctionSpec(1, 1, LENGTH_PERCENTAGE)),
            Map.entry("scale", new FunctionSpec(1, 2, NUMBER)),
            Map.entry("scalex", new FunctionSpec(1, 1, NUMBER)),
            Map.entry("scaley", new FunctionSpec(1, 1, NUMBER)),
            Map.entry("rotate", new FunctionSpec(1, 1, ANGLE)),
            Map.entry("skew", new FunctionSpec(1, 2, ANGLE)),
            Map.entry("skewx", new FunctionSpec(1, 1, ANGLE)),
            Map.entry("skewy", new FunctionSpec(1, 1, ANGLE))
    );

    @Override
    public List<PropertyDeclaration> buildDeclarations(
            CSSName cssName, List<? extends CSSPrimitiveValue> values, Origin origin,
            boolean important, boolean inheritAllowed) {
        if (values.size() == 1) {
            PropertyValue value = (PropertyValue) values.get(0);
            checkInheritAllowed(value, inheritAllowed);

            if (value.getCssValueType() == CSS_INHERIT) {
                return singletonList(new PropertyDeclaration(cssName, value, important, origin));
            }
            if (value.getPropertyValueType() == VALUE_TYPE_IDENT && "none".equals(value.getStringValue())) {
                return singletonList(new PropertyDeclaration(cssName, value, important, origin));
            }
        }

        List<PropertyValue> functions = values.stream()
                .map(rawValue -> {
                    PropertyValue value = (PropertyValue) rawValue;
                    if (value.getPropertyValueType() != VALUE_TYPE_FUNCTION) {
                        throw new CSSParseException(
                                "Value for " + cssName + " must be 'none' or one or more transform functions", -1);
                    }
                    checkFunction(cssName, value.getFunction());
                    return value;
                })
                .toList();

        return singletonList(new PropertyDeclaration(cssName, new PropertyValue(functions), important, origin));
    }

    private void checkFunction(CSSName cssName, FSFunction function) {
        FunctionSpec spec = FUNCTIONS.get(function.getName());
        if (spec == null) {
            throw new CSSParseException(
                    "Function " + function.getName() + "() is not a valid value for " + cssName, -1);
        }

        List<PropertyValue> params = function.getParameters();
        if (params.size() < spec.minArgs() || params.size() > spec.maxArgs()) {
            throw new CSSParseException(
                    "%s() requires between %d and %d argument(s) but %d were given"
                            .formatted(function.getName(), spec.minArgs(), spec.maxArgs(), params.size()), -1);
        }

        for (PropertyValue param : params) {
            checkArgument(cssName, function.getName(), spec.argumentKind(), param);
        }
    }

    private void checkArgument(CSSName cssName, String functionName, ArgumentKind kind, PropertyValue value) {
        boolean valid = switch (kind) {
            case LENGTH_PERCENTAGE -> isLength(value) || value.getPrimitiveType() == CSS_PERCENTAGE;
            case NUMBER -> value.getPrimitiveType() == CSS_NUMBER;
            case ANGLE -> isAngle(value);
        };

        if (!valid) {
            throw new CSSParseException(
                    "Invalid argument '" + value.getCssText() + "' to " + functionName + "() in " + cssName, -1);
        }
    }

    private boolean isAngle(CSSPrimitiveValue value) {
        short type = value.getPrimitiveType();
        return type == CSS_DEG || type == CSS_RAD || type == CSS_GRAD;
    }
}
