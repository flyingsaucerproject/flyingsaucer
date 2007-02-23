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

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.Length;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.FloatManager;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.RenderingContext;

public class TableCellBox extends BlockBox {
    private int _row;
    private int _col;
    
    private TableBox _table;
    
    public TableCellBox() {
    }

    public int getCol() {
        return _col;
    }

    public void setCol(int col) {
        _col = col;
    }

    public int getRow() {
        return _row;
    }

    public void setRow(int row) {
        _row = row;
    }
    
    public void layout(LayoutContext c) {
        super.layout(c);
    }
    
    private TableBox getTable() {
        // cell -> row -> section -> table
        if (_table == null) {
            _table = (TableBox)getParent().getParent().getParent();
        }
        return _table;
    }
    
    public Length getOuterStyleWidth(CssContext c) {
        Length result = getStyle().asLength(c, CSSName.WIDTH);
        if (result.isVariable() || result.isPercent()) {
            return result;
        }
        
        int bordersAndPadding = 0;
        BorderPropertySet border = getBorder(c);
        bordersAndPadding += (int)border.left() + (int)border.right();
        
        RectPropertySet padding = getPadding(c);
        bordersAndPadding += (int)padding.left() + (int)padding.right();
        
        result.setValue(result.value() + bordersAndPadding);
        
        return result;
    }
    
    public Length getOuterStyleOrColWidth(CssContext c) {
        Length result = getOuterStyleWidth(c);
        if (getStyle().getColSpan() > 1 || ! result.isVariable()) {
            return result;
        }
        TableColumn col = getTable().colElement(getCol());
        if (col != null) {
            // XXX Need to add in collapsed borders from cell (if collapsing borders)
            result = col.getStyle().asLength(c, CSSName.WIDTH);
        }
        return result;
    }
    
    public void setLayoutWidth(LayoutContext c, int width) {
        calcDimensions(c);
        
        setContentWidth(width - getLeftMBP() - getRightMBP());
    }
    
    public boolean isAutoHeight() {
        return getStyle().isAutoHeight() || ! getStyle().hasAbsoluteUnit(CSSName.HEIGHT);
    }
    
    public int calcBaseline(LayoutContext c) {
        int result = super.calcBaseline(c);
        if (result != NO_BASELINE) {
            return result;
        } else {
            Rectangle contentArea = getContentAreaEdge(getAbsX(), getAbsY(), c);
            return (int)contentArea.getY();
        }
    }
    
    public void moveContent(LayoutContext c, final int deltaY) {
        for (int i = 0; i < getChildCount(); i++) {
            Box b = getChild(i);
            b.setY(b.getY() + deltaY);
        }
        
        getPersistentBFC().getFloatManager().performFloatOperation(
                new FloatManager.FloatOperation() {
                    public void operate(Box floater) {
                        floater.setY(floater.getY() + deltaY);
                    }
                });
        
        calcChildLocations();
    }
    
    public IdentValue getVerticalAlign() {
        IdentValue val = getStyle().getIdent(CSSName.VERTICAL_ALIGN);
        
        if (val == IdentValue.TOP || val == IdentValue.MIDDLE || val == IdentValue.BOTTOM) {
            return val;
        } else {
            return IdentValue.BASELINE;
        }
    }
    
    private boolean isPaintBackgroundsAndBorders() {
        boolean showEmpty = getStyle().isShowEmptyCells();
        // XXX Not quite right, but good enough for now 
        // (e.g. absolute boxes will be counted as content here when the spec 
        // says the cell should be treated as empty).  
        return showEmpty || getChildrenContentType() != BlockBox.CONTENT_EMPTY;
                    
    }
    
    public void paintBackground(RenderingContext c) {
        if (isPaintBackgroundsAndBorders() && getStyle().isVisible()) {
            Rectangle bounds = getPaintingBorderEdge(c);
            
            TableColumn column = getTable().colElement(getCol());
            if (column != null) {
                c.getOutputDevice().paintBackground(c, column.getStyle(), bounds);
            }
            
            Box row = getParent();
            Box section = row.getParent();
            
            CalculatedStyle sectionStyle = section.getStyle();
            c.getOutputDevice().paintBackground(c, sectionStyle, bounds);
            
            CalculatedStyle rowStyle = row.getStyle();
            c.getOutputDevice().paintBackground(c, rowStyle, bounds);
            
            super.paintBackground(c);
        }
    }
    
    public void paintBorder(RenderingContext c) {
        if (isPaintBackgroundsAndBorders()) {
            super.paintBorder(c);
        }
    }
    
    protected boolean isFixedWidthAdvisoryOnly() {
        return getTable().getStyle().isIdent(CSSName.TABLE_LAYOUT, IdentValue.AUTO);
    }
    
    protected boolean isSkipWhenCollapsingMargins() {
        return true;
    }    
}
