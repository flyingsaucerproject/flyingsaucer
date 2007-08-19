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

import java.awt.Shape;

import org.xhtmlrenderer.render.BlockBox;

public class BoxRangeData {
    private BlockBox _box;
    private BoxRange _range;
    
    private Shape _clip;
    
    public BoxRangeData() {
    }
    
    public BoxRangeData(BlockBox box, BoxRange range) {
        _box = box;
        _range = range;
    }
    
    public BlockBox getBox() {
        return _box;
    }
    
    public void setBox(BlockBox box) {
        _box = box;
    }

    public BoxRange getRange() {
        return _range;
    }

    public void setRange(BoxRange range) {
        _range = range;
    }

    public Shape getClip() {
        return _clip;
    }

    public void setClip(Shape clip) {
        _clip = clip;
    }
    
    public String toString() {
        return "[range= " + _range + ", box=" + _box + ", clip=" + _clip + "]";
    }
}
