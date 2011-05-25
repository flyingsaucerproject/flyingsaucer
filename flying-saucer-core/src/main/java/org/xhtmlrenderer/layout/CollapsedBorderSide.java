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

import org.xhtmlrenderer.newtable.CollapsedBorderValue;
import org.xhtmlrenderer.newtable.TableCellBox;
import org.xhtmlrenderer.render.BorderPainter;

/**
 * A class that contains a single border side of a collapsed cell.  Collapsed
 * border sides are painted in order of priority (so for example, wider borders
 * always paint over narrower borders regardless of the relative tree order of
 * the cells in question). 
 */
public class CollapsedBorderSide implements Comparable {
    private TableCellBox _cell;
    private int _side;
    
    public CollapsedBorderSide(TableCellBox cell, int side) {
        _side = side;
        _cell = cell;
    }
    
    public TableCellBox getCell() {
        return _cell;
    }
    
    public void setCell(TableCellBox cell) {
        _cell = cell;
    }
    
    public int getSide() {
        return _side;
    }
    
    public void setSide(int side) {
        _side = side;
    }
    
    public int compareTo(Object obj) {
        CollapsedBorderSide c1 = this;
        CollapsedBorderSide c2 = (CollapsedBorderSide)obj;
        
        CollapsedBorderValue v1 = null;
        CollapsedBorderValue v2 = null;
        
        switch (c1._side) {
            case BorderPainter.TOP:
                v1 = c1._cell.getCollapsedBorderTop();
                break;
            case BorderPainter.RIGHT:
                v1 = c1._cell.getCollapsedBorderRight();
                break;
            case BorderPainter.BOTTOM:
                v1 = c1._cell.getCollapsedBorderBottom();
                break;
            case BorderPainter.LEFT:
                v1 = c1._cell.getCollapsedBorderLeft();
                break;                
        }
        
        switch (c2._side) {
            case BorderPainter.TOP:
                v2 = c2._cell.getCollapsedBorderTop();
                break;
            case BorderPainter.RIGHT:
                v2 = c2._cell.getCollapsedBorderRight();
                break;
            case BorderPainter.BOTTOM:
                v2 = c2._cell.getCollapsedBorderBottom();
                break;
            case BorderPainter.LEFT:
                v2 = c2._cell.getCollapsedBorderLeft();
                break;                
        }
        
        CollapsedBorderValue result = TableCellBox.compareBorders(v1, v2, true);
        
        if (result == null) {
            return 0;
        } else {
            return result == v1 ? 1 : -1;
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollapsedBorderSide)) return false;

        CollapsedBorderSide that = (CollapsedBorderSide) o;

        if (_side != that._side) return false;
        if (!_cell.equals(that._cell)) return false;

        return true;
    }

    public int hashCode() {
        int result = _cell.hashCode();
        result = 31 * result + _side;
        return result;
    }
}
