package org.xhtmlrenderer.css.style;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.FSFontMetrics;

/**
 * User: tobe
 * Date: 2005-jun-23
 */
@CheckReturnValue
public interface CssContext {
    float getMmPerDot();

    int getDotsPerPixel();

    float getFontSize2D(FontSpecification font);

    float getXHeight(FontSpecification parentFont);

    @Nullable
    FSFont getFont(FontSpecification font);

    // FIXME Doesn't really belong here, but this is
    // the only common interface of LayoutContext
    // and RenderingContext
    StyleReference getCss();

    TextRenderer getTextRenderer();

    FontContext getFontContext();

    FSFontMetrics getFSFontMetrics(FSFont font);
}
