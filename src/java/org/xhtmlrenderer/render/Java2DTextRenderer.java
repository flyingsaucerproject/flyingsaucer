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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import org.xhtmlrenderer.extend.TextRenderer;


/**
 * Description of the Class
 *
 * @author   Joshua Marinacci
 * @author   Torbjörn Gannholm
 */
public class Java2DTextRenderer implements TextRenderer {

    /** Description of the Field */
    protected float scale = 1.0f;

    /** Description of the Field */
    protected float threshold = 25;

    /** Description of the Field */
    protected int level = HIGH;

    /**
     * Description of the Method
     *
     * @param graphics  PARAM
     * @param string    PARAM
     * @param x         PARAM
     * @param y         PARAM
     */
    public void drawString( Graphics2D graphics, String string, float x, float y ) {
        if ( graphics.getFont().getSize() > threshold && level > NONE ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        }
        graphics.drawString( string, (int)x, (int)y );
        if ( graphics.getFont().getSize() > threshold && level > NONE ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF );
        }
    }

    /**
     * Description of the Method
     *
     * @param graphics  PARAM
     */
    public void setupGraphics( Graphics2D graphics ) {
        //Uu.p("setup graphics called");
        graphics.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_OFF );
    }

    /**
     * Sets the fontScale attribute of the Java2DTextRenderer object
     *
     * @param scale  The new fontScale value
     */
    public void setFontScale( float scale ) {
        this.scale = scale;
    }

    /**
     * Sets the smoothingThreshold attribute of the Java2DTextRenderer object
     *
     * @param fontsize  The new smoothingThreshold value
     */
    public void setSmoothingThreshold( float fontsize ) {
        threshold = fontsize;
    }

    /**
     * Sets the smoothingLevel attribute of the Java2DTextRenderer object
     *
     * @param level  The new smoothingLevel value
     */
    public void setSmoothingLevel( int level ) {
        this.level = level;
    }

    /**
     * Gets the lineMetrics attribute of the Java2DTextRenderer object
     *
     * @param graphics  PARAM
     * @param font      PARAM
     * @param string    PARAM
     * @return          The lineMetrics value
     */
    public LineMetrics getLineMetrics( Graphics2D graphics, Font font, String string ) {
        return font.getLineMetrics( string, graphics.getFontRenderContext());
    }

    /**
     * Gets the logicalBounds attribute of the Java2DTextRenderer object
     *
     * @param graphics  PARAM
     * @param font      PARAM
     * @param string    PARAM
     * @return          The logicalBounds value
     */
    public Rectangle2D getLogicalBounds( Graphics2D graphics, Font font, String string ) {
        return graphics.getFontMetrics( font ).getStringBounds( string, graphics );
    }

    /**
     * Gets the fontScale attribute of the Java2DTextRenderer object
     *
     * @return   The fontScale value
     */
    public float getFontScale() {
        return this.scale;
    }

    /**
     * Gets the smoothingLevel attribute of the Java2DTextRenderer object
     *
     * @return   The smoothingLevel value
     */
    public int getSmoothingLevel() {
        return level;
    }

}

