package org.xhtmlrenderer.table;

import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.BoxRenderer;
import org.xhtmlrenderer.render.Renderer;

import java.awt.Rectangle;

public class TableRenderer extends BoxRenderer {

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

    public void paint(Context c, Box box) {

        //Uu.p("TableLayout.paint( " + c);

        // copy the bounds to we don't mess it up

        Rectangle oldBounds = new Rectangle(c.getExtents());

        //Rectangle contents = layout(c,elem);

        //adjustWidth(c,elem);

        paintBackground(c, box);

        paintComponent(c, box);

        //paintChildren(c,elem);

        paintBorder(c, box);

        // move the origin down to account for the contents plus the margin, borders, and padding

        oldBounds.y = oldBounds.y + box.height;

        c.setExtents(oldBounds);

    }


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintComponent(Context c, Box box) {

        paintTable(c, (TableBox) box);

    }


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintChildren(Context c, Box box) {
    }

    /**
     * Description of the Method
     *
     * @param c     PARAM
     * @param table PARAM
     */
    protected void paintTable(Context c, TableBox table) {

        c.getGraphics().translate(table.x, table.y);

        c.getGraphics().translate(table.margin.left + table.border.left + table.padding.left,
                table.margin.top + table.border.top + table.padding.top);

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

        c.getGraphics().translate(-table.margin.left - table.border.left - table.padding.left,
                -table.margin.top - table.border.top - table.padding.top);

        c.getGraphics().translate(-table.x, -table.y);

        //c.getGraphics().translate(-c.getExtents().Xx, -c.getExtents().y);

    }


    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param row PARAM
     */
    protected void paintRow(Context c, RowBox row) {

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
    protected void paintCell(Context c, CellBox cell) {

        if (cell.isReal()) {

            Rectangle oe = c.getExtents();

            c.getGraphics().translate(oe.x, oe.y);

            c.setExtents(new Rectangle(0, 0, cell.width, cell.height));

            Renderer rend = c.getRenderer(cell.content.getElement());

            //Uu.p("doing cell: " + cell);

            rend.paint(c, cell.sub_box);

            c.getGraphics().translate(-oe.x, -oe.y);

            c.setExtents(oe);

        }
    }

}
