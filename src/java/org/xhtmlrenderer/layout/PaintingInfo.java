/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
package org.xhtmlrenderer.layout;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * A bean which every box uses to provide its aggregate bounds (which may be 
 * larger than the bounds of the box itself when there is overhanging content)
 * and its outer margin corner (which is used to calculate the size of the
 * canvas).  The aggregate bounds calculation does not take the value of the
 * overflow property into account.
 */
public class PaintingInfo {
    private Dimension _outerMarginCorner;
    private Rectangle _aggregateBounds;
    
    public PaintingInfo() {
    }
    
    public Rectangle getAggregateBounds() {
        return _aggregateBounds;
    }
    
    public void setAggregateBounds(Rectangle aggregateBounds) {
        _aggregateBounds = aggregateBounds;
    }
    
    public Dimension getOuterMarginCorner() {
        return _outerMarginCorner;
    }
    
    public void setOuterMarginCorner(Dimension outerMarginCorner) {
        _outerMarginCorner = outerMarginCorner;
    }
    
    public PaintingInfo copyOf() {
        PaintingInfo result = new PaintingInfo();
        result.setOuterMarginCorner(new Dimension(_outerMarginCorner));
        result.setAggregateBounds(new Rectangle(_aggregateBounds));
        
        return result;
    }
    
    public void translate(int tx, int ty) {
        _aggregateBounds.translate(tx, ty);
        _outerMarginCorner.setSize(
                _outerMarginCorner.getWidth()+tx, _outerMarginCorner.getHeight()+ty);
    }
}
