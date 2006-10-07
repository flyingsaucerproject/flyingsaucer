/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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
/*
   todo:
   recombine the tablelayout and tablelayout2
   move more of the calculation code into the table package
   make a set of tests in addition to the demo
   no span
   col span
   row span
   col and row span
   col span contents that are too big
   row span contents that are too big
   col and row span contents that are too big
   implement row height growing based on row spanned contents
   investigate margin collapsing
   support captions, headers, and footers
   - joshy
  */
package org.xhtmlrenderer.table;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.layout.BlockFormattingContext;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.layout.content.TableCellContent;
import org.xhtmlrenderer.layout.content.TableContent;
import org.xhtmlrenderer.layout.content.TableRowContent;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.swing.SwingReplacedElement;
import org.xhtmlrenderer.util.XRLog;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class TableBoxing {
    public static Box layout(LayoutContext c, BlockBox outerBox, Content content) {
        boolean set_bfc = false;

        c.setLayingOutTable(true);

        if (content instanceof TableContent) {
        } else {
            XRLog.layout(Level.WARNING, "Unsupported table type " + content.getClass().getName());
            return null;
        }

        // install a block formatting context for the body,
        // ie. if it's null.
        // set up the outtermost bfc
        if (c.getBlockFormattingContext() == null) {
            BlockFormattingContext bfc = new BlockFormattingContext(outerBox, c);
            c.pushBFC(bfc);
            set_bfc = true;
        }
        // copy the extents
        Rectangle oe = c.getExtents();
        c.setExtents(new Rectangle(oe));
        //outerBox.x = c.getExtents().x;
        //outerBox.y = c.getExtents().y;
        //HACK: for now
        outerBox.contentWidth = c.getExtents().width;
        outerBox.height = c.getExtents().height;

        TableBox tableBox = new TableBox();
        outerBox.addChildForLayout(c, tableBox);
        tableBox.element = content.getElement();
        //OK, first set up the current style. All depends on this...
        CascadedStyle pushed = content.getStyle();
        if (pushed != null) {
            c.pushStyle(pushed);
        } else {
            c.pushStyle(CascadedStyle.emptyCascadedStyle);
        }

        tableBox.setStyle(c.getCurrentStyle());
        tableBox.setContainingBlockWidth((int) oe.getWidth());

        /*
        VerticalMarginCollapser.collapseVerticalMargins(c, tableBox, content, (float) oe.getWidth());
        */

        TableContent tableContent = (TableContent) content;
        if (tableContent.isTopMarginCollapsed()) {
            tableBox.setMarginTop(c, 0);
        }
        if (tableContent.isBottomMarginCollapsed()) {
            tableBox.setMarginBottom(c, 0);
        }

        BorderPropertySet border = c.getCurrentStyle().getBorder(c);
        //note: percentages here refer to width of containing block
        RectPropertySet margin = tableBox.getMargin(c);
        RectPropertySet padding = c.getCurrentStyle().getPaddingRect((float) oe.getWidth(), c);
        // CLEAN: cast to int
        int tx = (int) margin.left() + (int) border.left() + (int) padding.left();
        int ty = (int) margin.top() + (int) border.top() + (int) padding.top();
        tableBox.tx = tx;
        tableBox.ty = ty;
        c.translate(tx, ty);
        Rectangle extents =
                c.shrinkExtents(tx + (int) margin.right() + (int) border.right() + (int) padding.right(), ty + (int) margin.bottom() + (int) border.bottom() + (int) padding.bottom());
        IdentValue borderStyle = c.getCurrentStyle().getIdent(CSSName.BORDER_COLLAPSE);
        int borderSpacingHorizontal = (int) c.getCurrentStyle().getFloatPropertyProportionalWidth(CSSName.FS_BORDER_SPACING_HORIZONTAL, 0, c);
        int borderSpacingVertical = (int) c.getCurrentStyle().getFloatPropertyProportionalWidth(CSSName.FS_BORDER_SPACING_VERTICAL, 0, c);
        layoutChildren(c, tableBox, content, false, borderSpacingHorizontal, borderSpacingVertical);
        if (c.shouldStop()) return outerBox;
        c.setExtents(extents);
        c.translate(-tx, -ty);
        //OK, now we basically have the maximum cell widths, is that a smart order?
        //TODO: percentages?
        if (c.getCurrentStyle().isIdent(CSSName.WIDTH, IdentValue.AUTO)) {
            //we're normally fine, unless the maximum width is greater than the extents
            fixWidths(tableBox, borderSpacingHorizontal);
        } else {//if the algorithm is fixed, we need to do something else from the start
            int givenWidth = (int) c.getCurrentStyle().getFloatPropertyProportionalWidth(CSSName.WIDTH, c.getExtents().width, c);
            //also fine, if the total calculated is less than the extents and the width
            if (tableBox.contentWidth < givenWidth) {
                tableBox.contentWidth = givenWidth;
                fixWidths(tableBox, borderSpacingHorizontal);
            } else {
                c.getExtents().width = 1;
                c.translate(tx, ty);
                int[] preferredColumns = tableBox.columns;
                tableBox.columns = null;
                tableBox.removeAllChildren();
                tableBox.contentWidth = 0;
                tableBox.height = 0;
                layoutChildren(c, tableBox, content, false, borderSpacingHorizontal, borderSpacingVertical);
                c.translate(-tx, -ty);
                //here the table is layed out with minimum column widths
                if (tableBox.contentWidth < givenWidth) {
                    //do it right
                    tableBox.contentWidth = givenWidth;
                    tableBox.removeAllChildren();
                    fixWidths(tableBox, borderSpacingHorizontal, preferredColumns);
                    c.translate(tx, ty);
                    tableBox.contentWidth = 0;
                    tableBox.height = 0;
                    layoutChildren(c, tableBox, content, true, borderSpacingHorizontal, borderSpacingVertical);
                    c.translate(-tx, -ty);
                }
            }
        }
        fixHeights(tableBox, borderSpacingVertical);

        alignCellLines(tableBox);

        //TODO: margins go on the outer box
        tableBox.leftMBP = (int) border.left() + (int) padding.left();
        // CLEAN: cast to int
        tableBox.leftMBP += (int) margin.left();
        tableBox.rightMBP = (int) border.right() + (int) padding.right();
        tableBox.rightMBP += (int) margin.right();
        tableBox.height = (int) margin.top() + (int) border.top() + (int) padding.top() + tableBox.height + (int) padding.bottom() + (int) border.bottom() + (int) margin.bottom();

        c.popStyle();

        //restore the extents
        c.setExtents(oe);

        // remove the outtermost bfc
        if (set_bfc) {
            c.popBFC();
        }
        calculateOuterBoxDimensions(outerBox);
        tableBox.setState(Box.DONE);
        outerBox.setState(Box.DONE);

        outerBox.calcCanvasLocation();
        outerBox.calcChildLocations();

        c.setLayingOutTable(false);

        if (c.isPrint()) {
            positionRowsOnPage(c, tableBox);
            calculateOuterBoxDimensions(outerBox);
            outerBox.calcChildLocations();
        }

        return outerBox;
    }

    private static void calculateOuterBoxDimensions(BlockBox outerBox) {
        if (outerBox.getChildCount() > 0) {
            Box table = (Box) outerBox.getChild(0);
            outerBox.height = table.height;
            outerBox.contentWidth = table.contentWidth;
        }
    }

    private static void positionRowsOnPage(LayoutContext c, TableBox table) {
        int lastMoved = -1;
        for (int i = 0; i < table.getChildCount(); i++) {
            RowBox row = (RowBox) table.getChild(i);

            if (row.crossesPageBreak(c) && i != lastMoved) {
                int runStart = findStartOfRun(c, table, i);
                RowBox start = (RowBox) table.getChild(runStart);
                int delta = start.moveToNextPage(c);
                table.height += delta;

                for (int j = runStart + 1; j < table.getChildCount(); j++) {
                    RowBox followingSibling = (RowBox) table.getChild(j);
                    followingSibling.y += delta;
                    followingSibling.setAbsY(followingSibling.getAbsY() + delta);
                }

                i = runStart;
                lastMoved = runStart;
            }
        }
    }

    private static int findStartOfRun(LayoutContext c, TableBox table, int end) {
        if (end > 0) {
            int offset = end;

            while (offset > 0) {
                RowBox previous = (RowBox) table.getChild(offset - 1);
                RowBox current = (RowBox) table.getChild(offset);

                IdentValue after = previous.getStyle().getIdent(CSSName.PAGE_BREAK_AFTER);
                IdentValue before =
                        current.getStyle().getIdent(CSSName.PAGE_BREAK_BEFORE);

                if ((after == IdentValue.AVOID && before == IdentValue.AUTO) ||
                        (after == IdentValue.AUTO && before == IdentValue.AVOID) ||
                        (after == IdentValue.AVOID && before == IdentValue.AVOID)) {
                    offset--;
                } else {
                    return offset;
                }
            }
            return 0;
        } else {
            return 0;
        }
    }

    private static void alignCellLines(TableBox box) {
        for (int i = 0; i < box.getChildCount(); i++) {
            RowBox row = (RowBox) box.getChild(i);

            for (int j = 0; j < row.getChildCount(); j++) {
                CellBox cell = (CellBox) row.getChild(j);

                cell.alignLines();
            }
        }
    }

    /**
     * increases cell size without re-layout.
     */
    private static void fixWidths(TableBox tableBox, int borderSpacingHorizontal) {
        if ((tableBox != null) && (tableBox.columns != null)) {
            int sum = borderSpacingHorizontal;
            for (int i = 0; i < tableBox.columns.length; i++) sum += tableBox.columns[i] + borderSpacingHorizontal;
            if (sum < tableBox.contentWidth) {
                int extra = (tableBox.contentWidth - sum) / tableBox.columns.length;
                for (int i = 0; i < tableBox.columns.length; i++) tableBox.columns[i] += extra;
            } else {
                //if sum is greater, we probably screwed up earlier, just do something reasonable
                tableBox.contentWidth = sum;
            }
            //int maxRowWidth = 0;
            for (Iterator tci = tableBox.getChildIterator(); tci.hasNext();) {
                Object tc = tci.next();
                if (tc instanceof RowBox) {
                    RowBox row = (RowBox) tc;
                    int col = 0;
                    int x = borderSpacingHorizontal;
                    for (Iterator cbi = row.getChildIterator(); cbi.hasNext();) {
                        while (col < tableBox.columnRows.length && tableBox.columnRows[col] != 0) {
                            x += borderSpacingHorizontal + tableBox.columns[col];
                            col = col + 1;
                        }
                        CellBox cb = (CellBox) cbi.next();
                        cb.contentWidth = 0;
                        for (int i = 0; (i < cb.colspan) && (col + i < tableBox.columns.length); i++)
                            cb.contentWidth += tableBox.columns[col + i];
                        cb.contentWidth += borderSpacingHorizontal * (cb.colspan - 1);
                        cb.contentWidth -= cb.leftMBP + cb.rightMBP;
                        cb.x = x;
                        x += cb.getWidth() + borderSpacingHorizontal;
                        for (int j = 0; (j < cb.colspan) && (col + j < tableBox.columns.length); j++) {
                            tableBox.columnRows[col + j] = cb.rowspan;
                        }
                        col += cb.colspan;
                    }
                    for (int j = 0; j < tableBox.columns.length; j++) {
                        tableBox.columnRows[j]--;
                    }
                    //if (row.contentWidth > maxRowWidth) maxRowWidth = row.contentWidth;
                    //instead of calculating maxRowWidth, we can just set it, the row must have the width of the table
                    row.contentWidth = tableBox.contentWidth;
                } else
                    XRLog.layout(Level.WARNING, "Can't fix widths of " + tc.getClass().getName() + " yet!");
            }
            //Now we need to normalize all the rows.
            /*No. see above
            for (Iterator tci = tableBox.getChildIterator(); tci.hasNext();) {
                Object tc = tci.next();
                if (tc instanceof RowBox) {
                    RowBox rBox = (RowBox) tc;
                    if (rBox.contentWidth < maxRowWidth) rBox.contentWidth = maxRowWidth;
                }
            }*/
        }
    }

    private static void fixHeights(TableBox tableBox, int borderSpacingVertical) {
        tableBox.height += borderSpacingVertical;
        for (Iterator tci = tableBox.getChildIterator(); tci.hasNext();) {
            Object tc = tci.next();
            if (tc instanceof RowBox) {
                RowBox row = (RowBox) tc;
                int col = 0;
                for (Iterator cbi = row.getChildIterator(); cbi.hasNext();) {
                    while ((col < tableBox.columnRows.length) && (tableBox.columnRows[col] != 0)) {
                        if (tableBox.columnCell != null && tableBox.columnCell[col] != null) {
                            tableBox.columnCell[col].height += borderSpacingVertical + row.height;
                        }
                        col = col + 1;
                    }
                    CellBox cb = (CellBox) cbi.next();
                    for (int j = 0; (j < cb.colspan) && (col + j < tableBox.columnRows.length); j++) {
                        tableBox.columnRows[col + j] = cb.rowspan;
                        if (j == 0)
                            tableBox.columnCell[col + j] = cb;
                        else
                            tableBox.columnCell[col + j] = null;
                    }
                    cb.height = row.height;
                    col += cb.colspan;
                }
                while (col < tableBox.columnRows.length && tableBox.columnRows[col] != 0) {
                    if (tableBox.columnCell != null && tableBox.columnCell[col] != null) {
                        tableBox.columnCell[col].height += borderSpacingVertical + row.height;
                    }
                    col = col + 1;
                }
                for (int j = 0; j < tableBox.columns.length; j++) {
                    tableBox.columnRows[j]--;
                }
            } else
                XRLog.layout(Level.WARNING, "Can't fix widths of " + tc.getClass().getName() + " yet!");
        }
    }

    /**
     * distributes extra space where needed. Needs a re-layout
     */
    private static void fixWidths(TableBox tableBox, int borderSpacingHorizontal, int[] preferredColumns) {
        int min = borderSpacingHorizontal;
        int wantMore = 0;
        for (int i = 0; i < tableBox.columns.length; i++) {
            min += tableBox.columns[i] + borderSpacingHorizontal;
            if (tableBox.columns[i] < preferredColumns[i]) wantMore++;
        }
        if (min < tableBox.contentWidth) {
            int extra = (tableBox.contentWidth - min) / wantMore;
            for (int i = 0; i < tableBox.columns.length; i++) {
                int wanted = preferredColumns[i] - tableBox.columns[i];
                if (wanted > 0) {
                    int added = (int) Math.min(extra, wanted);
                    tableBox.columns[i] += added;
                    min += added;
                }
            }
            //any left? just give it from the left
            for (int i = 0; i < tableBox.columns.length; i++) {
                int wanted = preferredColumns[i] - tableBox.columns[i];
                if (wanted > 0) {
                    int added = (int) Math.min(tableBox.contentWidth - min, wanted);
                    tableBox.columns[i] += added;
                    min += added;
                }
            }
        } else {
            //can't be less than the minimum possible, shouldn't normally get here
            tableBox.contentWidth = min;
        }
    }

    //TODO: do this right. It is totally as simple as possible.
    private static void layoutChildren(LayoutContext c, TableBox tableBox, Content content, boolean fixed, int borderSpacingHorizontal, int borderSpacingVertical) {
        Iterator contentIterator = content.getChildContent(c).iterator();
        while (contentIterator.hasNext() && !c.shouldStop()) {
            Object o = contentIterator.next();
            if (o instanceof TableRowContent) {
                c.translate(0, tableBox.height);
                RowBox row = layoutRow(c, (TableRowContent) o, tableBox, fixed, borderSpacingHorizontal, borderSpacingVertical);
                c.translate(0, -tableBox.height);

                tableBox.addChildForLayout(c, row);
                row.setParent(tableBox);
                row.element = ((TableRowContent) o).getElement();
                // set the child_box location
                row.x = 0;
                row.y = tableBox.height + borderSpacingVertical;

                // increase the final layout height by the height of the child
                tableBox.height = row.y + row.height;
            } else {
                XRLog.layout(Level.WARNING, "Unsupported inside table: " + o.getClass().getName());
            }
        }
    }

    private static RowBox layoutRow(LayoutContext c, TableRowContent tableRowContent, TableBox table, boolean fixed, int borderSpacingHorizontal, int borderSpacingVertical) {
        // copy the extents
        Rectangle oe = c.getExtents();
        c.setExtents(new Rectangle(oe));
        RowBox row = new RowBox();
        row.setContainingBlock(table);

        CascadedStyle pushed = tableRowContent.getStyle();
        if (pushed != null) {
            c.pushStyle(pushed);
        } else {
            c.pushStyle(CascadedStyle.emptyCascadedStyle);
        }

        row.setStyle(c.getCurrentStyle());
        row.setContainingBlockWidth((int) oe.getWidth());

        BorderPropertySet border = c.getCurrentStyle().getBorder(c);
        //rows have no margin or padding
        row.leftMBP = (int) border.left();
        row.rightMBP = (int) border.right();
        //TODO: check how borders should interact with cell borders
        int tx = (int) border.left();
        int ty = (int) border.top();
        row.tx = tx;
        row.ty = ty;
        c.translate(tx, ty);
        Rectangle extents =
                c.shrinkExtents(tx + (int) border.right(), ty + (int) border.bottom());
        List cells = tableRowContent.getChildContent(c);
        checkColumns(table, cells.size());
        layoutCells(cells, c, row, table, fixed, borderSpacingHorizontal, borderSpacingVertical);
        c.setExtents(extents);
        c.translate(-tx, -ty);
        // calculate the total outer width
        row.contentWidth += borderSpacingHorizontal;
        row.height = (int) border.top() + row.height + (int) border.bottom();

        c.popStyle();

        //restore the extents
        c.setExtents(oe);
        row.setState(Box.DONE);

        row.setContainingBlock(null);
        return row;
    }

    private static void layoutCells(List cells, LayoutContext c, RowBox row, TableBox table, boolean fixed, int borderSpacingHorizontal, int borderSpacingVertical) {
        int col = 0;
        for (Iterator i = cells.iterator(); i.hasNext() && !c.shouldStop();) {
            checkColumns(table, col + 1);
            while (table.columnRows[col] != 0) {
                row.contentWidth += borderSpacingHorizontal + table.columns[col];
                table.columnCell[col] = null;
                col = col + 1;
            }
            TableCellContent tcc = (TableCellContent) i.next();
            CellBox cellBox = new CellBox();
            cellBox.setContainingBlock(row);
            cellBox.setContainingLayer(c.getLayer());
            c.translate(row.contentWidth, 0);
            c.setShrinkWrap();
            cellBox = (CellBox) layoutCell(c, cellBox, tcc, fixed, table, col);
            c.unsetShrinkWrap();
            c.translate(-row.contentWidth, 0);
            cellBox.setContainingBlock(null);

            cellBox.setParent(row);
            cellBox.element = tcc.getElement();
            // set the child_box location
            cellBox.x = row.contentWidth + borderSpacingHorizontal;
            //row.y = 0;

            checkColumns(table, col + cellBox.colspan);
            for (int j = 0; j < cellBox.colspan; j++) {
                table.columnRows[col + j] = cellBox.rowspan;
                table.columnHeight[col + j] = cellBox.height;
                table.columnCell[col + j] = null;
            }
            table.columnCell[col] = cellBox;
            int width = 0;
            for (int j = 0; j < cellBox.colspan; j++) width += table.columns[col + j];
            width += borderSpacingHorizontal * (cellBox.colspan - 1);
            if (!fixed && cellBox.getWidth() > width) {
                int extra = (cellBox.getWidth() - width) / cellBox.colspan;
                for (int j = 0; j < cellBox.colspan; j++) table.columns[col + j] += extra;
            }
            cellBox.contentWidth = 0;
            for (int j = 0; j < cellBox.colspan; j++) cellBox.contentWidth += table.columns[col + j];
            cellBox.contentWidth += (cellBox.colspan - 1) * borderSpacingHorizontal;
            cellBox.contentWidth -= cellBox.leftMBP + cellBox.rightMBP;
            row.contentWidth = cellBox.x + cellBox.getWidth();
            col += cellBox.colspan;
            //this will be fixed again later!
            cellBox.height = 0;
            //have to do this late in the game
            /* if (cellBox.rowspan > 1) row.setChildrenExceedBounds(true); */
            row.addChildForLayout(c, cellBox);
        }
        for (int j = 0; j < table.columns.length; j++) {
            // increase the final layout height if the child was greater
            if (table.columnRows[j] != 0) {
                int height = table.columnHeight[j] / table.columnRows[j];
                if (height > row.height) {
                    row.height = height;
                }
                table.columnHeight[j] -= height;
                table.columnRows[j]--;
            }
        }
        for (int j = 0; j < table.columns.length; j++) {
            if (table.columnCell[j] == null) continue;
            table.columnCell[j].height += row.height;
            if (table.columnRows[j] != 0) {
                table.columnCell[j].height += borderSpacingVertical;
            }
        }
    }

    private static void checkColumns(TableBox table, int cols) {
        if (table.columns == null)
            table.columns = new int[cols];
        else if (table.columns.length < cols) {
            int[] newColumns = new int[cols];
            for (int i = 0; i < table.columns.length; i++) newColumns[i] = table.columns[i];
            table.columns = newColumns;
        }
        if (table.columnRows == null)
            table.columnRows = new int[cols];
        else if (table.columnRows.length < cols) {
            int[] newColumnRows = new int[cols];
            for (int i = 0; i < table.columnRows.length; i++) newColumnRows[i] = table.columnRows[i];
            table.columnRows = newColumnRows;
        }
        if (table.columnHeight == null)
            table.columnHeight = new int[cols];
        else if (table.columnHeight.length < cols) {
            int[] newColumnHeight = new int[cols];
            for (int i = 0; i < table.columnHeight.length; i++) newColumnHeight[i] = table.columnHeight[i];
            table.columnHeight = newColumnHeight;
        }
        if (table.columnCell == null)
            table.columnCell = new CellBox[cols];
        else if (table.columnCell.length < cols) {
            CellBox[] newColumnCell = new CellBox[cols];
            for (int i = 0; i < table.columnCell.length; i++) newColumnCell[i] = table.columnCell[i];
            table.columnCell = newColumnCell;
        }
    }

    private static CellBox layoutCell(LayoutContext c, CellBox cell, Content content, boolean fixed, TableBox table, int col) {
        //OK, first set up the current style. All depends on this...
        CascadedStyle pushed = content.getStyle();
        if (pushed != null) {
            c.pushStyle(pushed);
        }

        if (c.getCurrentStyle().isIdent(CSSName.BACKGROUND_ATTACHMENT, IdentValue.FIXED)) {
            c.getRootLayer().setFixedBackground(true);
        }

        // a cell defines a new bfc
        cell.setStyle(c.getCurrentStyle());
        cell.setContainingBlockWidth(c.getExtents().width);
        cell.setElement(content.getElement());
        BlockFormattingContext bfc = new BlockFormattingContext(cell, c);
        c.pushBFC(bfc);

        // copy the extents
        Rectangle oe = c.getExtents();
        c.setExtents(new Rectangle(oe));

        cell.colspan = (int) c.getCurrentStyle().asFloat(CSSName.FS_COLSPAN);
        cell.rowspan = (int) c.getCurrentStyle().asFloat(CSSName.FS_ROWSPAN);
        if (fixed) {
            int width = 0;
            for (int i = 0; i < cell.colspan; i++) width += table.columns[col + i];
            c.getExtents().width = width;
        }

        CalculatedStyle style = c.getCurrentStyle();
        BorderPropertySet border = c.getCurrentStyle().getBorder(c);
        RectPropertySet padding = c.getCurrentStyle().getPaddingRect((float) oe.getWidth(), c);

        // calculate the width and height as much as possible
        int setHeight = -1;//means height is not set by css
        int setWidth = -1;//means width is not set by css
        if (!cell.getStyle().isAutoWidth()) {
            setWidth = (int) style.getFloatPropertyProportionalWidth(CSSName.WIDTH, c.getExtents().width, c);
            c.getExtents().width = setWidth;
            cell.contentWidth = (int) (setWidth - border.left() - padding.left() - padding.right() - border.right());
        }
        if (!cell.getStyle().isAutoHeight()) {
            setHeight = (int) style.getFloatPropertyProportionalHeight(CSSName.HEIGHT, c.getExtents().height, c);
            c.getExtents().height = setHeight;
            cell.height = setHeight;
        }
        //check if replaced

        ReplacedElement re = c.getReplacedElementFactory().createReplacedElement(
                c, cell, c.getUac(), setWidth, setHeight);
        if (re != null) {
            cell.contentWidth = re.getIntrinsicWidth();
            cell.height = re.getIntrinsicHeight();
            cell.setReplacedElement(re);
        }

        // save height incase fixed height
        int original_height = cell.height;

        // do children's layout
        cell.leftMBP = (int) border.left() + (int) padding.left();
        cell.rightMBP = (int) padding.right() + (int) border.right();
        int tx = (int) border.left() + (int) padding.left();
        int ty = (int) border.top() + (int) padding.top();
        cell.tx = tx;
        cell.ty = ty;
        c.translate(tx, ty);

        Rectangle extents =
                c.shrinkExtents(tx + (int) border.right() + (int) padding.right(), ty + (int) border.bottom() + (int) padding.bottom());
        if (! cell.isReplaced()) {
            int previousContentWidth = cell.contentWidth;
            cell.contentWidth = c.getExtents().width;
            cell.layoutChildren(c);
            cell.contentWidth = previousContentWidth;
        } else {
            if (c.isInteractive()) {
                c.getCanvas().add(
                        ((SwingReplacedElement) cell.getReplacedElement()).getJComponent());
            }
        }

        c.setExtents(extents);
        c.translate(-tx, -ty);

        // restore height incase fixed height
        if (!cell.getStyle().isAutoHeight()) {
            // Uu.p("restoring original height");
            cell.height = original_height;
        } else {
            int delta =
                    c.getBlockFormattingContext().getFloatManager().getClearDelta(
                            c, (int) border.top() + (int) padding.top() + cell.height);
            if (delta > 0) {
                cell.height += delta;
            }
        }

        cell.height = (int) border.top() + (int) padding.top() + cell.height + (int) padding.bottom() + (int) border.bottom();

        //restore the extents
        c.setExtents(oe);

        // remove the bfc
        c.popBFC();

        //and now, back to previous style
        if (pushed != null) {
            c.popStyle();
        }

        // Uu.p("BoxLayout: finished with cell: " + cell);
        cell.setState(Box.DONE);

        cell.expandToMaxChildWidth();

        return cell;
    }
}

