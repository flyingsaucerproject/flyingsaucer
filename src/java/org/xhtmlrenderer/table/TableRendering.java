/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci, Torbjšrn Gannholm
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

import org.xhtmlrenderer.css.Border;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.LayoutUtil;
import org.xhtmlrenderer.render.BoxRendering;

import java.awt.*;

public class TableRendering {

    /*
       =========== painting code =============
      */
    /**
     * The entry point to the painting routines. It takes the table box to be
     * painted and the current context. It will call paintBackground(),
     * paintComponent(), and paintBorder() on it's own.
     *
     * @param c   PARAM
     * @param box PARAM
     */

    /*public static void paint(Context c, Box box) {

        //Uu.p("TableLayout.paint( " + c);

        // copy the bounds to we don't mess it up

        Rectangle oldBounds = new Rectangle(c.getExtents());

        //Rectangle contents = layout(c,elem);

        //adjustWidth(c,elem);

        BoxRendering.paintBackground(c, box);

        paintComponent(c, box);

        //paintChildren(c,elem);

        BoxRendering.paintBorder(c, box);

        // move the origin down to account for the contents plus the margin, borders, and padding

        oldBounds.y = oldBounds.y + box.height;

        c.setExtents(oldBounds);

    }*/


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    /*public static void paintComponent(Context c, Box box) {

        paintTable(c, (TableBox) box);

    }  */


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    //public void paintChildren(Context c, Box box) {
    //}

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param table PARAM
     */
    public static void paintTable(Context c, TableBox table) {

        c.getGraphics().translate(table.x, table.y);

        Border border = LayoutUtil.getBorder(table, c.getCurrentStyle());
        Border margin = c.getCurrentStyle().getMarginWidth();
        Border padding = c.getCurrentStyle().getPaddingWidth();

        c.getGraphics().translate(margin.left + border.left + padding.left,
                margin.top + border.top + padding.top);

        // loop over the rows

        for (int i = 0; i < table.rows.size(); i++) {

            RowBox row = (RowBox) table.rows.get(i);

            // save the old extents

            Rectangle oe = c.getExtents();

            // move origin by row.Xx and row.y

            c.setExtents(new Rectangle(oe.x + row.x, oe.y + row.y, oe.width,
                    oe.height));

            c.getGraphics().translate(row.x, row.y);

            // paint the row

            paintRow(c, row);

            // restore the old extents and translate

            c.getGraphics().translate(-row.x, -row.y);

            c.setExtents(oe);

        }

        c.getGraphics().translate(-margin.left - border.left - padding.left,
                -margin.top - border.top - padding.top);

        c.getGraphics().translate(-table.x, -table.y);

        //c.getGraphics().translate(-c.getExtents().Xx, -c.getExtents().y);

    }


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param row PARAM
     */
    protected static void paintRow(Context c, RowBox row) {

        //Uu.p("Paint Row c = " + c);

        //Uu.p("paint row = " + row);

        // debug

        for (int i = 0; i < row.cells.size(); i++) {

            CellBox cell = (CellBox) row.cells.get(i);

            Rectangle oe = c.getExtents();

            c.setExtents(new Rectangle(cell.x, cell.y, oe.width, oe.height));

            paintCell(c, cell);

            c.setExtents(oe);

        }

    }


    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param cell PARAM
     */
    protected static void paintCell(Context c, CellBox cell) {

        if (cell.isReal()) {

            Rectangle oe = c.getExtents();

            c.getGraphics().translate(oe.x, oe.y);

            c.setExtents(new Rectangle(0, 0, cell.width, cell.height));

            //Uu.p("doing cell: " + cell);

            BoxRendering.paint(c, cell.sub_box);

            c.getGraphics().translate(-oe.x, -oe.y);

            c.setExtents(oe);

        }
    }

}
