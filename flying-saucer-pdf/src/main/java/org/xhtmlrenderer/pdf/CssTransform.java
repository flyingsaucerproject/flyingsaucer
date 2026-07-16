package org.xhtmlrenderer.pdf;

import org.jspecify.annotations.Nullable;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.parser.FSFunction;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.style.BackgroundPosition;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.LengthValue;
import org.xhtmlrenderer.render.Box;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.List;

import static org.w3c.dom.css.CSSPrimitiveValue.CSS_GRAD;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_RAD;

/**
 * Builds the {@link AffineTransform} for a box's CSS {@code transform}/{@code transform-origin},
 * for use by {@link ITextOutputDevice}. Percentages in {@code translate()} and
 * {@code transform-origin} are resolved against the box's border box, matching the CSS default.
 */
final class CssTransform {
    private CssTransform() {
    }

    @Nullable
    static AffineTransform toAffineTransform(CssContext ctx, Box box) {
        CalculatedStyle style = box.getStyle();
        List<PropertyValue> functions = style.getTransforms();
        if (functions.isEmpty()) {
            return null;
        }

        Rectangle bounds = box.getPaintingBorderEdge(ctx);
        BackgroundPosition transformOrigin = style.getTransformOrigin();
        float originX = bounds.x + resolveLength(ctx, style, transformOrigin.getHorizontal(), bounds.width);
        float originY = bounds.y + resolveLength(ctx, style, transformOrigin.getVertical(), bounds.height);

        AffineTransform result = new AffineTransform();
        result.translate(originX, originY);
        for (PropertyValue function : functions) {
            result.concatenate(toMatrix(ctx, style, function.getFunction(), bounds));
        }
        result.translate(-originX, -originY);

        return result;
    }

    static AffineTransform toMatrix(CssContext ctx, CalculatedStyle style, FSFunction function, Rectangle bounds) {
        List<PropertyValue> args = function.getParameters();

        // CSSParser lower-cases FUNCTION tokens (CSS identifiers are case-insensitive), so e.g.
        // "skewX" always arrives here as "skewx".
        return switch (function.getName()) {
            case "matrix" -> new AffineTransform(
                    number(args, 0), number(args, 1), number(args, 2),
                    number(args, 3), number(args, 4), number(args, 5));
            case "translate" -> AffineTransform.getTranslateInstance(
                    resolveLength(ctx, style, args.get(0), bounds.width),
                    args.size() == 2 ? resolveLength(ctx, style, args.get(1), bounds.height) : 0);
            case "translatex" -> AffineTransform.getTranslateInstance(
                    resolveLength(ctx, style, args.get(0), bounds.width), 0);
            case "translatey" -> AffineTransform.getTranslateInstance(
                    0, resolveLength(ctx, style, args.get(0), bounds.height));
            case "scale" -> {
                float sx = number(args, 0);
                yield AffineTransform.getScaleInstance(sx, args.size() == 2 ? number(args, 1) : sx);
            }
            case "scalex" -> AffineTransform.getScaleInstance(number(args, 0), 1);
            case "scaley" -> AffineTransform.getScaleInstance(1, number(args, 0));
            case "rotate" -> AffineTransform.getRotateInstance(angleToRadians(args.get(0)));
            case "skew" -> AffineTransform.getShearInstance(
                    Math.tan(angleToRadians(args.get(0))),
                    args.size() == 2 ? Math.tan(angleToRadians(args.get(1))) : 0);
            case "skewx" -> AffineTransform.getShearInstance(Math.tan(angleToRadians(args.get(0))), 0);
            case "skewy" -> AffineTransform.getShearInstance(0, Math.tan(angleToRadians(args.get(0))));
            default -> new AffineTransform();
        };
    }

    private static float number(List<PropertyValue> args, int index) {
        return args.get(index).getFloatValue();
    }

    static double angleToRadians(PropertyValue angle) {
        float value = angle.getFloatValue();
        return switch (angle.getPrimitiveType()) {
            case CSS_RAD -> value;
            case CSS_GRAD -> Math.toRadians(value * 0.9);
            default -> Math.toRadians(value); // CSS_DEG (also covers "turn", pre-converted to degrees at parse time)
        };
    }

    private static float resolveLength(CssContext ctx, CalculatedStyle style, PropertyValue value, float percentageBase) {
        return LengthValue.calcFloatProportionalValue(
                style, CSSName.TRANSFORM, value.getCssText(), value.getFloatValue(),
                value.getPrimitiveType(), percentageBase, ctx);
    }
}
