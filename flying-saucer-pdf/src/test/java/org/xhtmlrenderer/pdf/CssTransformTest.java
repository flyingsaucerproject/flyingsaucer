package org.xhtmlrenderer.pdf;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.css.parser.FSFunction;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.style.BackgroundPosition;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.render.Box;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_DEG;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_GRAD;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_NUMBER;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PERCENTAGE;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PX;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_RAD;

class CssTransformTest {
    private static final Offset<Double> TOLERANCE = within(0.00001);

    private final CssContext ctx = mock();
    private final CalculatedStyle style = mock();
    private final Rectangle bounds = new Rectangle(0, 0, 100, 50);

    @BeforeEach
    void setUp() {
        when(ctx.getDotsPerPixel()).thenReturn(1);
    }

    private static PropertyValue px(float value) {
        return new PropertyValue(CSS_PX, value, value + "px");
    }

    private static PropertyValue percent(float value) {
        return new PropertyValue(CSS_PERCENTAGE, value, value + "%");
    }

    private static PropertyValue number(float value) {
        return new PropertyValue(CSS_NUMBER, value, String.valueOf(value));
    }

    private static PropertyValue deg(float value) {
        return new PropertyValue(CSS_DEG, value, value + "deg");
    }

    private static PropertyValue rad(float value) {
        return new PropertyValue(CSS_RAD, value, value + "rad");
    }

    private static PropertyValue grad(float value) {
        return new PropertyValue(CSS_GRAD, value, value + "grad");
    }

    private static FSFunction function(String name, PropertyValue... args) {
        return new FSFunction(name, List.of(args));
    }

    private void assertMatrixEquals(AffineTransform actual, double m00, double m10, double m01, double m11, double m02, double m12) {
        double[] matrix = new double[6];
        actual.getMatrix(matrix);
        assertThat(matrix[0]).isCloseTo(m00, TOLERANCE);
        assertThat(matrix[1]).isCloseTo(m10, TOLERANCE);
        assertThat(matrix[2]).isCloseTo(m01, TOLERANCE);
        assertThat(matrix[3]).isCloseTo(m11, TOLERANCE);
        assertThat(matrix[4]).isCloseTo(m02, TOLERANCE);
        assertThat(matrix[5]).isCloseTo(m12, TOLERANCE);
    }

    @Test
    void matrix() {
        AffineTransform result = CssTransform.toMatrix(
                ctx, style, function("matrix", number(1), number(2), number(3), number(4), number(5), number(6)), bounds);

        assertMatrixEquals(result, 1, 2, 3, 4, 5, 6);
    }

    @Test
    void translateWithBothArguments() {
        AffineTransform result = CssTransform.toMatrix(ctx, style, function("translate", px(10), percent(20)), bounds);

        // percent(20) of bounds.height (50) == 10
        assertMatrixEquals(result, 1, 0, 0, 1, 10, 10);
    }

    @Test
    void translateWithOnlyHorizontalArgumentDefaultsVerticalToZero() {
        AffineTransform result = CssTransform.toMatrix(ctx, style, function("translate", px(10)), bounds);

        assertMatrixEquals(result, 1, 0, 0, 1, 10, 0);
    }

    @Test
    void translateX() {
        AffineTransform result = CssTransform.toMatrix(ctx, style, function("translatex", px(15)), bounds);

        assertMatrixEquals(result, 1, 0, 0, 1, 15, 0);
    }

    @Test
    void translateY() {
        AffineTransform result = CssTransform.toMatrix(ctx, style, function("translatey", px(15)), bounds);

        assertMatrixEquals(result, 1, 0, 0, 1, 0, 15);
    }

    @Test
    void scaleWithOneArgumentAppliesToBothAxes() {
        AffineTransform result = CssTransform.toMatrix(ctx, style, function("scale", number(2)), bounds);

        assertMatrixEquals(result, 2, 0, 0, 2, 0, 0);
    }

