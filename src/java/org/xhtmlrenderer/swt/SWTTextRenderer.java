/*
 * {{{ header & license
 * Copyright (c) 2007 Vianney le Clément
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
package org.xhtmlrenderer.swt;

import java.awt.Rectangle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.xhtmlrenderer.extend.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.util.Configuration;

/**
 * Render text with SWT.
 * 
 * @author Vianney le Clément
 * 
 */
public class SWTTextRenderer implements TextRenderer {

    private float _scale;
    private boolean _antialiasing;

    public SWTTextRenderer() {
        _scale = Configuration.valueAsFloat("xr.text.scale", 1.0f);
        setSmoothingThreshold(Configuration.valueAsInt(
            "xr.text.aa-fontsize-threshhold", 0));
    }

    public void setup(FontContext context) {
        GC gc = ((SWTFontContext) context).getGC();
        gc.setTextAntialias(_antialiasing ? SWT.ON : SWT.OFF);
    }

    public void drawString(OutputDevice outputDevice, String string, float x,
            float y) {
        GC gc = ((SWTOutputDevice) outputDevice).getGC();
        FontMetrics metrics = gc.getFontMetrics();
        y -= (metrics.getAscent() + metrics.getLeading());
        gc.drawText(string, (int) x, (int) y, SWT.DRAW_TRANSPARENT);
    }

    public FSFontMetrics getFSFontMetrics(FontContext context, FSFont font,
            String string) {
        return new SWTFontMetricsAdapter((SWTFontContext) context,
            (SWTFSFont) font);
    }

    public int getWidth(FontContext context, FSFont font, String string) {
        GC gc = ((SWTFontContext) context).getGC();
        Font previous = gc.getFont();
        gc.setFont(((SWTFSFont) font).getSWTFont());
        int width = gc.stringExtent(string).x;
        gc.setFont(previous);
        return width;
    }

    public float getFontScale() {
        return _scale;
    }

    public int getSmoothingLevel() {
        return 0;
    }

    public void setFontScale(float scale) {
        _scale = scale;
    }

    public void setSmoothingLevel(int level) {
        /* no-op */
    }

    public void setSmoothingThreshold(float fontsize) {
        _antialiasing = (fontsize >= 0);
    }

    public void drawGlyphVector(OutputDevice outputDevice, FSGlyphVector vector, float x, float y) {
        throw new UnsupportedOperationException();
    }

    public void drawString(OutputDevice outputDevice, String string, float x, float y,
            JustificationInfo info) {
        // TODO handle justification
        drawString(outputDevice, string, x, y);
    }

    public Rectangle getGlyphBounds(OutputDevice outputDevice, FSFont font,
            FSGlyphVector fsGlyphVector, int index, float x, float y) {
        throw new UnsupportedOperationException();
    }

    public float[] getGlyphPositions(OutputDevice outputDevice, FSFont font,
            FSGlyphVector fsGlyphVector) {
        throw new UnsupportedOperationException();
    }

    public FSGlyphVector getGlyphVector(OutputDevice outputDevice, FSFont font, String string) {
        throw new UnsupportedOperationException();
    }

}
