package org.xhtmlrenderer.css.style;

import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.render.FSFont;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-jun-23
 * Time: 00:12:50
 * To change this template use File | Settings | File Templates.
 */
public interface CssContext {
    float getMmPerPx();

    float getFontSize2D(FontSpecification font);

    float getXHeight(FontSpecification parentFont);

    float getFontSizeForXHeight(FontSpecification parent, FontSpecification desired, float xHeight);

    FSFont getFont(FontSpecification font);
}
