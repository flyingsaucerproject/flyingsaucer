/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
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

import org.w3c.dom.Element;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.Layout;
import org.xhtmlrenderer.layout.content.BlockContent;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.Box;

import java.awt.*;


/**
 * Description of the Class
 *
 * @author empty
 */
public class TableLayout2 extends TableLayout {

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content
     * @return Returns
     */
    public Box createBox(Context c, Content content) {
        TableBox table = new TableBox();
        table.content = content;
        // set up the box properties
        getMargin(c, table);
        getPadding(c, table);
        //getBorder(c, table);
        return table;
    }


    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param content PARAM
     * @return Returns
     */
    public Box layout(Context c, Content content) {
        // create the table box
        TableBox table_box = (TableBox) createBox(c, content);

        c.pushStyle(content.getStyle());
        // set up the border spacing
        float border_spacing = c.getCurrentStyle().getFloatProperty("border-spacing");
        table_box.spacing = new Point((int) border_spacing,
                (int) border_spacing);

        // set up the width
        int fixed_width = c.getExtents().width;
        if (content.getStyle().hasProperty("width")) {
            fixed_width = (int) c.getCurrentStyle().getFloatPropertyRelative("width", c.getExtents().width);
        }
        //not used: int orig_fixed_width = fixed_width;

        //subtract off the margin, border, padding, and spacing
        fixed_width -= table_box.totalHorizontalPadding(c.getCurrentStyle()) + table_box.spacing.x;
               
        // create the table
        // table is just for calculations. it's not a real box
        Table table = new Table();
        table.addTable(c, content.getElement());

        //calculate the widths
        table.calculateWidths(fixed_width, c);
        
        //pull out the boxes
        calculateBoxes(fixed_width, table_box, c, table);
        table_box.width += table_box.totalHorizontalPadding(c.getCurrentStyle());
        table_box.height += table_box.totalVerticalPadding(c.getCurrentStyle());

        c.popStyle();

        return table_box;
    }


