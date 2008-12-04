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

import java.awt.Rectangle;

import org.xhtmlrenderer.extend.FSGlyphVector;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.pdf.ITextFontResolver.FontDescription;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.FSFontMetrics;
import org.xhtmlrenderer.render.JustificationInfo;

import com.lowagie.text.pdf.BaseFont;

public class ITextTextRenderer implements TextRenderer {
    private static float TEXT_MEASURING_DELTA = 0.01f;
    
    public void setup(FontContext context) {
    }

    public void drawString(OutputDevice outputDevice, String string, float x, float y) {
        ((ITextOutputDevice)outputDevice).drawString(string, x, y, null);
    }
    
    public void drawString(
            OutputDevice outputDevice, String string, float x, float y, JustificationInfo info) {
        ((ITextOutputDevice)outputDevice).drawString(string, x, y, info);
    }

    public FSFontMetrics getFSFontMetrics(FontContext context, FSFont font, String string) {
        FontDescription descr = ((ITextFSFont)font).getFontDescription();
        BaseFont bf = descr.getFont();
        float size = font.getSize2D();
        ITextFSFontMetrics result = new ITextFSFontMetrics();
        result.setAscent(bf.getFontDescriptor(BaseFont.BBOXURY, size));
        result.setDescent(-bf.getFontDescriptor(BaseFont.BBOXLLY, size));
        
        result.setStrikethroughOffset(-descr.getYStrikeoutPosition() / 1000f * size);
        if (descr.getYStrikeoutSize() != 0) {
            result.setStrikethroughThickness(descr.getYStrikeoutSize() / 1000f * size);
        } else {
            result.setStrikethroughThickness(size / 12.0f);
        }
        
        result.setUnderlineOffset(-descr.getUnderlinePosition() / 1000f * size);
        result.setUnderlineThickness(descr.getUnderlineThickness() / 1000f * size);
        
        return result;
    }

    public int getWidth(FontContext context, FSFont font, String string) {
        BaseFont bf = ((ITextFSFont)font).getFontDescription().getFont();
        float result = bf.getWidthPoint(string, font.getSize2D());
        if (result - Math.floor(result) < TEXT_MEASURING_DELTA) {
            return (int)result;
        } else {
            return (int)Math.ceil(result); 
        }
    }

    public void setFontScale(float scale) {
    }

    public float getFontScale() {
        return 1.0f;
    }

    public void setSmoothingThreshold(float fontsize) {
    }

    public int getSmoothingLevel() {
        return 0;
    }

    public void setSmoothingLevel(int level) {
    }

    public Rectangle getGlyphBounds(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector, int index, float x, float y) {
        throw new UnsupportedOperationException();
    }

    public float[] getGlyphPositions(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector) {
        throw new UnsupportedOperationException();
    }

    public FSGlyphVector getGlyphVector(OutputDevice outputDevice, FSFont font, String string) {
        throw new UnsupportedOperationException();
    }

    public void drawGlyphVector(OutputDevice outputDevice, FSGlyphVector vector, float x, float y) {
        throw new UnsupportedOperationException();
    }
}
