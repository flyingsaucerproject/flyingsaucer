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

import java.awt.Rectangle;
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
    private boolean _haveBaseline = false;
    
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
    
    protected void layoutChildren(LayoutContext c, int contentStart) {
        setState(Box.CHILDREN_FLUX);
        ensureChildren(c);
        
        TableSectionBox section = getSection();
        if (section.isNeedCellWidthCalc()) {
            section.setCellWidths(c);
            section.setNeedCellWidthCalc(false);
        }
        
        if (getChildrenContentType() != CONTENT_EMPTY) {
            int cCol = 0;
            for (Iterator i = getChildIterator(); i.hasNext(); ) {
                TableCellBox cell = (TableCellBox)i.next();
                
                layoutCell(c, cell, 0);
                
                cCol++;
            }
        }
        
        setState(Box.DONE);
    }
    
    private void alignBaselineAlignedCells(LayoutContext c) {
        int[] baselines = new int[getChildCount()];
        int lowest = Integer.MIN_VALUE;
        boolean found = false;
        for (int i = 0; i < getChildCount(); i++) {
            TableCellBox cell = (TableCellBox)getChild(i);
            
            if (cell.getVerticalAlign() == IdentValue.BASELINE) {
                int baseline = cell.calcBaseline(c);
                baselines[i] = baseline;
                if (baseline > lowest) {
                    lowest = baseline;
                }
                found = true;
            }
        }
        for (int i = 0; i < getChildCount(); i++) {
            TableCellBox cell = (TableCellBox)getChild(i);
            
            if (cell.getVerticalAlign() == IdentValue.BASELINE) {
                int deltaY = lowest - baselines[i];
                if (deltaY != 0) {
                    if (c.isPrint() && cell.isPageBreaksChange(c, deltaY)) {
                        relayoutCell(c, cell, deltaY);
                    } else {
                        cell.moveContent(c, deltaY);
                        cell.setHeight(cell.getHeight() + deltaY);
                    }
                }
            }
        }
        
        if (found) {
            setBaseline(lowest - getAbsY());
            setHaveBaseline(true);
        }
    }
    
    private boolean alignMiddleAndBottomAlignedCells(LayoutContext c) {
        boolean needRowHeightRecalc = false;
        
        int cRow = getIndex();
        int totalRows = getSection().getChildCount();
        List row = ((RowData)getSection().getGrid().get(cRow)).getRow();
        for (int cCol = 0; cCol < row.size(); cCol++) {
            TableCellBox cell = (TableCellBox)row.get(cCol);
            
            if (cell == null || cell == TableCellBox.SPANNING_CELL) {
                continue;
            }
            if (cRow < totalRows - 1 && getSection().cellAt(cRow+1, cCol) == cell) {
                continue;
            }
            
            IdentValue val = cell.getVerticalAlign();
            if (val == IdentValue.MIDDLE || val == IdentValue.BOTTOM) {
                int deltaY = calcMiddleBottomDeltaY(cell, val);
                if (deltaY > 0) {
                    if (c.isPrint() && cell.isPageBreaksChange(c, deltaY)) {
                        int oldCellHeight = cell.getHeight();
                        relayoutCell(c, cell, deltaY);
                        if (oldCellHeight + deltaY != cell.getHeight()) {
                            needRowHeightRecalc = true;
                        }
                    } else {
                        cell.moveContent(c, deltaY);
                        // Set a provisional height in case we need to calculate
                        // a default baseline
                        cell.setHeight(cell.getHeight() + deltaY);
                    }
                }
            }
        }
        
        return needRowHeightRecalc;
    }
    
    private int calcMiddleBottomDeltaY(TableCellBox cell, IdentValue verticalAlign) {
        int result;
        if (cell.getStyle().getRowSpan() == 1) {
            result = getHeight() - cell.getChildrenHeight();
        } else {
            result = getAbsY() + getHeight() - (cell.getAbsY() + cell.getChildrenHeight());
        }
        
        if (verticalAlign == IdentValue.MIDDLE) {
            return result / 2;
        } else {  /* verticalAlign == IdentValue.BOTTOM */
            return result;
        }
    }
    
    protected void calcLayoutHeight(
            LayoutContext c, BorderPropertySet border, 
            RectPropertySet margin, RectPropertySet padding) {
        alignBaselineAlignedCells(c);
        
        calcRowHeight();
        
        boolean recalcRowHeight = alignMiddleAndBottomAlignedCells(c);
        
        if (recalcRowHeight) {
            calcRowHeight();
        }
        
        if (! isHaveBaseline()) {
            calcDefaultBaseline(c);
        }
        
        setCellHeights(c);
    }

    private void calcRowHeight() {
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
            
            if (cell == null || cell == TableCellBox.SPANNING_CELL) {
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
    }
    
    private void calcDefaultBaseline(LayoutContext c) {
        int lowestCellEdge = 0;
        int cRow = getIndex();
        int totalRows = getSection().getChildCount();
        List row = ((RowData)getSection().getGrid().get(cRow)).getRow();
        for (int cCol = 0; cCol < row.size(); cCol++) {
            TableCellBox cell = (TableCellBox)row.get(cCol);
            
            if (cell == null || cell == TableCellBox.SPANNING_CELL) {
                continue;
            }
            if (cRow < totalRows - 1 && getSection().cellAt(cRow+1, cCol) == cell) {
                continue;
            }
            
            Rectangle contentArea = cell.getContentAreaEdge(cell.getAbsX(), cell.getAbsY(), c);
            int bottomCellEdge = contentArea.y + contentArea.height;
            if (bottomCellEdge > lowestCellEdge) {
                lowestCellEdge = bottomCellEdge;
            }
        }
        if (lowestCellEdge > 0) {
            setBaseline(lowestCellEdge - getAbsY());
        }
        setHaveBaseline(true);
    }
    
    private void setCellHeights(LayoutContext c) {
        int cRow = getIndex();
        int totalRows = getSection().getChildCount();
        List row = ((RowData)getSection().getGrid().get(cRow)).getRow();
        for (int cCol = 0; cCol < row.size(); cCol++) {
            TableCellBox cell = (TableCellBox)row.get(cCol);
            
            if (cell == null || cell == TableCellBox.SPANNING_CELL) {
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
    
    private void relayoutCell(LayoutContext c, TableCellBox cell, int contentStart) {
        int width = cell.getWidth();
        cell.reset(c);
        cell.setLayoutWidth(c, width);
        layoutCell(c, cell, contentStart);
    }
    
    private void layoutCell(LayoutContext c, TableCellBox cell, int contentStart) {
        cell.initContainingLayer(c);
        cell.calcCanvasLocation();
        
        cell.layout(c, contentStart);
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
    
    public void reset(LayoutContext c) {
        super.reset(c);
        setHaveBaseline(false);
        getSection().setNeedCellWidthCalc(true);
    }

    public boolean isHaveBaseline() {
        return _haveBaseline;
    }

    public void setHaveBaseline(boolean haveBaseline) {
        _haveBaseline = haveBaseline;
    }
    
    protected String getExtraBoxDescription() {
        if (isHaveBaseline()) {
            return "(baseline=" + getBaseline() + ") ";
        } else {
            return "";
        }
    }
}