    /**
     * Runs through all of the boxes and calculates their sizes
     *
     * @param avail_width PARAM
     * @param box         PARAM
     * @param c           PARAM
     * @param table       PARAM
     */
    public void calculateBoxes(int avail_width, TableBox box, Context c, Table table) {
        //Uu.p("TableLayout2.calculateBoxes(" + avail_width  +
        //    " , " + box + " , " + c + " , " + table);
        box.width = avail_width;
        box.height = 100;
        box.x = 5;
        box.y = 5;
        // create a dummy prev row
        RowBox prev_row = new RowBox(0, 0, 0, 0);
        int max_width = 0;
        // loop throw the rows
        CellGrid grid = table.getCellGrid();
        for (int y = 0; y < grid.getHeight(); y++) {
            // create a new row box for this row
            RowBox row_box = new RowBox(0, 0, 0, 0);
            //row_box.node = row.node;
            box.rows.add(row_box);
            int row_height = 0;
            int column_count = 0;
            // loop through the cells
            for (int x = 0; x < grid.getWidth(); x++) {
                //Uu.p("Xx = " + Xx);
                //Uu.p("grid width = " + grid.getWidth());
                if (grid.isReal(x, y)) {
                    //Uu.p("it's real");
                    //Uu.p("getting real cell: " + Xx + " , " + y);
                    Cell cell = grid.getCell(x, y);
                    if (cell == null) {
                        System.err.println("Hit the null cell error in " + this.getClass().getName());
                        continue;
                    }

                    // create a new cell box for this cell
                    CellBox cell_box = new CellBox(0, 0, 10, 10);
                    cell.cb = cell_box;
                    cell_box.rb = row_box;
                    // set the Xx coord based on the current column
                    cell_box.x = table.calcColumnX(column_count);
                    // set the width
                    //Uu.p("column count = " + column_count + " col span = " + cell.col_span);
                    cell_box.width = table.calcColumnWidth(column_count, cell.col_span);
                    // do the internal layout
                    // save the old extents and create new with smaller width
                    Rectangle oe = c.getExtents();
                    c.setExtents(new Rectangle(c.getExtents().x, c.getExtents().y,
                            cell_box.width, 100));
                    // do child layout
                    Layout layout = c.getLayout(cell.node);
                    //Uu.p("cell box = " + cell_box);
                    //Uu.p("doing child layout on: " + layout + " for " + cell_box.node);
                    //Uu.p("cell_box properly = " + cell_box);
                    c.setSubBlock(true);
                    //TODO: temporary hack
                    Box cell_contents = layout.layout(c, new BlockContent((Element) cell.node, c.getCss().getCascadedStyle((Element) cell.node)));
                    c.setSubBlock(false);
                    cell_box.sub_box = cell_contents;
                    cell_box.height = cell_box.sub_box.height;
                    column_count += cell.col_span;
                    //Uu.p("cellbox = " + cell_box);
                    //Uu.p("sub box = " + cell_box.sub_box);
                    // restore old extents
                    c.setExtents(oe);
                    // y is relative to the rowbox so it's just 0
                    cell_box.y = 0;
                    // add the cell to the row
                    row_box.cells.add(cell_box);
                    //Uu.p("cell box width = " + cell_box.width);
                    // if this is a non row spanning cell then
                    // adjust the row height to fit this cell
                    if (cell.row_span == 1) {
                        if (cell_box.height > row_box.height) {
                            row_box.height = cell_box.height;
                        }
                    }
                    row_box.width += cell_box.width;
                } else {
                    //Uu.p("it's virtual");
                    Cell cell = grid.getCell(x, y);
                    // create a virtual cell box for this cell
                    CellBox cell_box = CellBox.createVirtual(cell.cb);
                    // skip doing layout
                    row_box.cells.add(cell_box);
                    // skip adjusting the row height for now
                    // set row height based on real cell contents
                    // set row width based on real cell contents
                }
                //Uu.p("looping");
            }

            //Uu.p("loop done");
            // move the row to the right y position
            row_height = 0;
            row_box.y = prev_row.y + prev_row.height;
            prev_row = row_box;
            // adjust the max width

            if (row_box.width > max_width) {
                max_width = row_box.width;
            }

            // adjust the height of each cell in this row to be the height of
            // the row

            for (int k = 0; k < row_box.cells.size(); k++) {
                CellBox cb = (CellBox) row_box.cells.get(k);
                if (cb.isReal()) {
                    cb.height = row_box.height;
                    cb.sub_box.height = row_box.height;
                } else {
                    // adjusting height based on virtual
                    //Uu.p("adjusting height based on virtual");
                    CellBox real = cb.getReal();
                    //Uu.p("the real cb = " + real);
                    RowBox orig_row = real.rb;
                    //Uu.p("orig row = " + orig_row);
                    RowBox cur_row = row_box;
                    //Uu.p("cur row = " + cur_row);
                    real.height = cur_row.y - orig_row.y + cur_row.height;
                    real.sub_box.height = real.height;
                    //Uu.p("now real = " + real);
                }
                //Uu.p("cell = " + cb);
            }
        }
        box.height = prev_row.y + prev_row.height;
        box.width = max_width;
        //return box;
    }

}

/*
   to support row spanning
   as we go across each row we have to figure out if the current cell
   is spanned to the one above or not.  first we need a growable grid
   object to manage the cells.
   addCell(Xx,y,col_span,row_span)
   getWidth()
   getHeight()
   isReal(Xx,y)
   //isVirtual(Xx,y)
   //getColSpan(Xx,y)
   //getRowSpan(Xx,y)
   //getRealCell(Xx,y)
   loop through all cells and add them
   calc the column widths
   for each row
   for each cell
   if isReal()
   add to row_box
   do internal layout
   set Xx based on column widths
   set y based on row
   set w based on contents and column widths
   set h based on row height
   if is virtual()
   update w based on column
   update h based on row heights between orig row and this row
  */
/*
   $Id$
   $Log$
   Revision 1.16  2005/01/01 23:38:41  tobega
   Cleaned out old rendering code

   Revision 1.15  2004/12/29 10:39:36  tobega
   Separated current state Context into ContextImpl and the rest into SharedContext.

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

