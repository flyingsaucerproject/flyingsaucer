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
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Description of the Class
 *
 * @author empty
 */
public class Table {
    /**
     * Description of the Field
     */
    List top_cells = new ArrayList();
    //List rows = new ArrayList();
    /**
     * Description of the Field
     */
    private int[] column_widths;
    /**
     * Description of the Field
     */
    private CellGrid grid;

    /**
     * Constructor for the Table object
     */
    public Table() {
        this.grid = new CellGrid();
    }


    /**
     * Adds a feature to the Table attribute of the Table object
     *
     * @param elem The feature to be added to the Table attribute
     */
    public void addTable(Context c, Element elem) {
        // for each tr
        NodeList rows = elem.getChildNodes();
        boolean first_row = true;
        int row_count = 0;
        for (int i = 0; i < rows.getLength(); i++) {
            Node row = rows.item(i);
            
            // NOTE: JMM 11/19/04
            // if there is a tbody then parse it instead
            // and break out early. is this spec compliant??
            if (isRowGroup(c, row)) {
                addTable(c, (Element) row);
                return;
            }

            if (isRow(c, row)) {
                boolean added_cells = false;
                if (first_row) {
                    added_cells = addFirstRow(c, row);
                    first_row = false;
                } else {
                    added_cells = addRow(c, row, row_count);
                }
                // checking for added_cells lets us skip
                // rows which were empty
                if (added_cells) {
                    row_count++;
                }
            }
        }
    }


    private boolean isRowGroup(Context c, Node node) {
        // only elements can be table row groups
        if (!(node instanceof Element)) {
            return false;
        }
        // check the display value
        CalculatedStyle style = c.css.getStyle(node);
        if (LayoutUtil.getDisplay(style).equals("table-row-group")) {
            return true;
        }
        return false;
    }

    private boolean isRow(Context c, Node node) {
        // only elements can be rows
        if (!(node instanceof Element)) {
            return false;
        }
        // check the display value
        CalculatedStyle style = c.css.getStyle(node);
        if (LayoutUtil.getDisplay(style).equals("table-row")) {
            return true;
        }
        return false;
    }

    /**
     * Adds a feature to the Row attribute of the Table object
     *
     * @param row The feature to be added to the Row attribute
     * @param y   The feature to be added to the Row attribute
     */
    public boolean addRow(Context c, Node row, int y) {
        return addRow(c, row, false, y);
    }

    /**
     * Adds a feature to the FirstRow attribute of the Table object
     *
     * @param row The feature to be added to the FirstRow attribute
     */
    public boolean addFirstRow(Context c, Node row) {
        return addRow(c, row, true, 0);
    }

    /**
     * Adds a feature to the Row attribute of the Table object
     *
     * @param row       The feature to be added to the Row attribute
     * @param first_row The feature to be added to the Row attribute
     * @param y         The feature to be added to the Row attribute
     */
    public boolean addRow(Context c, Node row, boolean first_row, int y) {
        //u.p("Table.addRow("+row+","+first_row+","+y+")");
        NodeList cells = row.getChildNodes();
        int col_counter = 0;
        // for each td
        boolean added = false;
        for (int j = 0; j < cells.getLength(); j++) {
            Node cell = cells.item(j);
            if (isTableCell(c, cell)) {
                //u.p("adding: " + col_counter + " " + y);
                // add the cell
                Cell cl = null;
                if (first_row) {
                    cl = addTopCell(c, cell, col_counter, y);
                } else {
                    cl = addCell(c, cell, col_counter, y);
                }
                col_counter += cl.getColumnSpan();
                added = true;
            }
        }
        first_row = false;
        return added;
    }


    private boolean isTableCell(Context c, Node node) {
        // only elements can be rows
        if (!(node instanceof Element)) {
            return false;
        }
        // check the display value
        CalculatedStyle style = c.css.getStyle(node);
        if (LayoutUtil.getDisplay(style).equals("table-cell")) {
            return true;
        }
        return false;
    }


    /**
     * Adds a feature to the Column attribute of the Table object
     *
     * @param elem The feature to be added to the Column attribute
     */
    public void addColumn(Element elem) {
    }

    // add cells from the first row
    /**
     * Adds a feature to the TopCell attribute of the Table object
     *
     * @param node The feature to be added to the TopCell attribute
     * @param x    The feature to be added to the TopCell attribute
     * @param y    The feature to be added to the TopCell attribute
     * @return Returns
     */
    public Cell addTopCell(Context c, Node node, int x, int y) {
        Cell cl = addCell(c, node, x, y);
        top_cells.add(cl);
        return cl;
    }

