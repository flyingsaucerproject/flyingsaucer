package org.xhtmlrenderer.render;

import java.awt.Graphics2D;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

import org.xhtmlrenderer.extend.TextRenderer;

public class Java2DTextRenderer implements TextRenderer {
    public void setupGraphics(Graphics2D graphics) {
//        graphics.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }
    
    public void drawString(Graphics2D graphics, String string, float x, float y) {
        if(graphics.getFont().getSize() > threshold && level > NONE) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        }
        graphics.drawString(string,(int)x,(int)y);
        if(graphics.getFont().getSize() > threshold && level > NONE) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF );
        }
    }
    
    public LineMetrics getLineMetrics(Graphics2D graphics, Font font, String string) { 
        return font.getLineMetrics(string,graphics.getFontRenderContext());
    }
    public Rectangle2D getLogicalBounds(Graphics2D graphics, Font font, String string) { 
        return graphics.getFontMetrics(font).getStringBounds(string,graphics);
    }
    
    protected float scale = 1.0f;
    public void setFontScale(float scale) {
        this.scale = scale;
    }
    public float getFontScale() {
        return this.scale;
    }

    protected float threshold = -1;
    public void setSmoothingThreshold(float fontsize) {
        threshold = fontsize;
    }

    protected int level = HIGH;
    public int getSmoothingLevel() {
        return level;
    }
    public void setSmoothingLevel(int level) { 
        this.level = level;
    }
    
}
