/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjšrn Gannholm
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

import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.render.BoxRendering;

import java.awt.Rectangle;

/**
 * Description of the Class
 *
 * @author Joshua Marinacci
 * @author Torbjörn Gannholm
 */
public class TableRendering {

    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param table   PARAM
     * @param restyle
     */
    public static void paintTable(Context c, TableBox table, boolean restyle) {
        //Uu.p("painting the table " + table);
        restyle = restyle || table.restyle;
        table.restyle = false;
        //c.getGraphics().translate( table.x, table.y );

        /* TODO: why never used?
        Border border = c.getCurrentStyle().getBorderWidth(
			c.getBlockFormattingContext().getWidth(),
			c.getBlockFormattingContext().getHeight(), c.getCtx());
        Border margin = c.getCurrentStyle().getMarginWidth(
			c.getBlockFormattingContext().getWidth(),
			c.getBlockFormattingContext().getHeight() );
        Border padding = c.getCurrentStyle().getPaddingWidth(
			c.getBlockFormattingContext().getWidth(),
			c.getBlockFormattingContext().getHeight() ); */

        //Uu.p("translating by: " +  margin.left + border.left + padding.left +","
        //+margin.top + border.top + padding.top );
        //c.getGraphics().translate( margin.left + border.left + padding.left,
        //        margin.top + border.top + padding.top );

        // loop over the rows

        for (int i = 0; i < table.rows.size(); i++) {

            RowBox row = (RowBox) table.rows.get(i);

            // save the old extents

            Rectangle oe = c.getExtents();


            // move origin by row.Xx and row.y

            c.setExtents(new Rectangle(oe.x + row.x, oe.y + row.y, oe.width,
                    oe.height));
            //Uu.p("old extents = " + oe);
            //Uu.p("new extents = " + c.getExtents());
            //Uu.p("translating by: " + row.x + " " + row.y);
            c.getGraphics().translate(row.x, row.y);

            // paint the row

            paintRow(c, row, restyle);

            // restore the old extents and translate

            c.getGraphics().translate(-row.x, -row.y);
            c.setExtents(oe);

        }

        //c.getGraphics().translate( -margin.left - border.left - padding.left,
        //        -margin.top - border.top - padding.top );

        //c.getGraphics().translate( -table.x, -table.y );

    }


    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param row     PARAM
     * @param restyle
     */
    protected static void paintRow(Context c, RowBox row, boolean restyle) {
        restyle = restyle || row.restyle;
        row.restyle = false;
        //Uu.p("Paint Row c = " + c);

        //Uu.p("paint row = " + row);

        // debug

        for (int i = 0; i < row.cells.size(); i++) {

            CellBox cell = (CellBox) row.cells.get(i);

            Rectangle oe = c.getExtents();

            c.setExtents(new Rectangle(cell.x, cell.y, oe.width, oe.height));

            paintCell(c, cell, restyle);

            c.setExtents(oe);

        }

    }


    /**
     * Description of the Method
     *
     * @param c       PARAM
     * @param cell    PARAM
     * @param restyle
     */
    protected static void paintCell(Context c, CellBox cell, boolean restyle) {
        //ASSERT: BoxRendering.paint will take care of dynamic restyle
        restyle = restyle || cell.restyle;//can't hurt
        cell.restyle = false;
        if (cell.isReal()) {

            Rectangle oe = c.getExtents();

            c.getGraphics().translate(oe.x, oe.y);

            c.setExtents(new Rectangle(0, 0, cell.width, cell.height));

            //Uu.p("doing cell: " + cell);

            BoxRendering.paint(c, cell.sub_box, false, restyle);

            c.getGraphics().translate(-oe.x, -oe.y);

            c.setExtents(oe);

        }
    }

}