    /**
     * Adds a feature to the Cell attribute of the Table object
     *
     * @param node The feature to be added to the Cell attribute
     * @param x    The feature to be added to the Cell attribute
     * @param y    The feature to be added to the Cell attribute
     * @return Returns
     */
    public Cell addCell(Context c, Node node, int x, int y) {
        //u.p("addCell("+node+","+x+","+y+")");
        if (node.getNodeType() != node.ELEMENT_NODE) {
            throw new Error("this isn't an element" + node);
        }
        Element cell = (Element) node;
        Cell cl = new Cell();
        cl.node = node;
        /*
        u.p("style = " + c.css.getStyle(cell));
        if(c.css.hasProperty(cell,"-fs-table-cell-colspan")) {
            u.p("has colspan");
            cl.col_span = (int)c.css.getFloatProperty(cell,"-fs-table-cell-colspan");
        }
        */
        
        if (cell.hasAttribute("colspan")) {
            cl.col_span = Integer.parseInt(cell.getAttribute("colspan"));
        }

        if (cell.hasAttribute("rowspan")) {
            cl.row_span = Integer.parseInt(cell.getAttribute("rowspan"));
        }
        grid.addCell(x, y, cl.col_span, cl.row_span, cl);
        return cl;
    }

    // calculate the widths
    /**
     * Description of the Method
     *
     * @param avail_width PARAM
     * @param c           PARAM
     */
    public void calculateWidths(int avail_width, Context c) {
        //u.p("calculating columns from total space of: " + avail_width);
        //u.p("total column width = " + total_cols);

        // get number of columns and init array
        int total_cols = getTotalColumnCount();
        int[] widths = new int[total_cols];

        // loop over top cells looking for explict widths
        int col_count = 0;
        for (int i = 0; i < top_cells.size(); i++) {
            Cell cell = (Cell) top_cells.get(i);
            if (c.css.getStyle(cell.node).hasProperty("width")) {
                // fixed bug that made cell sizing fail w/ %s
                int width = (int) c.css.getStyle(cell.node).getFloatPropertyRelative("width", avail_width);
                //u.p("got width: " + width);
                for (int j = col_count; j < col_count + cell.col_span; j++) {
                    widths[j] = width / cell.col_span;
                    avail_width -= width / cell.col_span;
                }
            }
            col_count += cell.col_span;
        }
        //u.p("widths");
        //u.p(widths);

        // get number of unset columns
        int unset_cols = 0;
        for (int i = 0; i < widths.length; i++) {
            if (widths[i] <= 0) {
                unset_cols++;
            }
        }
        //u.p("unset cols count = " + unset_cols);


        for (int i = 0; i < total_cols; i++) {
            //Cell cell = (Cell)top_cells.get(i);
            if (widths[i] == 0) {
                widths[i] = avail_width / unset_cols;
            }
            //u.p("looking at: " + cell);
            //u.p("set width to: " + widths[i]);
        }
        column_widths = widths;
        //u.p("final widths");
        //u.p(column_widths);
    }

    /**
     * Description of the Method
     *
     * @param col PARAM
     * @return Returns
     */
    public int calcColumnX(int col) {
        int x = 0;
        for (int i = 0; i < col; i++) {
            x += column_widths[i];
        }
        return x;
    }

    /**
     * Description of the Method
     *
     * @param col  PARAM
     * @param span PARAM
     * @return Returns
     */
    public int calcColumnWidth(int col, int span) {
        //u.p("calc column width: " + col + " " + span);
        int x = 0;
        for (int i = col; i < col + span; i++) {
            x += column_widths[i];
        }
        return x;
    }

    /*
       public Iterator getRowIterator() {
       return rows.iterator();
       }
      */
    /**
     * Gets the cellGrid attribute of the Table object
     *
     * @return The cellGrid value
     */
    public CellGrid getCellGrid() {
        return grid;
    }

    /**
     * Gets the widths attribute of the Table object
     *
     * @return The widths value
     */
    int[] getWidths() {
        return column_widths;
    }

    /**
     * Gets the totalColumnCount attribute of the Table object
     *
     * @return The totalColumnCount value
     */
    int getTotalColumnCount() {
        int total_cols = 0;
        Iterator it = top_cells.iterator();
        while (it.hasNext()) {
            Cell cell = (Cell) it.next();
            total_cols += cell.col_span;
        }
        return total_cols;
    }
}

/*
   $Id$
   $Log$
   Revision 1.8  2004/12/06 02:55:44  tobega
   More cleaning of use of Node, more preparation for Content-based inline generation.

   Revision 1.7  2004/12/05 00:48:59  tobega
   Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.

   Revision 1.6  2004/11/23 18:38:48  joshy
   removed isPrinting() method from rendering context because it's
   not needed. the panel can detect printing by checking for
   instanceof PrinterGraphics

   -j

   Issue number:
   Obtained from:
   Submitted by:
   Reviewed by:

   Revision 1.5  2004/11/19 14:39:08  joshy
   fixed crash when a tr is empty

   Issue number:
   Obtained from:
   Submitted by:
   Reviewed by:

   Revision 1.4  2004/11/19 14:27:38  joshy
   removed hard coded element names
   added support for tbody, or tbody missing



   Issue number:
   Obtained from:
   Submitted by:
   Reviewed by:

   Revision 1.3  2004/10/23 13:59:18  pdoubleya
   Re-formatted using JavaStyle tool.
   Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
   Added CVS log comments at bottom.

  */

