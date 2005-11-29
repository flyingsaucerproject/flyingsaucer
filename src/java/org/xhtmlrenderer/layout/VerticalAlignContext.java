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

import java.util.ArrayList;
import java.util.List;

public class VerticalAlignContext {
    private List measurements = new ArrayList();
    
    private int inlineTop;
    private boolean inlineTopSet = false;
    
    private int inlineBottom;
    private boolean inlineBottomSet = false;
    
    private int paintingTop;
    private boolean paintingTopSet = false;
    
    private int paintingBottom;
    private boolean paintingBottomSet = false;
    
    public int getInlineBottom() {
        return inlineBottom;
    }

    public int getInlineTop() {
        return inlineTop;
    }

    public void updateInlineTop(int inlineTop) {
        if (! inlineTopSet || inlineTop < this.inlineTop) {
            this.inlineTop = inlineTop;
            inlineTopSet = true;
        }
    }
    
    public void updatePaintingTop(int paintingTop) {
        if (! paintingTopSet || paintingTop < this.paintingTop) {
            this.paintingTop = paintingTop;
            paintingTopSet = true;
        }
    }
    
    public void updateInlineBottom(int inlineBottom) {
        if (! inlineBottomSet || inlineBottom > this.inlineBottom) {
            this.inlineBottom = inlineBottom;
            inlineBottomSet = true;
        }
    }
    
    public void updatePaintingBottom(int paintingBottom) {
        if (! paintingBottomSet || paintingBottom > this.paintingBottom) {
            this.paintingBottom = paintingBottom;
            paintingBottomSet = true;
        }
    }    
    
    public int getLineBoxHeight() {
        return this.inlineBottom - this.inlineTop;
    }
    
    public void pushMeasurements(InlineBoxMeasurements measurements) {
        this.measurements.add(measurements);
        
        if (measurements.isContainsContent()) {
            updateInlineTop(measurements.getInlineTop());
            updateInlineBottom(measurements.getInlineBottom());
            
            updatePaintingTop(measurements.getPaintingTop());
            updatePaintingBottom(measurements.getPaintingBottom());
        }
    }
    
    public InlineBoxMeasurements getParentMeasurements() {
        return (InlineBoxMeasurements)this.measurements.get(this.measurements.size()-1);
    }
    
    public void popMeasurements() {
        this.measurements.remove(this.measurements.size()-1);
    }

    public int getPaintingBottom() {
        return paintingBottom;
    }

    public int getPaintingTop() {
        return paintingTop;
    }
}
