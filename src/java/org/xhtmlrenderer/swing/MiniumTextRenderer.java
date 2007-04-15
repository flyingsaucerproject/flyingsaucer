/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjšrn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swing;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Map;

import org.sektor37.minium.TextRendererFactory;
import org.sektor37.minium.TextRenderingHints;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.FSFontMetrics;
import org.xhtmlrenderer.render.LineMetricsAdapter;
import org.xhtmlrenderer.util.Configuration;


/**
 * @author Joshua Marinacci
 * @author Torbjörn Gannholm
 */
public class MiniumTextRenderer implements org.xhtmlrenderer.extend.TextRenderer {
    public org.sektor37.minium.TextRenderer renderer;

    protected float scale;
    protected int level;

    public MiniumTextRenderer() {
        scale = Configuration.valueAsFloat("xr.text.scale", 1.0f);
        level = Configuration.valueAsInt("xr.text.aa-smoothing-level", HIGH);

        TextRendererFactory text_renderer_factory = TextRendererFactory.newOversamplingInstance();
        renderer = text_renderer_factory.newTextRenderer();

        String text_renderer_quality = Configuration.valueFor("xr.text.aa-smoothing.minium", "lowest");

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

    public void drawString(OutputDevice outputDevice, String string, float x, float y ) {
        renderer.drawString(((Java2DOutputDevice)outputDevice).getGraphics(), string, x, y);
    }

    public void setup(FontContext context) {
    }

    public void setFontScale(float scale) {
        this.scale = scale;
    }

    /*
     * set to -1 for no antialiasing. set to 0 for all antialising.
     * else, set to the threshold font size. does not take font scaling
     * into account.
     */
    public void setSmoothingThreshold(float fontsize) {
        renderer.setTextRenderingHint(TextRenderingHints.KEY_OVERSAMPLING_MIN_FONTSIZE, new Integer((int) fontsize));
    }

    public void setSmoothingLevel(int level) {
        this.level = level;
        Map defaultHints = TextRenderingHints.DEFAULT_HINTS_FASTEST;
        if (level == NONE) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_FASTEST;
        }
        if (level == LOW) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_LOW;
        }
        if (level == MEDIUM) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_MEDIUM;
        }
        if (level == HIGH) {
            defaultHints = TextRenderingHints.DEFAULT_HINTS_QUALITY_HIGHEST;
        }
        renderer.setTextRenderingHints(defaultHints);
    }

    public FSFontMetrics getFSFontMetrics(
            FontContext fontContext, FSFont font, String string ) {
        Java2DFontContext fc = (Java2DFontContext)fontContext;
        Font awtFont = ((AWTFSFont)font).getAWTFont();
        return new LineMetricsAdapter(
                renderer.getLineMetrics(fc.getGraphics(), awtFont, string));
    }
    
    public int getWidth(FontContext fontContext, FSFont font, String string) {
        Java2DFontContext fc = (Java2DFontContext)fontContext;
        Graphics2D graphics = fc.getGraphics();
        Font awtFont = ((AWTFSFont)font).getAWTFont();
        return (int)Math.ceil(renderer.getLogicalBounds(
                graphics, awtFont, string).getWidth());
    }

    public float getFontScale() {
        return this.scale;
    }

    public int getSmoothingLevel() {
        return level;
    }

}

