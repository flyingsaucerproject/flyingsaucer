package org.xhtmlrenderer.css.style;

import org.xhtmlrenderer.css.value.FontSpecification;

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
    
    IStyleReference getCss();
    
    FSFontMetrics getFSFontMetrics(FSFont font);
}
