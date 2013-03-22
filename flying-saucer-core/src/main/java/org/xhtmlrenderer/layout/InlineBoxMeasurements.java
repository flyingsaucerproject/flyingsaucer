/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.layout;

/**
 * A bean which tracks various charactistics of an inline box.  It is used
 * when calculating the vertical position of boxes in a line.  
 */
public class InlineBoxMeasurements {
    private int _textTop;
    private int _textBottom;
    private int _baseline;
    private int _inlineTop;
    private int _inlineBottom;
    
    private int _paintingTop;
    private int _paintingBottom;
    
    public InlineBoxMeasurements() {
        
    }

    public int getBaseline() {
        return _baseline;
    }

    public void setBaseline(int baseline) {
        _baseline = baseline;
    }

    public int getInlineBottom() {
        return _inlineBottom;
    }

    public void setInlineBottom(int inlineBottom) {
        _inlineBottom = inlineBottom;
    }

    public int getInlineTop() {
        return _inlineTop;
    }

    public void setInlineTop(int inlineTop) {
        _inlineTop = inlineTop;
    }

    public int getTextBottom() {
        return _textBottom;
    }

    public void setTextBottom(int textBottom) {
        _textBottom = textBottom;
    }

    public int getTextTop() {
        return _textTop;
    }

    public void setTextTop(int textTop) {
        _textTop = textTop;
    }

    public int getPaintingBottom() {
        return _paintingBottom;
    }

    public void setPaintingBottom(int paintingBottom) {
        _paintingBottom = paintingBottom;
    }

    public int getPaintingTop() {
        return _paintingTop;
    }

    public void setPaintingTop(int paintingTop) {
        _paintingTop = paintingTop;
    }
}
