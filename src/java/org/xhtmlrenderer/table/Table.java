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
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
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
//TODO: fix table rendering to handle restyles on render, fix table layout(boxing) to use ContentUtil, or at least restyle...
public class Table {
    /**
     * Description of the Field
     */
    List top_cells = new ArrayList();

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
     * @param c    The feature to be added to the Table attribute
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

    /**
     * Adds a feature to the Row attribute of the Table object
     *
     * @param c   The feature to be added to the Row attribute
     * @param row The feature to be added to the Row attribute
     * @param y   The feature to be added to the Row attribute
     * @return Returns
     */
    public boolean addRow(Context c, Node row, int y) {
        return addRow(c, row, false, y);
    }

    /**
     * Adds a feature to the FirstRow attribute of the Table object
     *
     * @param c   The feature to be added to the FirstRow attribute
     * @param row The feature to be added to the FirstRow attribute
     * @return Returns
     */
    public boolean addFirstRow(Context c, Node row) {
        return addRow(c, row, true, 0);
    }

    /**
     * Adds a feature to the Row attribute of the Table object
     *
     * @param c         The feature to be added to the Row attribute
     * @param row       The feature to be added to the Row attribute
     * @param first_row The feature to be added to the Row attribute
     * @param y         The feature to be added to the Row attribute
     * @return Returns
     */
    public boolean addRow(Context c, Node row, boolean first_row, int y) {
        //Uu.p("Table.addRow("+row+","+first_row+","+y+")");
        NodeList cells = row.getChildNodes();
        int col_counter = 0;
        // for each td
        boolean added = false;
        for (int j = 0; j < cells.getLength(); j++) {
            Node cell = cells.item(j);
            if (isTableCell(c, cell)) {
                //Uu.p("adding: " + col_counter + " " + y);
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
     * @param c    The feature to be added to the TopCell attribute
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
     * @param c    The feature to be added to the Cell attribute
     * @param node The feature to be added to the Cell attribute
     * @param x    The feature to be added to the Cell attribute
     * @param y    The feature to be added to the Cell attribute
     * @return Returns
     */
    public Cell addCell(Context c, Node node, int x, int y) {
        //Uu.p("addCell("+node+","+Xx+","+y+")");
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            throw new Error("this isn't an element" + node);
        }
        Element cell = (Element) node;
        Cell cl = new Cell();
        cl.node = node;

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
        //Uu.p("calculating columns from total space of: " + avail_width);
        //Uu.p("total column width = " + total_cols);

        // get number of columns and init array
        int total_cols = getTotalColumnCount();
        int[] widths = new int[total_cols];

        // loop over top cells looking for explict widths
        //TODO: fix the style stuff here
        int col_count = 0;
        for (int i = 0; i < top_cells.size(); i++) {
            Cell cell = (Cell) top_cells.get(i);
            if (c.getCss().getCascadedStyle((Element) cell.node, false).hasProperty(CSSName.WIDTH)) {
                // fixed bug that made cell sizing fail w/ %s
                int width = (int) c.getCurrentStyle().getFloatPropertyProportionalWidth(CSSName.WIDTH, avail_width, c.getCtx());
                //Uu.p("got width: " + width);
                for (int j = col_count; j < col_count + cell.col_span; j++) {
                    widths[j] = width / cell.col_span;
                    avail_width -= width / cell.col_span;
                }
            }
            col_count += cell.col_span;
        }
        //Uu.p("widths");
        //Uu.p(widths);

        // get number of unset columns
        int unset_cols = 0;
        for (int i = 0; i < widths.length; i++) {
            if (widths[i] <= 0) {
                unset_cols++;
            }
        }
        //Uu.p("unset cols count = " + unset_cols);


        for (int i = 0; i < total_cols; i++) {
            //Cell cell = (Cell)top_cells.get(i);
            if (widths[i] == 0) {
                widths[i] = avail_width / unset_cols;
            }
            //Uu.p("looking at: " + cell);
            //Uu.p("set width to: " + widths[i]);
        }
        column_widths = widths;
        //Uu.p("final widths");
        //Uu.p(column_widths);
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
        //Uu.p("calc column width: " + col + " " + span);
        int x = 0;
        for (int i = col; i < col + span; i++) {
            x += column_widths[i];
        }
        return x;
    }

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


    /**
     * Gets the rowGroup attribute of the Table object
     *
     * @param c    PARAM
     * @param node PARAM
     * @return The rowGroup value
     */
    private boolean isRowGroup(Context c, Node node) {
        // only elements can be table row groups
        if (!(node instanceof Element)) {
            return false;
        }
        // check the display value
        CascadedStyle style = c.getCss().getCascadedStyle((Element) node, false);
        return LayoutUtil.getDisplay(style) == IdentValue.TABLE_ROW_GROUP;
    }

    /**
     * Gets the row attribute of the Table object
     *
     * @param c    PARAM
     * @param node PARAM
     * @return The row value
     */
    private boolean isRow(Context c, Node node) {
        // only elements can be rows
        if (!(node instanceof Element)) {
            return false;
        }
        // check the display value
        CascadedStyle style = c.getCss().getCascadedStyle((Element) node, false);
        return LayoutUtil.getDisplay(style) == IdentValue.TABLE_ROW;
    }


    /**
     * Gets the tableCell attribute of the Table object
     *
     * @param c    PARAM
     * @param node PARAM
     * @return The tableCell value
     */
    private boolean isTableCell(Context c, Node node) {
        // only elements can be rows
        if (!(node instanceof Element)) {
            return false;
        }
        // check the display value
        CascadedStyle style = c.getCss().getCascadedStyle((Element) node, false);
        return LayoutUtil.getDisplay(style) == IdentValue.TABLE_CELL;
    }
}

/*
 * $Id$
 * $Log$
 * Revision 1.18  2005/05/08 14:36:59  tobega
 * Refactored away the need for having a context in a CalculatedStyle
 *
 * Revision 1.17  2005/01/29 20:18:43  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.16  2005/01/25 14:45:56  pdoubleya
 * Added support for IdentValue mapping on property declarations. On both CascadedStyle and PropertyDeclaration you can now request the value as an IdentValue, for object-object comparisons. Updated 99% of references that used to get the string value of PD to return the IdentValue instead; remaining cases are for pseudo-elements where the PD content needs to be manipulated as a String.
 * Revision 1.15  2005/01/24 19:01:09  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 * Revision 1.14  2005/01/24 14:36:36  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 * Revision 1.13  2005/01/16 18:50:06  tobega
 * Re-introduced caching of styles, which make hamlet and alice scroll nicely again. Background painting still slow though.
 * Revision 1.12  2004/12/29 10:39:36  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 * Revision 1.11  2004/12/29 07:35:39  tobega
 * Prepared for cloned Context instances by encapsulating fields
 * Revision 1.10  2004/12/12 04:18:58  tobega
 * Now the core compiles at least. Now we must make it work right. Table layout is one point that really needs to be looked over
 * Revision 1.9  2004/12/12 03:33:03  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 * Revision 1.8  2004/12/06 02:55:44  tobega
 * More cleaning of use of Node, more preparation for Content-based inline generation.
 * Revision 1.7  2004/12/05 00:48:59  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 * Revision 1.6  2004/11/23 18:38:48  joshy
 * removed isPrinting() method from rendering context because it's
 * not needed. the panel can detect printing by checking for
 * instanceof PrinterGraphics
 * -j
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 * Revision 1.5  2004/11/19 14:39:08  joshy
 * fixed crash when a tr is empty
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 * Revision 1.4  2004/11/19 14:27:38  joshy
 * removed hard coded element names
 * added support for tbody, or tbody missing
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 * Revision 1.3  2004/10/23 13:59:18  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 */

