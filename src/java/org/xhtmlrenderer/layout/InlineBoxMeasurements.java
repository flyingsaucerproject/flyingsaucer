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

public class InlineBoxMeasurements {
    private int textTop;
    private int textBottom;
    private int baseline;
    private int inlineTop;
    private int inlineBottom;
    
    private int paintingTop;
    private int paintingBottom;
    
    private boolean containsContent;
    
    public InlineBoxMeasurements() {
        
    }

    public int getBaseline() {
        return baseline;
    }

    public void setBaseline(int baseline) {
        this.baseline = baseline;
    }

    public int getInlineBottom() {
        return inlineBottom;
    }

    public void setInlineBottom(int inlineBottom) {
        this.inlineBottom = inlineBottom;
    }

    public int getInlineTop() {
        return inlineTop;
    }

    public void setInlineTop(int inlineTop) {
        this.inlineTop = inlineTop;
    }

    public int getTextBottom() {
        return textBottom;
    }

    public void setTextBottom(int textBottom) {
        this.textBottom = textBottom;
    }

    public int getTextTop() {
        return textTop;
    }

    public void setTextTop(int textTop) {
        this.textTop = textTop;
    }

    public boolean isContainsContent() {
        return containsContent;
    }

    public void setContainsContent(boolean containsContent) {
        this.containsContent = containsContent;
    }

    public int getPaintingBottom() {
        return paintingBottom;
    }

    public void setPaintingBottom(int paintingBottom) {
        this.paintingBottom = paintingBottom;
    }

    public int getPaintingTop() {
        return paintingTop;
    }

    public void setPaintingTop(int paintingTop) {
        this.paintingTop = paintingTop;
    }
}
