package org.xhtmlrenderer.css.style;

import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.FSFontMetrics;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-jun-23
 * Time: 00:12:50
 * To change this template use File | Settings | File Templates.
 */
public interface CssContext {
    float getMmPerDot();
    
    int getDotsPerPixel();

    float getFontSize2D(FontSpecification font);

    float getXHeight(FontSpecification parentFont);

    FSFont getFont(FontSpecification font);
    
    // FIXME Doesn't really belong here, but this is
    // the only common interface of LayoutContext
    // and RenderingContext
    StyleReference getCss();
    
    FSFontMetrics getFSFontMetrics(FSFont font);
}
