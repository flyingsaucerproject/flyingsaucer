/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
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
package org.xhtmlrenderer.pdf;

import org.openpdf.text.pdf.BaseFont;
import org.xhtmlrenderer.extend.FSGlyphVector;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.FSFontMetrics;
import org.xhtmlrenderer.render.JustificationInfo;

import java.awt.*;

public class ITextTextRenderer implements TextRenderer<ITextOutputDevice, ITextFSFont> {
    private static final float TEXT_MEASURING_DELTA = 0.01f;

    @Override
    public void setup(FontContext context) {
    }

    @Override
    public void drawString(ITextOutputDevice outputDevice, String string, float x, float y) {
        outputDevice.drawString(string, x, y, null);
    }

    @Override
    public void drawString(ITextOutputDevice outputDevice, String string, float x, float y, JustificationInfo info) {
        outputDevice.drawString(string, x, y, info);
    }

    @Override
    public FSFontMetrics getFSFontMetrics(FontContext context, ITextFSFont font, String string) {
        FontDescription description = font.getFontDescription();
        BaseFont bf = description.getFont();
        float size = font.getSize2D();
        float strikethroughThickness = description.getYStrikeoutSize() != 0 ?
                description.getYStrikeoutSize() / 1000.0f * size :
                size / 12.0f;

        return new ITextFSFontMetrics(
                bf.getFontDescriptor(BaseFont.BBOXURY, size),
                -bf.getFontDescriptor(BaseFont.BBOXLLY, size),
                -description.getYStrikeoutPosition() / 1000.0f * size,
                strikethroughThickness,
                -description.getUnderlinePosition() / 1000.0f * size,
                description.getUnderlineThickness() / 1000.0f * size
        );
    }

    @Override
    public int getWidth(FontContext context, ITextFSFont font, String string) {
        BaseFont bf = font.getFontDescription().getFont();
        float result = bf.getWidthPoint(string, font.getSize2D());
        if (result - Math.floor(result) < TEXT_MEASURING_DELTA) {
            return (int)result;
        } else {
            return (int)Math.ceil(result);
        }
    }

    @Override
    public void setFontScale(float scale) {
    }

    @Override
    public float getFontScale() {
        return 1.0f;
    }

    @Override
    public void setSmoothingThreshold(float fontsize) {
    }

    @Override
    public Rectangle getGlyphBounds(ITextOutputDevice outputDevice, ITextFSFont font, FSGlyphVector fsGlyphVector, int index, float x, float y) {
        throw new UnsupportedOperationException("Unsupported operation: getGlyphBounds");
    }

    @Override
    public float[] getGlyphPositions(ITextOutputDevice outputDevice, ITextFSFont font, FSGlyphVector fsGlyphVector) {
        throw new UnsupportedOperationException("Unsupported operation: getGlyphPositions");
    }

    @Override
    public FSGlyphVector getGlyphVector(ITextOutputDevice outputDevice, ITextFSFont font, String string) {
        throw new UnsupportedOperationException("Unsupported operation: getGlyphVector");
    }

    @Override
    public void drawGlyphVector(ITextOutputDevice outputDevice, FSGlyphVector vector, float x, float y) {
        throw new UnsupportedOperationException("Unsupported operation: drawGlyphVector");
    }
}
