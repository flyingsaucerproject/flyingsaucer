package org.xhtmlrenderer.css.style.derived;

import org.jspecify.annotations.Nullable;

import java.awt.*;

public record NullableInsets(
    @Nullable Integer top,
    @Nullable Integer left,
    @Nullable Integer bottom,
    @Nullable Integer right
) {
    public Insets withDefaults(Insets defaults) {
        int top = atLeast(top(), defaults.top);
        int left = atLeast(left(), defaults.left);
        int bottom = atLeast(bottom(), defaults.bottom);
        int right = atLeast(right(), defaults.right);
        return new Insets(top, left, bottom, right);
    }

    private int atLeast(@Nullable Integer value, int defaultValue) {
        return value == null ? defaultValue : Math.max(defaultValue, value);
    }
}
