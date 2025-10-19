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

import com.google.errorprone.annotations.CheckReturnValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.xhtmlrenderer.extend.FSGlyphVector;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.FSFontMetrics;
import org.xhtmlrenderer.render.JustificationInfo;
import org.xhtmlrenderer.util.Configuration;

import java.awt.*;

/**
 * Render text with SWT.
 *
 * @author Vianney le Clément
 */
public final class SWTTextRenderer implements TextRenderer<SWTOutputDevice, SWTFontContext, SWTFSFont> {

    private float _scale;
    private boolean _antialiasing = false;

    public SWTTextRenderer() {
        _scale = Configuration.valueAsFloat("xr.text.scale", 1.0f);
        setSmoothingThreshold(Configuration.valueAsInt(
            "xr.text.aa-fontsize-threshhold", 0));
    }

    @Override
    public void setup(SWTFontContext context) {
        GC gc = context.getGC();
        gc.setTextAntialias(_antialiasing ? SWT.ON : SWT.OFF);
    }

    @Override
    public void drawString(SWTOutputDevice outputDevice, String string, float x,
            float y) {
        GC gc = outputDevice.getGC();
        FontMetrics metrics = gc.getFontMetrics();
        y -= (metrics.getAscent() + metrics.getLeading());
        gc.drawText(string, (int) x, (int) y, SWT.DRAW_TRANSPARENT);
    }

    @CheckReturnValue
    @Override
    public FSFontMetrics getFSFontMetrics(SWTFontContext context, SWTFSFont font, String string) {
        return new SWTFontMetricsAdapter(context, font);
    }

    @Override
    public int getWidth(SWTFontContext context, SWTFSFont font, String string) {
        GC gc = context.getGC();
        Font previous = gc.getFont();
        gc.setFont(font.getSWTFont());
        int width = gc.stringExtent(string).x;
        gc.setFont(previous);
        return width;
    }

    @Override
    public float getFontScale() {
        return _scale;
    }

    @Override
    public void setFontScale(float scale) {
        _scale = scale;
    }

    @Override
    public void setSmoothingThreshold(float fontsize) {
        _antialiasing = (fontsize >= 0);
    }

    @Override
    public void drawGlyphVector(SWTOutputDevice outputDevice, FSGlyphVector vector, float x, float y) {
        throw new UnsupportedOperationException("Unsupported operation: drawGlyphVector");
    }

    @Override
    public void drawString(SWTOutputDevice outputDevice, String string, float x, float y,
            JustificationInfo info) {
        // TODO handle justification
        drawString(outputDevice, string, x, y);
    }

    @Override
    public Rectangle getGlyphBounds(SWTOutputDevice outputDevice, SWTFSFont font,
            FSGlyphVector fsGlyphVector, int index, float x, float y) {
        throw new UnsupportedOperationException("Unsupported operation: getGlyphBounds");
    }

    @Override
    public float[] getGlyphPositions(SWTOutputDevice outputDevice, SWTFSFont font,
            FSGlyphVector fsGlyphVector) {
        throw new UnsupportedOperationException("Unsupported operation: getGlyphPositions");
    }

    @Override
    public FSGlyphVector getGlyphVector(SWTOutputDevice outputDevice, SWTFSFont font, String string) {
        throw new UnsupportedOperationException("Unsupported operation: getGlyphVector");
    }

}
