package org.xhtmlrenderer.extend;

import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;
import org.xhtmlrenderer.render.*;
import org.sektor37.minium.*;

public class MiniumTextRenderer implements TextRenderer {
    public org.sektor37.minium.TextRenderer renderer;
    public MiniumTextRenderer() {
        TextRendererFactory text_renderer_factory = TextRendererFactory.newOversamplingInstance();
        renderer = text_renderer_factory.newTextRenderer();
        
        String text_renderer_quality = System.getProperty("org.xhtmlrenderer.minium.quality");
        if (null == text_renderer_quality) {
            text_renderer_quality = "lowest";
        }

        Map defaultHints;
        if ("low".equals(text_renderer_quality)) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_LOW;
        } else if ("medium".equals(text_renderer_quality)) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_MEDIUM;
        } else if ("high".equals(text_renderer_quality)) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_HIGH;
        } else if ("highest".equals(text_renderer_quality)) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_HIGHEST;
        } else {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_FASTEST;
        }
        renderer.setTextRenderingHints(defaultHints);
    }
    public void setupGraphics(Graphics2D graphics) {
    }
    public void drawString(Graphics2D graphics, String string, float x, float y) {
        renderer.drawString(graphics,string,x,y);
    }
    public LineMetrics getLineMetrics(Graphics2D graphics, Font font, String string) {
        return renderer.getLineMetrics(graphics, font, string);
    }
    public Rectangle2D getLogicalBounds(Graphics2D graphics, Font font, String string) {
        return renderer.getLogicalBounds(graphics,font,string);
    }

    protected float scale = 1.0f;
    public void setFontScale(float scale) {
        this.scale = scale;
    }
    public float getFontScale() {
        return this.scale;
    }

    /* set to -1 for no antialiasing. set to 0 for all antialising.
    else, set to the threshold font size. does not take font scaling
    into account. */

    public void setSmoothingThreshold(float fontsize) {
        renderer.setTextRenderingHint(TextRenderingHints.KEY_OVERSAMPLING_MIN_FONTSIZE, new Integer((int)fontsize));
    }
    protected int level = HIGH;
    public int getSmoothingLevel() {
        return level;
    }
    public void setSmoothingLevel(int level) {
        this.level = level;
        Map defaultHints = TextRenderingHints.DEFAULT_HINTS_FASTEST;
        if(level == NONE) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_FASTEST;
        }
        if(level == LOW) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_LOW;
        }
        if(level == MEDIUM) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_MEDIUM;
        }
        if(level == HIGH) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_HIGHEST;
        }
        renderer.setTextRenderingHints(defaultHints);
    }
    
}
