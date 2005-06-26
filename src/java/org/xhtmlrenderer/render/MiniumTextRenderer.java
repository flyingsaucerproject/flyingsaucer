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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.render;

import org.sektor37.minium.TextRendererFactory;
import org.sektor37.minium.TextRenderingHints;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.Map;


/**
 * Description of the Class
 *
 * @author Joshua Marinacci
 * @author Torbjörn Gannholm
 */
public class MiniumTextRenderer implements org.xhtmlrenderer.extend.TextRenderer {
    /**
     * Description of the Field
     */
    public org.sektor37.minium.TextRenderer renderer;

    /**
     * Description of the Field
     */
    protected float scale = 1.0f;

    /**
     * Description of the Field
     */
    protected int level = HIGH;

    /**
     * Constructor for the MiniumTextRenderer object
     */
    public MiniumTextRenderer() {
        TextRendererFactory text_renderer_factory = TextRendererFactory.newOversamplingInstance();
        renderer = text_renderer_factory.newTextRenderer();

        String text_renderer_quality = null;
        try {
            System.getProperty("org.xhtmlrenderer.minium.quality");
        } catch (SecurityException e) {
            System.err.println(e.getLocalizedMessage());
        }
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

    /**
     * Description of the Method
     *
     * @param graphics PARAM
     * @param string   PARAM
     * @param x        PARAM
     * @param y        PARAM
     */
    public void drawString(Graphics2D graphics, String string, float x, float y) {
        renderer.drawString(graphics, string, x, y);
    }

    /**
     * Description of the Method
     *
     * @param graphics PARAM
     */
    public void setupGraphics(Graphics2D graphics) {
    }

    /**
     * Sets the fontScale attribute of the MiniumTextRenderer object
     *
     * @param scale The new fontScale value
     */
    public void setFontScale(float scale) {
        this.scale = scale;
    }

    /*
     * set to -1 for no antialiasing. set to 0 for all antialising.
     * else, set to the threshold font size. does not take font scaling
     * into account.
     */
    /**
     * Sets the smoothingThreshold attribute of the MiniumTextRenderer object
     *
     * @param fontsize The new smoothingThreshold value
     */
    public void setSmoothingThreshold(float fontsize) {
        renderer.setTextRenderingHint(TextRenderingHints.KEY_OVERSAMPLING_MIN_FONTSIZE, new Integer((int) fontsize));
    }

    /**
     * Sets the smoothingLevel attribute of the MiniumTextRenderer object
     *
     * @param level The new smoothingLevel value
     */
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

    /**
     * Gets the lineMetrics attribute of the MiniumTextRenderer object
     *
     * @param graphics PARAM
     * @param font     PARAM
     * @param string   PARAM
     * @return The lineMetrics value
     */
    public LineMetrics getLineMetrics(Graphics2D graphics, Font font, String string) {
        return renderer.getLineMetrics(graphics, font, string);
    }

    /**
     * Gets the logicalBounds attribute of the MiniumTextRenderer object
     *
     * @param graphics PARAM
     * @param font     PARAM
     * @param string   PARAM
     * @return The logicalBounds value
     */
    public Rectangle2D getLogicalBounds(Graphics2D graphics, Font font, String string) {
        return renderer.getLogicalBounds(graphics, font, string);
    }

    /**
     * Gets the fontScale attribute of the MiniumTextRenderer object
     *
     * @return The fontScale value
     */
    public float getFontScale() {
        return this.scale;
    }

    /**
     * Gets the smoothingLevel attribute of the MiniumTextRenderer object
     *
     * @return The smoothingLevel value
     */
    public int getSmoothingLevel() {
        return level;
    }

}