    @Test
    void scaleWithTwoArguments() {
        AffineTransform result = CssTransform.toMatrix(ctx, style, function("scale", number(2), number(3)), bounds);

        assertMatrixEquals(result, 2, 0, 0, 3, 0, 0);
    }

    @Test
    void scaleX() {
        AffineTransform result = CssTransform.toMatrix(ctx, style, function("scalex", number(2)), bounds);

        assertMatrixEquals(result, 2, 0, 0, 1, 0, 0);
    }

    @Test
    void scaleY() {
        AffineTransform result = CssTransform.toMatrix(ctx, style, function("scaley", number(3)), bounds);

        assertMatrixEquals(result, 1, 0, 0, 3, 0, 0);
    }

    @Test
    void rotate90Degrees() {
        AffineTransform result = CssTransform.toMatrix(ctx, style, function("rotate", deg(90)), bounds);

        assertMatrixEquals(result, 0, 1, -1, 0, 0, 0);
    }

    @Test
    void skewWithBothArguments() {
        AffineTransform result = CssTransform.toMatrix(ctx, style, function("skew", deg(45), deg(0)), bounds);

        assertMatrixEquals(result, 1, 0, 1, 1, 0, 0);
    }

    @Test
    void skewX() {
        AffineTransform result = CssTransform.toMatrix(ctx, style, function("skewx", deg(45)), bounds);

        assertMatrixEquals(result, 1, 0, 1, 1, 0, 0);
    }

    @Test
    void skewY() {
        AffineTransform result = CssTransform.toMatrix(ctx, style, function("skewy", deg(45)), bounds);

        assertMatrixEquals(result, 1, 1, 0, 1, 0, 0);
    }

    @Test
    void angleToRadiansConvertsDegrees() {
        assertThat(CssTransform.angleToRadians(deg(180))).isCloseTo(Math.PI, TOLERANCE);
    }

    @Test
    void angleToRadiansPassesRadiansThrough() {
        assertThat(CssTransform.angleToRadians(rad(1.5f))).isCloseTo(1.5, TOLERANCE);
    }

    @Test
    void angleToRadiansConvertsGrads() {
        // 200 grad == 180 deg == pi radians
        assertThat(CssTransform.angleToRadians(grad(200))).isCloseTo(Math.PI, TOLERANCE);
    }

    @Test
    void toAffineTransformReturnsNullWhenThereIsNoTransform() {
        Box box = mock();
        when(box.getStyle()).thenReturn(style);
        when(style.getTransforms()).thenReturn(List.of());

        assertThat(CssTransform.toAffineTransform(ctx, box)).isNull();
    }

    @Test
    void toAffineTransformAnchorsRotationAtTheTransformOrigin() {
        Box box = mock();
        when(box.getStyle()).thenReturn(style);
        when(box.getPaintingBorderEdge(ctx)).thenReturn(new Rectangle(10, 20, 100, 50));
        when(style.getTransforms()).thenReturn(List.of(new PropertyValue(function("rotate", deg(90)))));
        // transform-origin: left top -- the box's top-left corner, at absolute position (10, 20)
        when(style.getTransformOrigin()).thenReturn(new BackgroundPosition(percent(0), percent(0)));

        AffineTransform result = CssTransform.toAffineTransform(ctx, box);

        // The origin point itself must stay fixed under a rotation about itself.
        Point2D origin = result.transform(new Point2D.Float(10, 20), null);
        assertThat(origin.getX()).isCloseTo(10, TOLERANCE);
        assertThat(origin.getY()).isCloseTo(20, TOLERANCE);

        // The box's top-right corner (110, 20) is 100 to the right of the origin;
        // rotating 90 degrees around the origin moves it 100 below the origin instead.
        Point2D corner = result.transform(new Point2D.Float(110, 20), null);
        assertThat(corner.getX()).isCloseTo(10, TOLERANCE);
        assertThat(corner.getY()).isCloseTo(120, TOLERANCE);
    }
}
