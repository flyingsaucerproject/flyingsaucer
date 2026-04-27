package org.xhtmlrenderer.layout;

import com.google.errorprone.annotations.CheckReturnValue;

public interface CssFunction {
    @CheckReturnValue
    String evaluate();
}
