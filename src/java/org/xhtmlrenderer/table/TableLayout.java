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
package org.xhtmlrenderer.table;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.layout.BoxLayout;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.Layout;
import org.xhtmlrenderer.layout.content.BlockContent;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.util.x;

import java.awt.*;


/*
   joshy:
   new features to add
   columns to specify width
   first row of cells to specify width
   //calculate height
   //draw margins, border, padding of outside table
   //draw borders and padding and background of inside cells
   //make cells fill their width
   do inner cell vertical and horizontal alignment
   implement spanned cells
   include these boxes in the redesign of the overall layout flow
  */

/**
 * TableLayout performs the layout and painting of an XHTML Table on screen. It
 * makes use of the TableBox and CellBox classes in the box package. It
 * currently implements only the fixed layout algorithm, meaning width must be
 * explictly set on the table or the columns. Width will not be calculated by
 * the size of the contents of each cell. That will be implemented later.
 *
 * @author empty
 */

public class TableLayout
        extends BoxLayout {

    /**
     * Description of the Field
     */
    private final static int fudge = 0;// is this used anymore?


    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param node PARAM
     * @return Returns
     */
    public Box createBox(Context c, Node node) {

        Box box = new TableBox(0, 0, 0, 0);

        box.setNode(node);

        return box;
    }


    /**
     * this is the core layout code of the table. it should be heavily
     * overhauled.
     *
     * @param c       PARAM
     * @param content PARAM
     * @return Returns
     */

    public Box layout(Context c, Content content) {
        //TODO: temporary hack?
        TableBox table = (TableBox) createBox(c, content.getElement());

        // calculate the available space

        getMargin(c, table);

        getPadding(c, table);

        getBorder(c, table);

        float border_spacing = content.getStyle().getFloatProperty("border-spacing");

        table.spacing = new Point((int) border_spacing, (int) border_spacing);

        //input available width

        //int fixed_width = (int) c.css.getFloatProperty(elem,"width");

        int fixed_width = c.getExtents().width;

        //u.p("initial fixed width = " + fixed_width);

        int orig_fixed_width = fixed_width;

        fixed_width -= table.margin.left + table.border.left + table.padding.left +
                table.spacing.x + table.padding.right + table.border.right + table.margin.right;

        //u.p("fixed width = " + fixed_width);

        int col_count = getColumnCount(content.getElement());

        //u.p("col count = " + col_count);

        int[] col_widths = new int[col_count];

        // initalize them all to -1

        for (int i = 0; i < col_count; i++) {

            col_widths[i] = -1;

        }

        //leftover space = table.width - sum(columns.widths)

        int leftover_width = (int) fixed_width - 0;

        // calculate how wide each column should be and return the total

        leftover_width -= calculateColumnWidths(c, content.getElement(), col_widths);

        // distribute the remaining space to the unset columns

        distributeRemainingColumnWidth(c, col_widths, leftover_width, table);

        //table.width = max(table.width, sum(columns.widths))

        table.width = fixed_width;

        layoutTableRows(c, table, content.getElement(), col_widths, orig_fixed_width);

        return table;
    }


    /**
     * Description of the Method
     *
     * @param c              PARAM
     * @param col_widths     PARAM
     * @param leftover_width PARAM
     * @param table          PARAM
     */
    public void distributeRemainingColumnWidth(Context c, int[] col_widths, int leftover_width, TableBox table) {

        // count the remaining unset columns

        int unset_count = 0;

        for (int i = 0; i < col_widths.length; i++) {

            if (col_widths[i] == -1) {

                unset_count++;

            }
        }

        //if(leftover space > 0) {

        if (leftover_width > 0) {

            //distribute leftover space to columns

            for (int i = 0; i < col_widths.length; i++) {

                // set width only if it's not already set

                if (col_widths[i] == -1) {

                    col_widths[i] = (leftover_width -
                            table.spacing.x * col_widths.length) / unset_count;

                }
            }

        }
    }


    /**
     * Description of the Method
     *
     * @param c          PARAM
     * @param row        PARAM
     * @param prev_row   PARAM
     * @param table      PARAM
     * @param col_widths PARAM
     * @return Returns
     */
    public RowBox layoutRow(Context c, Node row, RowBox prev_row, TableBox table, int[] col_widths) {

        // create a new rowbox

        RowBox rowbox = new RowBox(0, 0, 0, 0);

        rowbox.setNode(row);

        // create dummy previous cell

        CellBox prev_cell = new CellBox(0, 0, 0, 0);

        // loop through all of the cells in the current row

        NodeList cells = row.getChildNodes();

        int col_counter = 0;

        for (int j = 0; j < cells.getLength(); j++) {

            Node cell = cells.item(j);

            if (cell.getNodeName().equals("td") ||
                    cell.getNodeName().equals("th")) {

                // if there are too many cells on this line then skip the rest

                if (col_counter >= col_widths.length) {

                    u.p("WARNING: too many cells on this row");

                    continue;
                }

                prev_cell = layoutCell(c, cell, prev_cell, rowbox, table, col_widths[col_counter]);

                //u.p("col counter = " + col_counter);

                col_counter++;

                //u.p("now its: " + col_counter);

            }
        }

        // x is always 0 (rel to the parent table)

        rowbox.x = +table.margin.left + table.border.left +
                table.padding.left;

        // y is prev row.y + prev row.height

        rowbox.y = prev_row.y + prev_row.height + table.spacing.y +
                fudge;

        // width is width of table

        rowbox.width = table.width;

        // set the heights on all of the cells

        for (int k = 0; k < rowbox.cells.size(); k++) {

            ((CellBox) rowbox.cells.get(k)).height = rowbox.height;

        }

        table.rows.add(rowbox);

        //u.p("row = " + rowbox);

        return rowbox;
    }


    /**
     * Description of the Method
     *
     * @param c         PARAM
     * @param cell      PARAM
     * @param prev_cell PARAM
     * @param rowbox    PARAM
     * @param table     PARAM
     * @param cellwidth PARAM
     * @return Returns
     */
    public CellBox layoutCell(Context c, Node cell, CellBox prev_cell, RowBox rowbox, TableBox table, int cellwidth) {

        CellBox cellbox = new CellBox(0, 0, cellwidth, 0);

        // attach the node

        cellbox.setNode(cell);

        getBorder(c, cellbox);

        getMargin(c, cellbox);

        getPadding(c, cellbox);

        //layout cell w/ modified inline

        cellbox.x = prev_cell.x + prev_cell.width +
                table.spacing.x + fudge;

        // y is 0 relative to the parent row

        cellbox.y = 0;

        // set height to 50 until it's set by the cell contents

        cellbox.height = 50;

        Rectangle oe = c.getExtents();

        // new extents = old extents but smaller. same origin tho

        c.setExtents(new Rectangle(c.getExtents().x,
                c.getExtents().y,
                cellbox.width, 100));

        // lay out the cell's contents

        Layout layout = c.getLayout(cell);

        Box cell_contents = layout.layout(c, new BlockContent((Element) cellbox.getNode(), c.css.getStyle(cellbox.getNode())));

        cellbox.sub_box = cell_contents;

        // restore old extents

        c.setExtents(oe);

        // height of the cell will be based on the height of it's

        // contents

        cellbox.height = cell_contents.height;

        //save cellbox

        // height of row is max height of cells

        rowbox.height = Math.max(cellbox.height, rowbox.height);

        rowbox.cells.add(cellbox);

        return cellbox;
    }


    /**
     * Description of the Method
     *
     * @param c          PARAM
     * @param elem       PARAM
     * @param col_widths PARAM
     * @return Returns
     */
    protected int calculateColumnWidths(Context c, Element elem, int[] col_widths) {

        int total_width = 0;

        Element tr = x.child(elem, "tr");

        NodeList nl = elem.getChildNodes();

        //NodeList nl = x.children(tr,"td");

        int count = 0;

        for (int i = 0; i < nl.getLength(); i++) {

            if (nl.item(i).getNodeName().equals("td") ||
                    nl.item(i).getNodeName().equals("th")) {

                Element td = (Element) nl.item(i);

                u.p("got td: " + td + " " + i + " count = " + count);

                //if(cell.width)

                if (c.css.getStyle(td).hasProperty("width")) {

                    //save column.width;

                    if (count > col_widths.length) {

                        u.p("elem = ");

                        //x.p(elem);

                    }

                    col_widths[count] = (int) c.css.getStyle(td).getFloatProperty("width");

                    total_width += col_widths[count];

                }

                count++;

            }
        }

        return total_width;
    }


    /**
     * Description of the Method
     *
     * @param c                PARAM
     * @param table            PARAM
     * @param elem             PARAM
     * @param col_widths       PARAM
     * @param orig_fixed_width PARAM
     */
    protected void layoutTableRows(Context c, TableBox table, Element elem, int[] col_widths, int orig_fixed_width) {

        // create dummy previous row

        RowBox prev_row = new RowBox(0, 0, 0, 0);

        prev_row.y = table.margin.top + table.border.top +
                table.padding.top - fudge;

        // loop through all of the table rows

        NodeList rows = elem.getChildNodes();

        for (int i = 0; i < rows.getLength(); i++) {

            Node row = rows.item(i);

            if (row.getNodeName().equals("tr")) {

                prev_row = layoutRow(c, row, prev_row, table, col_widths);

            }
        }


        table.height = prev_row.y + prev_row.height + table.spacing.y +
                table.padding.bottom + table.border.bottom +
                table.margin.bottom;

        table.width = orig_fixed_width;

    }







    /*
       =============== utility code ================
      */
    /**
     * Gets the columnCount attribute of the TableLayout object
     *
     * @param tb PARAM
     * @return The columnCount value
     */
    private int getColumnCount(Element tb) {

        int count = 0;

        NodeList nl = tb.getChildNodes();

        for (int i = 0; i < nl.getLength(); i++) {

            Node row = nl.item(i);

            if (row.getNodeName().equals("tr")) {

                NodeList cells = row.getChildNodes();

                for (int j = 0; j < cells.getLength(); j++) {

                    Node cell = cells.item(j);

                    if (cell.getNodeName().equals("td") ||
                            cell.getNodeName().equals("th")) {

                        count++;

                    }
                }

                // return now since we only go through the first row

                return count;
            }
        }

        return count;
    }

}

/*
   $Id$
   $Log$
   Revision 1.8  2004/12/09 00:11:53  tobega
   Almost ready for Content-based inline generation.

   Revision 1.7  2004/12/05 00:49:00  tobega
   Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.

   Revision 1.6  2004/11/14 16:41:04  joshy
   refactored layout factory

   Issue number:
   Obtained from:
   Submitted by:
   Reviewed by:

   Revision 1.5  2004/10/28 01:34:26  joshy
   moved more painting code into the renderers

   Issue number:
   Obtained from:
   Submitted by:
   Reviewed by:

   Revision 1.4  2004/10/27 13:17:03  joshy
   beginning to split out rendering code
   Issue number:
   Obtained from:
   Submitted by:
   Reviewed by:

   Revision 1.3  2004/10/23 13:59:18  pdoubleya
   Re-formatted using JavaStyle tool.
   Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
   Added CVS log comments at bottom.

  */