/*
   $Id$
   $Log$
   Revision 1.68  2006/10/07 21:52:35  tobega
   fixed problem with row-widths found by J Steenkamp

   Revision 1.67  2006/10/04 23:52:58  peterbrant
   Implement support for margin: auto (centering blocks in their containing block)

   Revision 1.66  2006/09/01 23:49:37  peterbrant
   Implement basic margin collapsing / Various refactorings in preparation for shrink-to-fit / Add hack to treat auto margins as zero

   Revision 1.65  2006/08/30 18:25:42  peterbrant
   Further refactoring / Bug fix for problem reported by Mike Curtis

   Revision 1.64  2006/08/29 17:29:14  peterbrant
   Make Style object a thing of the past

   Revision 1.63  2006/08/27 00:37:10  peterbrant
   Initial commit of (initial) R7 work

   Revision 1.62  2006/04/26 15:43:10  peterbrant
   Bandaid for issue reported by Dan Blanks

   Revision 1.61  2006/04/24 23:58:13  peterbrant
   Fixes from Mike Curtis

   Revision 1.60  2006/03/01 00:45:03  peterbrant
   Provide LayoutContext when calling detach() and friends

   Revision 1.59  2006/01/27 01:15:42  peterbrant
   Start on better support for different output devices

   Revision 1.58  2006/01/10 19:56:01  peterbrant
   Fix inappropriate box resizing when width: auto

   Revision 1.57  2006/01/04 19:50:17  peterbrant
   More pagination bug fixes / Implement simple pagination for tables

   Revision 1.56  2006/01/01 02:38:19  peterbrant
   Merge more pagination work / Various minor cleanups

   Revision 1.55  2005/12/30 01:32:41  peterbrant
   First merge of parts of pagination work

   Revision 1.54  2005/12/21 02:36:29  peterbrant
   - Calculate absolute positions incrementally (prep work for pagination)
   - Light cleanup
   - Fix bug where floats nested in floats could cause the outer float to be positioned in the wrong place

   Revision 1.53  2005/12/17 02:24:12  peterbrant
   Remove last pieces of old (now non-working) clip region checking / Push down handful of fields from Box to BlockBox

   Revision 1.52  2005/12/09 21:41:22  peterbrant
   Finish support for relative inline layers

   Revision 1.51  2005/12/07 03:14:23  peterbrant
   Fixes to final float position when float BFC is not contained in the layer being positioned / Implement 10.6.7 of the spec

   Revision 1.50  2005/12/05 00:13:55  peterbrant
   Improve list-item support (marker positioning is now correct) / Start support for relative inline layers

   Revision 1.49  2005/11/29 02:37:26  peterbrant
   Make clear work again / Rip out old pagination code

   Revision 1.48  2005/11/28 22:26:29  peterbrant
   Comment out noisy debug logging

   Revision 1.47  2005/11/25 22:42:07  peterbrant
   Wait until table has completed layout before doing line alignment

   Revision 1.46  2005/11/25 16:57:53  peterbrant
   Initial commit of inline content refactoring

   Revision 1.45  2005/11/09 14:12:37  peterbrant
   Adapt to new way of indicating we need to do slow repaints

   Revision 1.44  2005/11/08 14:59:34  tobega
   Fix from Eric Neuhauser

   Revision 1.43  2005/11/05 03:30:03  peterbrant
   Start work on painting order and improved positioning implementation

   Revision 1.42  2005/11/03 22:36:23  tobega
   Fix from Eric Neuhauser

   Revision 1.41  2005/10/30 00:02:36  peterbrant
   - Minor cleanup to get rid of unused CssContext in Style constructor
   - Switch to ArrayList from LinkedList in a few places (saves several MBs of memory on Hamlet)
   - Introduce ScaledLineMetrics to work around apparent Java bug

   Revision 1.40  2005/10/29 00:58:05  tobega
   Split out restyling from rendering and fixed up hovering

   Revision 1.39  2005/10/28 08:00:44  tobega
   fixed rowspan repaint bug

   Revision 1.38  2005/10/27 22:54:00  tobega
   row span works, but not if the row is outside the viewport ?!?!?! Also cleaned LayoutContext and PersistentBFC

   Revision 1.37  2005/10/27 00:09:09  tobega
   Sorted out Context into RenderingContext and LayoutContext

   Revision 1.36  2005/10/22 23:00:31  peterbrant
   Fix memory leak (all box trees ever built remained in memory)

   Revision 1.35  2005/10/21 18:10:54  pdoubleya
   Support for cachable borders. Still buggy on some pages, but getting there.

   Revision 1.34  2005/10/21 13:17:18  pdoubleya
   Rename some methods in RectPropertySet, cleanup.

   Revision 1.33  2005/10/21 13:02:25  pdoubleya
   Changed to cache padding in RectPropertySet.

   Revision 1.32  2005/10/21 12:01:21  pdoubleya
   Added cachable rect property for margin, cleanup minor in styling.

   Revision 1.31  2005/10/20 20:48:06  pdoubleya
   Updates for refactoring to style classes. CalculatedStyle now has lookup methods to cover all general cases, so propertyByName() is private, which means the backing classes for styling were able to be replaced.

   Revision 1.30  2005/10/18 20:57:08  tobega
   Patch from Peter Brant

   Revision 1.29  2005/10/16 23:57:21  tobega
   Starting experiment with flat representation of render tree

   Revision 1.28  2005/10/15 23:39:19  tobega
   patch from Peter Brant

   Revision 1.27  2005/10/12 21:17:15  tobega
   patch from Peter Brant

   Revision 1.26  2005/10/06 03:20:25  tobega
   Prettier incremental rendering. Ran into more trouble than expected and some creepy crawlies and a few pages don't look right (forms.xhtml, splash.xhtml)

   Revision 1.25  2005/10/03 21:36:56  tobega
   fixed some details

   Revision 1.24  2005/10/02 21:30:03  tobega
   Fixed a lot of concurrency (and other) issues from incremental rendering. Also some house-cleaning.

   Revision 1.23  2005/09/26 22:40:23  tobega
   Applied patch from Peter Brant concerning margin collapsing

   Revision 1.22  2005/09/11 20:43:16  tobega
   Fixed table-css interaction bug, colspan now works again

   Revision 1.21  2005/08/03 21:44:00  tobega
   Now support rowspan

   Revision 1.20  2005/07/05 06:10:30  tobega
   text-align now works for table-cells (fixed an omission)

   Revision 1.19  2005/07/04 00:12:13  tobega
   text-align now works for table-cells too (is done in render, not in layout)

   Revision 1.18  2005/07/02 12:25:44  tobega
   colspan is working!

   Revision 1.17  2005/06/22 23:48:46  tobega
   Refactored the css package to allow a clean separation from the core.

   Revision 1.16  2005/06/19 23:02:38  tobega
   Implemented calculation of minimum cell-widths.
   Implemented border-spacing.

   Revision 1.15  2005/06/09 21:35:02  tobega
   Increases cells to fill out a given table width, otherwise just does something reasonable for now

   Revision 1.14  2005/06/08 19:48:55  tobega
   Rock 'n roll! Report looks quite good!

   Revision 1.13  2005/06/08 19:01:57  tobega
   Table cells get their preferred width

   Revision 1.12  2005/06/08 18:24:52  tobega
   Starting to get some kind of shape to tables

   Revision 1.11  2005/06/05 01:02:35  tobega
   Very simple and not completely functional table layout

   Revision 1.10  2005/05/13 15:23:57  tobega
   Done refactoring box borders, margin and padding. Hover is working again.

   Revision 1.9  2005/05/08 14:36:59  tobega
   Refactored away the need for having a context in a CalculatedStyle

   Revision 1.8  2005/01/29 20:18:43  pdoubleya
   Clean/reformat code. Removed commented blocks, checked copyright.

   Revision 1.7  2005/01/24 22:46:46  pdoubleya
   Added support for ident-checks using IdentValue instead of string comparisons.

   Revision 1.6  2005/01/24 19:01:09  pdoubleya
   Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.

   Revision 1.5  2005/01/24 14:36:36  pdoubleya
   Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.

   Revision 1.4  2005/01/16 18:50:07  tobega
   Re-introduced caching of styles, which make hamlet and alice scroll nicely again. Background painting still slow though.

   Revision 1.3  2005/01/07 00:29:31  tobega
   Removed Content reference from Box (mainly to reduce memory footprint). In the process stumbled over and cleaned up some messy stuff.

   Revision 1.2  2005/01/02 12:22:21  tobega
   Cleaned out old layout code

   Revision 1.1  2005/01/02 09:32:41  tobega
   Now using mostly static methods for layout

   Revision 1.16  2005/01/01 23:38:41  tobega
   Cleaned out old rendering code

   Revision 1.15  2004/12/29 10:39:36  tobega
   Separated current state Context into LayoutContext and the rest into SharedContext.

   Revision 1.14  2004/12/29 07:35:40  tobega
   Prepared for cloned Context instances by encapsulating fields

   Revision 1.13  2004/12/27 07:43:33  tobega
   Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?

   Revision 1.12  2004/12/12 04:18:58  tobega
   Now the core compiles at least. Now we must make it work right. Table layout is one point that really needs to be looked over

   Revision 1.11  2004/12/12 03:33:03  tobega
   Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it

   Revision 1.10  2004/12/09 21:18:53  tobega
   precaution: code still works

   Revision 1.9  2004/12/09 00:11:53  tobega
   Almost ready for Content-based inline generation.

   Revision 1.8  2004/12/05 00:49:00  tobega
   Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.

   Revision 1.7  2004/11/19 14:39:08  joshy
   fixed crash when a tr is empty

   Issue number:
   Obtained from:
   Submitted by:
   Reviewed by:

   Revision 1.6  2004/11/19 14:27:38  joshy
   removed hard coded element names
   added support for tbody, or tbody missing



   Issue number:
   Obtained from:
   Submitted by:
   Reviewed by:

   Revision 1.5  2004/11/14 16:41:04  joshy
   refactored layout factory

   Issue number:
   Obtained from:
   Submitted by:
   Reviewed by:

   Revision 1.4  2004/10/28 01:34:26  joshy
   moved more painting code into the renderers

   Issue number:
   Obtained from:
   Submitted by:
   Reviewed by:

   Revision 1.3  2004/10/23 13:59:18  pdoubleya
   Re-formatted using JavaStyle tool.
   Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
   Added CVS log comments at bottom.

  */

