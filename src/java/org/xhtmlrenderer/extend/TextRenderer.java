/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 
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
package org.xhtmlrenderer.extend;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;


/**
 * Description of the Interface
 *
 * @author Who?
 */
public interface TextRenderer {

    /**
     * Description of the Method
     *
     * @param graphics PARAM
     */
    public void setupGraphics(Graphics2D graphics);

    /**
     * Description of the Method
     *
     * @param graphics PARAM
     * @param string   PARAM
     * @param x        PARAM
     * @param y        PARAM
     */
    public void drawString(Graphics2D graphics, String string, float x, float y);

    /**
     * Gets the lineMetrics attribute of the TextRenderer object
     *
     * @param graphics PARAM
     * @param font     PARAM
     * @param string   PARAM
     * @return The lineMetrics value
     */
    public LineMetrics getLineMetrics(Graphics2D graphics, Font font, String string);

    /**
     * Gets the logicalBounds attribute of the TextRenderer object
     *
     * @param graphics PARAM
     * @param font     PARAM
     * @param string   PARAM
     * @return The logicalBounds value
     */
    public Rectangle2D getLogicalBounds(Graphics2D graphics, Font font, String string);

    /**
     * Sets the fontScale attribute of the TextRenderer object
     *
     * @param scale The new fontScale value
     */
    public void setFontScale(float scale);

    /**
     * Gets the fontScale attribute of the TextRenderer object
     *
     * @return The fontScale value
     */
    public float getFontScale();

    /*
     * set to -1 for no antialiasing. set to 0 for all antialising.
     * else, set to the threshold font size. does not take font scaling
     * into account.
     */
    /**
     * Sets the smoothingThreshold attribute of the TextRenderer object
     *
     * @param fontsize The new smoothingThreshold value
     */
    public void setSmoothingThreshold(float fontsize);

    /**
     * Gets the smoothingLevel attribute of the TextRenderer object
     *
     * @return The smoothingLevel value
     */
    public int getSmoothingLevel();

    /**
     * Sets the smoothingLevel attribute of the TextRenderer object
     *
     * @param level The new smoothingLevel value
     */
    public void setSmoothingLevel(int level);

    /**
     * Description of the Field
     */
    public static final int NONE = 0;
    /**
     * Description of the Field
     */
    public static final int LOW = 1;
    /**
     * Description of the Field
     */
    public static final int MEDIUM = 2;
    /**
     * Description of the Field
     */
    public static final int HIGH = 3;
}

