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
package org.xhtmlrenderer.newtable;

import java.util.Iterator;
import java.util.List;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.RenderingContext;

public class TableRowBox extends BlockBox {
    private int _baseline;
    
    public TableRowBox() {
    }
    
    public boolean isAutoHeight() {
        return getStyle().isAutoHeight() || ! getStyle().hasAbsoluteUnit(CSSName.HEIGHT);
    }
    
    private TableBox getTable() {
        // row -> section -> table
        return (TableBox)getParent().getParent();
    }
    
    private TableSectionBox getSection() {
        return (TableSectionBox)getParent();
    }
    
    public void layoutChildren(LayoutContext c) {
        setState(Box.CHILDREN_FLUX);
        ensureChildren(c);
        
        if (getChildrenContentType() != CONTENT_EMPTY) {
            int cCol = 0;
            for (Iterator i = getChildIterator(); i.hasNext(); ) {
                TableCellBox cell = (TableCellBox)i.next();
                
                layoutCell(c, cell);
                
                cCol++;
            }
        }
        
        setState(Box.DONE);
    }
    
    private void alignBaselineAlignedCells(LayoutContext c) {
        int[] baselines = new int[getChildCount()];
        int lowest = Integer.MIN_VALUE;
        for (int i = 0; i < getChildCount(); i++) {
            TableCellBox cell = (TableCellBox)getChild(i);
            
            if (cell.getVerticalAlign() == IdentValue.BASELINE) {
                int baseline = cell.calcBaseline(c);
                baselines[i] = baseline;
                if (baseline > lowest) {
                    lowest = baseline;
                }
            }
        }
        for (int i = 0; i < getChildCount(); i++) {
            TableCellBox cell = (TableCellBox)getChild(i);
            
            if (cell.getVerticalAlign() == IdentValue.BASELINE) {
                int deltaY = lowest - baselines[i];
                if (deltaY != 0) {
                    cell.moveContent(c, deltaY);
                }
            }
        }
    }
    
    private void alignMiddleAndBottomAlignedCells(LayoutContext c) {
        int cRow = getIndex();
        int totalRows = getSection().getChildCount();
        List row = ((RowData)getSection().getGrid().get(cRow)).getRow();
        for (int cCol = 0; cCol < row.size(); cCol++) {
            TableCellBox cell = (TableCellBox)row.get(cCol);
            
            if (cell == null || cell == TableSectionBox.SPANNING_CELL) {
                continue;
            }
            if (cRow < totalRows - 1 && getSection().cellAt(cRow+1, cCol) == cell) {
                continue;
            }
            
            IdentValue val = cell.getVerticalAlign();
            if (val == IdentValue.MIDDLE || val == IdentValue.BOTTOM) {
                int deltaY;
                if (cell.getStyle().getRowSpan() == 1) {
                    deltaY = getHeight() - cell.getChildrenHeight();
                } else {
                    deltaY = getAbsY() + getHeight() - (cell.getAbsY() + cell.getChildrenHeight());
                }
                if (deltaY > 0) {
                    if (val == IdentValue.MIDDLE) {
                        cell.moveContent(c, deltaY / 2);
                    } else if (val == IdentValue.BOTTOM) {
                        cell.moveContent(c, deltaY);
                    }
                }
            }
        }
    }
    
    protected void calcLayoutHeight(
            LayoutContext c, BorderPropertySet border, 
            RectPropertySet margin, RectPropertySet padding) {
        alignBaselineAlignedCells(c);
        
        int y1 = getAbsY();
        int y2;
        
        if (getHeight() != 0) {
            y2 = y1 + getHeight();
        } else {
            y2 = y1;
        }
        
        int cRow = getIndex();
        int totalRows = getSection().getChildCount();
        List row = ((RowData)getSection().getGrid().get(cRow)).getRow();
        for (int cCol = 0; cCol < row.size(); cCol++) {
            TableCellBox cell = (TableCellBox)row.get(cCol);
            
            if (cell == null || cell == TableSectionBox.SPANNING_CELL) {
                continue;
            }
            if (cRow < totalRows - 1 && getSection().cellAt(cRow+1, cCol) == cell) {
                continue;
            }
            
            int bottomCellEdge = cell.getAbsY() + cell.getHeight();
            if (bottomCellEdge > y2) {
                y2 = bottomCellEdge;
            }
        }
        
        setHeight(y2 - y1);
        
        alignMiddleAndBottomAlignedCells(c);
        
        setCellHeights(c);
    }
    
    private void setCellHeights(LayoutContext c) {
        int cRow = getIndex();
        int totalRows = getSection().getChildCount();
        List row = ((RowData)getSection().getGrid().get(cRow)).getRow();
        for (int cCol = 0; cCol < row.size(); cCol++) {
            TableCellBox cell = (TableCellBox)row.get(cCol);
            
            if (cell == null || cell == TableSectionBox.SPANNING_CELL) {
                continue;
            }
            if (cRow < totalRows - 1 && getSection().cellAt(cRow+1, cCol) == cell) {
                continue;
            }
            
            if (cell.getStyle().getRowSpan() == 1) {
                cell.setHeight(getHeight());
            } else {
                cell.setHeight(getAbsY() + getHeight() - cell.getAbsY());
            }
        }
    }
    
    private void layoutCell(LayoutContext c, TableCellBox cell) {
        cell.initContainingLayer(c);
        cell.calcCanvasLocation();
        
        cell.layout(c);
    } 
    
    public void initStaticPos(LayoutContext c, BlockBox parent, int childOffset) {
        setX(0);
        
        TableBox table = getTable();
        setY(parent.getHeight() + table.getStyle().getBorderVSpacing(c));
    }

    public int getBaseline() {
        return _baseline;
    }

    public void setBaseline(int baseline) {
        _baseline = baseline;
    }
    
    protected boolean isSkipWhenCollapsingMargins() {
        return true;
    }
    
    public void paintBorder(RenderingContext c) {
        // rows never have borders
    }
    
    public void paintBackground(RenderingContext c) {
        // painted at the cell level
    }   
}
