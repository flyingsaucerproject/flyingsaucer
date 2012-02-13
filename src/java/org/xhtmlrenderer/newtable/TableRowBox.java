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
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.ContentLimitContainer;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;

public class TableRowBox extends BlockBox {
    private int _baseline;
    private boolean _haveBaseline = false;
    private int _heightOverride;
    private ContentLimitContainer _contentLimitContainer;
    
    private int _extraSpaceTop;
    private int _extraSpaceBottom;
    
    public TableRowBox() {
    }
    
    public BlockBox copyOf() {
        TableRowBox result = new TableRowBox();
        result.setStyle(getStyle());
        result.setElement(getElement());
        
        return result;
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
    
    public void layout(LayoutContext c, int contentStart) {
        boolean running = c.isPrint() && getTable().getStyle().isPaginateTable();
        int prevExtraTop = 0;
        int prevExtraBottom = 0;
        
        if (running) {
            prevExtraTop = c.getExtraSpaceTop();
            prevExtraBottom = c.getExtraSpaceBottom();
            
            calcExtraSpaceTop(c);
            calcExtraSpaceBottom(c);
            
            c.setExtraSpaceTop(c.getExtraSpaceTop() + getExtraSpaceTop());
            c.setExtraSpaceBottom(c.getExtraSpaceBottom() + getExtraSpaceBottom());
        }
        
        super.layout(c, contentStart);
        
        if (running) {
            if (isShouldMoveToNextPage(c)) {
                if (getTable().getFirstBodyRow() == this) {
                    // XXX Performance problem here.  This forces the table
                    // to move to the next page (which we want), but the initial
                    // table layout run still completes (which we don't)
                    getTable().setNeedPageClear(true);
                } else {
                    setNeedPageClear(true);
                }
            }
            c.setExtraSpaceTop(prevExtraTop);
            c.setExtraSpaceBottom(prevExtraBottom);
        }
    }
    
    private boolean isShouldMoveToNextPage(LayoutContext c) {
        PageBox page = c.getRootLayer().getFirstPage(c, this);
        
        if (getAbsY() + getHeight() < page.getBottom()) {
            return false;
        }
        
        for (Iterator i = getChildIterator(); i.hasNext(); ) {
            TableCellBox cell = (TableCellBox)i.next();
            int baseline = cell.calcBlockBaseline(c);
            if (baseline != BlockBox.NO_BASELINE && baseline < page.getBottom()) {
                return false;
            }
        }
        
        return true;
    }
    
    public void analyzePageBreaks(LayoutContext c, ContentLimitContainer container) {
        if (getTable().getStyle().isPaginateTable()) {
            _contentLimitContainer = new ContentLimitContainer(c, getAbsY());
            _contentLimitContainer.setParent(container);
            
            if (container != null) {
                container.updateTop(c, getAbsY());
                container.updateBottom(c, getAbsY() + getHeight());
            }
            
            for (Iterator i = getChildIterator(); i.hasNext(); ) {
                Box b = (Box)i.next();
                b.analyzePageBreaks(c, _contentLimitContainer);
            }
            
            if (container != null && _contentLimitContainer.isContainsMultiplePages()) {
                propagateExtraSpace(c, container, _contentLimitContainer, getExtraSpaceTop(), getExtraSpaceBottom());
            }
        } else {
            super.analyzePageBreaks(c, container);
        }
    }   

    private void calcExtraSpaceTop(LayoutContext c) {
        int maxBorderAndPadding = 0;
        
        for (Iterator i = getChildIterator(); i.hasNext(); ) {
            TableCellBox cell = (TableCellBox)i.next();
            
            int borderAndPadding = (int)cell.getPadding(c).top() + (int)cell.getBorder(c).top();
            if (borderAndPadding > maxBorderAndPadding) {
                maxBorderAndPadding = borderAndPadding;
            }
        }

        _extraSpaceTop = maxBorderAndPadding;
    }
    
    private void calcExtraSpaceBottom(LayoutContext c) {
        int maxBorderAndPadding = 0;
        
        int cRow = getIndex();
        int totalRows = getSection().numRows();
        List grid = getSection().getGrid();
        if ((grid.size() > 0) && (cRow < grid.size())) {
            List row = ((RowData)grid.get(cRow)).getRow();
            for (int cCol = 0; cCol < row.size(); cCol++) {
                TableCellBox cell = (TableCellBox)row.get(cCol);
                
                if (cell == null || cell == TableCellBox.SPANNING_CELL) {
                    continue;
                }
                if (cRow < totalRows - 1 && getSection().cellAt(cRow+1, cCol) == cell) {
                    continue;
                }
                
                int borderAndPadding = (int)cell.getPadding(c).bottom() + (int)cell.getBorder(c).bottom();
                if (borderAndPadding > maxBorderAndPadding) {
                    maxBorderAndPadding = borderAndPadding;
                }
            }
        }
        
        _extraSpaceBottom = maxBorderAndPadding;
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
        
        if (found) {
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
        
            setBaseline(lowest - getAbsY());
            setHaveBaseline(true);
        }
    }
    
    private boolean alignMiddleAndBottomAlignedCells(LayoutContext c) {
        boolean needRowHeightRecalc = false;
        
        int cRow = getIndex();
        int totalRows = getSection().numRows();
        List grid = getSection().getGrid();
        if ((grid.size() > 0) && (cRow < grid.size())) {
            List row = ((RowData)grid.get(cRow)).getRow();
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
        if (getHeightOverride() > 0) {
            setHeight(getHeightOverride());
        }
        
        alignBaselineAlignedCells(c);
        
        calcRowHeight(c);
        
        boolean recalcRowHeight = alignMiddleAndBottomAlignedCells(c);
        
        if (recalcRowHeight) {
            calcRowHeight(c);
        }
        
        if (! isHaveBaseline()) {
            calcDefaultBaseline(c);
        }
        
        setCellHeights(c);
    }

    private void calcRowHeight(CssContext c) {
        int y1 = getAbsY();
        int y2;
        
        if (getHeight() != 0) {
            y2 = y1 + getHeight();
        } else {
            y2 = y1;
        }
        
        if (isLastRow()) {
            int bottom = getTable().calcFixedHeightRowBottom(c);
            if (bottom > 0 && bottom > y2) {
                y2 = bottom;
            }
        }
        
        int cRow = getIndex();
        int totalRows = getSection().numRows();
        List grid = getSection().getGrid();
        if ((grid.size() > 0) && (cRow < grid.size())) {
            List row = ((RowData)grid.get(cRow)).getRow();
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
        }
        
        setHeight(y2 - y1);
    }
    
    private boolean isLastRow() {
        TableBox table = getTable();
        TableSectionBox section = getSection();
        if (table.sectionBelow(section, true) == null) {
            return section.getChild(section.getChildCount()-1) == this;
        } else {
            return false;
        }
    }
    
    private void calcDefaultBaseline(LayoutContext c) {
        int lowestCellEdge = 0;
        int cRow = getIndex();
        int totalRows = getSection().numRows();
        List grid = getSection().getGrid();
        if ((grid.size() > 0) && (cRow < grid.size())) {
            List row = ((RowData)grid.get(cRow)).getRow();
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
        }
        if (lowestCellEdge > 0) {
            setBaseline(lowestCellEdge - getAbsY());
        }
        setHaveBaseline(true);
    }
    
    private void setCellHeights(LayoutContext c) {
        int cRow = getIndex();
        int totalRows = getSection().numRows();
        List grid = getSection().getGrid();
        if ((grid.size() > 0) && (cRow < grid.size())) {
            List row = ((RowData)grid.get(cRow)).getRow();
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
        c.translate(0, getY()-childOffset);
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
        setContentLimitContainer(null);
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

    public int getHeightOverride() {
        return _heightOverride;
    }

    public void setHeightOverride(int heightOverride) {
        _heightOverride = heightOverride;
    }
    
    public void exportText(RenderingContext c, Writer writer) throws IOException {
        if (getTable().isMarginAreaRoot()) {
            super.exportText(c, writer);
        } else {
            int yPos = getAbsY();
            if (yPos >= c.getPage().getBottom() && isInDocumentFlow()) {
                exportPageBoxText(c, writer, yPos);
            }
            
            for (Iterator i = getChildIterator(); i.hasNext(); ) {
                TableCellBox cell = (TableCellBox)i.next();
                StringBuffer buffer =  new StringBuffer();
                cell.collectText(c, buffer);
                writer.write(buffer.toString().trim());
                int cSpan = cell.getStyle().getColSpan();
                for (int j = 0; j < cSpan; j++) {
                    writer.write('\t');    
                }
            }
            
            writer.write(LINE_SEPARATOR);
        }
    }

    public ContentLimitContainer getContentLimitContainer() {
        return _contentLimitContainer;
    }

    public void setContentLimitContainer(ContentLimitContainer contentLimitContainer) {
        _contentLimitContainer = contentLimitContainer;
    }

    public int getExtraSpaceTop() {
        return _extraSpaceTop;
    }

    public void setExtraSpaceTop(int extraSpaceTop) {
        _extraSpaceTop = extraSpaceTop;
    }

    public int getExtraSpaceBottom() {
        return _extraSpaceBottom;
    }

    public void setExtraSpaceBottom(int extraSpaceBottom) {
        _extraSpaceBottom = extraSpaceBottom;
    }
    
    public int forcePageBreakBefore(LayoutContext c, IdentValue pageBreakValue,
            boolean pendingPageName) {
        int currentDelta = super.forcePageBreakBefore(c, pageBreakValue, pendingPageName);
        
        // additional calculations for collapsed borders.
        if (c.isPrint() && getStyle().isCollapseBorders()) {
            // get destination page for this row
            PageBox page = c.getRootLayer().getPage(c, getAbsY() + currentDelta);
            if (page!=null) {
                
                // calculate max spill from the collapsed top borders of each child
                int spill = 0;
                for (Iterator i = getChildIterator(); i.hasNext(); ) {
                    TableCellBox cell = (TableCellBox)i.next();
                    BorderPropertySet collapsed = cell.getCollapsedPaintingBorder();
                    if (collapsed != null) {
                        spill = Math.max(spill, (int)collapsed.top() / 2);
                    }
                }
    
                // be sure that the current start of the row is >= the start of the page
                int borderTop = getAbsY() + currentDelta + (int)getMargin(c).top() - spill;
                int rowDelta = page.getTop() - borderTop;
                if (rowDelta > 0) {
                    setY(getY() + rowDelta);
                    currentDelta += rowDelta;
                }
            }
        }
        return currentDelta;
    }
}
